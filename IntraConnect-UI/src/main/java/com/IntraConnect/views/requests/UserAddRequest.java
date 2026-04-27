package com.IntraConnect.views.requests;

import com.IntraConnect.BeanUtil.BeanUtil;
import com.IntraConnect._enum.Response;
import com.google.common.base.Strings;
import com.IntraConnect.queryExec.transaction.Transaction;
import com.IntraConnect.listViews.FieldMeta;
import com.IntraConnect.listViews.actionServices.IntraConnectServiceSingleRequest;
import com.IntraConnect.listViews.fieldType.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


public class UserAddRequest extends IntraConnectServiceSingleRequest {
	
	private static final Logger log = LoggerFactory.getLogger(UserAddRequest.class);

	
	@Override
    public Response handle(Map<String, Object> payload) {

        try (Transaction transaction = Transaction.create()){
            String name = (String) payload.get("Name");
            String email = (String)payload.get("EMail");
            String rolle = (String)payload.get("Rolle");
            String password = (String)payload.get("Password");
			
			if(Strings.isNullOrEmpty(name) || Strings.isNullOrEmpty(email) || Strings.isNullOrEmpty(rolle)){
				log.error("{}: Name kann darf nicht leer sein",UserAddRequest.class);
				return Response.INCOMPLETE_DATA;
			}
			PasswordEncoder encoder = BeanUtil.getBean(PasswordEncoder.class);
			String pswEncoded = encoder.encode(password);
			
            String query = "INSERT INTO APPUSERS (USERNAME,EMAIL, PASSWORD, ROLE_ID)" +
                    "VALUES (?, ?, ?, (SELECT ID FROM ROLE WHERE ROLE = ?))";

            transaction.insert(query,name,email,pswEncoded,rolle);

            transaction.commit();
			
			return Response.OK;
        }
        catch (Exception e){
            log.error(e.getMessage());
			return Response.INTERNAL_SERVER_ERROR;
        }
    }

    @Override
    public List<FieldMeta> viewData() {
		
        return  List.of(
                FieldMeta.of("Name").type(Field.TEXT_FIELD).editable(true),
                FieldMeta.of("EMail").type(Field.TEXT_FIELD).editable(true),
				FieldMeta.of("Password").type(Field.PASSWORD).editable(true),
                FieldMeta.of("Rolle").type(Field.COMBOBOX).editable(true).query("SELECT ROLE FROM ROLE"));
                //FieldMeta.of("Status").type(Field.TEXT_FIELD).editable(true));
    }
}
