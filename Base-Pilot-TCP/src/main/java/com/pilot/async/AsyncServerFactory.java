package com.pilot.async;

import com.pilot.services.TCPControllerContentService;
import controller.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AsyncServerFactory {
    private final TCPControllerContentService contentService;

    @Autowired
    public AsyncServerFactory(TCPControllerContentService contentService) {
        this.contentService = contentService;
    }

    public AsyncServer create(Controller controller) throws IOException {
        return new AsyncServer(controller, contentService);
    }
}
