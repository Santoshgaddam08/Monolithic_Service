package com.example.assistant_service.assistant;

import com.example.assistant_service.assistant.dto.ChatResponse;
import com.example.assistant_service.assistant.dto.TicketView;
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

    private final TicketClient ticketClient;

    public AssistantService(TicketClient ticketClient) {
        this.ticketClient = ticketClient;
    }

    public ChatResponse chat(String rawMessage) {
        String message = rawMessage == null ? "" : rawMessage.trim().toLowerCase();
        if (message.isBlank()) {
            return new ChatResponse("Ask a specific question like: status of ticket #1 or how many open tickets.", defaultSuggestions());
        }

        List<TicketView> tickets = ticketClient.fetchTickets();

        if (tickets.isEmpty()) {
            if (containsAny(message, "open")) return new ChatResponse("Open tickets: 0.", defaultSuggestions());
            if (containsAny(message, "summary", "overview")) return new ChatResponse("Queue summary: total=0, open=0, inProgress=0, blocked=0, resolved=0, breached=0, dueSoon=0.", defaultSuggestions());
            return new ChatResponse("There are no tickets yet. Create incoming issues and I can triage them.", defaultSuggestions());
        }

        Optional<TicketView> exact = findTicketFromMessage(message, tickets);
        if (exact.isPresent()) {
            TicketView t = exact.get();
            if (containsAny(message, "status", "state")) {
                return new ChatResponse("Ticket #" + t.getId() + " status is " + t.getStatus() + ".", defaultSuggestions());
            }
            if (containsAny(message, "assign", "owner", "assignee", "who")) {
                return new ChatResponse("Ticket #" + t.getId() + " is assigned to " + (isBlank(t.getAssignedTo()) ? "unassigned" : t.getAssignedTo()) + ".", defaultSuggestions());
            }
            if (containsAny(message, "sla", "due", "breach", "overdue")) {
                long sec = Duration.between(Instant.now(), t.getSlaDueAt()).getSeconds();
                return new ChatResponse("Ticket #" + t.getId() + " SLA remaining: " + formatSeconds(sec) + (sec < 0 ? " (breached)." : "."), defaultSuggestions());
            }
            if (containsAny(message, "priority")) {
                return new ChatResponse("Ticket #" + t.getId() + " priority is " + t.getPriority() + ".", defaultSuggestions());
            }
            return new ChatResponse("Ticket #" + t.getId() + ": status=" + t.getStatus() + ", priority=" + t.getPriority() + ".", defaultSuggestions());
        }

        if (containsAny(message, "how many open", "open tickets", "open count")) {
            long open = tickets.stream().filter(t -> "OPEN".equals(t.getStatus())).count();
            return new ChatResponse("Open tickets: " + open + ".", defaultSuggestions());
        }

        if (containsAny(message, "how many in progress", "in progress tickets")) {
            long inProgress = tickets.stream().filter(t -> "IN_PROGRESS".equals(t.getStatus())).count();
            return new ChatResponse("In-progress tickets: " + inProgress + ".", defaultSuggestions());
        }

        if (containsAny(message, "how many blocked", "blocked tickets")) {
            long blocked = tickets.stream().filter(t -> "BLOCKED".equals(t.getStatus())).count();
            return new ChatResponse("Blocked tickets: " + blocked + ".", defaultSuggestions());
        }

        if (containsAny(message, "how many resolved", "resolved tickets")) {
            long resolved = tickets.stream().filter(t -> "RESOLVED".equals(t.getStatus())).count();
            return new ChatResponse("Resolved tickets: " + resolved + ".", defaultSuggestions());
        }

        if (containsAny(message, "priority", "critical", "high")) {
            long critical = tickets.stream().filter(t -> "CRITICAL".equals(t.getPriority())).count();
            long high = tickets.stream().filter(t -> "HIGH".equals(t.getPriority())).count();
            long medium = tickets.stream().filter(t -> "MEDIUM".equals(t.getPriority())).count();
            long low = tickets.stream().filter(t -> "LOW".equals(t.getPriority())).count();
            return new ChatResponse("Priority split: critical=" + critical + ", high=" + high + ", medium=" + medium + ", low=" + low + ".", defaultSuggestions());
        }

        if (containsAny(message, "breach", "overdue", "escalate")) {
            List<TicketView> breached = breachedTickets(tickets);
            if (breached.isEmpty()) {
                return new ChatResponse("No active SLA breaches right now.", defaultSuggestions());
            }
            String top = breached.stream().limit(5).map(t -> "#" + t.getId() + " " + t.getTitle()).collect(Collectors.joining("; "));
            return new ChatResponse("SLA breached tickets: " + top + ".", defaultSuggestions());
        }

        if (containsAny(message, "workload", "owner", "assignee", "agent")) {
            Map<String, Long> byAssignee = tickets.stream()
                .filter(t -> !"RESOLVED".equals(t.getStatus()))
                .collect(Collectors.groupingBy(t -> isBlank(t.getAssignedTo()) ? "unassigned" : t.getAssignedTo(), Collectors.counting()));
            String workload = byAssignee.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(", "));
            return new ChatResponse("Current workload: " + workload + ".", defaultSuggestions());
        }

        if (containsAny(message, "next", "what should i do", "action")) {
            TicketView next = tickets.stream()
                .filter(t -> !"RESOLVED".equals(t.getStatus()))
                .min(Comparator.comparing(TicketView::getSlaDueAt))
                .orElse(null);
            if (next == null) {
                return new ChatResponse("No active tickets. Queue is clear.", defaultSuggestions());
            }
            return new ChatResponse("Next action: work on ticket #" + next.getId() + " - " + next.getTitle() + " (" + next.getPriority() + ").", defaultSuggestions());
        }

        if (containsAny(message, "summary", "overview", "dashboard")) {
            return new ChatResponse(buildSummary(tickets), defaultSuggestions());
        }

        return new ChatResponse("I could not map that exactly. Ask: status of ticket #id, open count, breaches, workload, or next action.", defaultSuggestions());
    }

    private static String buildSummary(List<TicketView> tickets) {
        Instant now = Instant.now();
        long open = tickets.stream().filter(t -> "OPEN".equals(t.getStatus())).count();
        long inProgress = tickets.stream().filter(t -> "IN_PROGRESS".equals(t.getStatus())).count();
        long blocked = tickets.stream().filter(t -> "BLOCKED".equals(t.getStatus())).count();
        long resolved = tickets.stream().filter(t -> "RESOLVED".equals(t.getStatus())).count();
        long breached = tickets.stream().filter(t -> !"RESOLVED".equals(t.getStatus()) && t.getSlaDueAt().isBefore(now)).count();
        long dueSoon = tickets.stream().filter(t -> !"RESOLVED".equals(t.getStatus()))
            .map(t -> Duration.between(now, t.getSlaDueAt()).getSeconds())
            .filter(sec -> sec >= 0 && sec <= 1800)
            .count();

        return "Queue summary: total=" + tickets.size() + ", open=" + open + ", inProgress=" + inProgress + ", blocked=" + blocked + ", resolved=" + resolved + ", breached=" + breached + ", dueSoon=" + dueSoon + ".";
    }

    private static List<TicketView> breachedTickets(List<TicketView> tickets) {
        Instant now = Instant.now();
        return tickets.stream()
            .filter(t -> !"RESOLVED".equals(t.getStatus()))
            .filter(t -> t.getSlaDueAt() != null && t.getSlaDueAt().isBefore(now))
            .sorted(Comparator.comparing(TicketView::getSlaDueAt))
            .toList();
    }

    private static Optional<TicketView> findTicketFromMessage(String message, List<TicketView> tickets) {
        Matcher matcher = TICKET_ID_PATTERN.matcher(message);
        if (!matcher.find()) {
            return Optional.empty();
        }
        long id = Long.parseLong(matcher.group(1));
        return tickets.stream().filter(t -> t.getId() != null && t.getId() == id).findFirst();
    }

    private static String formatSeconds(long sec) {
        long abs = Math.abs(sec);
        long minutes = abs / 60;
        long seconds = abs % 60;
        return (sec < 0 ? "-" : "") + String.format("%02d:%02d", minutes, seconds);
    }

    private static boolean containsAny(String text, String... keys) {
        for (String key : keys) {
            if (text.contains(key)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
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
