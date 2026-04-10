package com.IntraConnect.services;

import com.IntraConnect.command.handlerReg.time.FixedRateTrigger;
import com.IntraConnect.command.handlerReg.time.TriggerTime;
import com.IntraConnect.dispatcher.dbListner.DatabaseChangeDispatcher;
import com.IntraConnect.command.handlerReg.Register;
import com.IntraConnect.dispatcher.processors.TransferInDispatcher;
import com.IntraConnect.handler.ConnectableStateHandler;
import com.IntraConnect.persistence.Persistence;
import com.IntraConnect.helper.Console;
import com.IntraConnect.intf.PilotCoreServices;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import com.IntraConnect.xml.DatabaseConfig;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PersistenceService extends PilotCoreServices {
	
	private static final Logger log = LoggerFactory.getLogger(PersistenceService.class);

	
	private final HashMap<String, Persistence> Databases = new HashMap<>();
	private Persistence mainDatabase ;
	private boolean enabled;
	@Autowired
	private DatabaseChangeDispatcher databaseChangeDispatcher;
	@Autowired
	private TransferInDispatcher transferInDispatcher;
	
	public PersistenceService(Register register) {
		super(register);
	}
	
	@Override
	public String getName() {
		return "Persistence";
	}
	
	@Override
	public void configuration(Element module, ApplicationContext context) {
		if (module != null) {
			enabled = Boolean.parseBoolean(module.getAttributeValue("enabled"));
			if (!enabled) {
				return;
			}
			int main =0;
			List<Element> databases = module.getChildren("Database");
			for (Element database : databases) {
				DatabaseConfig databaseConfig = new DatabaseConfig();
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
				
				boolean isMain = Boolean.parseBoolean(database.getAttributeValue("main", "false"));
				
				String username = database.getChildText("Username");
				databaseConfig.setUsername(username);
				String password = database.getChildText("Password");
				databaseConfig.setPassword(password);
				
				Persistence persistence = new Persistence(databaseConfig, context);
				persistence.initialize();
				
				// 1. JEDE Datenbank kommt in die Map, damit sie verwaltet werden kann
				this.Databases.put(name, persistence);
				
				// 2. Wenn es die Main-DB ist, zusätzlich die Referenz setzen
				if (isMain) {
					if (this.mainDatabase != null) {
						throw new RuntimeException("Module 'Persistence' -> there are more than one 'main' databases");
					}
					this.mainDatabase = persistence;
				}
			}
			// 3. Sicherheitscheck: Wurde eine Main-DB definiert?
			if (enabled && this.mainDatabase == null) {
				// Optional: Falls die erste Datenbank automatisch "main" sein soll, wenn nichts definiert ist:
				// if (!Databases.isEmpty()) this.mainDatabase = Databases.values().iterator().next();
				// ODER (sicherer) Fehler werfen:
				log.warn("No main database defined in configuration!");
			}
		}
	}
	
	@Override
	public void register() {
		register.registerHandler(ConnectableStateHandler.class, new FixedRateTrigger(1000));
	}
	
	@Override
	public void validate() {
		if (!enabled) {
			return;
		}
		// Sicherstellen, dass die Main-DB existiert
		getMainDatabaseOrThrow();
		
		for (Persistence p : Databases.values()) {
			if (!p.isConnected()) {
				throw new RuntimeException("can't connect to Database '" + p.getDatabaseName() + "'");
			}
			Console.info.println("Database '" + p.getDatabaseName() + "' is connected");
		}
	}
	
	@Override
	public void run() {
		if (!enabled) {
			return;
		}
		// DB Listener starten
		databaseChangeDispatcher.start();
		// Transfer_In Dispatcher
		transferInDispatcher.start();
	}
	
	@Override
	public void stop() {
		if (!enabled) {
			return;
		}
		// DB Listener stoppen
		databaseChangeDispatcher.stop();
		// Transfer_In Dispatcher
		transferInDispatcher.stop();
		
		// Connections schließen
		List<Exception> errors = new ArrayList<>();
		try {
			Persistence mainDB = getMainDatabaseOrThrow();
			if (mainDB instanceof AutoCloseable ac){
				ac.close();
			}
		} catch (Exception e) {
			errors.add(e);
		}
		// Schließe alle DB-Transaktionen
		for (Persistence db : Databases.values()) {
			if (db instanceof AutoCloseable ac) {
				try {
					ac.close(); // AutoCloseable sorgt für try-with-resources-safe Close
				} catch (Exception e) {
					errors.add(e);
				}
			}
		}
		// Zusammenfassende Exception werfen
		if (!errors.isEmpty()) {
			throw new RuntimeException(
					"Module " + getName() + " stopped with " + errors.size() + " error(s).",
					errors.getFirst() // erstes Error als Cause
			);
		}
	}
	
	public Persistence getMainDatabaseOrThrow() {
		if (mainDatabase == null) {
			throw new IllegalStateException("Main database not initialized");
		}
		return mainDatabase;
	}
	
	public Persistence getDatabaseOrThrow(String name) {
		Persistence p = Databases.get(name);
		if (p == null) {
			throw new IllegalArgumentException("Unknown database: " + name);
		}
		return p;
	}
}