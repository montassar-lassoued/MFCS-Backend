package com.pilot;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

@Component
public class ModuleInitializer implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {

        String absoluteFilePath = args.getSourceArgs()[0];
        File config_file = new File(absoluteFilePath);

        NodeList modules;
        /// Configurationsdatei einlesen
        DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(config_file);
            document.getDocumentElement().normalize();
            /// bislang alles gut...Nun Module aus der konfigurationsdatei holen
            modules = document.getElementsByTagName("Module");

        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }

        if (modules == null) {
            throw new RuntimeException("Es sind Keine Module in die Konfigurationsdatei definiret");
        }
        // 1. Discovery
        System.out.println("========= MODULES DISCOVERY =========");

        for (int i = 0; i < modules.getLength(); i++) {
            Node node = modules.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element module_Node = (Element) node;
                String name = module_Node.getAttribute("name");
                String enabled = module_Node.getAttribute("enabled");
                String module_name = "Base-Pilot-" + name;
                // Versuch, das Modul zu finden
                /*if (modules.contains(module_name)) {
                    System.out.println("[INFO] ..... Module founded: " + module_name +"   -enabled = "+enabled);
                } else {
                    throw new RuntimeException("[INFO] .....Unknown Module: " + name);
                }*/
            }
        }
    }
}
