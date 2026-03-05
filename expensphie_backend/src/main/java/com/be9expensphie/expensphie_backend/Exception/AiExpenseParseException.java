package com.be9expensphie.expensphie_backend.Exception;

import lombok.Getter;

@Getter
@SuppressWarnings("serial")
public class AiExpenseParseException extends RuntimeException {

    public AiExpenseParseException(String message) {
    	super(message);
    }

}
