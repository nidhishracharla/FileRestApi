package com.ge.capital.dms.utility;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/*
* @author Varun Abbireddy
*/

@Component
@Scope("session")
public class UserLoginDetails {
	
	private String username;

    private String password;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

}
