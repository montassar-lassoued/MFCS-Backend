package com.IntraConnect.services.content;

import com.IntraConnect._enum.Transfer;
import com.IntraConnect.queryExec.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.IntraConnect.services.ControllerContentService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;


@Service
public class TCPControllerContentService extends ControllerContentService {


    private static final Logger log = LoggerFactory.getLogger(TCPControllerContentService.class);

    @Override
    public void handleMessage(String message) {
        String query = "INSERT INTO TRANSFER_IN " +
                "(CONTROLLER_ID,_DATE, CONTENT, PROCESSED) " +
                "VALUES " +
                "((SELECT ID FROM CONTROLLER WHERE NAME ='"+controller.getName()+"'),'" +
                LocalDate.now() +"'," +
                 Arrays.toString(message.getBytes(StandardCharsets.UTF_8)) +"," +
				 Transfer.NEW +")";

        try(Transaction transaction = Transaction.create()){
            transaction.insert(query);
            transaction.commit();

        } catch (Exception e){
            log.error(e.getMessage());
        }
    }
}
