package com.ge.capital.dms.fr.sle.controllers.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ge.capital.dms.entity.Data;
import com.ge.capital.dms.service.TestService;

//http://localhost:8080/porthealth/statusmonitor
//http://g2422usdwgpa02v.logon.ds.ge.com:9191/api/porthealth/statusmonitor
//https://commoncmssvc-uat.sites.ge.com/api/porthealth/statusmonitor
@RestController
@SpringBootConfiguration
@RequestMapping("/porthealth")

public class PortHealthMonitor {
	@Autowired
	TestService testService;

	@RequestMapping(method = RequestMethod.GET, produces = {
			MediaType.APPLICATION_JSON_VALUE }, value = "/statusmonitor")
	public Data read() {
		Data data = new Data();

		data.setPortStatus("WORKING");
		data.setDBStatus(testService.dbMonitor());
		return data;
	}

}