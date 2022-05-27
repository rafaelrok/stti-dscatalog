package com.rafaelvieira.letmebuy.config;

import com.rafaelvieira.letmebuy.services.email.EmailService;
import com.rafaelvieira.letmebuy.services.email.SendGridEmailService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("prod")
public class ProdConfig {
    //Bean responsavel por enviar email no profile dev
    @Bean
    public EmailService emailService() {
        return new SendGridEmailService();
    }
}
