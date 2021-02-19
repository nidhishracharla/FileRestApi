package com.ge.capital.dms.fr.sle.controllers.api;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.net.Proxy.Type;

import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.NullArgumentException;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.box.sdk.BoxItem.Info;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ge.capital.dms.dao.DocumentServiceDAO;
import com.ge.capital.dms.fr.sle.config.DecodeSSO;
import com.ge.capital.dms.model.FileMetadata;

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
 * MultiFileUploadController is defined as REST Service which is used to upload
 * the content i.e single or multiple documents of any type into Ge-box server.
 * Along with the file content it updates the metadata of those uploaded
 * documents into the database.
 *
 * @author PadmaKiran Vajjala & Varun Abbireddy & Gayatri Manvitha
 * @version 1.0
 * @since 2018-11-11
 */

@SessionAttributes("UserLoginDetails")
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/secure")
public class MultiFileUploadController {

	@Value("${upload.path}")
	public String UPLD_DIR;

	@Value("${upload.docstate.final}")
	public String UPLD_DOC_ST_FINAL;

	@Value("${upload.lockboxCashmedia}")
	private String UPLD_DOC_CASH;

	@Value("${upload.lockboxwires}")
	private String UPLD_DOC_WIRES;

	@Value("${upload.lockboxpnc}")
	private String UPLD_DOC_PNC;

	@Value("${upload.reportscash}")
	private String UPLD_DOC_CASH_REPORT;

	@Value("${upload.reportscheck}")
	private String UPLD_DOC_CHECK;

	@Value("${upload.docstate.default}")
	public String UPLD_DOC_ST_DEFAULT;

	@Value("${upload.max.file.count}")
	public String MAX_FILE_CNT;

	@Value("${upload.rootFolderId}")
	private String UPLD_RT_FLDR_ID;

	@Value("${upload.partyFolderId}")
	public String UPLD_PA_FLDR_ID;

	@Value("${upload.accountFolderId}")
	public String UPLD_ACC_FLDR_ID;

	@Value("${upload.opportunityFolderId}")
	public String UPLD_OPP_FLDR_ID;

	@Value("${upload.locFolderId}")
	public String UPLD_LOC_FLDR_ID;

	@Value("${upload.ctrtFolderId}")
	public String UPLD_CTRT_FLDR_ID;

	@Value("${upload.corptaxRootFolderId}")
	private String UPLD_CT_FLDR_ID;

	// Start - Mapping Corptax County Group keys to properties

	@Value("${upload.corptaxAKFolderId}")
	private String UPLD_CT_AK_FLDR_ID;

	@Value("${upload.corptaxALFolderId}")
	private String UPLD_CT_AL_FLDR_ID;

	@Value("${upload.corptaxALFolderId}")
	private String UPLD_CT_AR_FLDR_ID;

	@Value("${upload.corptaxAZFolderId}")
	private String UPLD_CT_AZ_FLDR_ID;

	@Value("${upload.corptaxCAFolderId}")
	private String UPLD_CT_CA_FLDR_ID;

	@Value("${upload.corptaxCOFolderId}")
	private String UPLD_CT_CO_FLDR_ID;

	@Value("${upload.corptaxCTFolderId}")
	private String UPLD_CT_CT_FLDR_ID;

	@Value("${upload.corptaxDCFolderId}")
	private String UPLD_CT_DC_FLDR_ID;

	@Value("${upload.corptaxFLFolderId}")
	private String UPLD_CT_FL_FLDR_ID;

	@Value("${upload.corptaxGAFolderId}")
	private String UPLD_CT_GA_FLDR_ID;

	@Value("${upload.corptaxHIFolderId}")
	private String UPLD_CT_HI_FLDR_ID;

	@Value("${upload.corptaxIAFolderId}")
	private String UPLD_CT_IA_FLDR_ID;

	@Value("${upload.corptaxIDFolderId}")
	private String UPLD_CT_ID_FLDR_ID;

	@Value("${upload.corptaxILFolderId}")
	private String UPLD_CT_IL_FLDR_ID;

	@Value("${upload.corptaxINFolderId}")
	private String UPLD_CT_IN_FLDR_ID;

	@Value("${upload.corptaxKSFolderId}")
	private String UPLD_CT_KS_FLDR_ID;

	@Value("${upload.corptaxKYFolderId}")
	private String UPLD_CT_KY_FLDR_ID;

	@Value("${upload.corptaxLAFolderId}")
	private String UPLD_CT_LA_FLDR_ID;

	@Value("${upload.corptaxMAFolderId}")
	private String UPLD_CT_MA_FLDR_ID;

	@Value("${upload.corptaxMDFolderId}")
	private String UPLD_CT_MD_FLDR_ID;

	@Value("${upload.corptaxMEFolderId}")
	private String UPLD_CT_ME_FLDR_ID;

	@Value("${upload.corptaxMIFolderId}")
	private String UPLD_CT_MI_FLDR_ID;

	@Value("${upload.corptaxMNFolderId}")
	private String UPLD_CT_MN_FLDR_ID;

	@Value("${upload.corptaxMOFolderId}")
	private String UPLD_CT_MO_FLDR_ID;

	@Value("${upload.corptaxMSFolderId}")
	private String UPLD_CT_MS_FLDR_ID;

	@Value("${upload.corptaxMTFolderId}")
	private String UPLD_CT_MT_FLDR_ID;

	@Value("${upload.corptaxNCFolderId}")
	private String UPLD_CT_NC_FLDR_ID;

	@Value("${upload.corptaxNDFolderId}")
	private String UPLD_CT_ND_FLDR_ID;

	@Value("${upload.corptaxNEFolderId}")
	private String UPLD_CT_NE_FLDR_ID;

	@Value("${upload.corptaxNHFolderId}")
	private String UPLD_CT_NH_FLDR_ID;

	@Value("${upload.corptaxNJFolderId}")
	private String UPLD_CT_NJ_FLDR_ID;

	@Value("${upload.corptaxNMFolderId}")
	private String UPLD_CT_NM_FLDR_ID;

	@Value("${upload.corptaxNVFolderId}")
	private String UPLD_CT_NV_FLDR_ID;

	@Value("${upload.corptaxNYFolderId}")
	private String UPLD_CT_NY_FLDR_ID;

	@Value("${upload.corptaxOHFolderId}")
	private String UPLD_CT_OH_FLDR_ID;

	@Value("${upload.corptaxOKFolderId}")
	private String UPLD_CT_OK_FLDR_ID;

	@Value("${upload.corptaxORFolderId}")
	private String UPLD_CT_OR_FLDR_ID;

