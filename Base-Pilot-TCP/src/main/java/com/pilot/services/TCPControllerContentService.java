package com.pilot.services;

import com.pilot.queryExec.QueryExecutor;
import org.springframework.stereotype.Service;
import services.ControllerContentService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;


@Service
public class TCPControllerContentService extends ControllerContentService {


    @Override
    public void handleMessage(String message) {
        QueryExecutor executor = QueryExecutor.Create();
        executor.submitUpdate("INSERT INTO JOURNAL (CONTROLLER,_DATE, CONTENT) " +
                "VALUES ('"+controller.getName()+"',"+ LocalDate.now() +",'"+
                Arrays.toString(message.getBytes(StandardCharsets.UTF_8)) +"')");
    }
}
