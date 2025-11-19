package com.pilot.services;

import com.pilot.queryExec.QueryExecutor;
import org.springframework.stereotype.Service;
import services.ControllerContentService;

@Service
public class TCPControllerContentService extends ControllerContentService {


    @Override
    public void handleMessage(String message) {
        QueryExecutor executor = QueryExecutor.Create();
        executor.submitUpdate("INSERT INTO BLACKLIST (NAME,BESCHREIBUNG) VALUES ('"+controller.getName()+"','"+message+"')");
    }
}