	@Value("${upload.corptaxPAFolderId}")
	private String UPLD_CT_PA_FLDR_ID;

	@Value("${upload.corptaxRIFolderId}")
	private String UPLD_CT_RI_FLDR_ID;

	@Value("${upload.corptaxSCFolderId}")
	private String UPLD_CT_SC_FLDR_ID;

	@Value("${upload.corptaxSDFolderId}")
	private String UPLD_CT_SD_FLDR_ID;

	@Value("${upload.corptaxTNFolderId}")
	private String UPLD_CT_TN_FLDR_ID;

	@Value("${upload.corptaxTXFolderId}")
	private String UPLD_CT_TX_FLDR_ID;

	@Value("${upload.corptaxUTFolderId}")
	private String UPLD_CT_UT_FLDR_ID;

	@Value("${upload.corptaxVAFolderId}")
	private String UPLD_CT_VA_FLDR_ID;

	@Value("${upload.corptaxVTFolderId}")
	private String UPLD_CT_VT_FLDR_ID;

	@Value("${upload.corptaxWAFolderId}")
	private String UPLD_CT_WA_FLDR_ID;

	@Value("${upload.corptaxWIFolderId}")
	private String UPLD_CT_WI_FLDR_ID;

	@Value("${upload.corptaxWVFolderId}")
	private String UPLD_CT_WV_FLDR_ID;

	@Value("${upload.corptaxWYFolderId}")
	private String UPLD_CT_WY_FLDR_ID;

	@Value("${to.email.address}")
	public String toEmail;

	@Value("${upload.docusign.noMetaData.documents}")
	private String strDocuSignNoMetadataDocuments;

	/*
	 * @Value("${upload.invoiceFolderId}") private String INV_FLDR_ID;
	 */
	// END - Mapping Corptax County Group keys to properties

	@Autowired
	UpdateService updateService;

	@Autowired
	DmsUtilityService dmsUtilityService;

	@Autowired
	EmailNotificationUtil emailNotificationUtil;

	@Autowired
	DocumentServiceDAO documentServiceDAO;

	@Autowired
	UploadContractRepository uploadContractRepository;

	@Autowired
	SelectService opportunityRepo;

	@Autowired
	UploadLOCRepository uploadLOCRepository;

	@Autowired
	HttpSession session;

	@Autowired
	DecodeSSO decodeSSO;

	private static final Logger log = Logger.getLogger(MultiFileUploadController.class);

	@RequestMapping(value = "/uploadFiles", method = RequestMethod.POST)
	public Object handleFileUpload(HttpServletRequest request, @FormDataParam("files") MultipartFile[] files,
			@FormDataParam("docType") String docType, @FormDataParam("fileMetadata") FileMetadata fileMetadata) {
		// boolean respflg = false;
		Properties docIdprops = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docIdMappingResource);
		String nodeId = "";
		String boxId;
		String respMsg = "";
		String Jsonstr = null;
		BoxAPIConnection api = null;
		Map<String, String> statusObj = new HashMap<String, String>();
		List<String> boxFileExistsList = new ArrayList<String>();
		List<String> failureFilesList = new ArrayList<String>();
		List<String> successFilesList = new ArrayList<String>();
		Map<String, String> uploadDataMap = new HashMap<String, String>();
		String boxToken = "";
		String strSignerMail = "";
		String loggedinUser = decodeSSO.getDecodedSSO(request.getHeader("loggedinuser"));
		log.info("Inside the MultiFile Upload Controller :" + docType + " " + fileMetadata);
		try {

			log.info(loggedinUser);

			// UserLoginDetails userloginDetails = (UserLoginDetails)
			// session.getAttribute("UserLoginDetails");
			log.info("Metadata : " + fileMetadata.getUploadMetadata());
			String[] updateMetadataStrArray = fileMetadata.getUploadMetadata().split("},");
			String[] updateMetadataStrArray1 = updateMetadataStrArray;
			for (int i = 0; i < updateMetadataStrArray.length - 1; i++) {
				updateMetadataStrArray[i] = updateMetadataStrArray[i] + "}";
			}
			JSONParser parser = new JSONParser();
			@SuppressWarnings("unused")
			String entityTypeValue = "";
			// START - Storing Upload Filemetadata Values into hashmap
			for (String keyValue : updateMetadataStrArray) {

				JSONObject json = (JSONObject) parser.parse(keyValue);
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
					log.info(metaKey + " " + metaValue);
					if (metaKey.equals("legalEntityType")) {
						entityTypeValue = metaValue;
					}

					uploadDataMap.put(metaKey, metaValue);

				}
			}

