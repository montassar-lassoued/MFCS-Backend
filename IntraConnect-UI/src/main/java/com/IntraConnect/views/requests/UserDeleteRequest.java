package com.IntraConnect.views.requests;

import com.IntraConnect._enum.Response;
import com.IntraConnect.listViews.actionServices.IntraConnectServiceSingleRequest;
import com.IntraConnect.queryExec.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class UserDeleteRequest extends IntraConnectServiceSingleRequest {
	private static final Logger log = LoggerFactory.getLogger(UserDeleteRequest.class);
	
	@Override
	public Response handle(Map<String, Object> payload) {
		int id = (int) payload.get("ID");
		String rolle = (String) payload.get("ROLLE");
		if (rolle.equals("ADMIN")) {
			return Response.AUTHORIZATION_ERROR;
		}
		else {
			try (Transaction transaction = Transaction.create()) {
				
				String sql = "DELETE FROM APPUSERS WHERE ID = ?";
				
				transaction.delete(sql, id);
				transaction.commit();
				
				return Response.OK;
				
			} catch (Exception e) {
				log.error("Fehler aufgetreten: {}",e.getMessage());
				return Response.INTERNAL_SERVER_ERROR;
			}
		}
	
	}
	
}
