package com.IntraConnect.dispatcher.processors;

import com.IntraConnect._enum.Transfer;
import com.IntraConnect.command.trigger.Trigger;
import com.IntraConnect.dispatcher.dbListner.DatabaseChangeDispatcher;
import com.IntraConnect.dispatcher.dbListner.TableChangeEvent;
import com.IntraConnect.dispatcher.dbListner.TableEventRegistry;
import com.IntraConnect.dispatcher.processors.handler.ProcessorData;
import com.IntraConnect.dispatcher.processors.handler.ProcessorDataHandler;
import com.IntraConnect.queryExec.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;

@Component
public class TransferInDispatcher {
	private static final Logger log = LoggerFactory.getLogger(TransferInDispatcher.class);

	private volatile boolean running = false;
	private Thread pollThread;
	
	public TransferInDispatcher() {

	}
	
	public void start() {
		running = true;
		pollThread= new Thread(this::pollLoop, "TransferInDispatcher");
		pollThread.start();
	}
	
	private void pollLoop() {
		
		while (running) {
			try (Transaction transaction = Transaction.create()) {
				
				ResultSet rs = transaction.select("SELECT *" +
						"FROM Transfer_In WITH (UPDLOCK, READPAST) " +
						"JOIN Controller ON (Controller.id = Transfer_In.controller_ID) " +
						"WHERE processed = '"+ Transfer.NEW+"' ORDER BY _date DESC");
				
				
				while (rs.next()) {
					
					long id = rs.getLong("id");
					String controller = rs.getString("name");
					byte[] content = rs.getBytes("content");
					
					// 🔥 an den Handler übergeben
					Trigger.get().addQueue(ProcessorDataHandler.class, new ProcessorData(controller, content));
					
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
