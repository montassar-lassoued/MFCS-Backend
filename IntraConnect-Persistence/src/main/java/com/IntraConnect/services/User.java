package com.IntraConnect.services;

public class User {
	
	String username;
	String email;
	String password;
	String role;
	
	public User(String username, String email, String password, String role ) {
		this.role = role;
		this.password = password;
		this.email = email;
		this.username = username;
	}
	
	public String getRole() {
		return role;
	}
	
	public String getPassword() {
		return password;
	}
	
	public String getEmail() {
		return email;
	}
	
	public String getUsername() {
		return username;
	}
}
