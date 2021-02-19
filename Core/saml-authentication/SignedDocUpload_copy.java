package com.ge.capital.dms.fr.sle.controllers.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ge.capital.dms.dao.DocumentServiceDAO;
import com.ge.capital.dms.dao.UpdateDAO;
import com.ge.capital.dms.entity.DocSubtype;
import com.ge.capital.dms.entity.DocSubtypeKeyword;
import com.ge.capital.dms.entity.DocuSignMessageStatus;
import com.ge.capital.dms.fr.sle.config.DecodeSSO;
import com.ge.capital.dms.model.FileMetadata;
import com.ge.capital.dms.repository.DocuSignStatusRepository;
import com.ge.capital.dms.repository.UploadContractRepository;
import com.ge.capital.dms.repository.UploadLOCRepository;
import com.ge.capital.dms.service.SelectService;
import com.ge.capital.dms.service.UpdateService;
import com.ge.capital.dms.utility.CommonConstants;
import com.ge.capital.dms.utility.DmsUtilityConstants;
import com.ge.capital.dms.utility.DmsUtilityService;
import com.ge.capital.dms.utility.EmailNotificationUtil;
import com.google.common.collect.Iterators;
import com.google.gson.Gson;

/**
 * @author GJ00557822
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/secure")
public class SignedDocUpload {
	private static final Logger logger = Logger.getLogger(SignedDocUpload.class);

	@Autowired
	SelectService selectService;

	@Autowired
	DmsUtilityService dmsUtilityService;

	@Autowired
	MultiFileUploadController multiFileUploadController;

	@Autowired
	UploadLOCRepository uploadLOCRepository;

	@Autowired
	DocuSignStatusRepository docuSignStatusRepository;

	@Value("${download.path}")
	private String DIRECTORY;

	@Value("#{${DocuSign.DocSubtype.Keyword}}")
	private Map<String, String> strDocuSignKeywords;

	@Value("${docusign.support.email}")
	private String docusignSupportEmail;

	@Value("${upload.docusign.noMetaData.documents}")
	private String strDocuSignNoMetadataDocuments;

	@Autowired
	EmailPackageController emailPackageController;

	@Autowired
	DecodeSSO decodeSSO;

	@Autowired
	UpdateService updateService;

	@Autowired
	EmailNotificationUtil emailNotificationUtil;

	@Autowired
	UploadContractRepository uploadContractRepository;

	@Autowired
	SelectService opportunityRepo;

	@Autowired
	DocumentServiceDAO documentServiceDAO;

	@RequestMapping(value = "/docuSignUpload", method = RequestMethod.POST)
	public Object uploadSigned(HttpServletRequest request, @FormDataParam("files") MultipartFile[] files,
			@FormDataParam("docType") String docType1, @FormDataParam("fileMetadata") FileMetadata fileMetadata) {
		String loggedinUser = request.getHeader("loggedinuser");
		Map<String, String> DMSMetadata = new HashMap<String, String>();
		Map<String, String> DocuSignMetadata = new HashMap<String, String>();
		Map<String, String> commonMetadataMap = new HashMap();
		Map<String, String> dealDocMetadataMap = new HashMap();

		// HashMap<String, String> Metadata = new HashMap<String, String>();
		String docType = "dealDoc";
		String respWelcome = "";
		String strSequenceNum = "";
		String strOpportunityID = "";
		BoxAPIConnection api = null;
		// decodeSSO.getDecodedSSO(request.getHeader("loggedinuser"));
		Properties docIdprops = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docIdMappingResource);
		String boxToken = dmsUtilityService.requestAccessToken();
		Iterable<DocSubtypeKeyword> keywords = selectService.getAllKeywords();

		if (boxToken == null) {
			return "Box Token is Empty ";
		}
		api = new BoxAPIConnection(boxToken);
		// java.net.Proxy proxy = new java.net.Proxy(Type.HTTP, new
		// InetSocketAddress("PITC-Zscaler-Americas-Cincinnati3PR.proxy.corporate.ge.com",
		// 80));
		// api.setProxy(proxy);
		DocuSignMessageStatus docusignrecord = new DocuSignMessageStatus();
		logger.info("Metadata received from request: " + fileMetadata.getUploadMetadata());
		String[] updateMetadataStrArray = fileMetadata.getUploadMetadata().split("},");
		String[] updateMetadataStrArray1 = updateMetadataStrArray;
		for (int i = 0; i < updateMetadataStrArray.length - 1; i++) {
			updateMetadataStrArray[i] = updateMetadataStrArray[i] + "}";
		}
		JSONParser parser = new JSONParser();
		@SuppressWarnings("unused")
		String entityTypeValue = "";

		HashSet<String> NotAvailableSet = new HashSet();
		NotAvailableSet.add("NA");
		NotAvailableSet.add("N/A");
		NotAvailableSet.add("na");
		NotAvailableSet.add("n/a");
		NotAvailableSet.add("notavailable");
		NotAvailableSet.add("not available");
		NotAvailableSet.add("Not Available");
		NotAvailableSet.add("NOT AVAILABLE");
		String creator = "";
		List<DocuSignMessageStatus> envelopes = null;

		for (String keyValue : updateMetadataStrArray) {

			JSONObject json = null;
			try {
				json = (JSONObject) parser.parse(keyValue);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (Object key : json.keySet()) {

				String metaKey = (String) key;
				String metaValue = "";
				// log.info(metaKey + " " + metaValue);
				if (json.get(metaKey).getClass() == (java.lang.Long.class)) {
					metaValue = (String) json.get(metaKey).toString();
				} else {
					metaValue = (String) json.get(metaKey);
					// log.info(metaValue);
				}
				logger.info(metaKey + " " + metaValue);
				if (metaKey.equals("legalEntityType")) {
					entityTypeValue = metaValue;
				}

				DocuSignMetadata.put(metaKey, metaValue);

			}
		}

		this.logger.info("DocuSignMetadata: " + DocuSignMetadata);

		try {

			String email = DocuSignMetadata.get("SignedUserEmail");
			creator = selectService.getUserId(email);
			strSequenceNum = DocuSignMetadata.get("lwContractSequenceNumber").trim();
			strOpportunityID = DocuSignMetadata.get("OpportunityID").trim();

			
			
			if (!NotAvailableSet.contains(strSequenceNum) && NotAvailableSet.contains(strOpportunityID)) {
				DMSMetadata = selectService.getDocMetadata(strSequenceNum);
				this.logger.info("block 1 : " + strSequenceNum + " " + strOpportunityID);
			} else if (!NotAvailableSet.contains(strOpportunityID) && NotAvailableSet.contains(strSequenceNum)) {
				DMSMetadata = selectService.getDocMetadataforOpportunityID(strOpportunityID);
				this.logger.info("block 2 : " + strSequenceNum + " " + strOpportunityID);
			} else if (!NotAvailableSet.contains(strOpportunityID) && !NotAvailableSet.contains(strSequenceNum)) {
				DMSMetadata = selectService.getDocMetadata(strSequenceNum, strOpportunityID);
				this.logger.info("block 3 : " + strSequenceNum + " " + strOpportunityID);
			}
			String strEnvelopeId = DocuSignMetadata.get("EnvelopeID") != null ? DocuSignMetadata.get("EnvelopeID") : "";
			String strFilename = DocuSignMetadata.get("Filename") != null ? DocuSignMetadata.get("Filename") : "";

			if (!NotAvailableSet.contains(strSequenceNum))
				DMSMetadata.put("lwContractSequenceNumber", strSequenceNum);
			if (!NotAvailableSet.contains(strOpportunityID))
				DMSMetadata.put("sfdcOpportunityId", strOpportunityID);		
			
			this.logger.info("DMSMetadata: " + DMSMetadata);

//			if (null == DMSMetadata.get("legalEntityType") || DMSMetadata.size() == 0
//					|| DMSMetadata.get("legalEntityType").equalsIgnoreCase("NoMetadata")) {
//				DMSMetadata.put("legalEntityType", "NoMetadata");
//				DMSMetadata.put("lwContractSequenceNumber", strSequenceNum);
//				if (!NotAvailableSet.contains(strOpportunityID))
//					DMSMetadata.put("sfdcOpportunityId", strOpportunityID);
//			}//commented for proper legal entity
			
			if (null == DMSMetadata.get("legalEntityType") || DMSMetadata.size() == 0
					|| DMSMetadata.get("legalEntityType").equalsIgnoreCase("NoMetadata")) {
				//DMSMetadata.put("NoMetadataFlag", "true");
				if (!strSequenceNum.isEmpty() && !NotAvailableSet.contains(strSequenceNum)) {
					dealDocMetadataMap.put("legalEntityType", "Contract");
					DMSMetadata.put("legalEntityType", "Contract");
				} else if (!strOpportunityID.isEmpty() && !NotAvailableSet.contains(strOpportunityID)) {
					dealDocMetadataMap.put("legalEntityType", "Opportunity");
					DMSMetadata.put("legalEntityType", "Opportunity");
				} else {
					dealDocMetadataMap.put("legalEntityType", "NoMetadata");
					DMSMetadata.put("legalEntityType", "NoMetadata");
				}
				
			}
			this.logger.info(
					"legalEntityType for the file name : " + strFilename + " is " + DMSMetadata.get("legalEntityType"));
			DMSMetadata.put("docType", "dealDoc");
			DMSMetadata.put("creator", creator);
			DMSMetadata.put("Filename", strFilename);

			List<DocSubtypeKeyword> matchfound = StreamSupport.stream(keywords.spliterator(), false)
					.filter(keyword -> strFilename.contains(keyword.getKeyword())).collect(Collectors.toList());
			String strDocSubTypeforCopy = "";
			if (matchfound != null && matchfound.size() == 1) {
				for (DocSubtypeKeyword docsubtypemetadata : matchfound) {
					DMSMetadata.put("docSubType", docsubtypemetadata.getDoc_subtype());// docSubType
					DMSMetadata.put("welcomePackage",
							docsubtypemetadata.getInclude_welcomepkg() != null
									? docsubtypemetadata.getInclude_welcomepkg()
									: "");
					DMSMetadata.put("finalPackage",
							docsubtypemetadata.getInclude_tiaapkg() != null ? docsubtypemetadata.getInclude_tiaapkg()
									: "");
					// if (!DMSMetadata.get("legalEntityType").equalsIgnoreCase("NoMetadata")) {
					// DMSMetadata.put("legalEntityType",
					// docsubtypemetadata.getEntitytype() != null ?
					// docsubtypemetadata.getEntitytype() : "");
					// }
				}
			} else if (!DMSMetadata.get("legalEntityType").equalsIgnoreCase("NoMetadata")) {
				DMSMetadata.put("docSubType", "Executed_Docusign");
			}

			dealDocMetadataMap.putAll(DMSMetadata);

			docusignrecord = selectService.getFileforEnvelopes(strEnvelopeId, strFilename);
			String tableName = docIdprops.getProperty(docType + ".multi.upload.table");
			DMSMetadata.put("tableName", tableName);

			if (docusignrecord != null) {
				this.logger.info("record from docusign table " + docusignrecord.toString());
			}
			if (docusignrecord != null && docusignrecord.getDocusignFilename().equalsIgnoreCase(strFilename)) {
				this.logger.info("file: " + strFilename + " exists in the docusign table ");
				DMSMetadata.put("docId", docusignrecord.getDocId());

				if (docusignrecord.getLegalentitytype().equalsIgnoreCase("NoMetadata")	&& !DMSMetadata.get("legalEntityType").equalsIgnoreCase("NoMetadata")) {
					this.logger.info("file exists in the docusign table with the entity type"+ docusignrecord.getLegalentitytype());

					dealDocMetadataMap = prepareMetadata(dealDocMetadataMap);
					dealDocMetadataMap.put("legalEntityType", DMSMetadata.get("legalEntityType"));
					//added by gayatri - start
					if (matchfound != null && matchfound.size() == 1) {
						for (DocSubtypeKeyword docsubtypemetadata : matchfound) {
							if (!DMSMetadata.get("legalEntityType").equalsIgnoreCase("NoMetadata")) {
								dealDocMetadataMap.put("legalEntityType",docsubtypemetadata.getEntitytype() != null ? docsubtypemetadata.getEntitytype(): "");
							}
						}
					} else if (!DMSMetadata.get("legalEntityType").equalsIgnoreCase("NoMetadata")) {
						dealDocMetadataMap.put("docSubType", "Executed_Docusign");
					}
					//added by gayatri - End			
					
					String boxuploadresponse = uploadingToBox(api, files, DocuSignMetadata, DMSMetadata, keywords);
					if (boxuploadresponse.contains("already been uploaded")) {
						String docId = selectService.isFileExists(strFilename);
						updateBoxFileVersion(api, DMSMetadata);
					}
					this.logger.info("dealDocMetadataMap in moveingBoxFile: " + dealDocMetadataMap);
					updateService.updateDocumentTypeMetadata(DMSMetadata.get("docType"), DMSMetadata.get("tableName"),
							dealDocMetadataMap);
					updateService.updateDocuSignData(docusignrecord.getDocId(), DMSMetadata.get("legalEntityType"));
					respWelcome = " file/(s) are uploaded to box successfully ";
				} else {
					this.logger.info("file does not exists in the docusign table with the entity type :"
							+ DMSMetadata.get("legalEntityType"));
					String boxuploadresponse = uploadingToBox(api, files, DocuSignMetadata, DMSMetadata, keywords);
					// String tableName = docIdprops.getProperty(docType + ".multi.upload.table");
					// update doc version if its feasible
					if (boxuploadresponse.contains("already been uploaded")) {
						String docId = selectService.isFileExists(strFilename);
						if (docId != null && !docId.isEmpty()) {
							updateBoxFileVersion(api, DMSMetadata);
						} else {
							this.logger.fatal(
									"DocId for the file: " + strFilename + " does not exists in the CommonDoc table ");
							respWelcome = "DocId for the file: " + strFilename
									+ " does not exists in the CommonDoc table";
						}
					}
					updateService.updateDocuSignData(docusignrecord.getDocId(), DMSMetadata.get("legalEntityType"));
					respWelcome = " file/(s) are uploaded to box successfully ";

				}
			} else {
				this.logger.info("file: " + strFilename + " does not exists in the docusign table ");
				String boxuploadresponse = uploadingToBox(api, files, DocuSignMetadata, DMSMetadata, keywords);
				this.logger.info("response received :" + boxuploadresponse);
				if (boxuploadresponse.contains("file/(s) are uploaded to box successfully")) {
					String boxid = boxuploadresponse.substring(boxuploadresponse.indexOf("successfully box") + 16,
							boxuploadresponse.length() - 1);
					InsertintoDocuSignTable(DocuSignMetadata, boxid, DMSMetadata.get("legalEntityType"));
					respWelcome = " file/(s) are uploaded to box successfully ";
					;
				} else {

					if (boxuploadresponse.contains("already been uploaded")) {
						updateBoxFileVersion(api, DMSMetadata);
					}
					respWelcome = " file/(s) are uploaded to box successfully ";

				}
			}
		} catch (Exception e) {// com.microsoft.sqlserver.jdbc.SQLServerException: The column name
			// include_tiaapkg is not valid.
			logger.error(e.getMessage(), e);
			respWelcome = e.getMessage();
		}
		return respWelcome;
	}

	public String getDestinationFolder(String legalEntityType, Map<String, String> finalPackMetadata) {
		String boxToken = dmsUtilityService.requestAccessToken();
		if (boxToken == null) {
			return "Box Token is Empty ";
		}
		BoxAPIConnection api = new BoxAPIConnection(boxToken);
		api = new BoxAPIConnection(boxToken);
		// java.net.Proxy proxy = new java.net.Proxy(Type.HTTP, new
		// InetSocketAddress("PITC-Zscaler-Americas-Cincinnati3PR.proxy.corporate.ge.com",
		// 80));
		// api.setProxy(proxy);

		this.logger.info("finalPackMetadata in getDestinationFolder:  " + finalPackMetadata);
		String entityTypeNumBoxId = null;
		if (legalEntityType.equals("Party")) {
			/*
			 * BoxFolder.Info partyFolderInfo = parentFolder1.createFolder("Party10");
			 * entityTypeBoxId = partyFolderInfo.getID();
			 */

			/* BoxFolder partyFolder = new BoxFolder(api, entityTypeBoxId); */
			String subFolderName = null;
			boolean flag = false;
			boolean partyNumFolderExists = false;

			BoxFolder partyfolder = new BoxFolder(api, multiFileUploadController.UPLD_PA_FLDR_ID);

			for (BoxItem.Info itemInfo : partyfolder) {
				if (flag == false) {
					if (itemInfo instanceof BoxFolder.Info) {
						partyNumFolderExists = true;
						subFolderName = (itemInfo.getName());
						BoxFolder.Info partyFolderInfo = (BoxFolder.Info) itemInfo;

						BoxFolder partyNumfolder = new BoxFolder(api, partyFolderInfo.getID());

						if (subFolderName.equals("PA_" + finalPackMetadata.get("partyNumber"))) {
							entityTypeNumBoxId = partyNumfolder.getID();

							flag = true;
							break;
						} else {
							partyNumFolderExists = false;
						}

					}
				}
			}

			if (partyNumFolderExists == false) {
				BoxFolder partyFolder = new BoxFolder(api, partyfolder.getID());
				BoxFolder.Info partychildFolderInfo = partyFolder
						.createFolder("PA_" + finalPackMetadata.get("partyNumber"));
				entityTypeNumBoxId = partychildFolderInfo.getID();
			}
		}

		if (legalEntityType.equals("Account")) {

			String subFolderName = null;
			boolean flag = false;
			boolean accNumFolderExists = false;

			BoxFolder accfolder = new BoxFolder(api, multiFileUploadController.UPLD_ACC_FLDR_ID);

			for (BoxItem.Info itemInfo : accfolder) {
				if (flag == false) {
					if (itemInfo instanceof BoxFolder.Info) {
						accNumFolderExists = true;
						subFolderName = (itemInfo.getName());
						BoxFolder.Info partyFolderInfo = (BoxFolder.Info) itemInfo;

						BoxFolder partyNumfolder = new BoxFolder(api, partyFolderInfo.getID());

						if (subFolderName.equals("AC_" + finalPackMetadata.get("sfdcAccountId"))) {
							entityTypeNumBoxId = partyNumfolder.getID();

							flag = true;
							break;
						} else {
							accNumFolderExists = false;
						}

					}
				}
			}

			if (accNumFolderExists == false) {
				BoxFolder accFolder = new BoxFolder(api, accfolder.getID());

				BoxFolder.Info accchildFolderInfo = accFolder
						.createFolder("AC_" + finalPackMetadata.get("sfdcAccountId"));
				entityTypeNumBoxId = accchildFolderInfo.getID();
			}
		}

		if (legalEntityType.equals("Opportunity")) {

			String subFolderName = null;
			boolean flag = false;
			boolean oppNumFolderExists = false;

			BoxFolder oppfolder = new BoxFolder(api, multiFileUploadController.UPLD_OPP_FLDR_ID);

			for (BoxItem.Info itemInfo : oppfolder) {
				if (flag == false) {
					if (itemInfo instanceof BoxFolder.Info) {
						oppNumFolderExists = true;
						subFolderName = (itemInfo.getName());
						BoxFolder.Info oppFolderInfo = (BoxFolder.Info) itemInfo;

						BoxFolder oppNumfolder = new BoxFolder(api, oppFolderInfo.getID());

						if (subFolderName.equals("OP_" + finalPackMetadata.get("sfdcOpportunityId"))) {
							entityTypeNumBoxId = oppNumfolder.getID();

							flag = true;
							break;
						} else {
							oppNumFolderExists = false;
						}

					}
				}
			}

			if (oppNumFolderExists == false) {
				BoxFolder oppFolder = new BoxFolder(api, oppfolder.getID());

				BoxFolder.Info oppChildFolderInfo = oppFolder
						.createFolder("OP_" + finalPackMetadata.get("sfdcOpportunityId"));
				entityTypeNumBoxId = oppChildFolderInfo.getID();

			}

		}

		if (legalEntityType.equals("LOC")) {
			if (finalPackMetadata.get("sfdcopportunityId") != null
					|| finalPackMetadata.get("sfdcopportunityId") != "") {
				// DB Logic for updating credit number and opportunity ID if provided
				uploadLOCRepository.updateLocOppotunityID(finalPackMetadata.get("lineofcreditNumber"),
						finalPackMetadata.get("sfdcopportunityId"));
			}

			String subFolderName = null;
			boolean flag = false;
			boolean locNumFolderExists = false;

			BoxFolder locfolder = new BoxFolder(api, multiFileUploadController.UPLD_LOC_FLDR_ID);

			for (BoxItem.Info itemInfo : locfolder) {
				if (flag == false) {
					if (itemInfo instanceof BoxFolder.Info) {
						locNumFolderExists = true;
						subFolderName = (itemInfo.getName());
						BoxFolder.Info locFolderInfo = (BoxFolder.Info) itemInfo;

						BoxFolder locNumfolder = new BoxFolder(api, locFolderInfo.getID());

						if (subFolderName.equals("LC_" + finalPackMetadata.get("lineOfCreditNumber"))) {
							entityTypeNumBoxId = locNumfolder.getID();

							flag = true;
							break;
						} else {
							locNumFolderExists = false;
						}

					}
				}
			}

			if (locNumFolderExists == false) {
				BoxFolder locFolder = new BoxFolder(api, locfolder.getID());

				BoxFolder.Info locChildFolderInfo = locFolder
						.createFolder("LC_" + finalPackMetadata.get("lineOfCreditNumber"));
				entityTypeNumBoxId = locChildFolderInfo.getID();

			}

		}

		if (legalEntityType.equalsIgnoreCase("Contract")) {

			String subFolderName = null;
			boolean flag = false;
			boolean ctrtNumFolderExists = false;

			BoxFolder ctrtfolder = new BoxFolder(api, multiFileUploadController.UPLD_CTRT_FLDR_ID);
			for (BoxItem.Info itemInfo : ctrtfolder) {
				if (flag == false) {
					if (itemInfo instanceof BoxFolder.Info) {
						ctrtNumFolderExists = true;
						subFolderName = (itemInfo.getName());
						BoxFolder.Info ctrtFolderInfo = (BoxFolder.Info) itemInfo;

						BoxFolder ctrtNumfolder = new BoxFolder(api, ctrtFolderInfo.getID());
						if (subFolderName.equals("CT_" + finalPackMetadata.get("lwContractSequenceNumber"))) {
							entityTypeNumBoxId = ctrtNumfolder.getID();
							this.logger.info("ContractNumberFolder BoxID is " + entityTypeNumBoxId);
							flag = true;
							break;
						} else {
							ctrtNumFolderExists = false;
						}

					}
				}
			}

			if (ctrtNumFolderExists == false) {
				BoxFolder partyFolder = new BoxFolder(api, ctrtfolder.getID());

				BoxFolder.Info partychildFolderInfo = partyFolder
						.createFolder("CT_" + finalPackMetadata.get("lwContractSequenceNumber").trim());
				entityTypeNumBoxId = partychildFolderInfo.getID();
			}
		}
		if (legalEntityType.equalsIgnoreCase("NoMetadata")) {
			BoxFolder DocuSignNoMetadataDocuments = new BoxFolder(api, strDocuSignNoMetadataDocuments.trim());
			entityTypeNumBoxId = DocuSignNoMetadataDocuments.getID();
		}
		return entityTypeNumBoxId;
	}

	public String moveingBoxFile(BoxAPIConnection api, String strDestinationfldr, Map<String, String> DMSMetadata,
			Map<String, String> dealDocMetadataMap) {
		String response = "";
		if (DMSMetadata.get("docId") != null && !DMSMetadata.get("docId").equalsIgnoreCase("")) {
			String boxToken = dmsUtilityService.requestAccessToken();
			if (boxToken == null) {
				return "Box Token is Empty ";
			}
			api = new BoxAPIConnection(boxToken);
			// java.net.Proxy proxy = new java.net.Proxy(Type.HTTP, new
			// InetSocketAddress("PITC-Zscaler-Americas-Cincinnati3PR.proxy.corporate.ge.com",
			// 80));
			// api.setProxy(proxy);
			String strSubDestinationfldr = destinationSubFolder(api, strDestinationfldr);

			this.logger.info("token recevied is : " + boxToken);
			if (!boxToken.isEmpty()) {

				try {
					BoxFile file = new BoxFile(api, DMSMetadata.get("docId"));

					BoxFolder Destinationfolder = new BoxFolder(api, strSubDestinationfldr);
					file.move(Destinationfolder);
					file.getInfo().getModifiedAt();
					Map<String, String> inputMetadataMap = new HashMap();
					inputMetadataMap.put("modifier", DMSMetadata.get("creator"));
					inputMetadataMap.put("modifyDate",
							new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(file.getInfo().getModifiedAt()));
					this.logger.info("inputMetadataMap in moveingBoxFile: " + inputMetadataMap);
					updateService.updateMetadata(DMSMetadata.get("docId"), inputMetadataMap);
					dealDocMetadataMap.put("docId", DMSMetadata.get("docId"));
					// update the legalentitytype into dealdocMap and commit dealdoc table
					this.logger.info("dealDocMetadataMap" + dealDocMetadataMap);
					// if(dealDocMetadataMap.get("legalEntityType").equalsIgnoreCase("Party")){
					// dealDocMetadataMap.entrySet().removeIf(entry ->
					// entry.getKey().equalsIgnoreCase("lwSeqNumber"));
					// dealDocMetadataMap.put("lwSeqNumber", "");
					// }
					this.logger.info("dealDocMetadataMap in moveingBoxFile: " + dealDocMetadataMap);
					updateService.updateDocumentTypeMetadata(DMSMetadata.get("docType"), DMSMetadata.get("tableName"),
							dealDocMetadataMap);

					response = "OK";
					this.logger.info("uploaded the versioned doc successfully ");
				} catch (BoxAPIException e) {
					response = e.getMessage();
					e.printStackTrace();
					logger.error("Failed to upload into the Box due to  : " + e.getMessage());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				response = "Box token is null";
			}
		} else {
			response = "DocID does not exists in the box for the file : ";// add filename also
		}
		return response;
	}

	public String uploadingToBox(BoxAPIConnection api, MultipartFile[] files, Map<String, String> DocuSignMetadata,
			Map<String, String> DMSMetadata, Iterable<DocSubtypeKeyword> keywords) {
		JSONObject response = new JSONObject();
		Map<String, String> statusObj = new HashMap<String, String>();
		List<String> boxFileExistsList = new ArrayList<String>();
		List<String> failureFilesList = new ArrayList<String>();
		List<String> successFilesList = new ArrayList<String>();
		Map<String, String> dealCopyMap = new HashMap<String, String>();

		String boxId = null;
		String nodeId = "";
		FileInputStream stream = null;
		String entityTypeNumBoxId = null;
		String respMsg = "";
		int indx = 0;
		String boxToken = null;
		String Jsonstr = null;
		String docType = DMSMetadata.get("docType");
		String loggedinUser = DMSMetadata.get("creator");
		try {
			boxToken = dmsUtilityService.requestAccessToken();
			if (boxToken == null) {
				return "Box Token is Empty ";
			}
			api = new BoxAPIConnection(boxToken);
			// java.net.Proxy proxy = new java.net.Proxy(Type.HTTP, new
			// InetSocketAddress("PITC-Zscaler-Americas-Cincinnati3PR.proxy.corporate.ge.com",
			// 80));
			// api.setProxy(proxy);

			String strDestinationfldr = getDestinationFolder(DMSMetadata.get("legalEntityType"), DMSMetadata);

			String entityID = strDestinationfldr;
			for (MultipartFile multipartFile : files) {
				long fileSize = multipartFile.getSize();

				boxId = null;
				File file = null;
				try {
					// Folder Nesting..
					int subFolderNum = 0;
					boolean flag = false;
					boolean subFolderExists = false;
					String subFolderId = null;
					BoxFolder fldr = new BoxFolder(api, entityID);
					if (fldr.getChildren().iterator().hasNext()) {
						for (BoxItem.Info itemInfo : fldr) {
							if (flag == false) {
								if (itemInfo instanceof BoxFolder.Info) {
									subFolderExists = true;
									subFolderNum = Integer.parseInt(itemInfo.getName());
									BoxFolder.Info folderInfo = (BoxFolder.Info) itemInfo;
									// Do something with the folder.
									BoxFolder folder = new BoxFolder(api, folderInfo.getID());
									int count = Iterators.size(folder.iterator());
									// log.info("MAX_FILE_CNT: " + MAX_FILE_CNT);
									// log.info("No of files: " + count);
									if (count >= Integer.parseInt(multiFileUploadController.MAX_FILE_CNT)) {
										flag = false;
										subFolderExists = false;
									} else {
										subFolderId = folder.getID();
										flag = true;
										break;
									}

								}
							}
						}

					}
					if (subFolderId == null) {
						BoxFolder.Info childFlr = fldr.createFolder("1");
						subFolderId = childFlr.getID();
						// create folder with name 1
					}

					BoxFolder uploadFolder = new BoxFolder(api, subFolderId);
					logger.info(multipartFile.getOriginalFilename() + ": " + multipartFile.getContentType());
					file = new File(multiFileUploadController.UPLD_DIR + "\\"
							+ java.nio.file.Paths.get(multipartFile.getOriginalFilename()).getFileName());

					multipartFile.transferTo(file);
					stream = new FileInputStream(file);
					int uploadCounter = 0;
					logger.info(api + " : Api With boxtoken : " + boxToken);
					BoxFile.Info newFileInfo = null;
					try {
						// BoxFile fil = new BoxFile(api,""); //existing doc
						// fil.uploadVersion(stream);
						newFileInfo = uploadFolder.uploadFile(stream, String
								.valueOf(java.nio.file.Paths.get(multipartFile.getOriginalFilename()).getFileName()));
					} catch (BoxAPIException e) {
						if (((e.getResponseCode() == CommonConstants.BOX_API_ERROR_0)
								|| (e.getResponseCode() == CommonConstants.BOX_API_ERROR_2)
								|| (e.getResponseCode() == CommonConstants.BOX_API_ERROR_3)
								|| (e.getResponseCode() == CommonConstants.BOX_API_ERROR_4)
								|| (e.getMessage().contains("Couldn't connect to the Box API due to a network error.")))
								&& (uploadCounter < 1)) {
							logger.info("Exception Occured :" + e.getResponseCode() + " , Retrying Upload....");
							Thread.sleep(10000);
							newFileInfo = uploadFolder.uploadFile(stream, String.valueOf(
									java.nio.file.Paths.get(multipartFile.getOriginalFilename()).getFileName()));
							uploadCounter++;
						} else {
							throw new BoxAPIException(e.getMessage(), e.getResponseCode(), e.getResponse());
						}
					} catch (Exception e) {
						throw new Exception(e);
					}
					boxId = newFileInfo.getID();

					nodeId = newFileInfo.getID();
					logger.info(newFileInfo.getName() + ":uploaded to box successfully, Document ID : " + nodeId);

					Map<String, String> documentDetails = new HashMap<String, String>();
					// update specific docType metadata in commonDoc
					documentDetails.put("environment", "UAT"); // Environment
					documentDetails.put("docId", boxId);
					documentDetails.put("fileSize", String.valueOf(fileSize / 1024) + " KB");
					documentDetails.put("docVersionId", newFileInfo.getVersion().getVersionID());
					documentDetails.put("docName", newFileInfo.getName());
					documentDetails.put("docTitle", newFileInfo.getName());
					documentDetails.put("docType", DMSMetadata.get("docType"));
					documentDetails.put("isMigrated", "");
					documentDetails.put("docSource", DocuSignMetadata.get("docSource"));
					documentDetails.put("retentionDate", "");
					documentDetails.put("mimeType", multipartFile.getContentType());
					documentDetails.put("permName", "");
					documentDetails.put("realmName", "");

					if (loggedinUser != null)
						documentDetails.put("ownerName", DMSMetadata.get("creator"));
					else
						documentDetails.put("ownerName", "HEF_DMS_USER");

					documentDetails.put("createDate",
							(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(newFileInfo.getCreatedAt())));
					if (loggedinUser != null) {
						documentDetails.put("creator", DMSMetadata.get("creator"));
						dealCopyMap.put("creator", DMSMetadata.get("creator"));
					} else
						documentDetails.put("creator", "HEF_DMS_USER");

					documentDetails.put("modifyDate",
							(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(newFileInfo.getModifiedAt())));

					if (loggedinUser != null)
						documentDetails.put("modifier", DMSMetadata.get("creator"));
					else
						documentDetails.put("modifier", "HEF_DMS_USER");

					documentDetails.put("isDeleted", "");
					documentDetails.put("isCurrent", "");
					documentDetails.put("versionNum", newFileInfo.getVersion().getVersionID());

					if (DMSMetadata.get("legalEntityType") != null) {
						if (DMSMetadata.get("legalEntityType").equalsIgnoreCase("Contract")) {
							String ctrtNumber = DMSMetadata.get("lwSeqNumber");

							// contract number lookup for checking contract commencement
							Integer uploadctrtCount = uploadContractRepository.getCount(ctrtNumber);

							if (uploadctrtCount > 0) {
								documentDetails.put("docState", multiFileUploadController.UPLD_DOC_ST_FINAL);
							} else
								documentDetails.put("docState", multiFileUploadController.UPLD_DOC_ST_DEFAULT);
						}

						if (!(DMSMetadata.get("legalEntityType").equalsIgnoreCase("Contract"))) {
							documentDetails.put("docState", multiFileUploadController.UPLD_DOC_ST_DEFAULT);
						}
						documentDetails.put("contentRef", "");
						documentDetails.put("isLocked", "");
						documentDetails.put("folderRef", "");
					}
					if (null != DMSMetadata.get("legalEntityType")
							&& DMSMetadata.get("legalEntityType").equals("NoMetadata")) {
						documentDetails.put("docState", "NoMetadata");
					}
					this.logger.info("documentDetails in uploadingToBox : " + documentDetails);
					updateService.updateDocumentMetadata(DMSMetadata.get("docType"), documentDetails,
							DMSMetadata.get("creator"));

					// String tableName = docIdprops.getProperty(docType + ".multi.upload.table");
					Map<String, String> updateParams = new HashMap<String, String>();
					ObjectMapper mapper = new ObjectMapper();
					List<DocSubtypeKeyword> matchfound = StreamSupport.stream(keywords.spliterator(), false)
							.filter(keyword -> DMSMetadata.get("Filename").contains(keyword.getKeyword()))
							.collect(Collectors.toList());
					String strDocSubTypeforCopy = "";

					updateParams.putAll(DMSMetadata);
					if (matchfound != null && matchfound.size() == 1) {
						for (DocSubtypeKeyword docsubtypemetadata : matchfound) {
							updateParams.put("docSubType", docsubtypemetadata.getDoc_subtype());// docSubType
							updateParams.put("welcomePackage",
									docsubtypemetadata.getInclude_welcomepkg() != null
											? docsubtypemetadata.getInclude_welcomepkg()
											: "");
							updateParams.put("finalPackage",
									docsubtypemetadata.getInclude_tiaapkg() != null
											? docsubtypemetadata.getInclude_tiaapkg()
											: "");
							if (!DMSMetadata.get("legalEntityType").equalsIgnoreCase("NoMetadata")) {
								updateParams.put("legalEntityType",
										docsubtypemetadata.getEntitytype() != null ? docsubtypemetadata.getEntitytype()
												: "");
							}
						}
					} else if (!DMSMetadata.get("legalEntityType").equalsIgnoreCase("NoMetadata")) {
						updateParams.put("docSubType", "Executed_Docusign");
					}
					updateParams = prepareMetadata(updateParams);
					updateParams.put("docId", boxId);
					// setting physicalStorageNotSent
					if (docType.equals("dealDoc")) {
						if (updateParams.get("physicalStorageStatus") == null
								|| updateParams.get("physicalStorageStatus").equals("0")) {
							updateParams.put("physicalStorageNotSent", "0");
						} else if (updateParams.get("physicalStorageStatus").equals("1")) {
							updateParams.put("physicalStorageNotSent", "1");
						}

						// updateParams.entrySet().removeIf(entry ->
						// "sourceSystem".equalsIgnoreCase(entry.getKey()));
					}
					// added by Gayatri - Aug - 14th- For Integration 4
					String opportunityId = "";
					Integer uploadOpportunityCount = 0;
					if (null != (DMSMetadata.get("legalEntityType"))
							&& (DMSMetadata.get("legalEntityType").equalsIgnoreCase("Opportunity"))) {
						if (DMSMetadata.get("sfdcOpportunityId") != null
								|| !(DMSMetadata.get("sfdcOpportunityId").isEmpty()))
							opportunityId = DMSMetadata.get("sfdcOpportunityId");
						if (!(null == opportunityId) && !("".equals(opportunityId)))
							uploadOpportunityCount = uploadContractRepository.getCountOpportunity(opportunityId);
						if (uploadOpportunityCount > 0) {
							updateParams.put("partyName", opportunityRepo.getData(opportunityId, "party_name"));
							updateParams.put("partyNumber", opportunityRepo.getData(opportunityId, "party_number"));
							updateParams.put("lineofcreditNumber",
									opportunityRepo.getData(opportunityId, "credit_number"));
						}
					}

					updateParams.put("legalEntityType",
							DMSMetadata.get("legalEntityType") != null ? DMSMetadata.get("legalEntityType") : "");
					this.logger.info("updateParams in uploadingToBox : " + updateParams);
					updateService.updateDocumentTypeMetadata(docType, DMSMetadata.get("tableName"), updateParams);

					// Added by Gayatri- july 5th 2019
					statusObj.put("code", "200");
					statusObj.put("nodeId", nodeId);
					statusObj.put("message", "Document and metadata updated succesfully");
					successFilesList.add(
							String.valueOf(java.nio.file.Paths.get(multipartFile.getOriginalFilename()).getFileName()));
					if (null != DMSMetadata.get("legalEntityType")
							&& DMSMetadata.get("legalEntityType").equals("NoMetadata")) {
						Map<String, String> mailAlertMetadata = new HashMap();
						mailAlertMetadata.put("FileName", multipartFile.getOriginalFilename());
						mailAlertMetadata.putAll(DocuSignMetadata);
						mailAlertMetadata.putAll(DMSMetadata);
						// dmsUploadMetadata.put("FileName", multipartFile.getOriginalFilename());
						//emailNotificationUtil.NoMetaDataMailAlertforDocuSign(mailAlertMetadata);
					}
					// ends here
				} catch (BoxAPIException boxAPIException) {
					respMsg = "BoxAPI Exception : " + boxAPIException.getResponseCode();
					if (boxAPIException.getResponseCode() == CommonConstants.FILE_ALREADY_EXISTS) {
						boxFileExistsList.add(String
								.valueOf(java.nio.file.Paths.get(multipartFile.getOriginalFilename()).getFileName()));
						respMsg = "";
						if (null != DMSMetadata.get("legalEntityType")
								&& DMSMetadata.get("legalEntityType").equals("NoMetadata")) {
							Map<String, String> mailAlertMetadata = new HashMap();
							mailAlertMetadata.put("FileName", multipartFile.getOriginalFilename());
							mailAlertMetadata.putAll(DocuSignMetadata);
							mailAlertMetadata.putAll(DMSMetadata);
							// dmsUploadMetadata.put("FileName", multipartFile.getOriginalFilename());
							//emailNotificationUtil.NoMetaDataMailAlertforDocuSign(mailAlertMetadata);
						}
					} else {
						failureFilesList.add(String
								.valueOf(java.nio.file.Paths.get(multipartFile.getOriginalFilename()).getFileName()));
						logger.error(boxAPIException.getMessage(), boxAPIException);
					}

					statusObj.put("code", String.valueOf(boxAPIException.getResponseCode()));
					statusObj.put("nodeId", nodeId);
					statusObj.put("message", boxAPIException.getMessage());
					// email notification for docusign

				} catch (NullPointerException e) {
					respMsg = "One of the required field(s) is null or empty";
					if (!(null == api) && !((null == boxId) || ("".equals(boxId)))) {
						BoxFile deleteFile = new BoxFile(api, boxId);
						deleteFile.delete();
						documentServiceDAO.deleteDoc(boxId, docType);
						documentServiceDAO.deleteFromTable(boxId, docType);
						logger.info("The document deleted is:" + boxId);
					}
					failureFilesList.add(
							String.valueOf(java.nio.file.Paths.get(multipartFile.getOriginalFilename()).getFileName()));
					statusObj.put("code", "400");
					statusObj.put("nodeId", nodeId);
					statusObj.put("message", e.getMessage());

					logger.error(e.getMessage(), e);
				} catch (Exception e) {
					respMsg = "";
					failureFilesList.add(
							String.valueOf(java.nio.file.Paths.get(multipartFile.getOriginalFilename()).getFileName()));
					if (!((null == api)) && !((null == boxId) || ("".equals(boxId)))) {
						BoxFile deleteFile = new BoxFile(api, boxId);
						deleteFile.delete();
						documentServiceDAO.deleteDoc(boxId, docType);
						documentServiceDAO.deleteFromTable(boxId, docType);
						logger.info("The document deleted is: " + boxId);
					}
					logger.error(e.getMessage(), e);
					statusObj.put("code", "400");
					statusObj.put("nodeId", nodeId);
					statusObj.put("message", e.getMessage());
				} finally {
					try {
						if (stream != null)
							stream.close();
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}

			}
		} catch (BoxAPIException boxAPIException) {
			logger.error(boxAPIException.getMessage(), boxAPIException);
			respMsg = "BoxAPI Exception : " + boxAPIException.getResponseCode();
			if (boxAPIException.getResponseCode() == CommonConstants.FILE_ALREADY_EXISTS) {
				for (int i = 0; i < files.length; i++)
					boxFileExistsList.add(files[i].getOriginalFilename());
			} else {
				logger.error(boxAPIException.getMessage(), boxAPIException);
				for (int i = 0; i < files.length; i++)
					failureFilesList.add(files[i].getOriginalFilename());
			}
		} catch (NullPointerException ex) {
			if (boxToken.isEmpty())
				respMsg = "Unable to obtain Box connection";
			else
				respMsg = "One of the required field(s) is null or empty";
			for (int i = 0; i < files.length; i++)
				failureFilesList.add(files[i].getOriginalFilename());
			logger.error(ex.getMessage(), ex);
		} catch (Exception ex) {
			respMsg = "";
			logger.error(ex.getMessage(), ex);
			for (int i = 0; i < files.length; i++) {
				failureFilesList.add(files[i].getOriginalFilename());
			}

		} finally {
			// added by gayatri to delete the files from application server
			/*
			 * for (int i = 0; i < successFilesList.size(); i++) { File file = new
			 * File(UPLD_DIR + "\\" + java.nio.file.Paths.get(successFilesList.get(i))); if
			 * (file.delete()) { log.debug("File deleted from the app server"); } else {
			 * log.debug(file.getAbsolutePath());
			 * log.debug("unable to delete the file from app server"); } }
			 */
		}
		if (!successFilesList.isEmpty()) {
			for (String succFile : successFilesList) {
				if (!(failureFilesList.isEmpty() || boxFileExistsList.isEmpty())) {
					failureFilesList.remove(succFile);
					boxFileExistsList.remove(succFile);
				}
			}
		}
		if (failureFilesList.isEmpty() && boxFileExistsList.isEmpty()) {
			int i = 1;
			for (String succFile : successFilesList) {
				respMsg += String.valueOf(i) + ". " + "'" + succFile + "',\n ";
			}
			respMsg = " file/(s) are uploaded to box successfully " + "box" + boxId;
		}

		if (!failureFilesList.isEmpty()) {
			int i = 1;
			respMsg = respMsg + " Failed to upload the file(s): \n";
			for (String fileFailed : failureFilesList) {
				respMsg += String.valueOf(i) + ". " + "'" + fileFailed + "',\n ";
				i++;
			}
			emailNotificationUtil.sendEmailAlert(multiFileUploadController.UPLD_DIR, failureFilesList,
					"User : " + loggedinUser + "\n Documents with Metadata : " + DMSMetadata, respMsg,
					multiFileUploadController.toEmail, "Upload");
			respMsg += " If you experience any issues, please contact hefdms.support@ge.com";

		}

		if (!boxFileExistsList.isEmpty()) {
			respMsg += "File(s) \n";
			int i = 1;
			for (String bfeL : boxFileExistsList) {
				respMsg += String.valueOf(i) + ". " + "'" + bfeL + "',\n ";
				i++;
			}
			respMsg += "have already been uploaded to this location.";
		}
		logger.info("Message Sent to User : " + respMsg);
		Gson g = new Gson();
		Jsonstr = g.toJson(respMsg);
		return Jsonstr;
	}

	public String updateBoxFileVersion(BoxAPIConnection api, Map<String, String> DMSMetadata) {
		String response = "";
		String filepath = multiFileUploadController.UPLD_DIR + "\\" + DMSMetadata.get("Filename");
		File uploadfile = new File(filepath);

		String boxToken = dmsUtilityService.requestAccessToken();
		if (boxToken == null) {
			return "Box Token is Empty ";
		}
		this.logger.info("token recevied is : " + boxToken);
		if (!boxToken.isEmpty()) {
			api = new BoxAPIConnection(boxToken);
			// java.net.Proxy proxy = new java.net.Proxy(Type.HTTP, new
			// InetSocketAddress("PITC-Zscaler-Americas-Cincinnati3PR.proxy.corporate.ge.com",
			// 80));
			// api.setProxy(proxy);

			try {
				BoxFile file = new BoxFile(api, DMSMetadata.get("docId"));
				FileInputStream stream = null;
				try {
					stream = new FileInputStream(uploadfile);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				file.uploadVersion(stream);// Uploads a new version of this file, replacing the current version. Note
											// that only users with premium accounts will be able to view and recover
											// previous versions of the file.
				file.getInfo().getModifiedAt();
				Map<String, String> commonMetadataMap = new HashMap();
				commonMetadataMap.put("modifyDate",
						new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(file.getInfo().getModifiedAt()));
				commonMetadataMap.put("modifier", DMSMetadata.get("creator"));// add modifier
				if (commonMetadataMap != null && commonMetadataMap.size() > 0) {
					updateService.updateMetadata(DMSMetadata.get("docId"), commonMetadataMap);
				}

			} catch (BoxAPIException e) {
				e.printStackTrace();
				logger.error("Failed to upload into the Box due to  : " + e.getMessage());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return response;
	}

	public String InsertintoDocuSignTable(Map<String, String> Metadata, String docid, String LegalEntityType) {
		String response = "";
		String strEnvelopeId = Metadata.get("EnvelopeID") != null ? Metadata.get("EnvelopeID") : "";
		String strFilename = Metadata.get("Filename") != null ? Metadata.get("Filename") : "";

		DocuSignMessageStatus docuSignMessageStatus = new DocuSignMessageStatus();
		docuSignMessageStatus.setDocId(docid);
		// docuSignMessageStatus.setSno(0);
		docuSignMessageStatus.setEnvelopeId(strEnvelopeId);
		docuSignMessageStatus.setDocusignFilename(strFilename);
		docuSignMessageStatus.setDocusignStatus("");
		docuSignMessageStatus.setErrorMessage("");
		docuSignMessageStatus.setCreatedBy(Metadata.get("signedUser") != null ? Metadata.get("signedUser") : "");
		docuSignMessageStatus.setCreatedDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		docuSignMessageStatus.setModifiedBy("");
		docuSignMessageStatus.setModifiedDate("");
		docuSignMessageStatus.setLegalentitytype(LegalEntityType);
		// docuSignMessageStatus.setLegalentitytype("");

		updateService.insertDocuSignStatus(docuSignMessageStatus);
		return response;
	}

	public Map<String, String> prepareMetadata(Map<String, String> finalPackMetadata) {
		Map<String, String> requestParameters = new HashMap<String, String>();
		// requestParameters.put("fileName", fileName);
		if (finalPackMetadata.get("sourceSystem") != null) {
			requestParameters.put("sourceSystem", finalPackMetadata.get("docSource"));
		}
		if (finalPackMetadata.get("docSubType") != null)// docSubType
			requestParameters.put("docSubType", finalPackMetadata.get("docSubType"));

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
			requestParameters.put("partyName", finalPackMetadata.get("partyName"));
		if (null != finalPackMetadata.get("contractDealType"))
			requestParameters.put("contractDealtype", finalPackMetadata.get("contractDealType"));
		if (null != finalPackMetadata.get("sfdcOpportunityId"))
			requestParameters.put("sfdcopportunityId", finalPackMetadata.get("sfdcOpportunityId"));
		if (null != finalPackMetadata.get("legalEntityType"))
			requestParameters.put("legalEntityType", finalPackMetadata.get("legalEntityType"));
		if (null != finalPackMetadata.get("lineOfCreditNumber"))
			requestParameters.put("lineofcreditNumber", finalPackMetadata.get("lineOfCreditNumber"));

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

		if (null != finalPackMetadata.get("CustomerNumber"))
			requestParameters.put("CustomerNumber", finalPackMetadata.get("CustomerNumber"));
		if (null != finalPackMetadata.get("DocuSignEnv"))
			requestParameters.put("DocuSignEnv", finalPackMetadata.get("DocuSignEnv"));
		if (null != finalPackMetadata.get("EnvelopeID"))
			requestParameters.put("EnvelopeID", finalPackMetadata.get("EnvelopeID"));

		return requestParameters;
	}

	public String destinationSubFolder(BoxAPIConnection api, String DestFolder) {
		int subFolderNum = 0;
		boolean flag = false;
		boolean subFolderExists = false;
		String subFolderId = null;
		BoxFolder fldr = new BoxFolder(api, DestFolder);
		for (BoxItem.Info itemInfo : fldr) {
			if (flag == false) {
				if (itemInfo instanceof BoxFolder.Info) {
					subFolderExists = true;
					subFolderNum = Integer.parseInt(itemInfo.getName());
					BoxFolder.Info folderInfo = (BoxFolder.Info) itemInfo;
					// Do something with the folder.
					BoxFolder folder = new BoxFolder(api, folderInfo.getID());
					int count = Iterators.size(folder.iterator());
					// log.info("MAX_FILE_CNT: " + MAX_FILE_CNT);
					// log.info("No of files: " + count);
					if (count >= Integer.parseInt(multiFileUploadController.MAX_FILE_CNT)) {
						flag = false;
						subFolderExists = false;
					} else {
						subFolderId = folder.getID();
						flag = true;
						break;
					}

				}
			}
		}
		return subFolderId;
	}

	public String copyDocument(Map<String, String> DMSMetadata) {
		String response = "";
		Map<String, String> dealCopyMap = new HashMap<String, String>();

		if (DMSMetadata.get("legalEntityType").equalsIgnoreCase("Party")
				|| DMSMetadata.get("legalEntityType").equalsIgnoreCase("Account")
				|| DMSMetadata.get("legalEntityType").equalsIgnoreCase("Opportunity")
				|| DMSMetadata.get("legalEntityType").equalsIgnoreCase("LOC")) {
			try {
				dealCopyMap.putAll(DMSMetadata);
				dealCopyMap = prepareMetadata(DMSMetadata);
				dealCopyMap.put("legalEntityType", "Contract");
				dealCopyMap.put("docId", DMSMetadata.get("docId"));
				// dealCopyMap.put("creator", DMSMetadata.get("creator"));
				response = "copied";
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				response = e.getMessage();
			}
		}

		this.logger.info("dealCopyMap: " + dealCopyMap);
		if (!dealCopyMap.isEmpty())
			updateService.copyDocument(dealCopyMap);
		return response;
	}
}
