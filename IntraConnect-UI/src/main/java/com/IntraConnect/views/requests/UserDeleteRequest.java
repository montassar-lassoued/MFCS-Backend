package com.IntraConnect.views.requests;

import com.IntraConnect._enum.Response;
import com.IntraConnect.listViews.actionServices.IntraConnectServiceSingleRequest;
import com.IntraConnect.queryExec.transaction.Transaction;

import java.util.Map;

public class UserDeleteRequest extends IntraConnectServiceSingleRequest {
	@Override
	public Response handle(Map<String, Object> payload) {
		int id = (int) payload.get("ID");
		String rolle = (String) payload.get("ROLLE");
		if (rolle.equals("ADMIN"))
			return Response.ERROR;
		
		try (Transaction transaction = Transaction.create()){
			
			String sql = "DELETE FROM APPUSERS WHERE ID = ?";
			
			transaction.delete(sql, id);
			transaction.commit();
			
			return Response.OK;
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	
	}
	
}
