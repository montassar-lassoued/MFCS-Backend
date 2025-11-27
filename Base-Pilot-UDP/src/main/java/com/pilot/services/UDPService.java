package com.pilot.services;

import intf.PilotServices;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import xml.ModuleConfig;

@Component
public class UDPService implements PilotServices<ModuleConfig> {
    @Override
    public String getName() {
        return "UDP";
    }

    @Override
    public void configuration(ModuleConfig config, ApplicationContext context) {

    }

    @Override
    public void validate() {

    }

    @Override
    public void run() {

    }

    @Override
    public void stop() {

    }
}
