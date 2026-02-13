package com.example.assistant_service.assistant;

import com.example.assistant_service.assistant.dto.TicketPageResponse;
import com.example.assistant_service.assistant.dto.TicketView;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class TicketClient {

    private final RestClient restClient;

    public TicketClient(RestClient ticketRestClient) {
        this.restClient = ticketRestClient;
    }

    public List<TicketView> fetchTickets() {
        TicketPageResponse page = restClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/tickets")
                .queryParam("page", 0)
                .queryParam("size", 500)
                .queryParam("sortBy", "id")
                .queryParam("direction", "asc")
                .build())
            .retrieve()
            .body(TicketPageResponse.class);

        return page == null || page.getItems() == null ? List.of() : page.getItems();
    }
}
