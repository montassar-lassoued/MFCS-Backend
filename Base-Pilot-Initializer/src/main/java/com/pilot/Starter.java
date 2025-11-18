package com.pilot;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;


@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class Starter {


     static void main(String[] args) {

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

         SpringApplication application = new SpringApplication(Starter.class);
         application.setBannerMode(Banner.Mode.OFF);
         application.run(absoluteFilePath.toString());
    }
}