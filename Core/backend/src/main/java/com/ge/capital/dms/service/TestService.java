//TestService dbMonitor

package com.ge.capital.dms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.ge.capital.dms.dao.TestServiceDAO;


@Service
public class TestService {
	
	@Autowired
	TestServiceDAO testServiceDAO;
	
	public String dbMonitor() {
		
		return testServiceDAO.dbMonitor();
	}	
}
