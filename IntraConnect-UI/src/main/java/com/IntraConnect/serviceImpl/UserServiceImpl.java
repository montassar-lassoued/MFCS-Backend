package com.IntraConnect.serviceImpl;

import com.IntraConnect.queryExec.transaction.Transaction;
import com.IntraConnect.user.User;
import com.IntraConnect.jwt.JwtFilter;
import com.IntraConnect.jwt.JwtUtil;
import com.IntraConnect.services.CustomUserDetailsService;
import com.IntraConnect.services.UserService;
import com.IntraConnect.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.IntraConnect.constants.Constants.*;


@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    CustomUserDetailsService customUserDetailsService;
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    JwtFilter jwtFilter;

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
			log.error(e.getMessage());
        }
        return Utils.getResponseEntity(INVALID_DATA, HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        log.info("Inside login{}", requestMap);
		
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestMap.get("email"),requestMap.get("password"))
            );
			
            if(authentication.isAuthenticated()){
                return new ResponseEntity<String>("{\"token\":\""+
                        jwtUtil.generateToken(
                                customUserDetailsService.getUser().getEmail(),
                                customUserDetailsService.getUser().getRole())+"\"}",
                HttpStatus.OK);
            }
            else {
                return new ResponseEntity<String>("{\"message\":\""+" Waite for admin approval."+"\"}",
                        HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return new ResponseEntity<String>("{\"message\":\""+" Bad credential."+"\"}",
                HttpStatus.BAD_REQUEST);
    }
	
	@Override
	public ResponseEntity<String> logout(Map<String, String> requestMap) {
		return Utils.getResponseEntity(LOGGED_OUT, HttpStatus.OK);
	}
	
	@Override
    public ResponseEntity<List<User>> getAllUser() {
        log.info("Inside getAllUser");
        try {
            List<User> users = new ArrayList<>();
            if(jwtFilter.isAdmin()){

                String query = "SELECT " +
                        "APPUSERS.USERNAME, " +
                        "APPUSERS.EMAIL, " +
                        "APPUSERS.PASSWORD, " +
                        "ROLE.ROLE " +
                        "FROM APPUSERS " +
                        "LEFT JOIN ROLE  ON ROLE.id = APPUSERS.role_id ";

                try(Transaction transaction = Transaction.create()){
                    ResultSet rs = transaction.select(query);

                    while (rs.next()) {
                        String name = rs.getString("USERNAME");
                        String email = rs.getString("EMAIL");
                        String password = rs.getString("PASSWORD");
                        String role = rs.getString("ROLE"); // "ADMIN" oder "USER"

                        User user = new User(name,email,password,role);
                        users.add(user);
                    }

                }catch (Exception e){
                   log.error(e.getMessage());
                }
            }

            return new ResponseEntity<List<User>>(users, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return new ResponseEntity<List<User>>(new ArrayList<>(), HttpStatus.BAD_REQUEST);
    }

    private boolean validationSignUpMap(Map<String, String> requestMap){
        return requestMap.containsKey("name") && requestMap.containsKey("email")
                && requestMap.containsKey("password") && requestMap.containsKey("role");
    }

    private boolean existsUserByEmail(String email){
        boolean exist = false;
        String query = "SELECT * FROM APPUSERS WHERE EMAIL = ?";
        try(Transaction transaction = Transaction.create()){
            ResultSet rs = transaction.select(query, email);
            if(rs.next()){
              exist = true;
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return exist;
    }
    private void saveUser(Map<String, String> requestMap){

        String query = "INSERT INTO APPUSERS (USERNAME, EMAIL,PASSWORD, ROLE_ID,STATE) " +
                "VALUES (" +
                "'"+requestMap.get("name")+"'," +
                "'"+requestMap.get("email")+"'," +
                "'"+requestMap.get("password")+"'," +
                "(SELECT ID FROM ROLE WHERE ROLE = '"+requestMap.get("role")+"'),'-')";

        try(Transaction transaction = Transaction.create()){
            transaction.insert(query);
            transaction.commit();
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }
}
