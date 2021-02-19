package com.ge.capital.dms.utility;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.ge.capital.dms.service.AuthService;

@Component
public class AuthUtilityService {

	@Autowired
	AuthService authService;
	
	private static final Logger logger = Logger.getLogger(AuthUtilityService.class);

	public Boolean isAuthorized(String token, javax.servlet.http.Cookie[] cookie) {
		return true;
		/*String backToken = "", smSession = "", rsoSession = "";
		for (int i = 0; i < cookie.length; i++) {
			logger.info(cookie[i].getName() + " " + cookie[i].getValue());
			if (cookie[i].getName().equalsIgnoreCase("SMSESSION")) {
				smSession = cookie[i].getValue();

			}
			if (cookie[i].getName().equalsIgnoreCase("RSOSESSION")) {
				rsoSession = cookie[i].getValue();
			}
		}
		if (token != null && token.equalsIgnoreCase("DMS_BATCH_UTILITY")) {
			return true;
		} else if (token == null || smSession == null || rsoSession == null || "".equals(smSession)
				|| "".equals(rsoSession)) {
			return false;
		} else {
			backToken = authService.getToken(smSession, rsoSession);
			if (token.equals(backToken))
				return true;
			else
				return false;
		}*/

	}
}
