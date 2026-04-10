package com.IntraConnect.handler;

import com.IntraConnect.handler.records.ConnectableIntfState;
import com.IntraConnect.queryExec.transaction.Transaction;


public class ConnectableUpdateStateHandler extends ConnectableStateHandler {
	
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
					throw new RuntimeException(e);
				}
			}
		}
	}
}
