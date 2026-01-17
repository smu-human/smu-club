package com.example.smu_club.config;

import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class OciConfig {
    @Bean
    @Profile("prod")
    public InstancePrincipalsAuthenticationDetailsProvider authenticationDetailsProvider() {
        return InstancePrincipalsAuthenticationDetailsProvider.builder().build();
    }
}
