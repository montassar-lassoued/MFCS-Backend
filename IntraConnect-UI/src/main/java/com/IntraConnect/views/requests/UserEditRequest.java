package com.IntraConnect.views.requests;

import com.IntraConnect._enum.Response;
import com.IntraConnect.queryExec.transaction.Transaction;
import com.IntraConnect.listViews.FieldMeta;
import com.IntraConnect.listViews.actionServices.IntraConnectServiceSingleRequest;
import com.IntraConnect.listViews.fieldType.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class UserEditRequest extends IntraConnectServiceSingleRequest {
	
	private static final Logger log = LoggerFactory.getLogger(UserEditRequest.class);
	
	@Override
	public Response handle(Map<String, Object> payload) {
		
		try (Transaction transaction = Transaction.create()) {
			int id = (int) payload.get("ID");
			String name = (String) payload.get("Name");
			String email = (String) payload.get("EMail");
			String rolle = (String) payload.get("Rolle");
			String status = (String) payload.get("Status");
			
			String query = "UPDATE APPUSERS" +
					" SET USERNAME ='" + name + "', " +
					"EMAIL='" + email + "', " +
					"STATE='" + status + "', " +
					"ROLE_ID = (SELECT ID FROM " +
					"ROLE WHERE ROLE='" + rolle + "')  WHERE ID =" + id;
			transaction.update(query);
			transaction.commit();
			return Response.OK;
		} catch (Exception e) {
			log.error(e.getMessage());
			return Response.INTERNAL_SERVER_ERROR;
		}
	}
	
	@Override
	public List<FieldMeta> viewData() {
//		return null;
		
		return List.of(
				FieldMeta.of("Name").type(Field.TEXT_FIELD).editable(true),
				FieldMeta.of("EMail").type(Field.TEXT_FIELD).editable(true),
				FieldMeta.of("Rolle").type(Field.COMBOBOX).editable(true).query("SELECT ROLE FROM ROLE"),
				FieldMeta.of("Status").type(Field.TEXT_FIELD).editable(true));
	}
}
