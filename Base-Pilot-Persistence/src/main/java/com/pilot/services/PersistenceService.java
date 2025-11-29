package com.pilot.services;

import com.pilot.persistence.Persistence;
import com.pilot.queryExec.QueryExecutor;
import intf.PilotServices;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import xml.DatabaseConfig;
import xml.ModuleConfig;

import javax.sql.DataSource;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PersistenceService implements PilotServices<ModuleConfig>{

    private static final Pattern CONSTRAINT_PATTERN =
            Pattern.compile("CONSTRAINT\\s+\\[?(\\w+)\\]?\\s+", Pattern.CASE_INSENSITIVE);
    HashMap<String,Persistence> _persistences = new HashMap<>();
    private boolean running = false;
    private ApplicationContext context;

    @Override
    public String getName() {
        return "Persistence";
    }

    @Override
    public void configuration(ModuleConfig config, ApplicationContext context) {
        this.context = context;
        String module_name = config.getName();
        DatabaseConfig database = config.getDatabase();
        if(database == null){
            throw  new RuntimeException("[ERROR]... Module: "+module_name+"  - Database Configuration is missing");
        }
        /// wir haben erstmal nur eine Datenbank. Das kann später erweitert werden
        Persistence persistence = new Persistence(database, context);
        persistence.initialize();

        _persistences.put(database.getHost(),persistence);
        QueryExecutor.setPersistences(_persistences);
    }

    @Override
    public void validate() {

        for(Persistence p : _persistences.values()){
            if(!p.isConnected()){
                throw new RuntimeException("Database not connected: " + p.getDatabaseName());
            }
            System.out.println("[INFO]... Database: "+p.getDatabaseName()+" is connected");
        }
    }

    @Override
    public void run() {
        createSchemas();
    }

    @Override
    public void stop() {
        for(Persistence p : _persistences.values()){
            try {
                p.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * SOLL NICHT HIER BLEIBEN----------------*/
    public void createSchemas() {
        try {
            // Alle schema.sql Dateien im Klassenpfad finden
            Enumeration<URL> resources = Thread.currentThread()
                    .getContextClassLoader()
                    .getResources("database/schema.sql");

            Vector<java.net.URL> urls = new Vector<>();
            while (resources.hasMoreElements()) {
                urls.add(resources.nextElement());
            }

            if (urls.isEmpty()) {
                System.out.println("Keine schema.sql Dateien gefunden!");
                return;
            }
            try (Connection conn = _persistences.values().iterator().next().getDataSource().getConnection();
                 Statement stmt = conn.createStatement()) {

                DatabaseMetaData metaData = conn.getMetaData();
            // Jede Datei nacheinander ausführen
            for (java.net.URL url : urls) {
                //System.out.println("Schema ausführen: " + url);

                try (InputStream is = url.openStream();
                     Scanner scanner = new Scanner(is, StandardCharsets.UTF_8)) {

                    scanner.useDelimiter(";"); // Trennung der Statements


                        while (scanner.hasNext()) {
                            String sql = scanner.next().trim();
                            if (!sql.isEmpty()) {
                                if(sql.startsWith("CREATE")){
                                    String tableName = extractTableName(sql);
                                    if (tableExists(metaData, tableName)) {
                                        System.out.println("Table " + tableName + " already exists, skipping...");
                                        continue;
                                    }
                                } else if (isConstraintStatement(sql)) {
                                    String constraintName = extractConstraintName(sql);
                                    if(constraintName == null){
                                        continue;
                                    }
                                    if(constraintExists(conn, constraintName)){
                                        continue;
                                    }
                                }
                                System.out.println(sql);
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

    /**
     * Prüft, ob ein SQL-Statement ein Constraint-Statement ist
     */
    public static boolean isConstraintStatement(String sql) {
        if (sql == null) return false;
        return sql.toLowerCase().contains("constraint");
    }

    public static String extractConstraintName(String sql) {
        Matcher matcher = CONSTRAINT_PATTERN.matcher(sql);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null; // konnte nicht gefunden werden
    }

    /**
     * Prüft, ob ein Constraint in der DB existiert
     */
    public static boolean constraintExists(Connection conn, String constraintName) throws SQLException {
        if (constraintName == null) return false;

        String sql = "SELECT 1 FROM sys.foreign_keys WHERE name = ? " +
                "UNION ALL " +
                "SELECT 1 FROM sys.key_constraints WHERE name = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, constraintName);
            ps.setString(2, constraintName);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean tableExists(DatabaseMetaData metaData, String tableName) throws SQLException {
        try (ResultSet rs = metaData.getTables(null, null, tableName.toUpperCase(), new String[]{"TABLE"})) {
            return rs.next();
        }
    }
    private String extractTableName(String sql) {
        // Regex für "CREATE TABLE <name>"
        Pattern pattern = Pattern.compile("(?i)CREATE\\s+TABLE\\s+([a-zA-Z0-9_]+)");
        Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
