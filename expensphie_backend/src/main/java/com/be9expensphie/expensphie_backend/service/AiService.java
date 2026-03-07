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
	}
	
}
