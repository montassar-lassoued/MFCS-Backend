package com.IntraConnect.handler;

import com.IntraConnect._enum.LuState;
import com.IntraConnect.intf.Handler;
import com.IntraConnect.nodes.Graph;
import com.IntraConnect.nodes.LoadUnitPath;
import com.IntraConnect.nodes.NextStep;
import com.IntraConnect.queryExec.transaction.Transaction;
import com.IntraConnect.record.LoadUnitPathTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.ResultSet;

public class LoadUnitPathTriggerHandler implements Handler<LoadUnitPathTrigger> {
	private static final Logger log = LoggerFactory.getLogger(LoadUnitPathTriggerHandler.class);
	
	@Override
	public void handle(LoadUnitPathTrigger payload) {
		
		try (Transaction transaction = Transaction.create()){
			String sql;
			if(payload != null && !payload.LoadUnitNumber().isBlank()){
				sql = "SELECT * FROM LoadUnit_Roadway WHERE NUMBER ='"+payload.LoadUnitNumber()+"'";
			}
			else {
				sql = "SELECT * FROM LoadUnit_Roadway WHERE STATE NOT IN ('" + LuState.ACTIVE + "','" + LuState.FINISHED + "')";
			}
			ResultSet rs = transaction.select(sql);
			while (rs.next()){
				try {
					long id = rs.getLong("ID");
					String location = rs.getString("LOCATION");
					String destination = rs.getString("DESTINATION");
					
					String update;
					if (location.equals(destination)) {
						update = "UPDATE LoadUnit_Roadway SET STATE = '" + LuState.FINISHED + "' WHERE ID = " + id;
					} else {
						NextStep step = LoadUnitPath.calculateNextStep(Graph.get(), location, destination);
						if (!step.getError().isBlank()) {
							update = "UPDATE LoadUnit_Roadway SET STATE = '" + step.getError() + "' WHERE ID = " + id;
							log.error("LoadUnit-ID:{} From {} to {} -> {}", id, location, destination, step.getError());
						} else {
							update = "UPDATE LoadUnit_Roadway SET NEXTLOCATION ='" + step.getNextTarget() + "'," +
									" STATE = '" + LuState.ACTIVE + "' WHERE ID = " + id;
						}
					}
					
					transaction.update(update);
					transaction.commit();
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}
			
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
}
