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
import java.util.stream.Collectors;

@Service
public class AssistantService {

    private final TicketRepository ticketRepository;

    public AssistantService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public ChatResponse chat(String rawMessage) {
        String message = rawMessage == null ? "" : rawMessage.trim().toLowerCase();
        List<Ticket> tickets = ticketRepository.findAll();

        if (tickets.isEmpty()) {
            return new ChatResponse(
                "There are no tickets yet. Create incoming issues and I can prioritize, detect SLA risk, and suggest actions.",
                defaultSuggestions()
            );
        }

        if (containsAny(message, "summary", "overview", "dashboard", "status")) {
            return new ChatResponse(buildSummary(tickets), defaultSuggestions());
        }

        if (containsAny(message, "breach", "sla", "escalate", "overdue")) {
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

        if (containsAny(message, "high", "critical", "priority")) {
            long critical = tickets.stream().filter(t -> t.getPriority() == TicketPriority.CRITICAL).count();
            long high = tickets.stream().filter(t -> t.getPriority() == TicketPriority.HIGH).count();
            return new ChatResponse("Priority split: critical=" + critical + ", high=" + high + ".", defaultSuggestions());
        }

        if (containsAny(message, "owner", "assignee", "workload", "agent")) {
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
            "I can help with queue triage. Ask summary, SLA breaches, priority split, owner workload, or next action.",
            defaultSuggestions()
        );
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
