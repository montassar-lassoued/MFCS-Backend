package com.IntraConnect.rest;

import com.IntraConnect.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@RequestMapping(path = "/user")
public interface UserRest {

    @PostMapping(path = "/signup")
    public ResponseEntity<String> signup(@RequestBody(required = true)Map<String,String> requestMap);

    @PostMapping(path = "/login")
    public ResponseEntity<String> login(@RequestBody(required = true)Map<String,String> requestMap);

    @GetMapping(path = "/get")
    public ResponseEntity<List<User>> getAllUser();
	
	@PostMapping(path = "/logout")
	public ResponseEntity<String> logout();
}
