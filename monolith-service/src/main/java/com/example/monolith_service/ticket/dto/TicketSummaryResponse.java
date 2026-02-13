package com.example.monolith_service.ticket.dto;

public class TicketSummaryResponse {

    private final long total;
    private final long open;
    private final long inProgress;
    private final long blocked;
    private final long resolved;
    private final long breached;
    private final long dueSoon;

    public TicketSummaryResponse(long total, long open, long inProgress, long blocked, long resolved, long breached, long dueSoon) {
        this.total = total;
        this.open = open;
        this.inProgress = inProgress;
        this.blocked = blocked;
        this.resolved = resolved;
        this.breached = breached;
        this.dueSoon = dueSoon;
    }

    public long getTotal() {
        return total;
    }

    public long getOpen() {
        return open;
    }

    public long getInProgress() {
        return inProgress;
    }

    public long getBlocked() {
        return blocked;
    }

    public long getResolved() {
        return resolved;
    }

    public long getBreached() {
        return breached;
    }

    public long getDueSoon() {
        return dueSoon;
    }
}
