package com.example.assistant_service.assistant.dto;

import java.util.List;

public class ChatResponse {

    private final String reply;
    private final List<String> suggestions;

    public ChatResponse(String reply, List<String> suggestions) {
        this.reply = reply;
        this.suggestions = suggestions;
    }

    public String getReply() {
        return reply;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }
}
