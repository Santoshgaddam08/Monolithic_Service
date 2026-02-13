package com.example.monolith_service.ticket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {
    Page<Ticket> findByTitleContainingIgnoreCaseOrCustomerNameContainingIgnoreCaseOrCustomerEmailContainingIgnoreCase(
        String title,
        String customerName,
        String customerEmail,
        Pageable pageable
    );

    long countByStatus(TicketStatus status);
}
