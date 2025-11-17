package com.pilot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModuleScanConfig {
    @Bean
    public PersistenceService persistenceService(){
        return new PersistenceService();
    }
}
