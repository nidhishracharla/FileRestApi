package com.ge.capital.dms.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ge.capital.dms.dao.UserRealmDAO;

@Component
public class UserRealmService {

	@Autowired
	UserRealmDAO userRealmDAO;

	public List<String> userRealm(String userId) {
		
		return userRealmDAO.getRealm(userId);

	}
	public List<HashMap<String, String>> userDetails(String userId) {
		
		return userRealmDAO.getDetails(userId);

	}

	public void deleteUser(String all_users, String group_name) {
		
		userRealmDAO.deleteUser(all_users,group_name);
		
	}
	
	public String addUser(Map<String,String> userData) {
		return userRealmDAO.addUser(userData);
		
	}
}
