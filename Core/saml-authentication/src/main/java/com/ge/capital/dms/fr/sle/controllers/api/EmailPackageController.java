
package com.ge.capital.dms.fr.sle.controllers.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
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
import com.ge.capital.dms.service.SelectService;
import com.ge.capital.dms.utility.DmsUtilityService;
import com.ge.capital.dms.utility.EmailNotificationUtil;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;

/**
 * @author GJ00557822
 */
@CrossOrigin(origins = "*", exposedHeaders = "fileName")
@RestController
@RequestMapping("/secure")
public class EmailPackageController {
	private static final Logger log = Logger.getLogger(EmailPackageController.class);

	@Value("${download.path}")
	private String DIRECTORY;

	@Value("${box.upload.url}")
	private String boxUploadURL;

	@Value("${to.email.address}")
	private String toEmailAddress;

	@Value("${docusign.support.email}")
	private String docusignSupportEmail;

	@Autowired
	DmsUtilityService dmsUtilityService;

	@Autowired
	SelectService selectService;

	@Autowired
	EmailNotificationUtil emailNotificationUtil;

	@Autowired
	DocumentController documentController;

	@Autowired
	DecodeSSO decodeSSO;

	@SuppressWarnings("unchecked")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
	@RequestMapping(value = "/emailPackage")
	@ResponseBody
	public void emailPackage(HttpServletRequest httprequest, @RequestBody Object request) throws JSONException {
		List<HashMap<String, String>> resultList = new ArrayList<HashMap<String, String>>();
		resultList = (List<HashMap<String, String>>) request;

		String toEmail = decodeSSO.getDecodedSSO(httprequest.getHeader("loggedinuser")) + "@ge.com";
		String packType = "";
		Boolean isCV19Doc = false;
		HashMap<String, String> finalPackMetadata = new HashMap<String, String>();
		try {
			for (HashMap<String, String> temp : resultList) {
				log.info(temp.get("title") + "::" + temp.get("documnetSubType"));
				if (temp.get("documnetSubType").equalsIgnoreCase("CV19 Restructure Doc")) {
					isCV19Doc = true;
				}
			}
			for (HashMap<String, String> temp : resultList) {
				String boxToken = dmsUtilityService.requestAccessToken();
				if (!boxToken.isEmpty()) {
					BoxAPIConnection api = new BoxAPIConnection(boxToken);
					
//					  java.net.Proxy proxy = new java.net.Proxy(Type.HTTP, new InetSocketAddress( "PITC-Zscaler-Americas-Cincinnati3PR.proxy.corporate.ge.com", 80));
//					  api.setProxy(proxy);
					 
					if ((temp.get("documnetSubType").equalsIgnoreCase("Welcome Pkg Coversheet"))
							|| (temp.get("documnetSubType").equalsIgnoreCase("Finance Commencement Letter"))) {
						finalPackMetadata = temp;
						if (isCV19Doc) {
							packType = "PKG - CV19 Welcome Package";
						} else {
							packType = "PKG - Welcome Package";
						}
					}
					if (temp.get("documnetSubType").equalsIgnoreCase("TIAA Final Doc Coversheet")) {
						finalPackMetadata = temp;
						if (isCV19Doc) {
							packType = "PKG - CV19 TIAA Final Package";
						} else {
							packType = "PKG - TIAA Final Package";
						}
					}
					BoxFile file = new BoxFile(api, temp.get("docId"));
					FileOutputStream stream;
					stream = new FileOutputStream(DIRECTORY + "\\" + temp.get("title"));
					file.download(stream);
					stream.close();
				}
			}
		} catch (BoxAPIException e) {
			log.error(e.getMessage(), e);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		String finalPackName = finalPackMetadata.get("lwContractSequenceNumber") + "_" + packType + ".pdf";
		String doIdExists = selectService.isFileExists(finalPackName);
		if (doIdExists != null && !(doIdExists.equals(""))) {
			JSONObject json = new JSONObject();
			json.put("docType", "dealDoc");
			json.put("docId", doIdExists);
			String[] finalArray = new String[1];
			finalArray[0] = json.toString();
			log.info("Delete Request sent to delete ::" + doIdExists);
			documentController.deleteDoc(httprequest, finalArray);
		}
		log.info("Package ::" + packType);
		List<File> pdfFiles = getPDFFiles(DIRECTORY, resultList, packType);
		Document finalPDF = mergePDF(pdfFiles, DIRECTORY, finalPackName);
		emailDocument(finalPDF, finalPackName, toEmail);
		finalPackMetadata.put("finalPackage", "0");
		finalPackMetadata.put("welcomePackage", "0");
		String modifier = decodeSSO.getDecodedSSO(httprequest.getHeader("loggedinuser"));
		log.info("Modifier" + modifier);
		finalPackMetadata.put("modifier", modifier);
		uploadDocument(httprequest, finalPackName, finalPackMetadata, packType);
	}

	public String uploadDocument(HttpServletRequest httprequest, String fileName,
			HashMap<String, String> finalPackMetadata, String packType) {
		try {
			String finalPackMetadataStr = prepareMetadata(fileName, finalPackMetadata, packType);
			PostMethod mPost = null;
			String mime = "application/pdf";
			if (packType.equals("Booking"))
				mime = "application/vnd.ms-excel";
			Part[] parts = { new StringPart("docType", "dealDoc"),
					new StringPart("uploadMetadata", finalPackMetadataStr,"UTF-8"),
					new FilePart("files", fileName, new File(DIRECTORY + "\\" + fileName), mime, null), };
			MultipartRequestEntity multipartRequestEntity = new MultipartRequestEntity(parts, new HttpMethodParams());
			HttpClient client = new HttpClient();
			mPost = new PostMethod(boxUploadURL);
			mPost.setRequestEntity(multipartRequestEntity);
			mPost.setRequestHeader("Authorization", httprequest.getHeader("Authorization"));
			if (finalPackMetadata.get("creator") != null) {
				mPost.setRequestHeader("loggedinuser", decodeSSO.encodeSSO(finalPackMetadata.get("creator")));
			} else {
				mPost.setRequestHeader("loggedinuser", httprequest.getHeader("loggedinuser"));
			}

			mPost.setRequestHeader("x-auth-token", "DMS_BATCH_UTILITY");
			client.executeMethod(mPost);
			log.info("Response recevied from Upload Service " + mPost.getResponseBodyAsString());

			return mPost.getResponseBodyAsString();
		} catch (HttpException e) {
			log.error(e.getMessage(), e);
			return e.getMessage();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return e.getMessage();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return e.getMessage();
		}
	}

	public String prepareMetadata(String fileName, HashMap<String, String> finalPackMetadata, String packType) {
		Map<String, String> requestParameters = new HashMap<String, String>();
		requestParameters.put("fileName", fileName);
		if (finalPackMetadata.get("sourceSystem") != null) {
			requestParameters.put("sourceSystem", finalPackMetadata.get("sourceSystem"));
		} else
			requestParameters.put("sourceSystem", "DMS");
		if (packType != null)
			requestParameters.put("docSubType", packType);

		if ((null != finalPackMetadata.get("finalPackage")) && (finalPackMetadata.get("finalPackage").equals("1")
				|| Boolean.valueOf(finalPackMetadata.get("finalPackage"))))
			requestParameters.put("finalPackage", "1");
		else
			requestParameters.put("finalPackage", "0");
		if ((null != finalPackMetadata.get("welcomePackage")) && (finalPackMetadata.get("welcomePackage").equals("1")
				|| Boolean.valueOf(finalPackMetadata.get("welcomePackage")) == true))
			requestParameters.put("welcomePackage", "1");
		else
			requestParameters.put("welcomePackage", "0");
		if (null != finalPackMetadata.get("partyNumber"))
			requestParameters.put("partyNumber", finalPackMetadata.get("partyNumber"));
		if (null != finalPackMetadata.get("partyName"))
			requestParameters.put("partyName", finalPackMetadata.get("partyName").trim());
		if (null != finalPackMetadata.get("contractDealType"))
			requestParameters.put("contractDealtype", finalPackMetadata.get("contractDealType"));
		if (null != finalPackMetadata.get("sfdcOpportunityId"))
			requestParameters.put("sfdcopportunityId", finalPackMetadata.get("sfdcOpportunityId"));
		if (null != finalPackMetadata.get("legalEntityType"))
			requestParameters.put("legalEntityType", finalPackMetadata.get("legalEntityType"));
		if (null != finalPackMetadata.get("lineOfCreditNumber"))
			requestParameters.put("lineofcreditNumber", finalPackMetadata.get("lineOfCreditNumber"));
		if (null != finalPackMetadata.get("lwContractSequenceNumber"))
			requestParameters.put("lwSeqNumber", finalPackMetadata.get("lwContractSequenceNumber"));
		/*
		 * if (null != finalPackMetadata.get("finalPackage") &&
		 * String.valueOf(finalPackMetadata.get("finalPackage")).equals("true"))
		 * requestParameters.put("finalPackage", "1");
		 */
		if (null != finalPackMetadata.get("syndicationPackage")
				&& String.valueOf(finalPackMetadata.get("syndicationPackage")).equals("true"))
			requestParameters.put("syndicationPackage", "1");
		/*
		 * if (null != finalPackMetadata.get("welcomePackage") &&
		 * String.valueOf(finalPackMetadata.get("welcomePackage")).equals("true"))
		 * requestParameters.put("welcomePackage", "1");
		 */
		if (null != finalPackMetadata.get("physicalStorageStatus")
				&& String.valueOf(finalPackMetadata.get("physicalStorageStatus")).equals("true"))
			requestParameters.put("physicalStorageStatus", "1");
		if (null != finalPackMetadata.get("sfdcAccountId"))
			requestParameters.put("sfdcAccountId", finalPackMetadata.get("sfdcAccountId"));
		if (null != finalPackMetadata.get("legalEntityName"))
			requestParameters.put("legalEntityName", finalPackMetadata.get("legalEntityName"));
		if (null != finalPackMetadata.get("lineofBusinessCode"))
			requestParameters.put("lineofBusinessCode", finalPackMetadata.get("lineofBusinessCode"));

		if (null != finalPackMetadata.get("SignedUserEmail"))
			requestParameters.put("SignedUserEmail",
					finalPackMetadata.get("SignedUserEmail") + "," + docusignSupportEmail);
		else
			requestParameters.put("SignedUserEmail", docusignSupportEmail);

		if (null != finalPackMetadata.get("CustomerNumber"))
			requestParameters.put("CustomerNumber", finalPackMetadata.get("CustomerNumber"));
		if (null != finalPackMetadata.get("DocuSignEnv"))
			requestParameters.put("DocuSignEnv", finalPackMetadata.get("DocuSignEnv"));
		if (null != finalPackMetadata.get("EnvelopeID"))
			requestParameters.put("EnvelopeID", finalPackMetadata.get("EnvelopeID"));

		JSONObject metaJson = new JSONObject(requestParameters);
		this.log.info("Json object for reqduest parameter is : " + requestParameters.toString());
		return metaJson.toString();
	}

	public void emailDocument(Document finalPDF, String finalPackName, String toEmail) {
		List<String> files = new ArrayList<String>();
		files.add(finalPackName);
		emailNotificationUtil.sendEmailAlert(DIRECTORY, files, finalPackName,
				"This is an automatically generated Email, please do not reply", toEmail, "EmailPackage");
		// \n\n contact hefdms.support@ge.com for details
	}

	public Document mergePDF(List<File> pdfFiles, String dIRECTORY, String finalPackName) {

		Document document = new Document();
		try {
			PdfCopy copy = new PdfCopy(document, new FileOutputStream(new File(dIRECTORY + "\\" + finalPackName)));
			document.open();
			PdfReader reader;
			for (int i = 0; i < pdfFiles.size(); i++) {
				reader = new PdfReader(new FileInputStream(dIRECTORY + "\\" + pdfFiles.get(i).getName()));
				copy.addDocument(reader);
				copy.freeReader(reader);
				reader.close();
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		document.close();
		return document;
	}

	public List<File> getPDFFiles(String docsPath, List<HashMap<String, String>> resultList, String packType) {
		int size = resultList.size();
		List<File> pdfFiles = new ArrayList<File>(size);
		try {
			if (packType.equals("PKG - Welcome Package") || packType.equals("PKG - CV19 Welcome Package")) {
				for (HashMap<String, String> temp : resultList) {
					if (temp.get("documnetSubType").equalsIgnoreCase("Finance Commencement Letter")) {
						pdfFiles.add(new File(docsPath + "\\" + temp.get("title")));
					}
				}
				for (HashMap<String, String> temp : resultList) {
					if (temp.get("documnetSubType").equalsIgnoreCase("Contract Executed Package")) {
						pdfFiles.add(new File(docsPath + "\\" + temp.get("title")));
					}
				}
				for (HashMap<String, String> temp : resultList) {
					if (temp.get("documnetSubType").equalsIgnoreCase("Contract Executed Amendment")) {
						pdfFiles.add(new File(docsPath + "\\" + temp.get("title")));
					}
				}
				for (HashMap<String, String> temp : resultList) {
					if (temp.get("documnetSubType").equalsIgnoreCase("Amortization Table")
							|| temp.get("documnetSubType").equalsIgnoreCase("Stip-Loss Table")) {
						pdfFiles.add(new File(docsPath + "\\" + temp.get("title")));
					}
				}
				for (HashMap<String, String> temp : resultList) {
					if (!pdfFiles.contains(new File(docsPath + "\\" + temp.get("title")))) {
						pdfFiles.add(new File(docsPath + "\\" + temp.get("title")));
					}
				}
			} else if (packType.equals("PKG - TIAA Final Package")
					|| packType.equals("PKG - CV19 TIAA Final Package")) {
				for (HashMap<String, String> temp : resultList) {
					if (temp.get("documnetSubType").equalsIgnoreCase("TIAA Final Doc Coversheet")) {
						pdfFiles.add(new File(docsPath + "\\" + temp.get("title")));
					}
				}
				for (HashMap<String, String> temp : resultList) {
					if (temp.get("documnetSubType").equalsIgnoreCase("Partner - A&A Agreement")) {
						pdfFiles.add(new File(docsPath + "\\" + temp.get("title")));
					}
				}
				for (HashMap<String, String> temp : resultList) {
					if (temp.get("documnetSubType").equalsIgnoreCase("LW Booking Report")) {
						pdfFiles.add(new File(docsPath + "\\" + temp.get("title")));
					}
				}
				for (HashMap<String, String> temp : resultList) {
					if (temp.get("documnetSubType").equalsIgnoreCase("Contract Executed Package")) {
						pdfFiles.add(new File(docsPath + "\\" + temp.get("title")));
					}
				}
				for (HashMap<String, String> temp : resultList) {
					if (temp.get("documnetSubType").equalsIgnoreCase("Contract Executed Amendment")) {
						pdfFiles.add(new File(docsPath + "\\" + temp.get("title")));
					}
				}
				for (HashMap<String, String> temp : resultList) {
					if (temp.get("documnetSubType").equalsIgnoreCase("Pricing and SuperTrump")) {
						pdfFiles.add(new File(docsPath + "\\" + temp.get("title")));
					}
				}
				for (HashMap<String, String> temp : resultList) {
					if (!pdfFiles.contains(new File(docsPath + "\\" + temp.get("title")))) {
						pdfFiles.add(new File(docsPath + "\\" + temp.get("title")));
					}
				}
			}
		} catch (Exception e) {
			log.info(e.getMessage(), e);
		}
		return pdfFiles;
	}

	/*
	 * public List<File> getPDFFilesOld(String docsPath, List<HashMap<String,
	 * String>> resultList, String packType) { int size = resultList.size();
	 * List<File> pdfFiles = new ArrayList<File>(size); // log.info("Size" +
	 * pdfFiles.size()); try { for (int i = 0; i < size; i++) { HashMap<String,
	 * String> temp = resultList.get(i); File fileEntry = new File(docsPath +
	 * "\\" + temp.get("title")); log.info("File Name ::" + temp.get("title")); if
	 * (packType.equals("PKG - Welcome Package") ||
	 * packType.equals("PKG - CV19 Welcome Package")) { if
	 * (temp.get("title").endsWith(".pdf")) { if (i >= 0 &&
	 * (temp.get("documnetSubType").equalsIgnoreCase("Welcome Pkg Coversheet") ||
	 * temp.get("documnetSubType").equalsIgnoreCase("Finance Commencement Letter")))
	 * { pdfFiles.add(0, fileEntry); } else if (i >= 1 &&
	 * temp.get("documnetSubType").equalsIgnoreCase("Contract Executed Package")) {
	 * pdfFiles.add(1, fileEntry); } else if (i >= 2 &&
	 * temp.get("documnetSubType").equalsIgnoreCase("Contract Executed Amendment"))
	 * { pdfFiles.add(2, fileEntry); } else if (i >= 3 &&
	 * (temp.get("documnetSubType").equalsIgnoreCase("Amortization Table") ||
	 * temp.get("documnetSubType").equalsIgnoreCase("Stip-Loss Table"))) {
	 * pdfFiles.add(3, fileEntry); } else { pdfFiles.add(fileEntry); } } } else if
	 * (packType.equals("PKG - TIAA Final Package") ||
	 * packType.equals("PKG - CV19 TIAA Final Package")) { if
	 * (temp.get("title").endsWith(".pdf")) { if (i >= 0 &&
	 * temp.get("documnetSubType").equalsIgnoreCase("TIAA Final Doc Coversheet")) {
	 * pdfFiles.add(0, fileEntry); } else if (i >= 1 &&
	 * temp.get("documnetSubType").equalsIgnoreCase("Partner - A&A Agreement")) {
	 * pdfFiles.add(1, fileEntry); } else if (i >= 2 &&
	 * temp.get("documnetSubType").equalsIgnoreCase("LW Booking Report") && size >=
	 * 2) { pdfFiles.add(2, fileEntry); } else if (i >= 3 &&
	 * temp.get("documnetSubType").equalsIgnoreCase("Contract Executed Package")) {
	 * pdfFiles.add(3, fileEntry); } else if (i >= 4 &&
	 * temp.get("documnetSubType").equalsIgnoreCase("Contract Executed Amendment"))
	 * { pdfFiles.add(4, fileEntry); } else if (i >= 5 &&
	 * temp.get("documnetSubType").equalsIgnoreCase("Pricing and SuperTrump")) {
	 * pdfFiles.add(5, fileEntry); } else { pdfFiles.add(fileEntry); } } } } } catch
	 * (Exception e) { log.info(e.getMessage(), e); } return pdfFiles; }
	 */

}
