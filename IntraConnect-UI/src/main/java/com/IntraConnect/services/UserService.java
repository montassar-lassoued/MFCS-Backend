package com.IntraConnect.services;

import com.IntraConnect.user.User;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface UserService {

    ResponseEntity<String> signup(Map<String, String> requestMap);

    ResponseEntity<String> login(Map<String, String> requestMap);

    ResponseEntity<List<User>> getAllUser();
	
	ResponseEntity<String> logout(Map<String, String> requestMap);
}
