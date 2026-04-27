package com.IntraConnect.handler;

import com.IntraConnect.handler.records.ConnectableIntfState;
import com.IntraConnect.queryExec.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConnectableUpdateStateHandler extends ConnectableStateHandler {
	
	private static final Logger log = LoggerFactory.getLogger(ConnectableUpdateStateHandler.class);
	
	@Override
	public void handle(ConnectableIntfState payload) {
		if(payload != null){
			if(!payload.Connectable_Name().isBlank() && payload.state() != null){
				String sql = "UPDATE CONTROLLER SET STATE = ? WHERE NAME = ?";
				try (Transaction transaction = Transaction.create()){
					transaction.update(
							sql,
							payload.state(),
							payload.Connectable_Name()
					);
					transaction.commit();
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}
		}
	}
}
