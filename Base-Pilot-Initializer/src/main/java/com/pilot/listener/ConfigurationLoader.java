package com.pilot.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import intf.PilotServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import xml.ModuleConfig;
import xml.ModulesConfig;
import xml.SystemConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ConfigurationLoader {

    @Autowired
    private static ApplicationContext context;
    @Autowired
    private static List<PilotServices<?>> pilotServices;

    public ConfigurationLoader(){

    }

    public static SystemConfig startingLoading() {
        /// Erstaml die Configurationsdatei finden
        String xml_file ="App_Config.xml";
        // Oder Sie erstellen ein Path-Objekt für das aktuelle Verzeichnis und fügen dann den Dateinamen hinzu
        Path currentDirectory = Paths.get(""); // Repräsentiert das aktuelle Verzeichnis
        Path absoluteFilePath = currentDirectory.resolve(xml_file).toAbsolutePath();

        File config_file = new File(absoluteFilePath.toString());
        /// Prüfen, ob die Datei tatsächlich existiert
        if(!config_file.exists()){

            throw new RuntimeException(String.format(
                    "Keine Configurationsdatei ist vorhanden. Die '%s' unter %s ist nicht vorhanden",
                    xml_file, absoluteFilePath
            ));
        }
        SystemConfig systemConfig;
        try {
            XmlMapper xmlMapper = new XmlMapper();
            systemConfig = xmlMapper.readValue(config_file, SystemConfig.class);
        }
        catch (MismatchedInputException e) {
            throw new RuntimeException("XML-Struktur passt nicht zum Datenmodell: " + e.getMessage(), e);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException("Parsing-Fehler in Zeile " + e.getLocation().getLineNr() +
                    ", Spalte " + e.getLocation().getColumnNr());
        }
        catch (IOException e) {
            throw new RuntimeException("Fehler beim Lesen der XML-Datei: " + e.getMessage(), e);
        }
        if (systemConfig == null) {
            throw new RuntimeException("Es sind Keine Module in die Konfigurationsdatei definiret");
        }

        return systemConfig;
    }

}
