package com.IntraConnect.handler;

import com.IntraConnect.intf.Handler;
import com.IntraConnect.queryExec.transaction.Transaction;
import com.IntraConnect.record.LoadUnitDelete;

import java.sql.ResultSet;

public class LoadUnitDeleteHandler implements Handler<LoadUnitDelete> {
	
	@Override
	public void handle(LoadUnitDelete payload) {

		if(payload != null && !payload.LoadUnitNumber().isBlank()){
			
			try (Transaction transaction = Transaction.create()){
				String sql = "SELECT ID FROM LOADUNIT WHERE NUMBER =" + payload.LoadUnitNumber();
				ResultSet rs = transaction.select(sql);
				if(rs.next()){
					long id = rs.getLong("ID");
					transaction.delete("DELETE FROM LoadUnit_Article WHERE loadUnit_ID = " +id);
					
					transaction.delete("DELETE FROM LoadUnit_Roadway WHERE loadUnit_ID = "+id);
					
					transaction.delete("DELETE FROM LOADUNIT WHERE NUMBER = "+payload.LoadUnitNumber());
				}
				transaction.commit();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
