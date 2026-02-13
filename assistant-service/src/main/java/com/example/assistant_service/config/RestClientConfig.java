package com.example.assistant_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(TicketServiceProperties.class)
public class RestClientConfig {

    @Bean
    public RestClient ticketRestClient(TicketServiceProperties props) {
        return RestClient.builder().baseUrl(props.getBaseUrl()).build();
    }
}
