package com.pilot.serviceImpl;

import com.pilot.queryExec.QueryExecutor;
import com.pilot.services.UserService;
import com.pilot.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static com.pilot.constants.Constants.*;


@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public ResponseEntity<String> signup(Map<String, String> requestMap) {
        log.info("Inside signup{}", requestMap);
        try {
            if(validationSignUpMap(requestMap)){
                if(!existsUserByEmail(requestMap.get("email"))){
                    saveUser(requestMap);
                    return Utils.getResponseEntity(REGISTERED, HttpStatus.OK);
                }
                else {
                    return Utils.getResponseEntity(EMAIL_ALREADY_EXISTS, HttpStatus.BAD_REQUEST);
                }
            }
            return Utils.getResponseEntity(INVALID_DATA, HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Utils.getResponseEntity(INVALID_DATA, HttpStatus.BAD_REQUEST);
    }

    private boolean validationSignUpMap(Map<String, String> requestMap){
        return requestMap.containsKey("name") && requestMap.containsKey("email")
                && requestMap.containsKey("password");
    }

    private boolean existsUserByEmail(String email){
        QueryExecutor executor = QueryExecutor.Create();
        ResultSet rs = executor.submitSelect(
                "SELECT * FROM APPUSERS WHERE EMAIL = '" + email + "'");

        try {
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private void saveUser(Map<String, String> requestMap){
        QueryExecutor executor = QueryExecutor.Create();
        executor.submitUpdate(
                "INSERT INTO APPUSERS (ID,USERNAME, EMAIL,PASSWORD, ROLE_ID,STATE) " +
                        "VALUES ('1'," +
                        "'"+requestMap.get("name")+"'," +
                        "'"+requestMap.get("email")+"'," +
                        "'"+requestMap.get("password")+"','1','-')" );
    }
}
