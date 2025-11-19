package com.pilot.services;

import com.pilot.async.AsyncServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModuleScanConfig {
    @Bean
    public TCPService tcpService(AsyncServerFactory serverFactory){
        return new TCPService(serverFactory);
    }
}
