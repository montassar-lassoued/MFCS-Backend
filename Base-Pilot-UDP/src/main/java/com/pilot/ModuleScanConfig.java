package com.pilot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModuleScanConfig {
    @Bean
    public UDPService persistenceService(){
        return new UDPService();
    }
}
