package com.dsevSport.DSEV_Sport;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DsevSportApplication {

	public static void main(String[] args) {
		SpringApplication.run(DsevSportApplication.class, args);
	}

	@Value("${DB_USER}")
	private String dbUser;

	@PostConstruct
	public void testEnv() {
		System.out.println("DB_USER = " + dbUser);
	}
}
