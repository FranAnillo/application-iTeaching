package iteaching.app.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication(scanBasePackages = "iteaching.app")
@EntityScan(basePackages = "iteaching.app.Models")
@EnableJpaRepositories(basePackages = "iteaching.app.repository")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		log.info("iTeaching 2.0 Backend iniciado correctamente");
	}

}
