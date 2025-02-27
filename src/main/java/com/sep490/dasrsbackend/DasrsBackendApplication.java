package com.sep490.dasrsbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DasrsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DasrsBackendApplication.class, args);
    }

}
