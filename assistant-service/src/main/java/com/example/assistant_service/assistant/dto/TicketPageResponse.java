package com.example.assistant_service.assistant.dto;

import java.util.ArrayList;
import java.util.List;

public class TicketPageResponse {

    private List<TicketView> items = new ArrayList<>();

    public List<TicketView> getItems() {
        return items;
    }

    public void setItems(List<TicketView> items) {
        this.items = items;
    }
}
