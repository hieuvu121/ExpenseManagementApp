package com.be9expensphie.expensphie_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;


@Service
public class AiService {
	private final ChatClient chatClient;

	public AiService(@Qualifier("openAiChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

	public String chat(String prompt) {
		return chatClient
			.prompt(prompt)
			.call()
			.content();

	}
	
}
