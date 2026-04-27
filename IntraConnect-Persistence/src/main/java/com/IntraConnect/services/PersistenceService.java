package com.IntraConnect.services;

import com.IntraConnect.command.handlerReg.time.FixedRateTrigger;
import com.IntraConnect.dispatcher.dbListner.DatabaseChangeDispatcher;
import com.IntraConnect.command.handlerReg.Register;
import com.IntraConnect.dispatcher.processors.TransferInDispatcher;
import com.IntraConnect.handler.ConnectableStateHandler;
import com.IntraConnect.persistence.Persistence;
import com.IntraConnect.helper.Console;
import com.IntraConnect.intf.IntraConnectCoreServices;
import com.IntraConnect.queryExec.transaction.Transaction;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import com.IntraConnect.xml.DatabaseConfig;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PersistenceService extends IntraConnectCoreServices {
	
	private static final Logger log = LoggerFactory.getLogger(PersistenceService.class);

	
	private final HashMap<String, Persistence> Databases = new HashMap<>();
	private Persistence mainDatabase ;
	private boolean enabled;
	private List<User> users = new ArrayList<>();
	private List<Object[]> userRoles = new ArrayList<>();
	@Autowired
	private DatabaseChangeDispatcher databaseChangeDispatcher;
	@Autowired
	private TransferInDispatcher transferInDispatcher;
	@Autowired
	private PasswordEncoder passwordEncoder;
	
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
				throw new RuntimeException("No main database defined in configuration!");
			}
			
			Element elUsers = module.getChild("Users");
			List<Element> elUserList = elUsers.getChildren("User");
			for (Element elUser : elUserList){
				String name = elUser.getAttributeValue("name");
				String email = elUser.getAttributeValue("email");
				String password = elUser.getAttributeValue("password");
				String role = elUser.getAttributeValue("role");
				
				users.add(new User(name,email,password,role));
			}
			
			Element elRoles = module.getChild("Roles");
			List<Element> elRoleList = elRoles.getChildren("Role");
			for (Element elRole : elRoleList){
				String name = elRole.getAttributeValue("name");
				String description = elRole.getAttributeValue("description");
				
				userRoles.add(new Object[]{name, description});
			}
		}
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
	public void register() {
		//Handlers
		register.registerHandler(ConnectableStateHandler.class);
		//Users
		addLoginUsers();
	}
	
	private void addLoginUsers() {
		try (Transaction transaction = Transaction.create()){
			
			int countR = transaction.queryCount("SELECT COUNT(*) FROM ROLE");
			if (countR <1) {
				String sqlRole = "INSERT INTO ROLE (ROLE, DESCRIPTION) VALUES (?,?)";
				transaction.insertBatch(sqlRole, userRoles);
			}
			int countU = transaction.queryCount("SELECT COUNT(*) FROM APPUSERS");
			if (countU <1) {
				String sql = "INSERT INTO APPUSERS (USERNAME, EMAIL, PASSWORD, ROLE_ID) " +
						"VALUES (?, ?, ?, (SELECT ID FROM ROLE WHERE ROLE = ?))";
				for (User user :users){
					String password = passwordEncoder.encode(user.getPassword());
					transaction.insert(sql,
							user.getUsername(),
							user.getEmail(),
							password,
							user.getRole());
				}
			}
			transaction.commit();
		} catch (Exception e) {
			throw new RuntimeException(e);
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