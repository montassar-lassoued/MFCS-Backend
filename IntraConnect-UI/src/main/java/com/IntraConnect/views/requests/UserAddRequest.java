package com.IntraConnect.views.requests;

import com.IntraConnect._enum.Request;
import com.google.common.base.Strings;
import com.IntraConnect.queryExec.transaction.Transaction;
import com.IntraConnect.listViews.FieldMeta;
import com.IntraConnect.listViews.actionServices.PilotServiceSingleRequest;
import com.IntraConnect.listViews.fieldType.Field;

import java.util.List;
import java.util.Map;

public class UserAddRequest extends PilotServiceSingleRequest {

    @Override
    public Object handle(Map<String, Object> payload) {

        try (Transaction transaction = Transaction.create()){
            String name = (String) payload.get("Name");
            String email = (String)payload.get("EMail");
            String rolle = (String)payload.get("Rolle");
            String status = (String)payload.get("Status");
			
			if(Strings.isNullOrEmpty(name) || Strings.isNullOrEmpty(email) || Strings.isNullOrEmpty(rolle)){
				return new Exception("Name kann nicht null sein");
			}

            String query = "INSERT INTO APPUSERS (USERNAME,EMAIL, ROLE_ID, STATE)" +
                    "VALUES ('"+name+"','"+email+"',(SELECT ID " +
                    "                               FROM ROLE WHERE ROLE = '"+rolle+"')," +
                    " '"+status+"')";

            transaction.insert(query);

            transaction.commit();

            return Request.OK;
        }
        catch (Exception e){
            return e.getMessage();
        }
    }

    @Override
    public List<FieldMeta> viewData() {
		
        return  List.of(
                FieldMeta.of("Name").type(Field.TEXT_FIELD).editable(false).defaultValue("fbvfbvf"),
                FieldMeta.of("EMail").type(Field.TEXT_FIELD).editable(true).defaultValue("hallo"),
                FieldMeta.of("Rolle").type(Field.COMBOBOX).editable(true).query("SELECT ROLE FROM ROLE"),
                FieldMeta.of("Status").type(Field.TEXT_FIELD).editable(true));
    }
}
