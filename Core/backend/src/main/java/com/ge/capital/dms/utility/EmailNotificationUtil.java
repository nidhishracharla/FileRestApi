package com.ge.capital.dms.utility;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;

@Component
public class EmailNotificationUtil {
	private static final Logger logger = Logger.getLogger(EmailNotificationUtil.class);

	//private static final String senderEmail = "DMS_Prod_NoReply@ge.com";
	private static final String senderEmail = "DMS_UAT_NoReply@ge.com";
	//private static final String senderEmail = "DMS_DEV_NoReply@ge.com";
	
	@Autowired
	DmsUtilityService dmsUtilityService;

	public void sendEmailAlert(String UPLD_DIR, List<String> files, String metadata, String message, String toEmail,
			String operation) {
		try {
			sendEmail(UPLD_DIR, files, metadata, toEmail, message, operation);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			// emailWithoutAttachments(500, e.getMessage(), toEmail);
		} catch (MessagingException e) {
			logger.error(e.getMessage(), e);
			emailWithoutAttachments(500, e.getMessage(), toEmail);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			emailWithoutAttachments(500, e.getMessage(), toEmail);
		}
	}

	private void sendEmail(String UPLD_DIR, List<String> files, String metadata, String toEmail, String message,
			String operation) throws MessagingException, UnsupportedEncodingException {
		String toEmailArray[] = toEmail.split(",");
		Properties props = System.getProperties();
		props.put("mail.smtp.host", "mail.ad.ge.com");
		props.put("mail.protocol", "smtp");
		props.put("mail.smtps.auth", "false");
		props.put("mail.smtp.port", "25");
		props.put("mail.smtps.starttls.enable", "false");
		Session session = Session.getDefaultInstance(props);
		session.setDebug(false);

		MimeMessage msg = new MimeMessage(session);
		msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
		msg.addHeader("format", "flowed");
		msg.addHeader("Content-Transfer-Encoding", "8bit");
		String body = "";
		if (operation.equals("Upload")) {
			msg.setFrom(new InternetAddress("DMS_Upload_Failure_Notification@ge.com", "DMS_Upload"));
			msg.setReplyTo(InternetAddress.parse(senderEmail, false));
			msg.setSubject("Documents upload failure alert", "UTF-8");
			body = metadata + "\n Message : " + message;
		} else if (operation.equals("Email")) {
			msg.setFrom(new InternetAddress(senderEmail, "Requested_Documents"));
			msg.setReplyTo(InternetAddress.parse(senderEmail, false));
			msg.setSubject("Documents from DMS", "UTF-8");
			body = metadata + "\n Message : " + message;
		} else if (operation.equals("EmailPackage")) {
			msg.setFrom(new InternetAddress(senderEmail, "Doc Package from DMS"));
			msg.setReplyTo(InternetAddress.parse(senderEmail, false));
			msg.setSubject(metadata + " - Doc Package from DMS", "UTF-8");
			body = message;
		}
		msg.setSentDate(new Date());
		InternetAddress[] toAddresses = null;
		if (null != toEmailArray) {
			for (String toAddress : toEmailArray) {
				toAddresses = ArrayUtils.add(toAddresses, new InternetAddress(toAddress));
			}
			msg.addRecipients(Message.RecipientType.TO, toAddresses);
		}
		BodyPart messageBodyPart = new MimeBodyPart();
		logger.info("Body of the email : " + body);
		messageBodyPart.setText(body);
		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(messageBodyPart);
		for (String file : files) {
			FileDataSource source = new FileDataSource(UPLD_DIR + "\\" + file);
			messageBodyPart = new MimeBodyPart();
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(file);
			multipart.addBodyPart(messageBodyPart);
		}
		msg.setContent(multipart);
		Transport.send(msg);
	}