			if (files.length != 0) {
				/*
				 * AkanaToken akanaToken = dmsUtilityService.generateAkanaAccessToken();
				 * JSONObject boxTokenJSON =
				 * dmsUtilityService.generateBoxAccessToken(akanaToken.getAccess_token());
				 * String boxToken = boxTokenJSON.get("accessToken").toString();
				 */
				boxToken = dmsUtilityService.requestAccessToken();

				if (!boxToken.isEmpty()) {
					// box connectivity
					api = new BoxAPIConnection(boxToken);
//					java.net.Proxy proxy = new java.net.Proxy(Type.HTTP,
//							new InetSocketAddress("PITC-Zscaler-Americas-Cincinnati3PR.proxy.corporate.ge.com", 80));
//					api.setProxy(proxy);
					
					FileInputStream stream = null;
					String entityTypeNumBoxId = null;

					if (docType != null && docType.equalsIgnoreCase("dealDoc")) {
						if (uploadDataMap.get("legalEntityType").equals("NoMetadata")) {
							BoxFolder DocuSignNoMetadataDocuments = new BoxFolder(api,
									strDocuSignNoMetadataDocuments.trim());
							entityTypeNumBoxId = DocuSignNoMetadataDocuments.getID();
						}
						if (uploadDataMap.get("legalEntityType").equals("Party")) {
							/*
							 * BoxFolder.Info partyFolderInfo = parentFolder1.createFolder("Party10");
							 * entityTypeBoxId = partyFolderInfo.getID();
							 */

							/* BoxFolder partyFolder = new BoxFolder(api, entityTypeBoxId); */
							String subFolderName = null;
							boolean flag = false;
							boolean partyNumFolderExists = false;

							BoxFolder partyfolder = new BoxFolder(api, UPLD_PA_FLDR_ID);

							for (BoxItem.Info itemInfo : partyfolder) {
								if (flag == false) {
									if (itemInfo instanceof BoxFolder.Info) {
										partyNumFolderExists = true;
										subFolderName = (itemInfo.getName());
										BoxFolder.Info partyFolderInfo = (BoxFolder.Info) itemInfo;

										BoxFolder partyNumfolder = new BoxFolder(api, partyFolderInfo.getID());

										if (subFolderName.equals("PA_" + uploadDataMap.get("partyNumber"))) {
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
										.createFolder("PA_" + uploadDataMap.get("partyNumber"));
								entityTypeNumBoxId = partychildFolderInfo.getID();

							}

						}

						if (uploadDataMap.get("legalEntityType").equals("Account")) {

							String subFolderName = null;
							boolean flag = false;
							boolean accNumFolderExists = false;

							BoxFolder accfolder = new BoxFolder(api, UPLD_ACC_FLDR_ID);

							for (BoxItem.Info itemInfo : accfolder) {
								if (flag == false) {
									if (itemInfo instanceof BoxFolder.Info) {
										accNumFolderExists = true;
										subFolderName = (itemInfo.getName());
										BoxFolder.Info partyFolderInfo = (BoxFolder.Info) itemInfo;

										BoxFolder partyNumfolder = new BoxFolder(api, partyFolderInfo.getID());

										if (subFolderName.equals("AC_" + uploadDataMap.get("sfdcAccountId"))) {
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
										.createFolder("AC_" + uploadDataMap.get("sfdcAccountId"));
								entityTypeNumBoxId = accchildFolderInfo.getID();

							}

						}

						if (uploadDataMap.get("legalEntityType").equals("Opportunity")) {

							String subFolderName = null;
							boolean flag = false;
							boolean oppNumFolderExists = false;

							BoxFolder oppfolder = new BoxFolder(api, UPLD_OPP_FLDR_ID);

							for (BoxItem.Info itemInfo : oppfolder) {
								if (flag == false) {
									if (itemInfo instanceof BoxFolder.Info) {
										oppNumFolderExists = true;
										subFolderName = (itemInfo.getName());
										BoxFolder.Info oppFolderInfo = (BoxFolder.Info) itemInfo;

										BoxFolder oppNumfolder = new BoxFolder(api, oppFolderInfo.getID());

										if (subFolderName.equals("OP_" + uploadDataMap.get("sfdcopportunityId"))) {
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
										.createFolder("OP_" + uploadDataMap.get("sfdcopportunityId"));
								entityTypeNumBoxId = oppChildFolderInfo.getID();

							}

						}

						if (uploadDataMap.get("legalEntityType").equals("LOC")) {
							if (uploadDataMap.get("sfdcopportunityId") != null
									|| uploadDataMap.get("sfdcopportunityId") != "") {
								// DB Logic for updating credit number and opportunity ID if provided
								uploadLOCRepository.updateLocOppotunityID(uploadDataMap.get("lineofcreditNumber"),
										uploadDataMap.get("sfdcopportunityId"));
							}

							String subFolderName = null;
							boolean flag = false;
							boolean locNumFolderExists = false;

							BoxFolder locfolder = new BoxFolder(api, UPLD_LOC_FLDR_ID);

							for (BoxItem.Info itemInfo : locfolder) {
								if (flag == false) {
									if (itemInfo instanceof BoxFolder.Info) {
										locNumFolderExists = true;
										subFolderName = (itemInfo.getName());
										BoxFolder.Info locFolderInfo = (BoxFolder.Info) itemInfo;

										BoxFolder locNumfolder = new BoxFolder(api, locFolderInfo.getID());

										if (subFolderName.equals("LC_" + uploadDataMap.get("lineofcreditNumber"))) {
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
										.createFolder("LC_" + uploadDataMap.get("lineofcreditNumber"));
								entityTypeNumBoxId = locChildFolderInfo.getID();

							}

						}

						if (uploadDataMap.get("legalEntityType").equalsIgnoreCase("Contract")) {

							String subFolderName = null;
							boolean flag = false;
							boolean ctrtNumFolderExists = false;

							BoxFolder ctrtfolder = new BoxFolder(api, UPLD_CTRT_FLDR_ID);
							for (BoxItem.Info itemInfo : ctrtfolder) {
								if (flag == false) {
									if (itemInfo instanceof BoxFolder.Info) {
										ctrtNumFolderExists = true;
										subFolderName = (itemInfo.getName());
										BoxFolder.Info ctrtFolderInfo = (BoxFolder.Info) itemInfo;

										BoxFolder ctrtNumfolder = new BoxFolder(api, ctrtFolderInfo.getID());

										if (subFolderName.equals("CT_" + uploadDataMap.get("lwSeqNumber"))) {
											entityTypeNumBoxId = ctrtNumfolder.getID();
											log.info("ContractNumberFolder BoxID is " + entityTypeNumBoxId);
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
										.createFolder("CT_" + uploadDataMap.get("lwSeqNumber").trim());
								entityTypeNumBoxId = partychildFolderInfo.getID();

							}

						}

					}

					if (docType != null && docType.equals("lockbox.cashmedia")) {

						BoxFolder lockBoxFolder = new BoxFolder(api, UPLD_DOC_CASH);

						BoxFolder oppFolder = new BoxFolder(api, lockBoxFolder.getID());
						String folderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
						String finalFolderName = "";
						for (Info bx : oppFolder.getChildren()) {
							if (bx.getName().equalsIgnoreCase(folderName)) {
								finalFolderName = bx.getID();
							}
						}
						if (finalFolderName == "") {
							BoxFolder.Info oppChildFolderInfo = oppFolder.createFolder(folderName);
							entityTypeNumBoxId = oppChildFolderInfo.getID();
						} else {
							entityTypeNumBoxId = finalFolderName;
						}
						log.info("Folder ID :" + entityTypeNumBoxId);
					}
					if (docType.equals("lockbox.wireslb") || docType.equals("lockbox.pnc")
							|| docType.equalsIgnoreCase("Invoices")) {

						if (docType.equals("lockbox.wireslb")) {
							BoxFolder wiresBoxFolder = new BoxFolder(api, UPLD_DOC_WIRES);
							BoxFolder oppFolder = new BoxFolder(api, wiresBoxFolder.getID());

							if (uploadDataMap.get("folderPath") != null) {
								String folderName = uploadDataMap.get("folderPath");
								String finalFolderName = "";
								for (Info bx : oppFolder.getChildren()) {
									if (bx.getName().equalsIgnoreCase(folderName)) {
										finalFolderName = bx.getID();
									}
								}
								if (finalFolderName == "") {
									BoxFolder.Info oppChildFolderInfo = oppFolder.createFolder(folderName);
									entityTypeNumBoxId = oppChildFolderInfo.getID();
								} else {
									entityTypeNumBoxId = finalFolderName;
								}

							} else {
								throw new NullArgumentException("folderPath");
							}

						}
						/*
						 * if (docType.equalsIgnoreCase("Invoices")) { BoxFolder invoicesBoxFolder = new
						 * BoxFolder(api, INV_FLDR_ID); BoxFolder oppFolder = new BoxFolder(api,
						 * invoicesBoxFolder.getID()); String folderName = new
						 * SimpleDateFormat("MM-dd-yyyy").format(new Date()); String finalFolderName =
						 * ""; for (Info bx : oppFolder.getChildren()) { if
						 * (bx.getName().equalsIgnoreCase(folderName)) { finalFolderName = bx.getID(); }
						 * } if (finalFolderName == "") { BoxFolder.Info oppChildFolderInfo =
						 * oppFolder.createFolder(folderName); entityTypeNumBoxId =
						 * oppChildFolderInfo.getID(); } else { entityTypeNumBoxId = finalFolderName; }
						 * }
						 */
						if (docType.equals("lockbox.pnc")) {
							BoxFolder pncBoxFolder = new BoxFolder(api, UPLD_DOC_PNC);
							BoxFolder oppFolder = new BoxFolder(api, pncBoxFolder.getID());

							if (uploadDataMap.get("folderPath") != null) {
								String folderName = uploadDataMap.get("folderPath");
								String finalFolderName = "";
								for (Info bx : oppFolder.getChildren()) {
									if (bx.getName().equalsIgnoreCase(folderName)) {
										finalFolderName = bx.getID();
									}
								}
								if (finalFolderName == "") {
									BoxFolder.Info oppChildFolderInfo = oppFolder.createFolder(folderName);
									entityTypeNumBoxId = oppChildFolderInfo.getID();
								} else {
									entityTypeNumBoxId = finalFolderName;
								}

							} else {
								throw new NullArgumentException("folderPath");
							}

						}

					}

					if (docType.equals("reports.cash") || docType.equals("reports.check")) {
						String id = "";
						if (docType.equals("reports.cash")) {
							id = UPLD_DOC_CASH_REPORT;
						} else if (docType.equals("reports.check")) {
							id = UPLD_DOC_CHECK;
						}
						BoxFolder repportsBoxFolder = new BoxFolder(api, id);
						BoxFolder oppFolder = new BoxFolder(api, repportsBoxFolder.getID());

						if (uploadDataMap.get("folderPath") != null) {
							String[] folderName = uploadDataMap.get("folderPath").split("/");
							String finalFolderName = "";
							String finalFolderName1 = "";
							for (Info bx : oppFolder.getChildren()) {
								if (bx.getName().equalsIgnoreCase(folderName[0])) {
									finalFolderName = bx.getID();
								}
							}
							if (!finalFolderName.isEmpty()) {
								BoxFolder oppFolder1 = new BoxFolder(api, finalFolderName);
								for (Info bx : oppFolder1.getChildren()) {
									if (bx.getName().equalsIgnoreCase(folderName[1])) {
										finalFolderName1 = bx.getID();
									}
								}
								if (finalFolderName1.isEmpty()) {
									BoxFolder childFolder = new BoxFolder(api, finalFolderName);
									BoxFolder.Info childFolderInfo = childFolder.createFolder(folderName[1]);
									finalFolderName1 = childFolderInfo.getID();
								}
								entityTypeNumBoxId = finalFolderName1;
							}
							if (finalFolderName == "") {
								BoxFolder.Info oppChildFolderInfo = oppFolder.createFolder(folderName[0]);
								BoxFolder childFolder = new BoxFolder(api, oppChildFolderInfo.getID());
								BoxFolder.Info childFolderInfo = childFolder.createFolder(folderName[1]);
								entityTypeNumBoxId = childFolderInfo.getID();

							}

						} else {
							throw new NullArgumentException("folderPath");
						}
					}

					if (docType != null && docType.equals("corptax")) {
						String subFolderName = null;
						boolean flag = false;
						boolean countynameFolderExists = false;

						BoxFolder copTaxfolder = getCorpTaxCGUploadFolder(uploadDataMap, api);

						for (BoxItem.Info itemInfo : copTaxfolder) {

							if (flag == false) {
								if (itemInfo instanceof BoxFolder.Info) {
									countynameFolderExists = true;
									subFolderName = (itemInfo.getName());
									BoxFolder.Info oppFolderInfo = (BoxFolder.Info) itemInfo;

									BoxFolder oppNumfolder = new BoxFolder(api, oppFolderInfo.getID());

									if (subFolderName.equals(uploadDataMap.get("ctCountyName"))) {
										entityTypeNumBoxId = oppNumfolder.getID();
										log.info("Countyname BoxID is " + entityTypeNumBoxId);
										flag = true;
										break;
									} else {
										countynameFolderExists = false;
									}

								}
							}
						}

						if (countynameFolderExists == false) {
							BoxFolder oppFolder = new BoxFolder(api, copTaxfolder.getID());
							BoxFolder.Info oppChildFolderInfo = oppFolder
									.createFolder(uploadDataMap.get("ctCountyName"));
							entityTypeNumBoxId = oppChildFolderInfo.getID();
							log.info("Countyname BoxID is " + entityTypeNumBoxId);
						}

					}

					// END
					int indx = 0;
					String entityID = entityTypeNumBoxId;

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
										if (count >= Integer.parseInt(MAX_FILE_CNT)) {
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

							if (subFolderExists == false) {
								if (!(docType.contains("lockbox") || docType.contains("reports")
										|| docType.equals("Invoices"))) {
									BoxFolder parentFolder = new BoxFolder(api, fldr.getID());
									subFolderNum += 1;
									BoxFolder.Info childFolderInfo = parentFolder
											.createFolder(Integer.toString(subFolderNum));
									subFolderId = childFolderInfo.getID();
								}
							}

							if (docType.contains("reports") || docType.equalsIgnoreCase("lockbox.cashmedia")
									|| docType.equalsIgnoreCase("lockbox.wireslb")
									|| docType.equalsIgnoreCase("lockbox.pnc") || docType.equalsIgnoreCase("invoice")) {
								log.info("Folder ID " + subFolderId);
								subFolderId = entityID;
							}

							BoxFolder uploadFolder = new BoxFolder(api, subFolderId);
							log.info(multipartFile.getOriginalFilename() + ": " + multipartFile.getContentType());
							file = new File(UPLD_DIR + "\\"
									+ java.nio.file.Paths.get(multipartFile.getOriginalFilename()).getFileName());

							multipartFile.transferTo(file);
							stream = new FileInputStream(file);
							int uploadCounter = 0;
							log.info(api + " : Api With boxtoken : " + boxToken);
							BoxFile.Info newFileInfo = null;
							try {
								// BoxFile fil = new BoxFile(api,""); //existing doc
								// fil.uploadVersion(stream);
								newFileInfo = uploadFolder.uploadFile(stream, String.valueOf(
										java.nio.file.Paths.get(multipartFile.getOriginalFilename()).getFileName()));
							} catch (BoxAPIException e) {
								if (((e.getResponseCode() == CommonConstants.BOX_API_ERROR_0)
										|| (e.getResponseCode() == CommonConstants.BOX_API_ERROR_2)
										|| (e.getResponseCode() == CommonConstants.BOX_API_ERROR_3)
										|| (e.getResponseCode() == CommonConstants.BOX_API_ERROR_4)
										|| (e.getMessage()
												.contains("Couldn't connect to the Box API due to a network error.")))
										&& (uploadCounter < 1)) {
									log.info("Exception Occured :" + e.getResponseCode() + " , Retrying Upload....");
									Thread.sleep(10000);
									newFileInfo = uploadFolder.uploadFile(stream, String.valueOf(java.nio.file.Paths
											.get(multipartFile.getOriginalFilename()).getFileName()));
									uploadCounter++;
								} else {
									throw new BoxAPIException(e.getMessage(), e.getResponseCode(), e.getResponse());
								}
							} catch (Exception e) {
								throw new Exception(e);
							}
							boxId = newFileInfo.getID();

							nodeId = newFileInfo.getID();
							log.info(newFileInfo.getName() + ":uploaded to box successfully, Document ID : " + nodeId);

							Map<String, String> documentDetails = new HashMap<String, String>();
							// update specific docType metadata in commonDoc
							Map<String, String> metadataMap = new HashMap<String, String>();
							ObjectMapper mapperMetadata = new ObjectMapper();
							metadataMap = mapperMetadata.readValue(updateMetadataStrArray[indx].concat("}"),
									new TypeReference<HashMap<String, String>>() {
									});
							documentDetails.put("environment", "UAT"); // Environment
							documentDetails.put("docId", boxId);
							documentDetails.put("fileSize", String.valueOf(fileSize / 1024) + " KB");
							documentDetails.put("docVersionId", newFileInfo.getVersion().getVersionID());
							documentDetails.put("docName", newFileInfo.getName());
							documentDetails.put("docTitle", newFileInfo.getName());
							// metadata for commondoc specific for Invoices
							if (docType.equals("Invoices")) {
								documentDetails.put("docType", docType);
								documentDetails.put("docSource", "Leasewave");
								documentDetails.put("retentionDate", "");
								documentDetails.put("isMigrated", "");
							} else if (docType.equals("lockbox.wireslb")) {
								documentDetails.put("docType", "lockbox.wireslb");
								documentDetails.put("isMigrated", "");
								documentDetails.put("docSource", "");
								documentDetails.put("retentionDate", "");
							} else if (docType.equals("lockbox.pnc")) {
								documentDetails.put("docType", "lockbox.pnc");
								documentDetails.put("isMigrated", "");
								documentDetails.put("docSource", "");
								documentDetails.put("retentionDate", "");
							} else if (docType.equals("lockbox.cashmedia")) {

								documentDetails.put("docType", "lockbox");
								documentDetails.put("isMigrated", "");
								documentDetails.put("docSource", "");
								documentDetails.put("retentionDate", "");

							} else if (docType.equals("reports.cash") || docType.equals("reports.check")) {
								documentDetails.put("docType", "reports");
								documentDetails.put("isMigrated",
										metadataMap.get(docIdprops.getProperty(docType + ".isMigrated")));
								documentDetails.put("docSource",
										metadataMap.get(docIdprops.getProperty(docType + ".docSource")));
								documentDetails.put("retentionDate", metadataMap
										.get(docIdprops.getProperty(docType + ".retentionDate")).replace('T', ' '));
							} else {
								documentDetails.put("docType", docType);
								documentDetails.put("isMigrated", "");
								documentDetails.put("docSource", uploadDataMap.get("sourceSystem"));
								documentDetails.put("retentionDate", "");
							}

							documentDetails.put("mimeType", multipartFile.getContentType());
							documentDetails.put("permName", "");
							documentDetails.put("realmName", "");

							if (loggedinUser != null)
								documentDetails.put("ownerName", loggedinUser);
							else
								documentDetails.put("ownerName", "HEF_DMS_USER");

							documentDetails.put("createDate",
									(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(newFileInfo.getCreatedAt())));
							if (loggedinUser != null) {
								documentDetails.put("creator", loggedinUser);
								uploadDataMap.put("creator", loggedinUser);
							} else
								documentDetails.put("creator", "HEF_DMS_USER");

							documentDetails.put("modifyDate",
									(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(newFileInfo.getModifiedAt())));

							if (loggedinUser != null)
								documentDetails.put("modifier", loggedinUser);
							else
								documentDetails.put("modifier", "HEF_DMS_USER");

							documentDetails.put("isDeleted", "");
							documentDetails.put("isCurrent", "");
							documentDetails.put("versionNum", newFileInfo.getVersionNumber());

							if (uploadDataMap.get("legalEntityType") != null) {
								if (uploadDataMap.get("legalEntityType").equalsIgnoreCase("Contract")) {
									String ctrtNumber = uploadDataMap.get("lwSeqNumber");

									// contract number lookup for checking contract commencement
									Integer uploadctrtCount = uploadContractRepository.getCount(ctrtNumber);

									if (uploadctrtCount > 0) {
										documentDetails.put("docState", UPLD_DOC_ST_FINAL);
									} else
										documentDetails.put("docState", UPLD_DOC_ST_DEFAULT);
								}

								if (!(uploadDataMap.get("legalEntityType").equalsIgnoreCase("Contract"))) {
									documentDetails.put("docState", UPLD_DOC_ST_DEFAULT);
								}
								documentDetails.put("contentRef", "");
								documentDetails.put("isLocked", "");
								documentDetails.put("folderRef", "");
							}
							if (null != uploadDataMap.get("legalEntityType")
									&& uploadDataMap.get("legalEntityType").equals("NoMetadata")) {
								documentDetails.put("docState", "NoMetadata");
							}
							HashMap<String, String> dealCopyMap = new HashMap<String, String>();
							if (  uploadDataMap.get("sourceSystem") !=null && uploadDataMap.get("sourceSystem").equalsIgnoreCase("DocuSign")) {
								if (uploadDataMap.get("legalEntityType").equalsIgnoreCase("Party")
										|| uploadDataMap.get("legalEntityType").equalsIgnoreCase("Account")
										|| uploadDataMap.get("legalEntityType").equalsIgnoreCase("Opportunity")
										|| uploadDataMap.get("legalEntityType").equalsIgnoreCase("LOC")) {
									try {
										uploadDataMap.put("docId", boxId);
										String enType = uploadDataMap.get("legalEntityType");
										uploadDataMap.put("legalEntityType", "Contract");
										dealCopyMap.putAll(uploadDataMap);
										uploadDataMap.put("legalEntityType", enType);
									} catch (Exception e) {
										log.error(e.getMessage(), e);
									}
								}
							}

							updateService.updateDocumentMetadata(docType, documentDetails, loggedinUser);//commondoc

							// metadata update for Party doc upload
							if (docType.equals(docIdprops.getProperty(docType + ".multi.upload"))) {
								String tableName = docIdprops.getProperty(docType + ".multi.upload.table");
								Map<String, String> updateParams = new HashMap<String, String>();
								ObjectMapper mapper = new ObjectMapper();

								// convert JSON string to Map
								updateParams = mapper.readValue(updateMetadataStrArray1[indx].concat("}"),
										new TypeReference<HashMap<String, String>>() {
										});
								updateParams.put("docId", boxId);
								updateParams.entrySet().removeIf(entry -> "fileName".equalsIgnoreCase(entry.getKey()));
								updateParams.entrySet()
										.removeIf(entry -> "SignedUserEmail".equalsIgnoreCase(entry.getKey()));
								updateParams.entrySet()
										.removeIf(entry -> "CustomerNumber".equalsIgnoreCase(entry.getKey()));
								updateParams.entrySet()
										.removeIf(entry -> "DocuSignEnv".equalsIgnoreCase(entry.getKey()));
								updateParams.entrySet()
										.removeIf(entry -> "EnvelopeID".equalsIgnoreCase(entry.getKey()));
								// setting physicalStorageNotSent
								if (docType.equals("dealDoc")) {
									if (updateParams.get("physicalStorageStatus") == null
											|| updateParams.get("physicalStorageStatus").equals("0")) {
										updateParams.put("physicalStorageNotSent", "0");
									} else if (updateParams.get("physicalStorageStatus").equals("1")) {
										updateParams.put("physicalStorageNotSent", "1");
									}

									updateParams.entrySet()
											.removeIf(entry -> "sourceSystem".equalsIgnoreCase(entry.getKey()));
								}
								if (docType.equals("corptax")) {
									updateParams.remove("legalEntityType");
									updateParams.remove("sourceSystem");
									updateParams.put("documentId", boxId);
									updateParams.remove("docId");
								}
								if (docType.equals("Invoices")) {
									updateParams.put("documentId", boxId);
									updateParams.remove("docId");
								}
								if (docType.equals("lockbox.cashmedia")) {
									updateParams.remove("legalEntityType");
									updateParams.remove("sourceSystem");
									updateParams.put("documentId", boxId);
									updateParams.remove("docId");
									if (updateParams.get("gecap_date_loaded") != null) {
										updateParams.put("gecap_date_loaded",
												updateParams.get("gecap_date_loaded").concat(" 00:00:00"));
									}
								}
								if (docType.equals("lockbox.pnc")) {
									updateParams.remove("legalEntityType");
									updateParams.remove("sourceSystem");
									updateParams.put("documentId", boxId);
									updateParams.remove("docId");
									if (updateParams.get("folderPath") != null) {
										updateParams.put("date_loaded",
												updateParams.get("folderPath").concat(" 00:00:00"));
									}
									updateParams.put("cash_archival_type", "PNC Lockbox");
									updateParams.remove("folderPath");
									updateParams.remove("docType");

								}
								if (docType.equals("lockbox.wireslb")) {
									updateParams.remove("legalEntityType");
									updateParams.remove("sourceSystem");
									updateParams.put("documentId", boxId);
									updateParams.remove("docId");
									if (updateParams.get("folderPath") != null) {
										updateParams.put("date_loaded",
												updateParams.get("folderPath").concat(" 00:00:00"));
									}
									updateParams.put("cash_archival_type", "Wires LB");
									updateParams.remove("folderPath");
									updateParams.remove("docType");
								}
								if (docType.equals("reports.cash") || docType.equals("reports.check")) {
									updateParams.put("gecap_reportRunDate",
											updateParams.get("gecap_reportRunDate").replace('T', ' '));
									updateParams.put("gecap_reportDate",
											updateParams.get("gecap_reportDate").replace('T', ' '));
									updateParams.entrySet().removeIf(entry -> docIdprops
											.getProperty(docType + ".isMigrated").equalsIgnoreCase(entry.getKey()));
									updateParams.entrySet().removeIf(entry -> docIdprops
											.getProperty(docType + ".docSource").equalsIgnoreCase(entry.getKey()));
									updateParams.entrySet().removeIf(entry -> docIdprops
											.getProperty(docType + ".retentionDate").equalsIgnoreCase(entry.getKey()));
									updateParams.remove("legalEntityType");
									updateParams.remove("folderPath");
									updateParams.put("documentId", boxId);
									updateParams.remove("docId");
								}
								// added by Gayatri - Aug - 14th- For Integration 4
								String opportunityId = "";
								Integer uploadOpportunityCount = 0;
								if (null != (uploadDataMap.get("legalEntityType"))
										&& (uploadDataMap.get("legalEntityType").equalsIgnoreCase("Opportunity"))) {
									if (uploadDataMap.get("sfdcopportunityId") != null
											|| !(uploadDataMap.get("sfdcopportunityId").isEmpty()))
										opportunityId = uploadDataMap.get("sfdcopportunityId");
									if (!(null == opportunityId) && !("".equals(opportunityId)))
										uploadOpportunityCount = uploadContractRepository
												.getCountOpportunity(opportunityId);
									if (uploadOpportunityCount > 0) {
										updateParams.put("partyName",
												opportunityRepo.getData(opportunityId, "party_name"));
										updateParams.put("partyNumber",
												opportunityRepo.getData(opportunityId, "party_number"));
										updateParams.put("lineofcreditNumber",
												opportunityRepo.getData(opportunityId, "credit_number"));
									}
								}
								if (null != (uploadDataMap.get("legalEntityType"))
										&& (uploadDataMap.get("legalEntityType").equalsIgnoreCase("Party"))) {
									updateParams.entrySet()
											.removeIf(entry -> "lwSeqNumber".equalsIgnoreCase(entry.getKey()));
									updateParams.entrySet()
											.removeIf(entry -> "sfdcopportunityId".equalsIgnoreCase(entry.getKey()));
									updateParams.entrySet()
											.removeIf(entry -> "sfdcAccountId".equalsIgnoreCase(entry.getKey()));
									updateParams.entrySet()
											.removeIf(entry -> "contractDealtype".equalsIgnoreCase(entry.getKey()));
									updateParams.entrySet()
											.removeIf(entry -> "lineofcreditNumber".equalsIgnoreCase(entry.getKey()));
								}
								updateService.updateDocumentTypeMetadata(docType, tableName, updateParams);//update table based on the doctype
								
								if (!dealCopyMap.isEmpty())
								updateService.copyDocument(dealCopyMap);
								indx += 1;
							}

							// Added by Gayatri- july 5th 2019
							statusObj.put("code", "200");
							statusObj.put("nodeId", nodeId);
							statusObj.put("message", "Document and metadata updated succesfully");
							successFilesList.add(String.valueOf(
									java.nio.file.Paths.get(multipartFile.getOriginalFilename()).getFileName()));
							if (null != uploadDataMap.get("legalEntityType")
									&& uploadDataMap.get("legalEntityType").equals("NoMetadata")) {
								uploadDataMap.put("FileName", multipartFile.getOriginalFilename());
								emailNotificationUtil.NoMetaDataMailAlertforDocuSign(uploadDataMap);
								// log.info("***** EmailSent to User : " + loggedinUser + "@ge.com");
								// //String param=
								// uploadDataMap.get("lwSeqNumber").equalsIgnoreCase("NA")?"sfdcopportunityId":"lwSeqNumber";
								// //String paramvalue= uploadDataMap.get(param);
								// this.log.info("uploadDataMap.get(\"lwSeqNumber\")"+uploadDataMap.get("lwSeqNumber"));
								// this.log.info("sfdcopportunityId: "+uploadDataMap.get("sfdcopportunityId"));
								// this.log.info("CustomerNumber"+uploadDataMap.get("CustomerNumber"));
								// this.log.info("EnvelopeID"+uploadDataMap.get("EnvelopeID"));
								// this.log.info("DocuSignEnv"+uploadDataMap.get("DocuSignEnv"));
								//
								// String EmailSubject= "<html><p>Following document is pulled from Docusign to
								// DMS. But DMS System could not find a valid Sequence number or Opportunity Id
								// to associate the Signed document. Please verify:</p>"
								// + " <table width='100%' border='1' align='center'
								// style='border-collapse:collapse;display:inline-table'><tr align='center'
								// style='background-color:darkseagreen;font-weight:100'>"
								// + "<td>Sequence Number:</td>"
								// + "<td>Opportunity ID</td>"
								// + "<td>Customer Number</td>"
								// + "<td>Docusign Envelope ID</td>"
								// + "<td>Name of the Document</td"
								// + "><td>DMS Environment</td>"
								// + "<td>DocuSign Environment</td>"
								// + "</tr> "
								// + "<tr align='center'>"
								// + "<td>"+ uploadDataMap.get("lwSeqNumber") + "</td>"
								// + "<td>" + uploadDataMap.get("sfdcopportunityId")+ "</td>"
								// + "<td>" + uploadDataMap.get("CustomerNumber") + "</td>"
								// + "<td>"+ uploadDataMap.get("EnvelopeID") + "</td>"
								// + "<td>" + multipartFile.getOriginalFilename()+ "</td>"
								// + "<td>" + "UAT" + "</td>"
								// + "<td>" + uploadDataMap.get("DocuSignEnv")+ "</td>"
								// + "</tr> "
								// + "</table><br/>"
								// + "<p><b>Note:</b>This is a System Generated email, Please contact
								// HFSDocuSign.Support@ge.com for any queries </p>"
								// + "</html>";
								//
								// emailNotificationUtil.emailWithoutAttachments(200, EmailSubject,
								// uploadDataMap.get("SignedUserEmail"));
							}

							// ends here
						} catch (BoxAPIException boxAPIException) {
							respMsg = "BoxAPI Exception : " + boxAPIException.getResponseCode();
							if (boxAPIException.getResponseCode() == CommonConstants.FILE_ALREADY_EXISTS) {
								boxFileExistsList.add(String.valueOf(
										java.nio.file.Paths.get(multipartFile.getOriginalFilename()).getFileName()));
								respMsg = "";
								// if (uploadDataMap.get("legalEntityType").equals("NoMetadata")) {
								// log.info("***** EmailSent to User : " + loggedinUser + "@ge.com");
								//
								// String param=
								// uploadDataMap.get("lwSeqNumber").equalsIgnoreCase("NA")?"sfdcopportunityId
								// ":"lwSeqNumber";
								// String paramvalue= uploadDataMap.get(param);
								// emailNotificationUtil.emailWithoutAttachments(200,
								// "MetaData Does not exist for the "+param+": "+ paramvalue+" for the document
								// and "
								// + multipartFile.getOriginalFilename() + " document already exists in the DMS
								// System",
								// uploadDataMap.get("SignedUserEmail"));
								// }

								if (null != uploadDataMap.get("legalEntityType")
										&& uploadDataMap.get("legalEntityType").equals("NoMetadata")) {
									uploadDataMap.put("FileName", multipartFile.getOriginalFilename());
									emailNotificationUtil.NoMetaDataMailAlertforDocuSign(uploadDataMap);
									// log.info("***** EmailSent to User : " + loggedinUser + "@ge.com");
									// //String param=
									// uploadDataMap.get("lwSeqNumber").equalsIgnoreCase("NA")?"sfdcopportunityId":"lwSeqNumber";
									// //String paramvalue= uploadDataMap.get(param);
									// this.log.info("uploadDataMap.get(\"lwSeqNumber\")"+uploadDataMap.get("lwSeqNumber"));
									// this.log.info("sfdcopportunityId: "+uploadDataMap.get("sfdcopportunityId"));
									// this.log.info("CustomerNumber"+uploadDataMap.get("CustomerNumber"));
									// this.log.info("EnvelopeID"+uploadDataMap.get("EnvelopeID"));
									// this.log.info("DocuSignEnv"+uploadDataMap.get("DocuSignEnv"));
									// String EmailSubject= "<html><p>Following document is pulled from Docusign to
									// DMS. But DMS System could not find a valid Sequence number or Opportunity Id
									// to associate the Signed document. Please verify:</p>"
									// + " <table width='100%' border='1' align='center'
									// style='border-collapse:collapse;display:inline-table'><tr align='center'
									// style='background-color:darkseagreen;font-weight:100'>"
									// + "<td>Sequence Number:</td>"
									// + "<td>Opportunity ID</td>"
									// + "<td>Customer Number</td>"
									// + "<td>Docusign Envelope ID</td>"
									// + "<td>Name of the Document</td"
									// + "><td>DMS Environment</td>"
									// + "<td>DocuSign Environment</td>"
									// + "</tr> "
									// + "<tr align='center'>"
									// + "<td>"+ uploadDataMap.get("lwSeqNumber") + "</td>"
									// + "<td>" + uploadDataMap.get("sfdcopportunityId")+ "</td>"
									// + "<td>" + uploadDataMap.get("CustomerNumber") + "</td>"
									// + "<td>"+ uploadDataMap.get("EnvelopeID") + "</td>"
									// + "<td>" + multipartFile.getOriginalFilename()+ "</td>"
									// + "<td>" + "UAT" + "</td>"
									// + "<td>" + uploadDataMap.get("DocuSignEnv")+ "</td>"
									// + "</tr> "
									// + "</table><br/>"
									// + "<p><b>Note:</b>This is a System Generated email, Please contact
									// HFSDocuSign.Support@ge.com for any queries </p>"
									// + "</html>";
									//
									// emailNotificationUtil.emailWithoutAttachments(200, EmailSubject,
									// uploadDataMap.get("SignedUserEmail"));
								}
							} else {
								failureFilesList.add(String.valueOf(
										java.nio.file.Paths.get(multipartFile.getOriginalFilename()).getFileName()));
								log.error(boxAPIException.getMessage(), boxAPIException);
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
								log.info("The document deleted is:" + boxId);
							}
							failureFilesList.add(String.valueOf(
									java.nio.file.Paths.get(multipartFile.getOriginalFilename()).getFileName()));
							statusObj.put("code", "400");
							statusObj.put("nodeId", nodeId);
							statusObj.put("message", e.getMessage());

							log.error(e.getMessage(), e);
						} catch (Exception e) {
							respMsg = "";
							failureFilesList.add(String.valueOf(
									java.nio.file.Paths.get(multipartFile.getOriginalFilename()).getFileName()));
							if (!((null == api)) && !((null == boxId) || ("".equals(boxId)))) {
								BoxFile deleteFile = new BoxFile(api, boxId);
								deleteFile.delete();
								documentServiceDAO.deleteDoc(boxId, docType);
								documentServiceDAO.deleteFromTable(boxId, docType);
								log.info("The document deleted is: " + boxId);
							}
							log.error(e.getMessage(), e);
							statusObj.put("code", "400");
							statusObj.put("nodeId", nodeId);
							statusObj.put("message", e.getMessage());
						} finally {
							try {
								if (stream != null)
									stream.close();
							} catch (Exception e) {
								log.error(e.getMessage(), e);
							}
						}

					}

				}

			}

			if (docType != null) {
				if (docType.equalsIgnoreCase("lockbox.pnc") || docType.equalsIgnoreCase("lockbox.wireslb")
						|| docType.equalsIgnoreCase("reports.cash") || docType.equalsIgnoreCase("reports.check")
						|| docType.equalsIgnoreCase("Invoices")) {
					JSONObject metaJson = new JSONObject(statusObj);
					log.info(metaJson);
					return metaJson.toString();
				}
			}

		} catch (BoxAPIException boxAPIException) {
			log.error(boxAPIException.getMessage(), boxAPIException);
			respMsg = "BoxAPI Exception : " + boxAPIException.getResponseCode();
			if (boxAPIException.getResponseCode() == CommonConstants.FILE_ALREADY_EXISTS) {
				for (int i = 0; i < files.length; i++)
					boxFileExistsList.add(files[i].getOriginalFilename());
			} else {
				log.error(boxAPIException.getMessage(), boxAPIException);
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
			log.error(ex.getMessage(), ex);
		} catch (Exception ex) {
			respMsg = "";
			log.error(ex.getMessage(), ex);
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
			respMsg = " file/(s) are uploaded to box successfully";
		}

		if (!failureFilesList.isEmpty()) {
			int i = 1;
			respMsg = respMsg + " Failed to upload the file(s): \n";
			for (String fileFailed : failureFilesList) {
				respMsg += String.valueOf(i) + ". " + "'" + fileFailed + "',\n ";
				i++;
			}
			emailNotificationUtil.sendEmailAlert(UPLD_DIR, failureFilesList,
					"User : " + loggedinUser + "\n Documents with Metadata : " + fileMetadata.getUploadMetadata(),
					respMsg, toEmail, "Upload");
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
		log.info("Message Sent to User : " + respMsg);
		Gson g = new Gson();
		Jsonstr = g.toJson(respMsg);
		return Jsonstr;

	}

	public BoxFolder getCorpTaxCGUploadFolder(Map<String, String> uploadDataMap, BoxAPIConnection api) {

		BoxFolder copTaxfolder = null;

		if (uploadDataMap.get("ctState").equals("AK")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_AK_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("AL")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_AL_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("AR")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_AR_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("AZ")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_AZ_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("CA")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_CA_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("CO")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_CO_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("CT")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_CT_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("DC")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_DC_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("FL")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_FL_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("GA")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_GA_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("HI")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_HI_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("IA")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_IA_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("ID")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_ID_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("IL")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_IL_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("IN")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_IN_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("KS")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_KS_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("KY")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_KY_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("LA")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_LA_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("MA")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_MA_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("MD")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_MD_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("ME")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_ME_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("MI")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_MI_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("MN")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_MN_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("MO")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_MO_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("MS")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_MS_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("MT")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_MT_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("NC")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_NC_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("ND")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_ND_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("NE")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_NE_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("NH")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_NH_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("NJ")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_NJ_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("NM")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_NM_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("NV")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_NV_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("NY")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_NY_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("OH")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_OH_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("OK")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_OK_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("OR")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_OR_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("PA")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_PA_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("RI")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_RI_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("SC")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_SC_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("SD")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_SD_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("TN")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_TN_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("TX")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_TX_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("UT")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_UT_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("VA")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_VA_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("VT")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_VT_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("WA")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_WA_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("WI")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_WI_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("WV")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_WV_FLDR_ID);
		}

		if (uploadDataMap.get("ctState").equals("WY")) {
			copTaxfolder = new BoxFolder(api, UPLD_CT_WY_FLDR_ID);
		}

		return copTaxfolder;
	}
}
