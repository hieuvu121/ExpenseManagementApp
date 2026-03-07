package com.be9expensphie.expensphie_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.ai.chat.client.ChatClient;


@Service
public class AiService {
	private final ChatClient chatClient;
	public AiService(ChatClient.Builder builder) {
		chatClient=builder.build();
	}
	public String chat(String prompt) {
		return chatClient
			.prompt(prompt)
			.call()
			.content();
//		return """
//				{
//				  "amount": 900.00,
//				  "date": "2026-01-15",
//				  "category": "WATER",
//				  "method": "EQUAL",
//				  "currency":"AUD",
//				  "splits": [
//				    { "memberId": 14, "amount": 450.00 },
//				    { "memberId": 15, "amount": 450.00 }
//				  ]
//				}
//			    """;
	}
	
}
