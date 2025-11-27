package com.pilot.dataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import xml.DatabaseConfig;
import xml.ModuleConfig;
import xml.SystemConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 # =====================
        # DATENBANK CONFIG
 # =====================
*/

public class DataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private final DatabaseConfig databaseConfig;
    public DataSourceInitializer(SystemConfig systemConfig) {
        // Suche das Modul „Persistence“
        this.databaseConfig = systemConfig.getModules().getModules()
                .stream()
                .filter(m -> m.getName().equalsIgnoreCase("Persistence"))
                .map(ModuleConfig::getDatabase)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Persistence module not found"));

    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment env = applicationContext.getEnvironment();

        Map<String, Object> props = new HashMap<>();
        props.put("spring.datasource.url", buildJdbcUrl(databaseConfig));
        props.put("spring.datasource.username", databaseConfig.getUsername());
        props.put("spring.datasource.password", databaseConfig.getPassword());
        if(databaseConfig.getType().startsWith("sql")){
            props.put("spring.datasource.driver-class-name", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        }

        env.getPropertySources().addFirst(new MapPropertySource("dynamicProps", props));

        System.out.println(">>> ApplicationContextInitializer: DataSource Properties gesetzt!");
    }

    private String buildJdbcUrl(DatabaseConfig db) {
        return String.format(
                "jdbc:%s://%s:%d;databaseName=%s;encrypt=%s",
                db.getType(),
                db.getHost(),
                db.getPort(),
                db.getName(),
                db.getEncrypt()
        );
    }
}
