package com.ge.capital.dms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ge.capital.dms.dao.AuthServiceDAO;


@Service
public class AuthService {
	
	@Autowired
	AuthServiceDAO authServiceDAO;

	public String getToken(String jSessionID) {
		
		return authServiceDAO.getToken(jSessionID);
	}
	public String getID(String apiToken) {
		// TODO Auto-generated method stub
		return authServiceDAO.getID(apiToken);
	}
	
}
