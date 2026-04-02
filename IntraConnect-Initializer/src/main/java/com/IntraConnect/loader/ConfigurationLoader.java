package com.IntraConnect.loader;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigurationLoader {

    public ConfigurationLoader(){

    }
	
    public static Element startingLoading() {
        // Erstmal die Konfigurationsdatei finden
        String xmlFile ="App_Config.xml";
        // Oder Sie erstellen ein Path-Objekt für das aktuelle Verzeichnis und fügen dann den Dateinamen hinzu
        Path currentDirectory = Paths.get(""); // Repräsentiert das aktuelle Verzeichnis
		
		Document document = getDocument(currentDirectory, xmlFile);
		
		// Root-Element
		return document.getRootElement();
    }
	
	private static Document getDocument(Path currentDirectory, String xmlFile) {
		
		Path absoluteFilePath = currentDirectory.resolve(xmlFile).toAbsolutePath();
		File config_file = new File(absoluteFilePath.toString());
		// Prüfen, ob die Datei tatsächlich existiert
		if(!config_file.exists()){

			throw new RuntimeException(String.format(
					"Keine Konfigurationsdatei ist vorhanden. Die '%s' unter %s ist nicht vorhanden",
					xmlFile, absoluteFilePath
			));
		}
		// SAXBuilder erzeugen
		SAXBuilder saxBuilder = new SAXBuilder();
		
		// Dokument einlesen
		Document document = null;
		try {
			document = saxBuilder.build(xmlFile);
		} catch (JDOMException | IOException e) {
			throw new RuntimeException(e);
		}
		return document;
	}
	
}
