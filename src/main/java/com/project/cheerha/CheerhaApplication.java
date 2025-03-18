package com.project.cheerha;

import com.project.cheerha.common.properties.JwtSecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableJpaAuditing
@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties({JwtSecurityProperties.class})
public class CheerhaApplication {

	public static void main(String[] args) {
		SpringApplication.run(CheerhaApplication.class, args);
	}

}
