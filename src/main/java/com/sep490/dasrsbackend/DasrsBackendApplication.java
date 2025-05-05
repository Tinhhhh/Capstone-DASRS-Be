package com.sep490.dasrsbackend;

import com.sep490.dasrsbackend.config.FirebaseConfig;
import com.sep490.dasrsbackend.controller.FileController;
import com.sep490.dasrsbackend.service.FirebaseStorageService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(
        basePackages = {"com.sep490.dasrsbackend"},
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = FirebaseStorageService.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = FirebaseConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = FileController.class),
        }
)
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
public class DasrsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DasrsBackendApplication.class, args);
    }

}
