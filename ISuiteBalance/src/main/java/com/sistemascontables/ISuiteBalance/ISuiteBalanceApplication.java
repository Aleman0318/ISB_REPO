package com.sistemascontables.ISuiteBalance;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class ISuiteBalanceApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ISuiteBalanceApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
