package com.example.smu_club;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class SmuClubApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmuClubApplication.class, args);
	}

}
