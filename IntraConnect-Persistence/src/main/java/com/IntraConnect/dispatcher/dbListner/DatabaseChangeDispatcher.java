package com.IntraConnect.dispatcher.dbListner;

import com.IntraConnect.queryExec.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;

@Component
public class DatabaseChangeDispatcher {
	
	private static final Logger log = LoggerFactory.getLogger(DatabaseChangeDispatcher.class);
	private final TableEventRegistry registry;
	private volatile boolean running = false;
	private Thread pollThread;
	
	public DatabaseChangeDispatcher(TableEventRegistry registry) {
		this.registry = registry;
	}
	
	public void start() {
		running = true;
		pollThread= new Thread(this::pollLoop, "ChangeEventPoller");
		pollThread.start();
	}
	
	private void pollLoop() {
		
		while (running) {
			try (Transaction transaction = Transaction.create()) {
				
				ResultSet rs = transaction.select("SELECT TOP 50 id, " +
						"table_name, operation, entity_id  " +
						"FROM ChangeEventListener WITH (UPDLOCK, READPAST)  " +
						"WHERE processed = 0 ORDER BY id");
				
				
				while (rs.next()) {
					
					long id = rs.getLong("id");
					String table = rs.getString("table_name");
					String operation = rs.getString("operation");
					long entityId = rs.getLong("entity_id");
					
					// 🔥 Listener feuern
					registry.fire(
							new TableChangeEvent(table, operation, entityId)
					);
					
					transaction.update("UPDATE ChangeEventListener SET processed = 1 WHERE id ="+id);
				}
				
				transaction.commit();
				Thread.sleep(500);
			}  catch (InterruptedException e) {
				// sauberer Abbruch
				Thread.currentThread().interrupt();
				break;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}
	
	public void stop() {
		running = false;                  // Flag setzen
		if (pollThread != null) {
			pollThread.interrupt();        // ggf. Thread aufwecken
			try {
				pollThread.join(2000);    // auf sauberes Beenden warten
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
