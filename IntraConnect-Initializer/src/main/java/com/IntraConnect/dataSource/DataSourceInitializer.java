package com.IntraConnect.dataSource;

import org.jdom2.filter.ElementFilter;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.jdom2.Element;
import com.IntraConnect.xml.DatabaseConfig;

import java.util.HashMap;
import java.util.Map;


/**
 # =====================
        # DATENBANK CONFIG
 # =====================
*/

public class DataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private DatabaseConfig databaseConfig;
    public DataSourceInitializer( Element root) {
        // Suche das Modul „Persistence“
		Element modules = root.getChild("Modules");
		
		Element persistenceModule =
				modules.getContent(new ElementFilter("Module"))
						.stream()
						.filter(m -> "Persistence".equals(m.getAttributeValue("name")))
						.findFirst()
						.orElseThrow(() -> new IllegalArgumentException("Persistence module not found"));
		
		if (persistenceModule != null) {
			readDatabase(persistenceModule);
		}

    }
	private void readDatabase(Element module) {
		Element database = module.getChild("Database");
		databaseConfig = new DatabaseConfig();
		String name = database.getAttributeValue("name");
		databaseConfig.setName(name);
		String type = database.getAttributeValue("type");
		databaseConfig.setType(type);
		String host = database.getAttributeValue("host");
		databaseConfig.setHost(host);
		int port = Integer.parseInt(database.getAttributeValue("port"));
		databaseConfig.setPort(port);
		boolean encrypt = Boolean.parseBoolean(database.getAttributeValue("encrypt"));
		databaseConfig.setEncrypt(encrypt);
		
		String username = database.getChildText("Username");
		databaseConfig.setUsername(username);
		String password = database.getChildText("Password");
		databaseConfig.setPassword(password);

	}
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment env = applicationContext.getEnvironment();

        Map<String, Object> props = new HashMap<>();
        props.put("spring.datasource.url", buildJdbcUrl(databaseConfig)+";selectMethod=cursor");
        props.put("spring.datasource.username", databaseConfig.getUsername());
        props.put("spring.datasource.password", databaseConfig.getPassword());
        props.put("spring.datasource.hikari.auto-commit", false);
        //props.put("spring.datasource.hikari.leak-detection-threshold", 2000);
        props.put("spring.datasource.hikari.maximum-pool-size", 20);
        props.put("spring.datasource.hikari.connection-timeout", 60000);
        if(databaseConfig.getType().startsWith("sql")){ // com.microsoft.sqlserver.jdbc.SQLServerDriver
            props.put("spring.datasource.driver-class-name", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        }

		props.put("spring.flyway.enabled", "true");
		//Scripts
		props.put("spring.flyway.locations", "classpath:/database");
		// FALSE - verhindert „heimliches Einsteigen“
		// TRUE - egal, wir steigen irgendwo ein
		props.put("spring.flyway.baseline-on-migrate", "false");
		//erkennung von Inkonsistenzen sofort
		props.put("spring.flyway.validate-on-migrate", "true");
		props.put("logging.level.org.flywaydb", "DEBUG");
		// wenn classpath:/database nicht gefunden werden
		props.put("spring.flyway.fail-on-missing-locations", "true");
		//löscht ALLE Tabellen,
		//setzt DB auf leer.
		//Schutz vor: versehentlichem Datenverlust, falschen Scripts / Testcode in Produktion
		// PROD-SYS immer TRUE
		props.put("spring.flyway.clean-disabled", "false");
		
		
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
