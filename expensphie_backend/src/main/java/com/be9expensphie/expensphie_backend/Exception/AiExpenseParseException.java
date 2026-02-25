package com.be9expensphie.expensphie_backend.Exception;

@SuppressWarnings("serial")
public class AiExpenseParseException extends RuntimeException {
	private final String aiResponse;
	
    public AiExpenseParseException(String message,String aiResponse) {
    	super(message);
    	this.aiResponse=aiResponse;
    }
    
    public String getAiResponse() {
    	return aiResponse;
    }
}
