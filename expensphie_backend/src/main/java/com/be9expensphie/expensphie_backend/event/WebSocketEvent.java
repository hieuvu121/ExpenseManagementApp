package com.be9expensphie.expensphie_backend.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketEvent {
    private String destination;
    private String payload; // JSON-serialized event payload
}
