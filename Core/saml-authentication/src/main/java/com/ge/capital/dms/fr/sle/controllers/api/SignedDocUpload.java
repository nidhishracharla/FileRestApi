package com.ge.capital.dms.fr.sle.controllers.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ge.capital.dms.dao.DocumentServiceDAO;
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
 * @author NR471922  Nidhish Kumar Verma, NidhishKumar.Racharla@ge.com
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

	@Value("${docusign.notavailableset}")
	private String strNotAvailableSet;


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
	//@RequestMapping(value = "/docuSignUploadProd", method = RequestMethod.POST)
	public Object uploadSigned(HttpServletRequest request, @FormDataParam("files") MultipartFile[] files,
			@FormDataParam("docType") String docType1, @FormDataParam("fileMetadata") FileMetadata fileMetadata) {
		Map<String, String> DMSMetadata = new HashMap<String, String>();
		Map<String, String> DocuSignMetadata = new HashMap<String, String>();
		HashSet<String> NotAvailableSet = new HashSet();

		String docType = "dealDoc";
		String respWelcome = "";
		String strSequenceNum = "";
		String strOpportunityID = "";
		String creator = "";
		
		BoxAPIConnection api = null;
		Properties docIdprops = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docIdMappingResource);
		String boxToken = dmsUtilityService.requestAccessToken();
		Iterable<DocSubtypeKeyword> keywords = selectService.getAllKeywords();		
		
		if (boxToken == null) {
			return "Box Token is Empty ";
		}
		api = new BoxAPIConnection(boxToken);
		//		java.net.Proxy proxy = new java.net.Proxy(Type.HTTP,new InetSocketAddress("PITC-Zscaler-Americas-Cincinnati3PR.proxy.corporate.ge.com", 80));
		//		api.setProxy(proxy);
		DocuSignMessageStatus docusignrecord = new DocuSignMessageStatus();		
		String[] updateMetadataStrArray = fileMetadata.getUploadMetadata().split("},");
		String[] updateMetadataStrArray1 = updateMetadataStrArray;
		for (int i = 0; i < updateMetadataStrArray.length - 1; i++) {
			updateMetadataStrArray[i] = updateMetadataStrArray[i] + "}";
		}
		JSONParser parser = new JSONParser();
		@SuppressWarnings("unused")
		String entityTypeValue = "";

		String[] strArrNotAvailableSet= strNotAvailableSet.split(",");
		for(String notAvailable: strArrNotAvailableSet )
			NotAvailableSet.add(notAvailable);
		List<DocuSignMessageStatus> envelopes = null;

		for (String keyValue : updateMetadataStrArray) {

			JSONObject json = null;
			try {
				json = (JSONObject) parser.parse(keyValue);
			} catch (ParseException e) {
				System.out.println(new Date());
				e.printStackTrace();
				logger.error("Failed to Parse, due to  : " + e.getMessage());
			}
			for (Object key : json.keySet()) {

				String metaKey = (String) key;
				String metaValue = "";
				if (json.get(metaKey).getClass() == (java.lang.Long.class)) {
					metaValue = (String) json.get(metaKey).toString();
				} else {
					metaValue = (String) json.get(metaKey);					
				}
				if (metaKey.equals("legalEntityType")) {
					entityTypeValue = metaValue;
				}
				DocuSignMetadata.put(metaKey, metaValue);
			}
		}
		this.logger.info("******* Processing of file : "+DocuSignMetadata.get("Filename") +"  Started *******");
		logger.info("Metadata received from DocuSign: " + fileMetadata.getUploadMetadata());
		try {

			String email = DocuSignMetadata.get("SignedUserEmail");
			creator = selectService.getUserId(email);
			strSequenceNum = DocuSignMetadata.get("lwContractSequenceNumber");
			strOpportunityID = DocuSignMetadata.get("OpportunityID");

			if (!NotAvailableSet.contains(strSequenceNum.toLowerCase())
					&& NotAvailableSet.contains(strOpportunityID.toLowerCase())) {
				DMSMetadata = selectService.getDocMetadata(strSequenceNum);
				this.logger.info("block 1 , SequenceNum:  " + strSequenceNum + " OpportunityID:  " + strOpportunityID);
			} else if (!NotAvailableSet.contains(strOpportunityID.toLowerCase())
					&& NotAvailableSet.contains(strSequenceNum.toLowerCase())) {
				DMSMetadata = selectService.getDocMetadataforOpportunityID(strOpportunityID);
				this.logger.info("block 2 , SequenceNum:  " + strSequenceNum + " OpportunityID:  " + strOpportunityID);
			} else if (!NotAvailableSet.contains(strOpportunityID.toLowerCase())
					&& !NotAvailableSet.contains(strSequenceNum.toLowerCase())) {
				DMSMetadata = selectService.getDocMetadata(strSequenceNum, strOpportunityID);
				this.logger.info("block 3 , SequenceNum:  " + strSequenceNum + " OpportunityID:  " + strOpportunityID);
			}
			String strEnvelopeId = DocuSignMetadata.get("EnvelopeID") != null ? DocuSignMetadata.get("EnvelopeID") : "";
			String strFilename = DocuSignMetadata.get("Filename") != null ? DocuSignMetadata.get("Filename") : "";

			DMSMetadata.put("NoMetadataFlag", "false");
			DMSMetadata.put("AllNAFlag", "false");
			DMSMetadata.put("CopyDoc", "false");
			this.logger.info("DMSMetadata: " + DMSMetadata);
			if (null == DMSMetadata.get("legalEntityType") || DMSMetadata.size() == 0
					|| DMSMetadata.get("legalEntityType").equalsIgnoreCase("NoMetadata")) {
				this.logger.info("Received SeqNum  : " + strSequenceNum + " strOpportunityID: " + strOpportunityID
						+ " does not have the metadata");
				DMSMetadata.put("NoMetadataFlag", "true");//when no Metadata retrieved for the given strSequenceNum and strOpportunityID
				if (!strSequenceNum.isEmpty() && !NotAvailableSet.contains(strSequenceNum)) {
					DMSMetadata.put("legalEntityType", "Contract");
					DocuSignMetadata.put("legalEntityType", "Contract");
				} else if (!strOpportunityID.isEmpty() && !NotAvailableSet.contains(strOpportunityID)) {
					DMSMetadata.put("legalEntityType", "Opportunity");
					DocuSignMetadata.put("legalEntityType", "Opportunity");
				} else {
					DMSMetadata.put("AllNAFlag", "true");// when all DocuSign inputs are NA
					DMSMetadata.put("legalEntityType", "NoMetadata");
					DocuSignMetadata.put("legalEntityType", "NoMetadata");
				}
				if (!NotAvailableSet.contains(strSequenceNum))
					DMSMetadata.put("lwContractSequenceNumber", strSequenceNum);
				if (!NotAvailableSet.contains(strOpportunityID))
					DMSMetadata.put("sfdcOpportunityId", strOpportunityID);
			}

			if (!strSequenceNum.isEmpty() && !NotAvailableSet.contains(strSequenceNum)) {
				DocuSignMetadata.put("legalEntityType", "Contract");
			} else if (!strOpportunityID.isEmpty() && !NotAvailableSet.contains(strOpportunityID)) {
				DocuSignMetadata.put("legalEntityType", "Opportunity");
			} else {
				DocuSignMetadata.put("legalEntityType", "NoMetadata");
			}

			this.logger.info("legalEntityType for the file name : " + strFilename + " is "+
					(DMSMetadata.get("legalEntityType") != null ? DMSMetadata.get("legalEntityType") : ""));
			DMSMetadata.put("docType", "dealDoc");
			DMSMetadata.put("creator", creator);
			DMSMetadata.put("Filename", strFilename);
			DMSMetadata.put("Keyword", DocuSignMetadata.get("Keyword").equalsIgnoreCase("") ? "": DocuSignMetadata.get("Keyword")) ;
			this.logger.info("DocuSignMetadata: " + DocuSignMetadata);

			this.logger.info("Final DMSMetadata:  " + DMSMetadata);
			docusignrecord = selectService.getFileforEnvelopes(strEnvelopeId, strFilename);
			String tableName = docIdprops.getProperty(docType + ".multi.upload.table");
			DMSMetadata.put("tableName", tableName);
			File newfile=null;
			for(MultipartFile file: files){
				try {
					newfile = new File(multiFileUploadController.UPLD_DIR + "\\"+ java.nio.file.Paths.get(file.getOriginalFilename()).getFileName());
					file.transferTo(newfile);
					Thread.sleep(2000);	
				}catch(Exception e) {
					System.out.println(new Date());
					e.printStackTrace();
					this.logger.error("Error occured while transfering the file : "+e.getMessage());
					Thread.sleep(2000);					
				}
			}

			if (docusignrecord != null) {
				this.logger.info("record from docusign table " + docusignrecord.toString());
			}else {
				this.logger.info("there is no record from docusign table for the file: "+newfile.getName());
			}
			if (docusignrecord != null && docusignrecord.getDocusignFilename().equalsIgnoreCase(strFilename)) {// legalEntityType is different
				this.logger.info("file: " + strFilename + " exists in the docusign table ");
				DMSMetadata.put("docId", docusignrecord.getDocId());

				if (docusignrecord.getLegalentitytype().equalsIgnoreCase("NoMetadata")
						&& DMSMetadata.get("NoMetadataFlag").equalsIgnoreCase("false")) {//EntityTypeChanged
					this.logger.info("EntityTypeChanged: file exists in the docusign table with the entity type: "+ docusignrecord.getLegalentitytype());
					this.logger.info("EntityTypeChanged: file has the entity type in DMSMetadata now is : " + DMSMetadata.get("legalEntityType"));
					this.logger.info("EntityTypeChanged: we will update the file verison ");
					String boxuploadresponse = uploadingToBox(api, newfile, DocuSignMetadata, DMSMetadata, keywords,false);
					if(boxuploadresponse == null || boxuploadresponse.isEmpty()) {
						this.logger.error("EntityTypeChanged: Exception occured while uploading a file : "+strFilename);
						respWelcome="EntityTypeChanged: Exception occured while uploading a file :"+strFilename;
					}else if (boxuploadresponse.contains("already been uploaded")) {
						String versionResponse = updateBoxFileVersion(api, DMSMetadata);
						if(versionResponse == null || versionResponse.isEmpty()) {
							respWelcome="EntityTypeChanged: Exception occured while version update";
						}else if(versionResponse.contains("File version updated for the file")) {
							this.logger.info("EntityTypeChanged: File version updated successfully1 for file: " + strFilename);
							Map<String, String> dealdocMap = new HashMap<String, String>();
							dealdocMap.putAll(DMSMetadata);
							List<DocSubtypeKeyword> matchfound = StreamSupport.stream(keywords.spliterator(), false)
									.filter(keyword -> DocuSignMetadata.get("Keyword").equalsIgnoreCase(keyword.getKeyword().trim().trim())).collect(Collectors.toList());
							if (matchfound != null && matchfound.size() == 1) {
								this.logger.info("File: "+DMSMetadata.get("Filename")+" has keyword match with details :"+matchfound.toString());
								for (DocSubtypeKeyword docsubtypemetadata : matchfound) {
									dealdocMap.put("docSubType", docsubtypemetadata.getDoc_subtype().trim());// docSubType
									dealdocMap.put("welcomePackage",docsubtypemetadata.getInclude_welcomepkg() != null? docsubtypemetadata.getInclude_welcomepkg().trim(): "");
									dealdocMap.put("finalPackage",docsubtypemetadata.getInclude_tiaapkg() != null? docsubtypemetadata.getInclude_tiaapkg().trim(): "");
									dealdocMap.put("legalEntityType",docsubtypemetadata.getEntitytype() != null ? docsubtypemetadata.getEntitytype().trim(): "");
									if(docsubtypemetadata.getEntitytype() != null && !docsubtypemetadata.getEntitytype().trim().equalsIgnoreCase("Contract"))
										DMSMetadata.put("CopyDoc", "true");
								}
							} else if (!DMSMetadata.get("legalEntityType").equalsIgnoreCase("NoMetadata")) {
								this.logger.info("File: "+DMSMetadata.get("Filename")+" has NO keyword match ");
								dealdocMap.put("docSubType", "Executed_Docusign");
							}
							dealdocMap = prepareMetadata(dealdocMap);							
							this.logger.info("EntityTypeChanged: Updating the Metadata after file version update for the file :" + strFilename
									+ " , with Metadata: " + dealdocMap);
							updateService.updateDocumentTypeMetadata(DMSMetadata.get("docType"), DMSMetadata.get("tableName"),dealdocMap);
							if(DMSMetadata.get("CopyDoc").equalsIgnoreCase("true") && DMSMetadata.get("NoMetadataFlag").equalsIgnoreCase("false") 
									&& DMSMetadata.get("AllNAFlag").equalsIgnoreCase("false")) { 
								dealdocMap.put("creator", creator);
								dealdocMap.put("Filename", strFilename);
								String CopyResponse=this.copyDocument(dealdocMap, keywords);
								if(CopyResponse.equalsIgnoreCase("Copy Document inserted")) {
									String DocuSignResponse= updateService.updateDocuSignData(docusignrecord.getDocId(), dealdocMap.get("legalEntityType"));
									if(DocuSignResponse.equalsIgnoreCase("DocuSign table updated successfully")) {
										this.logger.info("EntityTypeChanged: record updated into DocuSign table with filename : " + strFilename
												+ " with legalEntitytype:  " + dealdocMap.get("legalEntityType"));
										respWelcome = "EntityTypeChanged: Version Updated Successfully for the file : "+strFilename+" with BoxId: "+docusignrecord.getDocId();
										this.logger.info("EntityTypeChanged: Version Updated Successfully for the file : "+strFilename+" with BoxId: "+docusignrecord.getDocId());
									}else {
										this.logger.info("EntityTypeChanged: Failed to update DocuSign table with filename : " + strFilename
												+ " with legalEntitytype:  " + dealdocMap.get("legalEntityType"));
										respWelcome = "EntityTypeChanged: Failed to update DocuSign table with filename : "+strFilename+" with BoxId: "+docusignrecord.getDocId();
										this.logger.info("EntityTypeChanged: Failed to update DocuSign table with filename : "+strFilename+" with BoxId: "+docusignrecord.getDocId());
									}	
								}else {
									this.logger.info("EntityTypeChanged: Failed to insert Copy Document for filename : " + strFilename
											+ " with legalEntitytype:  " + dealdocMap.get("legalEntityType"));
									respWelcome = "EntityTypeChanged: Failed to insert Copy Document for filename : "+strFilename+" with BoxId: "+docusignrecord.getDocId();
									this.logger.info("EntityTypeChanged: Failed to insert Copy Document for filename : "+strFilename+" with BoxId: "+docusignrecord.getDocId());
								}
							}else {
								String DocuSignResponse= updateService.updateDocuSignData(docusignrecord.getDocId(), dealdocMap.get("legalEntityType"));
								if(DocuSignResponse.equalsIgnoreCase("DocuSign table updated successfully")) {
									this.logger.info("EntityTypeChanged: record updated into DocuSign table with filename : " + strFilename
											+ " with legalEntitytype:  " + dealdocMap.get("legalEntityType"));
									respWelcome = "EntityTypeChanged: Version Updated Successfully for the file : "+strFilename+" with BoxId: "+docusignrecord.getDocId();
									this.logger.info("EntityTypeChanged: Version Updated Successfully for the file : "+strFilename+" with BoxId: "+docusignrecord.getDocId());
								}else {
									this.logger.info("EntityTypeChanged: Failed to update DocuSign table with filename : " + strFilename
											+ " with legalEntitytype:  " + dealdocMap.get("legalEntityType"));
									respWelcome = "EntityTypeChanged: Failed to update DocuSign table with filename : "+strFilename+" with BoxId: "+docusignrecord.getDocId();
									this.logger.info("EntityTypeChanged: Failed to update DocuSign table with filename : "+strFilename+" with BoxId: "+docusignrecord.getDocId());
								}	
							}

						}else {
							this.logger.error("EntityTypeChanged: Exception response received while updating the file version for the file : "+strFilename+ " is: "+versionResponse );
							respWelcome = "EntityTypeChanged: Exception response received while updating the file version for the file : "+strFilename+ " is: "+versionResponse;
						}

					}else {
						this.logger.error("EntityTypeChanged: Exception response received while uploading the file for the file : "+strFilename+ " is: "+boxuploadresponse );
						respWelcome = "EntityTypeChanged: Exception response received while uploading the file for the file : "+strFilename+ " is: "+boxuploadresponse;
					}

				} else {// file exists in same legalEntityType type i.e, SameEntityType
					this.logger.info("SameEntityType: file exists in the docusign table with the entity type"+ docusignrecord.getLegalentitytype());
					this.logger.info("SameEntityType: file has the entity type in DMSMetadata now is : " + DMSMetadata.get("legalEntityType"));
					String boxuploadresponse = uploadingToBox(api, newfile, DocuSignMetadata, DMSMetadata, keywords,false);					
					if(boxuploadresponse == null || boxuploadresponse.isEmpty()) {
						this.logger.error("SameEntityType: Exception occured while uploading a file ");
						respWelcome="SameEntityType: Exception occured while uploading a file "+strFilename;
					}else if (boxuploadresponse.contains("already been uploaded")) {
						String versionResponse = updateBoxFileVersion(api, DMSMetadata);
						if(versionResponse == null || versionResponse.isEmpty()) {
							respWelcome="SameEntityType: Exception occured while version update";
						}else if(versionResponse.contains("File version updated for the file")) {
							this.logger.info("SameEntityType: File version updated successfully1 for file: " + strFilename);
							respWelcome = "SameEntityType: Version Updated Successfully for the file : "+strFilename+" with BoxId: "+docusignrecord.getDocId();
							this.logger.info("SameEntityType: Version Updated Successfully for the file : "+strFilename+" with BoxId: "+docusignrecord.getDocId());
						}else {
							this.logger.error("SameEntityType: Exception response received while updating the file version for the file : "+strFilename+ " is: "+versionResponse );
							respWelcome = "Exception response received while updating the file version for the file : "+strFilename+ " is: "+versionResponse;
						}						
					}else {
						this.logger.error("SameEntityType: Exception response received while uploading the file for the file : "+strFilename+ " is: "+boxuploadresponse );
						respWelcome = "SameEntityType: Exception response received while uploading the file for the file : "+strFilename+ " is: "+boxuploadresponse;
					}					
				}
			} else {// NoMetaData block
				this.logger.info("NoMetaData: file: " + strFilename + " does not exists in the docusign table ");
				this.logger.info("NoMetaData: file has the entity type in DMSMetadata now is : " + DMSMetadata.get("legalEntityType"));
				String boxuploadresponse = uploadingToBox(api, newfile, DocuSignMetadata, DMSMetadata, keywords, false);
				this.logger.info("NoMetaData: response received :" + boxuploadresponse);
				String boxid = "";
				if(boxuploadresponse == null || boxuploadresponse.isEmpty()) {
					this.logger.error("NoMetaData: Exception occured while uploading a file : "+strFilename);
					respWelcome="NoMetaData: Exception occured while uploading a file :"+strFilename;
				}else if (boxuploadresponse.contains("file/(s) are uploaded to box successfully")) {
					boxid = boxuploadresponse.substring(boxuploadresponse.indexOf("successfully box") + 18,	boxuploadresponse.length() - 1);
					if(DMSMetadata.get("CopyDoc").equalsIgnoreCase("true") && DMSMetadata.get("NoMetadataFlag").equalsIgnoreCase("false") 
							&& DMSMetadata.get("AllNAFlag").equalsIgnoreCase("false")) {
						String CopyResponse=this.copyDocument(DMSMetadata,keywords);
						if(CopyResponse.equalsIgnoreCase("Copy Document inserted")) {
							InsertintoDocuSignTable(DocuSignMetadata, boxid, DMSMetadata);
							respWelcome = "NoMetaData: file/(s) are uploaded to box successfully with BoxId: "+boxid;
							this.logger.info("NoMetaData: file/(s) are uploaded to box successfully with BoxId: "+boxid);
						}else {
							this.logger.info("Failed to insert Copy Document for filename : " + DMSMetadata.get("Filename")
							+ " with legalEntitytype:  " + DMSMetadata.get("legalEntityType"));
							this.logger.info("Failed to insert Copy Document for filename : "+DMSMetadata.get("Filename")+" with BoxId: "+boxid);
							respWelcome = "Failed to insert Copy Document for filename : "+DMSMetadata.get("Filename")+" with BoxId: "+boxid;
						}
					}else {
						InsertintoDocuSignTable(DocuSignMetadata, boxid, DMSMetadata);
						respWelcome = "NoMetaData: file/(s) are uploaded to box successfully with BoxId: "+boxid;
						this.logger.info("NoMetaData: file/(s) are uploaded to box successfully with BoxId: "+boxid);
					}
				} else if (boxuploadresponse.contains("already been uploaded")) {
					//docusignrecord = selectService.getFileforEnvelopes(strEnvelopeId, DMSMetadata.get("Filename"));
					boolean isExistingFilewithNewEnvelope = true;
					String boxuploadresponse1 = uploadingToBox(api, newfile, DocuSignMetadata, DMSMetadata, keywords,	isExistingFilewithNewEnvelope);
					if(boxuploadresponse1 == null || boxuploadresponse1.isEmpty()) {
						this.logger.error("NoMetaData: Exception occured while uploading a file : "+strFilename);
						respWelcome="NoMetaData: Exception occured while uploading a file :"+strFilename;
					}else if (boxuploadresponse1.contains("file/(s) are uploaded to box successfully")) {

						boxid = boxuploadresponse1.substring(boxuploadresponse1.indexOf("successfully box") + 18,	boxuploadresponse1.length() - 1);
						if(DMSMetadata.get("CopyDoc").equalsIgnoreCase("true") && DMSMetadata.get("NoMetadataFlag").equalsIgnoreCase("false") 
								&& DMSMetadata.get("AllNAFlag").equalsIgnoreCase("false")) {
							String CopyResponse=this.copyDocument(DMSMetadata,keywords);
							if(CopyResponse.equalsIgnoreCase("Copy Document inserted")) {
								InsertintoDocuSignTable(DocuSignMetadata, boxid, DMSMetadata);
								respWelcome = "NoMetaData: file/(s) are uploaded to box successfully with BoxId: "+boxid;
								this.logger.info("NoMetaData: file/(s) are uploaded to box successfully with BoxId: "+boxid);
							}else {
								this.logger.info("Failed to insert Copy Document for filename : " + DMSMetadata.get("Filename")
								+ " with legalEntitytype:  " + DMSMetadata.get("legalEntityType"));
								this.logger.info("Failed to insert Copy Document for filename : "+DMSMetadata.get("Filename")+" with BoxId: "+boxid);
								respWelcome = "Failed to insert Copy Document for filename : "+DMSMetadata.get("Filename")+" with BoxId: "+boxid;
							}
						}else {
							InsertintoDocuSignTable(DocuSignMetadata, boxid, DMSMetadata);
							respWelcome = "NoMetaData: file/(s) are uploaded to box successfully with BoxId: "+boxid;
							this.logger.info("NoMetaData: file/(s) are uploaded to box successfully with BoxId: "+boxid);
						}
					
//						boxid = boxuploadresponse1.substring(boxuploadresponse1.indexOf("successfully box") + 18,boxuploadresponse1.length() - 1);
//						InsertintoDocuSignTable(DocuSignMetadata, boxid, DMSMetadata);
//						respWelcome = "NoMetaData: file/(s) are uploaded to box successfully with BoxId: "+boxid;
//						this.logger.info("NoMetaData: file/(s) are uploaded to box successfully with BoxId: "+boxid);
					}else if (boxuploadresponse1.contains("already been uploaded")) {
						docusignrecord = selectService.getFileforEnvelopes(strEnvelopeId, DMSMetadata.get("Filename"));
						this.logger.info("NoMetaData: file: " + DMSMetadata.get("Filename") + " exists in the docusign table ");
						DMSMetadata.put("docId", docusignrecord.getDocId());
						String versionResponse = updateBoxFileVersion(api, DMSMetadata);
						if(versionResponse == null || versionResponse.isEmpty()) {
							respWelcome="NoMetaData: Exception occured while version update";
						}else if(versionResponse.contains("File version updated for the file")) {
							if (docusignrecord.getLegalentitytype().equalsIgnoreCase("NoMetadata")
									&& DMSMetadata.get("NoMetadataFlag").equalsIgnoreCase("false")) {
								Map<String, String> dealdocMap = new HashMap();
								dealdocMap.putAll(DMSMetadata);
								List<DocSubtypeKeyword> matchfound = StreamSupport.stream(keywords.spliterator(), false)
										.filter(keyword -> DocuSignMetadata.get("Keyword").equalsIgnoreCase(keyword.getKeyword().trim().trim())).collect(Collectors.toList());										
								if (matchfound != null && matchfound.size() == 1) {
									this.logger.info("File: "+DMSMetadata.get("Filename")+" has keyword match with details :"+matchfound.toString());
									for (DocSubtypeKeyword docsubtypemetadata : matchfound) {
										dealdocMap.put("docSubType", docsubtypemetadata.getDoc_subtype().trim());// docSubType
										dealdocMap.put("welcomePackage",docsubtypemetadata.getInclude_welcomepkg() != null? docsubtypemetadata.getInclude_welcomepkg().trim(): "");
										dealdocMap.put("finalPackage",docsubtypemetadata.getInclude_tiaapkg() != null? docsubtypemetadata.getInclude_tiaapkg().trim(): "");
										dealdocMap.put("legalEntityType",docsubtypemetadata.getEntitytype() != null ? docsubtypemetadata.getEntitytype().trim(): "");
									}
								} else if (!DMSMetadata.get("legalEntityType").equalsIgnoreCase("NoMetadata")) {
									this.logger.info("NoMetaData: File: "+DMSMetadata.get("Filename")+" has NO keyword match ");
									dealdocMap.put("docSubType", "Executed_Docusign");
								}
								dealdocMap = prepareMetadata(dealdocMap);
								this.logger.info("NoMetaData: Updating the Metadata after file version update for the file :" + DMSMetadata.get("Filename")
								+ " , with Metadata: " + dealdocMap);
								updateService.updateDocumentTypeMetadata(DMSMetadata.get("docType"), DMSMetadata.get("tableName"),dealdocMap);
								String DocuSignResponse= updateService.updateDocuSignData(docusignrecord.getDocId(), dealdocMap.get("legalEntityType"));
								if(DocuSignResponse.equalsIgnoreCase("DocuSign table updated successfully")) {
									this.logger.info("NoMetaData: record updated into DocuSign table with filename : " + strFilename
											+ " with legalEntitytype:  " + dealdocMap.get("legalEntityType"));
									respWelcome = "NoMetaData: Version Updated Successfully for the file : "+DMSMetadata.get("Filename")+" with BoxId: "+docusignrecord.getDocId();
									this.logger.info("NoMetaData: Version Updated Successfully for the file : "+DMSMetadata.get("Filename")+" with BoxId: "+docusignrecord.getDocId());
								}else {
									this.logger.info("NoMetaData: Failed to update DocuSign table with filename : " + strFilename
											+ " with legalEntitytype:  " + dealdocMap.get("legalEntityType"));
									respWelcome = "NoMetaData: Failed to update DocuSign table with filename : "+strFilename+" with BoxId: "+docusignrecord.getDocId();
									this.logger.info("NoMetaData: Failed to update DocuSign table with filename : "+strFilename+" with BoxId: "+docusignrecord.getDocId());
								}																															
							}
						}else {
							this.logger.error("NoMetaData: Exception response received while updating the file version for the file : "+DMSMetadata.get("Filename")+ " is: "+versionResponse );
							respWelcome = "NoMetaData: Exception response received while updating the file version for the file : "+DMSMetadata.get("Filename")+ " is: "+versionResponse;
						}						
					}else {
						this.logger.error("NoMetaData: Exception response received while uploading the file for the file : "+strFilename+ " is: "+boxuploadresponse1 );
						respWelcome = "NoMetaData: Exception response received while uploading the file for the file : "+strFilename+ " is: "+boxuploadresponse1;
					}
				} else {
					this.logger.error("NoMetaData: Exception response received while uploading the file for the file : "+strFilename+ " is: "+boxuploadresponse );
					respWelcome = "NoMetaData: Exception response received while uploading the file for the file : "+strFilename+ " is: "+boxuploadresponse;
				}			
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			respWelcome = e.getMessage();
		}
		this.logger.info("******* Processing of file : "+DMSMetadata.get("Filename")+"  ended *******"+"\n");
		return respWelcome;
	}

	public String getDestinationFolder(BoxAPIConnection api, String legalEntityType,Map<String, String> finalPackMetadata) {
		String entityTypeNumBoxId = null;
		try {
			if (legalEntityType.equals("Party")) {
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
							if (subFolderName.equals("CT_" + finalPackMetadata.get("lwContractSequenceNumber").trim())) {
								entityTypeNumBoxId = ctrtNumfolder.getID();
								//this.logger.info("ContractNumberFolder BoxID is " + entityTypeNumBoxId);
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

		}catch(Exception e) {
			this.logger.info("Exception Occured while getting Destination folder:  " + e.getMessage());
			System.out.println(new Date());
			e.printStackTrace();
			return "Exception Occured while getting Destination folder:  " +entityTypeNumBoxId;
		}
		this.logger.info(" destination folder : " + entityTypeNumBoxId);
		return entityTypeNumBoxId;
	}
	public String uploadingToBox(BoxAPIConnection api, File newfile, Map<String, String> DocuSignMetadata,
			Map<String, String> DMSMetadata, Iterable<DocSubtypeKeyword> keywords, boolean isExistingFilewithNewEnvelope) {
		JSONObject response = new JSONObject();
		Map<String, String> statusObj = new HashMap<String, String>();
		List<String> boxFileExistsList = new ArrayList<String>();
		List<String> failureFilesList = new ArrayList<String>();
		List<String> successFilesList = new ArrayList<String>();
		Map<String, String> dealCopyMap = new HashMap<String, String>();

		String boxId = null;
		String nodeId = "";
		FileInputStream stream = null;
		String respMsg = "";
		String boxToken = null;
		String Jsonstr = null;
		String docType = DMSMetadata.get("docType");
		String loggedinUser = DMSMetadata.get("creator");
		try {
			String strDestinationfldr = getDestinationFolder(api, DocuSignMetadata.get("legalEntityType"), DMSMetadata);
			if(strDestinationfldr==null || strDestinationfldr.isEmpty()) {
				this.logger.error("Exception Occured while fetching the Destination folder ");
				return "Exception Occured while fetching the Destination folder ";
			}
			String entityID = strDestinationfldr;
			long fileSize = newfile.getTotalSpace();
			boxId = null;

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
				if (subFolderExists == false) {

					BoxFolder parentFolder = new BoxFolder(api, fldr.getID());
					subFolderNum += 1;
					BoxFolder.Info childFolderInfo = parentFolder.createFolder(Integer.toString(subFolderNum));
					subFolderId = childFolderInfo.getID();
				}
				BoxFolder uploadFolder = new BoxFolder(api, subFolderId);
				int uploadCounter = 0;
				BoxFile.Info newFileInfo = null;
				try {
					if (isExistingFilewithNewEnvelope) {
						this.logger.info("Uploading the file : " + newfile.getName()+ " to Box folder : " + subFolderId);
						String filename= newfile.getName();
						filename= filename.replace(".pdf", "_" + DocuSignMetadata.get("EnvelopeID") + ".pdf");
						this.logger.info("Uploading the file : " +filename+ " to Box folder : " + subFolderId);
						DMSMetadata.put("Filename", filename);
						this.logger.info("Filename after appending the EnvelopeID: "+filename);
						File modifiedFilename = new File(multiFileUploadController.UPLD_DIR + "\\"+ filename);
						Files.copy(newfile.toPath(), modifiedFilename.toPath(), StandardCopyOption.REPLACE_EXISTING);
						stream = new FileInputStream(modifiedFilename);							
						newFileInfo = uploadFolder.uploadFile(stream,filename);							
						DocuSignMetadata.put("Filename", filename);						
					} else {
						this.logger.info("Uploading the file : " + newfile.getName()+ " to Box folder : " + subFolderId);
						File file = new File(multiFileUploadController.UPLD_DIR + "\\"+ newfile.getName());
						Files.copy(newfile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
						stream = new FileInputStream(file);	
						newFileInfo = uploadFolder.uploadFile(stream, newfile.getName());						
					}					
				} catch (BoxAPIException e) {
					if (((e.getResponseCode() == CommonConstants.BOX_API_ERROR_0)
							|| (e.getResponseCode() == CommonConstants.BOX_API_ERROR_2)
							|| (e.getResponseCode() == CommonConstants.BOX_API_ERROR_3)
							|| (e.getResponseCode() == CommonConstants.BOX_API_ERROR_4)
							|| (e.getMessage().contains("Couldn't connect to the Box API due to a network error.")))
							&& (uploadCounter < 1)) {
						logger.info("Exception Occured :" + e.getResponseCode() + " , Retrying Upload....");
						Thread.sleep(10000);
						newFileInfo = uploadFolder.uploadFile(stream, String.valueOf(java.nio.file.Paths.get(newfile.getName())));
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
				documentDetails.put("mimeType", "application/pdf; charset=ISO-8859-1");
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
				this.logger.info("CommonDoc Metadata : documentDetails in uploadingToBox : " + documentDetails);
				updateService.updateDocumentMetadata(DMSMetadata.get("docType"), documentDetails,
						DMSMetadata.get("creator"));

				Map<String, String> updateParams = new HashMap<String, String>();
				ObjectMapper mapper = new ObjectMapper();
				List<DocSubtypeKeyword> matchfound = StreamSupport.stream(keywords.spliterator(), false)
						.filter(keyword -> DocuSignMetadata.get("Keyword").equalsIgnoreCase(keyword.getKeyword().trim().trim()))
						.collect(Collectors.toList());
				this.logger.info("DocuSignMetadata.get(Keyword) : "+DocuSignMetadata.get("Keyword"));
				this.logger.info("matchfound : "+matchfound);
				updateParams.putAll(DMSMetadata);
				if (matchfound != null && matchfound.size() == 1) {
					this.logger.info("File: "+DMSMetadata.get("Filename")+" has keyword match with details :"+matchfound.toString());
					for (DocSubtypeKeyword docsubtypemetadata : matchfound) {
						updateParams.put("docSubType", docsubtypemetadata.getDoc_subtype().trim());// docSubType
						updateParams.put("welcomePackage",docsubtypemetadata.getInclude_welcomepkg() != null? docsubtypemetadata.getInclude_welcomepkg().trim(): "");
						updateParams.put("finalPackage",docsubtypemetadata.getInclude_tiaapkg() != null	? docsubtypemetadata.getInclude_tiaapkg().trim(): "");
						if (DMSMetadata.get("AllNAFlag") != null && !DMSMetadata.get("AllNAFlag").equalsIgnoreCase("true")) {
							updateParams.put("legalEntityType",docsubtypemetadata.getEntitytype() != null ? docsubtypemetadata.getEntitytype().trim(): "");
							if(docsubtypemetadata.getEntitytype() != null && !docsubtypemetadata.getEntitytype().trim().equalsIgnoreCase("Contract"))
								DMSMetadata.put("CopyDoc", "true");
						} else {
							updateParams.put("legalEntityType", "NoMetadata");
						}
					}
				} else if (!DMSMetadata.get("legalEntityType").equalsIgnoreCase("NoMetadata")) {
					this.logger.info("File: "+DMSMetadata.get("Filename")+" has NO keyword match ");
					updateParams.put("docSubType", "Executed_Docusign");
				}
				updateParams = prepareMetadata(updateParams);
				updateParams.put("docId", boxId);
				DMSMetadata.put("docId", boxId);

				// setting physicalStorageNotSent
				if (docType.equals("dealDoc")) {
					if (updateParams.get("physicalStorageStatus") == null
							|| updateParams.get("physicalStorageStatus").equals("0")) {
						updateParams.put("physicalStorageNotSent", "0");
					} else if (updateParams.get("physicalStorageStatus").equals("1")) {
						updateParams.put("physicalStorageNotSent", "1");
					}					
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
				this.logger.info("DealDoc Metadata : updateParams in uploadingToBox : " + updateParams);
				updateService.updateDocumentTypeMetadata(docType, DMSMetadata.get("tableName"), updateParams);

				// Added by Gayatri- july 5th 2019
				statusObj.put("code", "200");
				statusObj.put("nodeId", nodeId);
				statusObj.put("message", "Document and metadata updated succesfully");
				successFilesList.add(String.valueOf(newFileInfo.getName()));
				if (null != DMSMetadata.get("legalEntityType")
						&& DMSMetadata.get("legalEntityType").equals("NoMetadata")) {
					Map<String, String> mailAlertMetadata = new HashMap();
					mailAlertMetadata.put("FileName", newfile.getName());
					mailAlertMetadata.putAll(DocuSignMetadata);
					mailAlertMetadata.putAll(DMSMetadata);
					// dmsUploadMetadata.put("FileName", multipartFile.getOriginalFilename());
					// emailNotificationUtil.NoMetaDataMailAlertforDocuSign(mailAlertMetadata);
				}
				// ends here
			} catch (BoxAPIException boxAPIException) {
				respMsg = "BoxAPI Exception : " + boxAPIException.getResponseCode();
				if (boxAPIException.getResponseCode() == CommonConstants.FILE_ALREADY_EXISTS) {
					boxFileExistsList.add(newfile.getName());
					respMsg = "";
					if (null != DMSMetadata.get("legalEntityType")
							&& DMSMetadata.get("legalEntityType").equals("NoMetadata")) {
						Map<String, String> mailAlertMetadata = new HashMap();
						mailAlertMetadata.put("FileName", newfile.getName());
						mailAlertMetadata.putAll(DocuSignMetadata);
						mailAlertMetadata.putAll(DMSMetadata);
						// dmsUploadMetadata.put("FileName", multipartFile.getOriginalFilename());
						// emailNotificationUtil.NoMetaDataMailAlertforDocuSign(mailAlertMetadata);
					}
				} else {
					failureFilesList.add(newfile.getName());
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
				failureFilesList.add(newfile.getName());
				statusObj.put("code", "400");
				statusObj.put("nodeId", nodeId);
				statusObj.put("message", e.getMessage());

				logger.error(e.getMessage(), e);
			} catch (Exception e) {
				respMsg = "";
				failureFilesList.add(newfile.getName());
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


		} catch (BoxAPIException boxAPIException) {
			logger.error(boxAPIException.getMessage(), boxAPIException);
			respMsg = "BoxAPI Exception : " + boxAPIException.getResponseCode();
			if (boxAPIException.getResponseCode() == CommonConstants.FILE_ALREADY_EXISTS) {
				//for (int i = 0; i < files.length; i++)
				boxFileExistsList.add(newfile.getName());
			} else {
				logger.error(boxAPIException.getMessage(), boxAPIException);
				//for (int i = 0; i < files.length; i++)
				failureFilesList.add(newfile.getName());
			}
		} catch (NullPointerException ex) {
			if (boxToken.isEmpty())
				respMsg = "Unable to obtain Box connection";
			else
				respMsg = "One of the required field(s) is null or empty";
			//for (int i = 0; i < files.length; i++)
			failureFilesList.add(newfile.getName());
			logger.error(ex.getMessage(), ex);
		} catch (Exception ex) {
			respMsg = "";
			logger.error(ex.getMessage(), ex);
			//for (int i = 0; i < files.length; i++) {
			failureFilesList.add(newfile.getName());
			//}

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
			respMsg = " file/(s) are uploaded to box successfully " + "box: " + boxId;
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
		String response = null;
		String filepath = multiFileUploadController.UPLD_DIR + "\\" + DMSMetadata.get("Filename");
		File uploadfile = new File(filepath);

		try {
			BoxFile file = new BoxFile(api, DMSMetadata.get("docId"));
			FileInputStream stream = null;
			try {
				stream = new FileInputStream(uploadfile);
			} catch (FileNotFoundException e) {
				this.logger.error("Error while getting the stream for the uploading file  : "+DMSMetadata.get("Filename")+ " is "+e.getMessage());
				System.out.println(new Date());
				e.printStackTrace();
				return "Error while updating the file version for file : "+e.getMessage();
			}
			file.uploadVersion(stream);// Uploads a new version of this file, replacing the current version. Note
			// that only users with premium accounts will be able to view and recover previous versions of the file.
			file.getInfo().getModifiedAt();
			this.logger.info("File version updated for the file with docID  : " + DMSMetadata.get("docId"));
			Map<String, String> commonMetadataMap = new HashMap();
			commonMetadataMap.put("modifyDate",	new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(file.getInfo().getModifiedAt()));
			commonMetadataMap.put("modifier", DMSMetadata.get("creator"));// add modifier
			if (commonMetadataMap != null && commonMetadataMap.size() > 0) {
				updateService.updateMetadata(DMSMetadata.get("docId"), commonMetadataMap);
				this.logger.info("CommonDoc updated with ModifiedDate for the file version update is successfull ");
			}

			response= "File version updated for the file";
		} catch (BoxAPIException e) {
			System.out.println(new Date());
			e.printStackTrace();
			logger.error("Failed to update the version for the file :"+DMSMetadata.get("Filename")+"  due to  : " + e.getMessage());
			return "Failed to update the version for the file :"+DMSMetadata.get("Filename")+"  due to  : " + e.getMessage();
		} catch (Exception e) {
			System.out.println(new Date());
			e.printStackTrace();
			logger.error("Failed to update the version for the file :"+DMSMetadata.get("Filename")+"  due to  : " + e.getMessage());
			return "Failed to update the version for the file :"+DMSMetadata.get("Filename")+"  due to  : " + e.getMessage();
		}
		return response;
	}

	public String InsertintoDocuSignTable(Map<String, String> DocuSignMetadata, String docid, Map<String, String> DMSMetadata) {
		String response = "";
		try {
			String strEnvelopeId = DocuSignMetadata.get("EnvelopeID") != null ? DocuSignMetadata.get("EnvelopeID") : "";
			String strFilename = DocuSignMetadata.get("Filename") != null ? DocuSignMetadata.get("Filename") : "";

			DocuSignMessageStatus docuSignMessageStatus = new DocuSignMessageStatus();
			docuSignMessageStatus.setDocId(docid);
			docuSignMessageStatus.setEnvelopeId(strEnvelopeId);
			docuSignMessageStatus.setDocusignFilename(strFilename);
			docuSignMessageStatus.setDocusignStatus("");
			docuSignMessageStatus.setErrorMessage("");
			docuSignMessageStatus.setCreatedBy(DocuSignMetadata.get("signedUser") != null ? DocuSignMetadata.get("signedUser") : "");
			docuSignMessageStatus.setCreatedDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			docuSignMessageStatus.setModifiedBy("");
			docuSignMessageStatus.setModifiedDate("");
			docuSignMessageStatus.setLegalentitytype(DMSMetadata.get("NoMetadataFlag").equalsIgnoreCase("true") ? "NoMetadata": DMSMetadata.get("legalEntityType"));

			updateService.insertDocuSignStatus(docuSignMessageStatus);
			this.logger.info("record inserted into DocuSign table with filename : " + strFilename + " with legalEntitytype:  "+DMSMetadata.get("legalEntityType"));
		}catch(Exception e) {
			System.out.println(new Date());
			e.printStackTrace();
			logger.error("Failed to insert into DocuSign table due to  : " + e.getMessage());
			return "Failed to insert into DocuSign table due to  : "+e.getMessage();
		}
		return response;
	}

	public Map<String, String> prepareMetadata(Map<String, String> finalPackMetadata) {
		Map<String, String> requestParameters = new HashMap<String, String>();
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
		if (null != finalPackMetadata.get("contractDealType"))//contractDealType
			requestParameters.put("contractDealtype", finalPackMetadata.get("contractDealType"));
		if (null != finalPackMetadata.get("contractDealtype"))
			requestParameters.put("contractDealtype", finalPackMetadata.get("contractDealtype"));
		
		if (null != finalPackMetadata.get("sfdcOpportunityId"))
			requestParameters.put("sfdcopportunityId", finalPackMetadata.get("sfdcOpportunityId"));
		else if (null != finalPackMetadata.get("sfdcopportunityId"))
			requestParameters.put("sfdcopportunityId", finalPackMetadata.get("sfdcopportunityId"));
		if (null != finalPackMetadata.get("legalEntityType"))
			requestParameters.put("legalEntityType", finalPackMetadata.get("legalEntityType"));
		if (null != finalPackMetadata.get("lineOfCreditNumber"))
			requestParameters.put("lineofcreditNumber", finalPackMetadata.get("lineOfCreditNumber"));
		else if (null != finalPackMetadata.get("lineofcreditNumber"))
			requestParameters.put("lineofcreditNumber", finalPackMetadata.get("lineofcreditNumber"));		
		if (null != finalPackMetadata.get("lwContractSequenceNumber") ) {
			requestParameters.put("lwSeqNumber", finalPackMetadata.get("lwContractSequenceNumber"));			
		}else if ( null != finalPackMetadata.get("lwSeqNumber")) {
			requestParameters.put("lwSeqNumber", finalPackMetadata.get("lwSeqNumber"));
		}
		if (null != finalPackMetadata.get("syndicationPackage")
				&& String.valueOf(finalPackMetadata.get("syndicationPackage")).equals("true"))
			requestParameters.put("syndicationPackage", "1");
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
			requestParameters.put("SignedUserEmail",finalPackMetadata.get("SignedUserEmail") + "," + docusignSupportEmail);

		if (null != finalPackMetadata.get("CustomerNumber"))
			requestParameters.put("CustomerNumber", finalPackMetadata.get("CustomerNumber"));
		if (null != finalPackMetadata.get("DocuSignEnv"))
			requestParameters.put("DocuSignEnv", finalPackMetadata.get("DocuSignEnv"));
		if (null != finalPackMetadata.get("EnvelopeID"))
			requestParameters.put("EnvelopeID", finalPackMetadata.get("EnvelopeID"));
		if (null != finalPackMetadata.get("docId"))
			requestParameters.put("docId", finalPackMetadata.get("docId"));		

		return requestParameters;
	}

	public String copyDocument(Map<String, String> DMSMetadata, Iterable<DocSubtypeKeyword> keywords) {
		Map<String, String> DealCopyMap= new HashMap();
		try {					
			DealCopyMap.putAll(DMSMetadata);
			DealCopyMap = prepareMetadata(DealCopyMap);
			DealCopyMap.put("legalEntityType", "Contract");
			DealCopyMap.put("docId", DMSMetadata.get("docId"));
			DealCopyMap.put("creator", DMSMetadata.get("creator"));
			List<DocSubtypeKeyword> matchfound = StreamSupport.stream(keywords.spliterator(), false)
					.filter(keyword -> DMSMetadata.get("Keyword").equalsIgnoreCase(keyword.getKeyword().trim().trim()))
					.collect(Collectors.toList());			
			if (matchfound != null && matchfound.size() == 1) {
				this.logger.info("File: "+DMSMetadata.get("Filename")+" has keyword match with details :"+matchfound.toString());
				for (DocSubtypeKeyword docsubtypemetadata : matchfound) {
					DealCopyMap.put("docSubType", docsubtypemetadata.getDoc_subtype().trim());// docSubType
					DealCopyMap.put("welcomePackage",docsubtypemetadata.getInclude_welcomepkg() != null? docsubtypemetadata.getInclude_welcomepkg().trim(): "");
					DealCopyMap.put("finalPackage",docsubtypemetadata.getInclude_tiaapkg() != null	? docsubtypemetadata.getInclude_tiaapkg().trim(): "");
				}
			}
			this.logger.info("dealCopyMap: " + DealCopyMap);			
			return updateService.copyDocument(DealCopyMap);				
		}catch(Exception e) {
			System.out.println(new Date());
			e.printStackTrace();
			logger.error("Failed to insert copy document due to  : " + e.getMessage());
			return "Failed to insert copy document due to  : "+e.getMessage();
		}		
	}
}
