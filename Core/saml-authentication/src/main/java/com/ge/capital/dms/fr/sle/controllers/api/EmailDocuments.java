package com.ge.capital.dms.fr.sle.controllers.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy.Type;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxFile;
import com.ge.capital.dms.fr.sle.config.DecodeSSO;
import com.ge.capital.dms.utility.DmsUtilityService;
import com.ge.capital.dms.utility.EmailNotificationUtil;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/secure")
public class EmailDocuments {

	private static final Logger log = Logger.getLogger(EmailDocuments.class);

	@Value("${download.path}")
	private String DIRECTORY;

	@Autowired
	DmsUtilityService dmsUtilityService;
	@Autowired
	DecodeSSO decodeSSO;
	@Autowired
	EmailNotificationUtil emailNotificationUtil;

	@Value("${to.email.address}")
	private String toEmailAddress;

	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
	@RequestMapping(value = "/emailDocs")
	@ResponseBody
	public String emailDocs(HttpServletRequest httprequest, @RequestBody String docIdArrayString) {
		String toEmail = "";
		decodeSSO.getDecodedSSO(httprequest.getHeader("loggedinuser"));
		try {
			String[] docId = docIdArrayString.split("@");
			String emailBody = "";
			String message = "Please contact hefdms.support@ge.com for details";
			String operation = "Email";
			List<String> finalFileNames = new ArrayList<String>();
			toEmail = docId[0] + "@ge.com";
			for (int i = 1; i < docId.length; i++) {
				String box_Id = docId[i].split(":")[0];
				String docName = docId[i].split(":")[1];
				try {
					String boxToken = dmsUtilityService.requestAccessToken();
					if (!boxToken.isEmpty()) {
						BoxAPIConnection api = new BoxAPIConnection(boxToken);
						/*
						 * java.net.Proxy proxy = new java.net.Proxy(Type.HTTP, new InetSocketAddress(
						 * "PITC-Zscaler-Americas-Cincinnati3PR.proxy.corporate.ge.com", 80));
						 * api.setProxy(proxy);
						 */
						BoxFile file = new BoxFile(api, box_Id);
						// BoxFile.Info info = file.getInfo();
						FileOutputStream stream = new FileOutputStream(DIRECTORY + "\\" + docName);
						file.download(stream);
						finalFileNames.add(docName);
						stream.close();
					}
				} catch (Exception e) {
					emailBody = emailBody + "Failed to download " + docName + ", due to exception :" + e.getMessage();
				}
			}
			emailBody = emailBody + "\n \n This is an auto generated Email, please donot reply to this";
			emailNotificationUtil.sendEmailAlert(DIRECTORY, finalFileNames, emailBody, message, toEmail, operation);
		} catch (BoxAPIException e) {
			log.error(e.getMessage(), e);
			toEmail = toEmail + "," + toEmailAddress;
			emailNotificationUtil.emailWithoutAttachments(e.getResponseCode(), e.getMessage(), toEmail);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			toEmail = toEmail + "," + toEmailAddress;
			emailNotificationUtil.emailWithoutAttachments(500, e.getMessage(), toEmail);
		}
		return "";
	}
}
