package services;

import controller.Controller;
import intf.ContentServices;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ControllerContentService implements ContentServices {

    public Controller controller;

    public void startHandleContent(Controller _controller, byte[] data){
        // die Schnittstelle
        controller =_controller;
        // die Nachricht (kann auch mehrere Nachrichten sein inkl. Prefix und Suffix)
        List<byte[]>  contents = extractContents(data);
        for(byte[] content: contents){
            byte[] cnt = handleContent(content);
            String message = new String(cnt, StandardCharsets.ISO_8859_1);
            handleMessage(message);
        }
    }
    @Override
    public List<byte[]> extractContents(byte[] data) {
        return extract(data);
    }

    @Override
    public byte[] handleContent(byte[] data) {
        return data;
    }

    @Override
    public void handleMessage(String message) {

    }

    private List<byte[]> extract(byte[] data){
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
