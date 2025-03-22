package com.sep490.dasrsbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins(
                "http://localhost:5173",
                "https://dasrs-frontend.vercel.app",
                "http://192.168.1.54:5173"
            )
            .allowedHeaders("*")
            .allowedHeaders("*")
            .allowedMethods("*");
    }
}
