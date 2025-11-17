package com.pilot.services;

import com.pilot.persistence.Persistence;
import intf.PilotServices;
import org.springframework.stereotype.Component;
import xml.DatabaseConfig;
import xml.ModuleConfig;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.*;

@Component
public class PersistenceService implements PilotServices<ModuleConfig> {

    HashMap<String,Persistence> _persistences = new HashMap<>();

    @Override
    public String getName() {
        return "Persistence";
    }

    @Override
    public void configuration(ModuleConfig config) {

        String module_name = config.getName();
        DatabaseConfig database = config.getDatabase();
        if(database == null){
            throw  new RuntimeException("[ERROR]... Module: "+module_name+"  - Database Configuration is missing");
        }
        /// wir haben erstmal nur eine Datenbank. Das kann später erweitert werden
        Persistence persistence = new Persistence(database);
        persistence.initialize();

        _persistences.put(database.getHost(),persistence);

    }

    @Override
    public void validate() {

        for(Persistence p : _persistences.values()){
            if(!p.isConnected()){
                throw new RuntimeException("Database not connected: " + p.getHost());
            }
            System.out.println("[INFO]... Database: "+p.getHost()+" is connected");
        }
    }

    @Override
    public void run() {

    }

    @Override
    public void stop() {
        for(Persistence p : _persistences.values()){
            p.close();
        }
    }

    /**
     * SOLL NICHT HIER BLEIBEN----------------*/
    public void initializeSchemas() {
        try {
            // Alle schema.sql Dateien im Klassenpfad finden
            Enumeration<URL> resources = Thread.currentThread()
                    .getContextClassLoader()
                    .getResources("db/schema.sql");

            Vector<java.net.URL> urls = new Vector<>();
            while (resources.hasMoreElements()) {
                urls.add(resources.nextElement());
            }

            if (urls.isEmpty()) {
                System.out.println("Keine schema.sql Dateien gefunden!");
                return;
            }

            // Jede Datei nacheinander ausführen
            for (java.net.URL url : urls) {
                System.out.println("Schema ausführen: " + url);

                try (InputStream is = url.openStream();
                     Scanner scanner = new Scanner(is, StandardCharsets.UTF_8)) {

                    scanner.useDelimiter(";"); // Trennung der Statements
                    try (Connection conn = _persistences.values().iterator().next().getDataSource().getConnection();
                         Statement stmt = conn.createStatement()) {

                        while (scanner.hasNext()) {
                            String sql = scanner.next().trim();
                            if (!sql.isEmpty()) {
                                stmt.execute(sql);
                            }
                        }
                    }
                }
            }

            System.out.println("Alle Module erfolgreich initialisiert!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
