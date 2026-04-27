package com.example.lotto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LottoPatternGeneratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(LottoPatternGeneratorApplication.class, args);
	}

}
