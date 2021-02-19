package com.ge.capital.dms.fr.sle.config;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.log4j.MDC;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DecodeSSO {
	public String getDecodedSSO(String encodedLoggedinUser) {
		String loggedDecodedValue = StringUtils.newStringUtf8(Base64.decodeBase64(encodedLoggedinUser));
		MDC.put("userId", loggedDecodedValue);
		return loggedDecodedValue;
	}

	public String encodeSSO(String userSSO) {
		String loggedEncodedValue = StringUtils.newStringUtf8(Base64.encodeBase64(userSSO.getBytes()));
		return loggedEncodedValue;
	}

}
