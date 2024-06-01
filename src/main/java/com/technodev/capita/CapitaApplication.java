package com.technodev.capita;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class CapitaApplication {
	private static final int STRENGHT = 12;

	public static void main(String[] args) {
		SpringApplication.run(CapitaApplication.class, args);
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder(){
		return new BCryptPasswordEncoder(STRENGHT);
	}
}
