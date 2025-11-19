package com.pilot.services;

import com.pilot.async.AsyncClient;
import com.pilot.async.AsyncServer;
import com.pilot.async.AsyncServerFactory;
import controller.Controller;
import intf.PilotServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xml.ControllerConfig;
import xml.ModuleConfig;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

@Component
public class TCPService implements PilotServices<ModuleConfig> {

    List<Controller> _controllers = new ArrayList<>();

    private final AsyncServerFactory serverFactory;

    @Autowired
    public TCPService(AsyncServerFactory serverFactory) {
        this.serverFactory = serverFactory;
    }

    @Override
    public String getName() {
        return "TCP";
    }

    @Override
    public void configuration(ModuleConfig config) {
        String module_name = config.getName();
        List<ControllerConfig> controllers = config.getControllers();
        if (controllers == null) {
            throw new RuntimeException("[ERROR]... Module: " + module_name + "  - controllers Configuration is missing");
        }
        /// Controller-Liste durchgehen und initialisieren
        for (ControllerConfig clc : controllers) {
            Controller controller = new Controller(clc);

            _controllers.add(controller);
        }
    }

    @Override
    public void validate() {

        for (Controller controller : _controllers) {
            if (controller.isActive()) {
                try (ServerSocket serverSocket = new ServerSocket(controller.getPort())) {
                    System.out.println(controller.getName() + " -Port " + controller.getPort() + " ist verfügbar.");
                } catch (BindException e) {
                    throw new RuntimeException(controller.getName() + " -Port " + controller.getPort() + " ist bereits belegt.");
                } catch (IOException e) {
                    throw new RuntimeException(controller.getName() + " - Ein Fehler ist aufgetreten: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            for (Controller controller : _controllers) {
                if (controller.isActive()) {

                    serverFactory.create(controller);

                } else {
                    new AsyncClient(controller);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        // Server weiterlaufen lassen
        try {
            Thread.currentThread().join();
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public void stop() {

    }
}
