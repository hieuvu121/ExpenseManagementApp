package com.be9expensphie.expensphie_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ExpensphieBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(ExpensphieBackendApplication.class, args);
	}
}
