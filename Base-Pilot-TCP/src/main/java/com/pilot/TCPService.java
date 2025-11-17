package com.pilot;

import intf.PilotServices;
import org.springframework.stereotype.Component;
import xml.ControllerConfig;
import xml.DatabaseConfig;
import xml.ModuleConfig;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
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

        for (Controller controller : _controllers){
            if (controller.isActive()){
                try (ServerSocket serverSocket = new ServerSocket(controller.getPort())) {
                    System.out.println(controller.getName()+" -Port " + controller.getPort() + " ist verfügbar.");
                } catch (BindException e) {
                    System.out.println(controller.getName()+" -Port " + controller.getPort() + " ist bereits belegt.");
                } catch (IOException e) {
                    System.err.println(controller.getName()+" - Ein Fehler ist aufgetreten: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void run() {

    }

    @Override
    public void stop() {

    }
}
