package com.ge.capital.dms.utility;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.apache.tika.Tika;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Component
public class DmsUtilityService {

	private Properties props;
	public String strAuthenticationTokenURL;
	private static final Logger logger = Logger.getLogger(DmsUtilityService.class);
	public DmsUtilityService() {
		props = loadPropertiesFile(DmsUtilityConstants.applicationConstantsResource);
		strAuthenticationTokenURL = props.getProperty("AccessTokenUrl");
	}
	
	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
	@SuppressFBWarnings(value = { "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
			"DLS_DEAD_LOCAL_STORE" }, justification = "let me just make the build pass")
	public AkanaToken generateAkanaAccessToken() {

		String akanaUrl = null;
		RestTemplate restTemplate = new RestTemplate();
		AkanaToken akanaToken;

		akanaUrl = props.getProperty("akanaUri");

		akanaToken = restTemplate.postForObject(akanaUrl, null, AkanaToken.class);

		if (akanaToken.getAccess_token() == null) {
			try {
				throw new Exception("Something went wrong in generating the access token");
			} catch (Exception e) {

				logger.error(e.getMessage(),e);
			}
		}

		return akanaToken;
	}

	public JSONObject generateBoxAccessToken(String akanaAccessToken) {

		String boxAccessToken = null;
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + akanaAccessToken);
		String BOX_TOKEN_REQUEST = props.getProperty("boxTokenUri");
		HttpEntity<String> entityReq = new HttpEntity<String>("parameters", headers);
		ResponseEntity<String> respEntity = restTemplate.exchange(BOX_TOKEN_REQUEST, HttpMethod.GET, entityReq,
				String.class);
		boxAccessToken = respEntity.getBody();
		JSONObject jsonObject = (JSONObject) JSONValue.parse(boxAccessToken);
		return jsonObject;

	}

	public Properties loadPropertiesFile(String resourceName) {

		Properties properties = new Properties();
		try {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();

			InputStream resourceStream = loader.getResourceAsStream(resourceName);
			properties.load(resourceStream);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return properties;
	}

	public static MediaType getMediaTypeForFileName(ServletContext servletContext, File _file) {
		// application/pdf
		// application/xml
		// image/gif, ...
		// String mimeType = servletContext.getMimeType(fileName);
		MediaType mediaType = null;
		try {
			Tika tika = new Tika();
			String mimeType = tika.detect(_file);
			mediaType = MediaType.parseMediaType(mimeType);
		} catch (Exception e) {
			
			logger.error(e.getMessage(),e);
		}
		return mediaType;
	}

	public String requestAccessToken() {
		return new AccessToken().requestAccessToken();
	}

}
