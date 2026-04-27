package com.example.lotto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LottoPatternGeneratorApplication {

	public static void main(String[] args) {
		// Spring Boot 애플리케이션의 시작점입니다.
		// @EnableScheduling 때문에 @Scheduled가 붙은 메서드도 함께 활성화됩니다.
		SpringApplication.run(LottoPatternGeneratorApplication.class, args);
	}

}
