package com.IntraConnect.handler;

import com.IntraConnect.intf.Handler;
import com.IntraConnect.queryExec.transaction.Transaction;
import com.IntraConnect.record.LoadUnitCreate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class LoadUnitCreateHandler implements Handler<LoadUnitCreate> {
	
	private static final Logger log = LoggerFactory.getLogger(LoadUnitCreateHandler.class);
	
	@Override
	public void handle(LoadUnitCreate payload) {
		
		try (Transaction transaction = Transaction.create()){
			String sql = "SELECT ID FROM LOADUNIT WHERE NUMBER =" + payload.LoadUnitNumber();
			boolean exists = transaction.exists(sql);
			if(!exists){
				transaction.insert("INSERT INTO LOADUNIT " +
						"(NUMBER, DESCRIPTION) " +
						"VALUES (" +
						payload.LoadUnitNumber()+"," +
						(payload.description().isBlank() ? "": payload.description())+")");
						
				
				if(payload.article_ID() != null){
					transaction.insert("INSERT INTO LoadUnit_Article " +
							"(LOADUNIT_ID, ARTICLE_ID) " +
							"VALUES ((SELECT ID FROM LOADUNIT WHERE NUMBER =" + payload.LoadUnitNumber()+"),"+payload.article_ID()+")");
				}
			}
			transaction.commit();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
