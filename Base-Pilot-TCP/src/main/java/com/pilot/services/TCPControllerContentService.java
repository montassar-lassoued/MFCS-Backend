package com.pilot.services;

import services.ControllerContentService;

public class TCPControllerContentService extends ControllerContentService {

    @Override
    public void handleMessage(String message) {
        System.out.println(message);
    }
}
