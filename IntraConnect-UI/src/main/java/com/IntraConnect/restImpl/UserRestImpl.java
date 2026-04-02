package com.IntraConnect.restImpl;

import com.IntraConnect.user.User;
import com.IntraConnect.rest.UserRest;
import com.IntraConnect.services.UserService;
import com.IntraConnect.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.IntraConnect.constants.Constants.SOMETHING_WENT_WRONG;

@RestController
public class UserRestImpl implements UserRest {
	private static final Logger log = LoggerFactory.getLogger(UserRestImpl.class);
	
    @Autowired
    UserService userService;
    @Override
    public ResponseEntity<String> signup(Map<String, String> requestMap) {
        try {
            return userService.signup(requestMap);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return Utils.getResponseEntity(SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        try {
            return userService.login(requestMap);
        }catch (Exception ex){
            log.error(ex.getMessage());
        }
        return Utils.getResponseEntity(SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<User>> getAllUser() {
        try {
            return userService.getAllUser();
        }catch (Exception ex){
            log.error(ex.getMessage());
        }
        return new ResponseEntity<List<User>>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
	
	@Override
	public ResponseEntity<String> logout() {
		try {
			Map<String, String> requestMap = new HashMap<>();
			return userService.logout(requestMap);
		}catch (Exception ex){
			log.error(ex.getMessage());
		}
		return Utils.getResponseEntity(SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
}