	public void emailWithoutAttachments(int responseCode, String exceptionMessage, String toEmail) {
		String toEmailArray[] = toEmail.split(",");
		Properties props = System.getProperties();
		props.put("mail.smtp.host", "mail.ad.ge.com");
		props.put("mail.protocol", "smtp");
		props.put("mail.smtps.auth", "false");
		props.put("mail.smtp.port", "25");
		props.put("mail.smtps.starttls.enable", "false");
		Session session = Session.getDefaultInstance(props);
		session.setDebug(false);

		MimeMessage msg = new MimeMessage(session);
		try {
			msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
			msg.addHeader("format", "flowed");
			msg.addHeader("Content-Transfer-Encoding", "8bit");
			msg.setFrom(new InternetAddress(senderEmail, "DMS_Documents"));
			msg.setReplyTo(InternetAddress.parse(senderEmail, false));
			msg.setSubject("Failed to Deliver files requested", "UTF-8");
			msg.setSentDate(new Date());
			InternetAddress[] toAddresses = null;
			if (null != toEmailArray) {
				for (String toAddress : toEmailArray) {
					toAddresses = ArrayUtils.add(toAddresses, new InternetAddress(toAddress));
				}
				msg.addRecipients(Message.RecipientType.TO, toAddresses);
			}

			msg.setSubject("Failed to deliver documents");
			msg.setText("Document Operation failed due to " + exceptionMessage + ", Response Code : " + responseCode);

			Transport.send(msg);
		} catch (Exception e) {
			emailWithoutAttachments(500, e.getMessage(), toEmail);
		}
	}

	public void NoMetaDataMailAlertforDocuSign(Map<String, String> uploadDataMap) {
		
		
		String MailContent= "<html><p>Following document is pulled from Docusign to DMS. But DMS System could not find a valid Sequence number or Opportunity Id to associate the Signed document. Please verify:</p>"
				+ " <table width='100%' border='1' align='center' style='border-collapse:collapse;display:inline-table'><tr align='center' style='background-color:darkseagreen;font-weight:100'>"
				+ "<td>Sequence Number:</td>"
				+ "<td>Opportunity ID</td>"
				+ "<td>Customer Number</td>"
				+ "<td>Docusign Envelope ID</td>"
				+ "<td>Name of the Document</td"
				+ "><td>DMS Environment</td>"
				+ "<td>DocuSign Environment</td>"
				+ "</tr>	"
				+ "<tr align='center'>"
				+ "<td>"+ uploadDataMap.get("lwContractSequenceNumber") + "</td>"
				+ "<td>" + uploadDataMap.get("sfdcOpportunityId")+ "</td>"
				+ "<td>" +  uploadDataMap.get("CustomerNumber") + "</td>"
				+ "<td>"+  uploadDataMap.get("EnvelopeID") + "</td>"
				+ "<td>" + uploadDataMap.get("FileName")+ "</td>"
				+ "<td>" + "UAT" + "</td>"
				+ "<td>" +  uploadDataMap.get("DocuSignEnv")+ "</td>"
						+ "</tr> "
						+ "</table><br/>"
						+ "<p><b>Note:</b>This is a System Generated email, Please contact HFSDocuSign.Support@ge.com for any queries </p>"
						+ "</html>";
		String toEmailArray[] = uploadDataMap.get("SignedUserEmail").split(",");
		Properties props = System.getProperties();
		props.put("mail.smtp.host", "mail.ad.ge.com");
		props.put("mail.protocol", "smtp");
		props.put("mail.smtps.auth", "false");
		props.put("mail.smtp.port", "25");
		props.put("mail.smtps.starttls.enable", "false");
		Session session = Session.getDefaultInstance(props);
		session.setDebug(false);

		MimeMessage msg = new MimeMessage(session);
		try {
			msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
			msg.addHeader("format", "flowed");
			msg.addHeader("Content-Transfer-Encoding", "8bit");
			msg.setFrom(new InternetAddress(senderEmail, "DMS_Documents"));
			msg.setReplyTo(InternetAddress.parse(senderEmail, false));
			msg.setSubject("Failed to Deliver files requested", "UTF-8");
			msg.setSentDate(new Date());
			InternetAddress[] toAddresses = null;
			if (null != toEmailArray) {
				for (String toAddress : toEmailArray) {
					toAddresses = ArrayUtils.add(toAddresses, new InternetAddress(toAddress));
				}
				msg.addRecipients(Message.RecipientType.TO, toAddresses);
			}

			if(uploadDataMap.get("DocuSignEnv").equalsIgnoreCase("Stage"))
			msg.setSubject("DocuSign DMS Integration "+uploadDataMap.get("DocuSignEnv")+" Warning");
			else
				msg.setSubject("DocuSign DMS Integration Warning");	
			
			msg.setContent(MailContent, "text/HTML"); 				

			Transport.send(msg);
		} catch (Exception e) {
			emailWithoutAttachments(500, e.getMessage(), uploadDataMap.get("SignedUserEmail"));
		}
	}

}
