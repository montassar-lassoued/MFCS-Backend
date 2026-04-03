package com.IntraConnect.controllerContent;

import com.IntraConnect._enum.Transfer;
import com.IntraConnect.controller.Controller;
import com.IntraConnect.intf.ContentServices;
import com.IntraConnect.queryExec.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class ControllerContentService implements ContentServices {
	
	private static final Logger log = LoggerFactory.getLogger(ControllerContentService.class);
	public Controller controller;

	@Override
    public void handleIncomingData(Controller _controller, byte[] data){
        // die Schnittstelle
        controller =_controller;
        // die Nachricht (kann auch mehrere Nachrichten sein inkl. Prefix und Suffix)
        List<byte[]>  contents = extractContents(data);
        for(byte[] content: contents){
            byte[] cnt = handleContent(content);
            String message = new String(cnt, StandardCharsets.ISO_8859_1);
			if(!skipMessage(message)){
				handleMessage(message);
			}
        
        }
    }
    
    protected List<byte[]> extractContents(byte[] data) {
        return extract(data);
    }

    
	protected byte[] handleContent(byte[] data) {
        return data;
    }
	
	
	protected void handleMessage(String message) {
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
	
	
	protected boolean skipMessage(String message) {
		return false;
	}
	
	protected List<byte[]> extract(byte[] data){
        // leer?
        if(data.length < 1) return null;

        byte[] prefix = controller.getPrefix().getBytes();
        byte[] suffix = controller.getSuffix().getBytes();
        List<byte[]> messages = new ArrayList<>();
        int index = 0;

        while (index < data.length) {

            // Prefix suchen
            int prefixPos = 0;
            // nur wenn ein Prefix schon definiert ist (sprich nicht leer)
            if(prefix.length > 0) {
                prefixPos = findPattern(data, prefix, index);
            }
            if (prefixPos < 0) break;

            int contentStart = prefixPos + prefix.length;

            // Suffix suchen
            int suffixPos = data.length-1;
            if(suffix.length > 0) {
                suffixPos = findPattern(data, suffix, contentStart);
            }
            if (suffixPos < 0) break;

            // Nachricht extrahieren
            //int contentLength = suffixPos - contentStart;
            byte[] content = Arrays.copyOfRange(data, contentStart, suffixPos);
            messages.add(content);

            // Weiter suchen ab Ende des Suffix
            index = suffixPos + suffix.length;
        }
        return messages;
    }
    // Hilfsfunktion: suche Pattern im Byte-Array
    public static int findPattern(byte[] data, byte[] pattern, int start) {
        for (int i = start; i <= data.length - pattern.length; i++) {
            boolean match = true;
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) {
                    match = false;
                    break;
                }
            }
            if (match) return i;
        }
        return -1;
    }
}
