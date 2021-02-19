package com.ge.capital.dms.utility;

import java.io.IOException;
import java.io.StringReader;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.PKCSException;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.lang.JoseException;

//import com.ge.capital.dms.fr.sle.controllers.api.MultiFileUploadController;
import com.ge.capital.dms.utility.DmsUtilityService;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.security.PrivateKey;

public class AccessToken {

	private final Logger log = Logger.getLogger(this.getClass());
	DmsUtilityService dmsUtilityService = new DmsUtilityService();
	// public Config config = dmsUtilityService.readJsonFile();
	private Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.applicationConstantsResource);;

	public PrivateKey decryptPrivateKey() {
		PrivateKey key = null;
		Security.addProvider(new BouncyCastleProvider());
		PEMParser pemParser = new PEMParser(new StringReader(props.getProperty("privateKey")));
		Object keyPair;
		try {
			keyPair = pemParser.readObject();
			pemParser.close();
			char[] passphrase = props.getProperty("passphrase").toCharArray();
			JceOpenSSLPKCS8DecryptorProviderBuilder decryptBuilder = new JceOpenSSLPKCS8DecryptorProviderBuilder()
					.setProvider("BC");
			InputDecryptorProvider decryptProvider = decryptBuilder.build(passphrase);
			PrivateKeyInfo keyInfo = ((PKCS8EncryptedPrivateKeyInfo) keyPair).decryptPrivateKeyInfo(decryptProvider);
			key = (new JcaPEMKeyConverter()).getPrivateKey(keyInfo);

		} catch (OperatorCreationException | IOException | PKCSException e) {
//			System.out.println("AccessToken : decryptPrivateKey : Exception Occured is :  " + e.getMessage());
			log.error(e.getMessage(), e);
		}
		return key;
	}

	public String createJWTAssertion() {
		String assertion = null;
		JwtClaims claims = new JwtClaims();
		try {
			claims.setIssuer(props.getProperty("clientID"));
			claims.setAudience(dmsUtilityService.strAuthenticationTokenURL);
			claims.setSubject(props.getProperty("enterpriseID"));
			claims.setClaim("box_sub_type", "enterprise");
			claims.setGeneratedJwtId(64);
			NumericDate exp = NumericDate.now();
			exp.addSeconds(Long.parseLong(props.getProperty("expTime")));
			claims.setExpirationTime(exp);
			JsonWebSignature jws = new JsonWebSignature();
			jws.setPayload(claims.toJson());
			jws.setKey(decryptPrivateKey());
			jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA512);
			jws.setHeader("typ", "JWT");
			jws.setHeader("kid", props.getProperty("publicKeyID"));

			assertion = jws.getCompactSerialization();

		} catch (JoseException e) {
			log.error(e.getMessage(), e);
		}
		return assertion;
	}

	public String requestAccessToken() {

		String strToken = null;
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer"));
		params.add(new BasicNameValuePair("assertion", new AccessToken().createJWTAssertion()));
		params.add(new BasicNameValuePair("client_id", props.getProperty("clientID")));
		params.add(new BasicNameValuePair("client_secret", props.getProperty("clientSecret")));
		HttpPost request = new HttpPost(dmsUtilityService.strAuthenticationTokenURL);
//		HttpHost proxy = new HttpHost("PITC-Zscaler-Americas-Cincinnati3PR.proxy.corporate.ge.com", 80, "http");
//		RequestConfig requestConfig = RequestConfig.custom().setProxy(proxy).build();
		RequestConfig requestConfig = RequestConfig.custom().build(); // For Cloud, no need of proxy

		request.setConfig(requestConfig);
		CloseableHttpClient httpClient = HttpClientBuilder.create().disableCookieManagement().build();
		try {
			request.setEntity(new UrlEncodedFormEntity(params));
			CloseableHttpResponse httpResponse = httpClient.execute(request);
			HttpEntity entity = httpResponse.getEntity();
			String response = EntityUtils.toString(entity);
			// log.info("Response received : " + response);
			 System.out.println ("Response received : " + response);
			httpClient.close();
			Gson gson = new GsonBuilder().create();
			Token token = (Token) gson.fromJson(response, Token.class);
			strToken = token.access_token;
			//System.out.println(" DmsUtilityService : requestAccessToken : token generated is : " + strToken);

		} catch (IOException e) {
//			System.out.println("DmsUtilityService : requestAccessToken : Exception : " + e.getMessage());
			log.error(e.getMessage(), e);
		}
		return strToken;
	}

	class Token {
		String access_token;
	}

}
