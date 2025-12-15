package com.example._t1020159;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Application {

	public static void main(String[] args) {

		SpringApplication.run(Application.class, args);
		System.out.println("Server run at: http://localhost:8080");
	}


}
