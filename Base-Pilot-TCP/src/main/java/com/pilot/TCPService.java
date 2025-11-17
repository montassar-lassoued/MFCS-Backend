package com.pilot;

import intf.PilotServices;
import org.springframework.stereotype.Component;
import xml.ControllerConfig;
import xml.DatabaseConfig;
import xml.ModuleConfig;

import java.util.ArrayList;
import java.util.List;

@Component
public class TCPService implements PilotServices<ModuleConfig> {

    List<Controller> _controllers = new ArrayList<>();

    @Override
    public String getName() {
        return "TCP";
    }

    @Override
    public void configuration(ModuleConfig config) {
        String module_name = config.getName();
        List<ControllerConfig> controllers = config.getControllers();
        if(controllers == null){
            throw  new RuntimeException("[ERROR]... Module: "+module_name+"  - controllers Configuration is missing");
        }
        /// Controller-Liste durchgehen und initialisieren
        for(ControllerConfig clc:controllers){
            Controller controller = new Controller(clc);

            _controllers.add(controller);
        }
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
