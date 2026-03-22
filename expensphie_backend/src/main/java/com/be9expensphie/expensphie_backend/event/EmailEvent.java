package com.be9expensphie.expensphie_backend.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailEvent {
    private String to; //to consumer
    private String subject;
    private String body; //main content
    private String eventType; //activate or forgot password
    private Map<String, String> metadata; // Optional extra data
}
