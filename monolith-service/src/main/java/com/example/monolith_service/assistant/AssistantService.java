package com.example.monolith_service.assistant;

import com.example.monolith_service.assistant.dto.ChatResponse;
import com.example.monolith_service.ticket.Ticket;
import com.example.monolith_service.ticket.TicketPriority;
import com.example.monolith_service.ticket.TicketRepository;
import com.example.monolith_service.ticket.TicketStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AssistantService {

    private static final Pattern TICKET_ID_PATTERN = Pattern.compile("(?:ticket\\s*#?\\s*|#)(\\d+)");

    private final TicketRepository ticketRepository;

    public AssistantService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public ChatResponse chat(String rawMessage) {
        String message = rawMessage == null ? "" : rawMessage.trim().toLowerCase();
        if (message.isBlank()) {
            return new ChatResponse(
                "Ask a specific question, for example: status of ticket #12, how many open tickets, or who has most workload.",
                defaultSuggestions()
            );
        }

        List<Ticket> tickets = ticketRepository.findAll();

        if (tickets.isEmpty()) {
            if (containsAny(message, "how many open", "open tickets", "open count")) {
                return new ChatResponse("Open tickets: 0.", defaultSuggestions());
            }
            if (containsAny(message, "how many in progress", "in progress tickets")) {
                return new ChatResponse("In-progress tickets: 0.", defaultSuggestions());
            }
            if (containsAny(message, "how many blocked", "blocked tickets")) {
                return new ChatResponse("Blocked tickets: 0.", defaultSuggestions());
            }
            if (containsAny(message, "how many resolved", "resolved tickets")) {
                return new ChatResponse("Resolved tickets: 0.", defaultSuggestions());
            }
            if (containsAny(message, "summary", "overview", "dashboard")) {
                return new ChatResponse(
                    "Queue summary: total=0, open=0, inProgress=0, blocked=0, resolved=0, breached=0, dueSoon=0.",
                    defaultSuggestions()
                );
            }
            if (containsAny(message, "breach", "escalate", "overdue")) {
                return new ChatResponse("No active SLA breaches right now.", defaultSuggestions());
            }
            if (containsAny(message, "due soon", "sla risk")) {
                return new ChatResponse("No tickets are due within the next 30 minutes.", defaultSuggestions());
            }
            if (containsAny(message, "next", "what should i do", "action")) {
                return new ChatResponse("No active tickets. Queue is clear.", defaultSuggestions());
            }
            return new ChatResponse(
                "There are no tickets yet. Create incoming issues and I can prioritize, detect SLA risk, and suggest actions.",
                defaultSuggestions()
            );
        }

        Optional<Ticket> ticketById = findTicketFromMessage(message, tickets);
        if (ticketById.isPresent()) {
            Ticket t = ticketById.get();
            if (containsAny(message, "status", "state")) {
                return new ChatResponse("Ticket #" + t.getId() + " status is " + t.getStatus() + ".", defaultSuggestions());
            }
            if (containsAny(message, "assign", "owner", "assignee", "who")) {
                String owner = t.getAssignedTo() == null ? "unassigned" : t.getAssignedTo();
                return new ChatResponse("Ticket #" + t.getId() + " is assigned to " + owner + ".", defaultSuggestions());
            }
            if (containsAny(message, "sla", "due", "breach", "overdue")) {
                long sec = Duration.between(Instant.now(), t.getSlaDueAt()).getSeconds();
                if (t.getStatus() == TicketStatus.RESOLVED) {
                    return new ChatResponse("Ticket #" + t.getId() + " is resolved. SLA timer is no longer active.", defaultSuggestions());
                }
                return new ChatResponse(
                    "Ticket #" + t.getId() + " SLA remaining: " + formatSeconds(sec) + (sec < 0 ? " (breached)." : "."),
                    defaultSuggestions()
                );
            }
            if (containsAny(message, "priority")) {
                return new ChatResponse("Ticket #" + t.getId() + " priority is " + t.getPriority() + ".", defaultSuggestions());
            }
            return new ChatResponse(
                "Ticket #" + t.getId() + ": " + t.getTitle() + ", status=" + t.getStatus() + ", priority=" + t.getPriority()
                    + ", assignee=" + (t.getAssignedTo() == null ? "unassigned" : t.getAssignedTo()) + ".",
                defaultSuggestions()
            );
        }

        if (containsAny(message, "how many open", "open tickets", "open count")) {
            long open = tickets.stream().filter(t -> t.getStatus() == TicketStatus.OPEN).count();
            return new ChatResponse("Open tickets: " + open + ".", defaultSuggestions());
        }

        if (containsAny(message, "how many in progress", "in progress tickets")) {
            long inProgress = tickets.stream().filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS).count();
            return new ChatResponse("In-progress tickets: " + inProgress + ".", defaultSuggestions());
        }

        if (containsAny(message, "how many blocked", "blocked tickets")) {
            long blocked = tickets.stream().filter(t -> t.getStatus() == TicketStatus.BLOCKED).count();
            return new ChatResponse("Blocked tickets: " + blocked + ".", defaultSuggestions());
        }

        if (containsAny(message, "how many resolved", "resolved tickets")) {
            long resolved = tickets.stream().filter(t -> t.getStatus() == TicketStatus.RESOLVED).count();
            return new ChatResponse("Resolved tickets: " + resolved + ".", defaultSuggestions());
        }

        if (containsAny(message, "critical", "high priority", "priority split", "priority")) {
            long critical = tickets.stream().filter(t -> t.getPriority() == TicketPriority.CRITICAL).count();
            long high = tickets.stream().filter(t -> t.getPriority() == TicketPriority.HIGH).count();
            long medium = tickets.stream().filter(t -> t.getPriority() == TicketPriority.MEDIUM).count();
            long low = tickets.stream().filter(t -> t.getPriority() == TicketPriority.LOW).count();
            return new ChatResponse(
                "Priority split: critical=" + critical + ", high=" + high + ", medium=" + medium + ", low=" + low + ".",
                defaultSuggestions()
            );
        }

        if (containsAny(message, "breach", "escalate", "overdue")) {
            List<Ticket> breached = breachedTickets(tickets);
            if (breached.isEmpty()) {
                return new ChatResponse("No active SLA breaches right now.", defaultSuggestions());
            }
            String top = breached.stream()
                .limit(5)
                .map(t -> "#" + t.getId() + " " + t.getTitle())
                .collect(Collectors.joining("; "));
            return new ChatResponse("SLA breached tickets: " + top + ". Escalate these first.", defaultSuggestions());
        }

        if (containsAny(message, "due soon", "sla risk")) {
            Instant now = Instant.now();
            List<Ticket> dueSoon = tickets.stream()
                .filter(t -> t.getStatus() != TicketStatus.RESOLVED)
                .filter(t -> {
                    long sec = Duration.between(now, t.getSlaDueAt()).getSeconds();
                    return sec >= 0 && sec <= 1800;
                })
                .sorted(Comparator.comparing(Ticket::getSlaDueAt))
                .toList();
            if (dueSoon.isEmpty()) {
                return new ChatResponse("No tickets are due within the next 30 minutes.", defaultSuggestions());
            }
            String top = dueSoon.stream().limit(5).map(t -> "#" + t.getId() + " " + t.getTitle()).collect(Collectors.joining("; "));
            return new ChatResponse("Due-soon tickets: " + top + ".", defaultSuggestions());
        }

        if (containsAny(message, "workload", "assignee", "owner", "agent")) {
            Map<String, Long> byAssignee = tickets.stream()
                .filter(t -> t.getStatus() != TicketStatus.RESOLVED)
                .collect(Collectors.groupingBy(t -> t.getAssignedTo() == null ? "unassigned" : t.getAssignedTo(), Collectors.counting()));
            String workload = byAssignee.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", "));
            return new ChatResponse("Current workload: " + workload + ".", defaultSuggestions());
        }

        if (containsAny(message, "summary", "overview", "dashboard")) {
            return new ChatResponse(buildSummary(tickets), defaultSuggestions());
        }

        if (containsAny(message, "next", "what should i do", "action")) {
            Ticket next = tickets.stream()
                .filter(t -> t.getStatus() != TicketStatus.RESOLVED)
                .min(Comparator.comparing(Ticket::getSlaDueAt))
                .orElse(null);
            if (next == null) {
                return new ChatResponse("No active tickets. Queue is clear.", defaultSuggestions());
            }
            return new ChatResponse(
                "Next action: work on ticket #" + next.getId() + " - " + next.getTitle() + " (" + next.getPriority() + ").",
                defaultSuggestions()
            );
        }

        return new ChatResponse(
            "I could not map that exactly. Ask things like: status of ticket #1, SLA of #2, open count, workload, breaches, or next action.",
            defaultSuggestions()
        );
    }

    private static Optional<Ticket> findTicketFromMessage(String message, List<Ticket> tickets) {
        Matcher matcher = TICKET_ID_PATTERN.matcher(message);
        if (!matcher.find()) {
            return Optional.empty();
        }
        long id = Long.parseLong(matcher.group(1));
        return tickets.stream().filter(t -> t.getId() == id).findFirst();
    }

    private static String formatSeconds(long sec) {
        long abs = Math.abs(sec);
        long minutes = abs / 60;
        long seconds = abs % 60;
        return (sec < 0 ? "-" : "") + String.format("%02d:%02d", minutes, seconds);
    }

    private static List<Ticket> breachedTickets(List<Ticket> tickets) {
        Instant now = Instant.now();
        return tickets.stream()
            .filter(t -> t.getStatus() != TicketStatus.RESOLVED)
            .filter(t -> t.getSlaDueAt().isBefore(now))
            .sorted(Comparator.comparing(Ticket::getSlaDueAt))
            .toList();
    }

    private static String buildSummary(List<Ticket> tickets) {
        Instant now = Instant.now();
        long open = tickets.stream().filter(t -> t.getStatus() == TicketStatus.OPEN).count();
        long inProgress = tickets.stream().filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS).count();
        long blocked = tickets.stream().filter(t -> t.getStatus() == TicketStatus.BLOCKED).count();
        long resolved = tickets.stream().filter(t -> t.getStatus() == TicketStatus.RESOLVED).count();
        long breached = tickets.stream().filter(t -> t.getStatus() != TicketStatus.RESOLVED && t.getSlaDueAt().isBefore(now)).count();
        long dueSoon = tickets.stream()
            .filter(t -> t.getStatus() != TicketStatus.RESOLVED)
            .map(t -> Duration.between(now, t.getSlaDueAt()).getSeconds())
            .filter(sec -> sec >= 0 && sec <= 1800)
            .count();

        return "Queue summary: total=" + tickets.size()
            + ", open=" + open
            + ", inProgress=" + inProgress
            + ", blocked=" + blocked
            + ", resolved=" + resolved
            + ", breached=" + breached
            + ", dueSoon=" + dueSoon + ".";
    }

    private static boolean containsAny(String text, String... keys) {
        for (String key : keys) {
            if (text.contains(key)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> defaultSuggestions() {
        return List.of(
            "Give me queue summary",
            "Show SLA breaches",
            "Who has most workload?",
            "What should I handle next?"
        );
    }
}
