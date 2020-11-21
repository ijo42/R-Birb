package ru.ijo42.rbirb.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ComponentScan("ru.ijo42.rbirb")
@EnableWebMvc
public class SpringConfig implements WebMvcConfigurer {
}