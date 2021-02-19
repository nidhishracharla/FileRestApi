package com.ge.capital.dms.fr.sle.controllers.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.ge.capital.dms.dao.DocumentServiceDAO;
import com.ge.capital.dms.fr.sle.config.DecodeSSO;
import com.ge.capital.dms.model.ExportManifestIVO;
import com.ge.capital.dms.service.DocumentService;
import com.ge.capital.dms.service.UpdateService;
import com.ge.capital.dms.utility.DmsUtilityConstants;
import com.ge.capital.dms.utility.DmsUtilityService;
import java.io.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFRow;

/**
 * @author GJ00557822
 */

@CrossOrigin(origins = "*", exposedHeaders = "fileName")
@RestController
@RequestMapping("/secure")
public class ExportManifestController {

	@Value("${upload.path}")
	private String UPLD_DIR;

	@Value("${upload.max.file.count}")
	private String MAX_FILE_CNT;

	@Value("${upload.rootFolderId}")
	private String UPLD_RT_FLDR_ID;

	@Autowired
	private Environment env;

	@Autowired
	UpdateService updateService;

	@Autowired
	DmsUtilityService dmsUtilityService;

	@Autowired
	DocumentServiceDAO documentServiceDAO;

	@Autowired
	DocumentService documentService;

	@Autowired
	DecodeSSO decodeSSO;

	@Value("${download.path}")
	private String DIRECTORY;

	private static final Logger log = Logger.getLogger(ExportManifestController.class);

	@SuppressWarnings({ "unused", "unchecked" })
	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
	@RequestMapping(value = "/exportManifest")
	@ResponseBody
	public ResponseEntity<Resource> exportToExcel(HttpServletRequest httprequest,
			@RequestBody ExportManifestIVO request) throws ParseException {
		String encodedSSO = httprequest.getHeader("loggedinuser");
		String loggedinUser = decodeSSO.getDecodedSSO(encodedSSO);
		String boxId = "";

		String filename = null;
		String sendersName = request.getSenderName();
		String storerNumber = request.getStorerNum();
		String manifestSeqNumber = request.getManifestSeqNum();
		String businessLocation = request.getBusinessLocation();
		String trackingNumber = request.getTrackingNum();
		String sendersBusiness = request.getSenderBusiness();
		String shippingMethod = request.getShippingMethod();
		// String creation_to_dt_searched= null;
		// String creation_frm_dt_searched= null;
		// String mft_modifier_searched= null;
		// String mft_creater_searched= null;

		// Map[] map = new HashMap[];

		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Custom_Report");
		HSSFRow headerRow = sheet.createRow((short) 0);
		headerRow.createCell(0).setCellValue("Sender's Name");
		headerRow.createCell(1).setCellValue("Storer Number");
		headerRow.createCell(2).setCellValue("Shipping Method");
		headerRow.createCell(3).setCellValue("Tracking Number");
		headerRow.createCell(4).setCellValue("Sender Business");
		headerRow.createCell(5).setCellValue("Business Location");
		headerRow.createCell(6).setCellValue("Manifest Sequence No");
		HSSFRow row = sheet.createRow((short) 1);
		row.createCell(0).setCellValue(sendersName);
		row.createCell(1).setCellValue(storerNumber);
		row.createCell(2).setCellValue(shippingMethod);
		row.createCell(3).setCellValue(trackingNumber);
		row.createCell(4).setCellValue(sendersBusiness);
		row.createCell(5).setCellValue(businessLocation);
		row.createCell(6).setCellValue(manifestSeqNumber);

		HSSFRow row2 = sheet.createRow((short) 2);
		HSSFRow row3 = sheet.createRow((short) 3);
		row3.createCell(0).setCellValue("Takedown ID");
		row3.createCell(1).setCellValue("Created By");
		row3.createCell(2).setCellValue("Customer ID");
		row3.createCell(3).setCellValue("Modified By");
		row3.createCell(4).setCellValue("Document Name");
		row3.createCell(5).setCellValue("Creation Date");
		row3.createCell(6).setCellValue("Retention Date");
		row3.createCell(7).setCellValue("Customer Name");
		int n = 4;
		ArrayList<String> obj1 = (ArrayList<String>) request.getManifestInfo();
		for (String jsonStr : obj1) {
			HSSFRow rown = sheet.createRow((short) n);
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(jsonStr);
			for (Object key : json.keySet()) {
				Map<String, String> manifestInfoMap = new HashMap<String, String>();
				String metaKey = (String) key;
				String metaValue = "";
				if (json.get(metaKey) != null) {
					if (json.get(metaKey).getClass() == (java.lang.Long.class)) {
						metaValue = (String) json.get(metaKey).toString();
					} else {
						metaValue = (String) json.get(metaKey);
					}
				}
				manifestInfoMap.put(metaKey, metaValue);
				log.info("Value inside the Manifest Info :Key- " + metaKey + " Value - " + metaValue);

				if (manifestInfoMap.get("takedownId") != null)
					rown.createCell(0).setCellValue(manifestInfoMap.get("takedownId"));
				if (manifestInfoMap.get("createdBy") != null)
					rown.createCell(1).setCellValue(manifestInfoMap.get("createdBy"));
				if (manifestInfoMap.get("customerId") != null)
					rown.createCell(2).setCellValue(manifestInfoMap.get("customerId"));
				if (manifestInfoMap.get("modifiedBy") != null)
					rown.createCell(3).setCellValue(manifestInfoMap.get("modifiedBy"));
				if (manifestInfoMap.get("documentName") != null)
					rown.createCell(4).setCellValue(manifestInfoMap.get("documentName"));
				if (manifestInfoMap.get("creationDate") != null)
					rown.createCell(5).setCellValue(manifestInfoMap.get("creationDate"));
				if (manifestInfoMap.get("retentionDate") != null)
					rown.createCell(6).setCellValue(manifestInfoMap.get("retentionDate"));
				if (manifestInfoMap.get("customerName") != null)
					rown.createCell(7).setCellValue(manifestInfoMap.get("customerName"));
			}
			n++;
		}

		filename = "Manifest_Reports";
		String filename1 = DIRECTORY + "\\" + filename + "_" + manifestSeqNumber + ".xls";
		Properties docIdprops = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docIdMappingResource);
		/*
		 * AkanaToken akanaToken = dmsUtilityService.generateAkanaAccessToken();
		 * JSONObject boxTokenJSON =
		 * dmsUtilityService.generateBoxAccessToken(akanaToken.getAccess_token());
		 * String boxToken = boxTokenJSON.get("accessToken").toString();
		 */
		String boxToken = dmsUtilityService.requestAccessToken();
		try {
			FileOutputStream fileOut = new FileOutputStream(filename1);
			// File f = new File(filename1);
			workbook.write(fileOut);
			log.info("Your excel file has been generated! : " + filename);

			if (!boxToken.isEmpty()) {
				BoxAPIConnection api = new BoxAPIConnection(boxToken);
				/*
				 * java.net.Proxy proxy = new java.net.Proxy(Type.HTTP, new InetSocketAddress(
				 * "PITC-Zscaler-Americas-Cincinnati3PR.proxy.corporate.ge.com", 80));
				 * api.setProxy(proxy);
				 */
				BoxFolder rootFolder = new BoxFolder(api, UPLD_RT_FLDR_ID);
				String folderPath = null;
				folderPath = env.getProperty("upload.folderPath.exportManifest");
				String path[] = folderPath.split("\\\\");

				String fid = rootFolder.getID();
				for (String p : path) {
					BoxFolder folder = new BoxFolder(api, fid);
					for (BoxItem.Info itemInfo : folder) {
						if (itemInfo instanceof BoxFolder.Info && itemInfo.getName().equals(p)) {
							fid = itemInfo.getID();
							break;
						}
					}
				}
				String actualFolderId = fid;
				BoxFolder fldr = new BoxFolder(api, actualFolderId);
				FileInputStream stream = new FileInputStream(filename1);

				BoxFile.Info newFileInfo = fldr.uploadFile(stream, filename + "_" + manifestSeqNumber + ".xls");
				boxId = newFileInfo.getID();
				log.info(newFileInfo.getName() + ":uploaded to box successfully...");
				HashMap<String, String> metadata = new HashMap<String, String>();
				metadata.put("manifestSeqNumber", manifestSeqNumber);
				metadata.put("businessLocation", businessLocation);
				metadata.put("storerNumber", storerNumber);
				metadata.put("sendersName", sendersName);
				metadata.put("docID", newFileInfo.getID());
				metadata.put("docName", filename + "_" + manifestSeqNumber + ".xls");

				/*
				 * metdata.put("mft_creater_searched", mft_creater_searched);
				 * metdata.put("mft_modifier_searched", mft_modifier_searched);
				 * metdata.put("creation_frm_dt_searched", creation_frm_dt_searched);
				 * metdata.put("creation_to_dt_searched", creation_to_dt_searched);
				 */
				metadata.put("mft_creater_searched", loggedinUser);
				metadata.put("mft_modifier_searched", "");
				metadata.put("creation_frm_dt_searched", "");
				metadata.put("creation_to_dt_searched", "");
				metadata.put("ownerName", loggedinUser);
				metadata.put("modifier", loggedinUser);
				metadata.put("creator", loggedinUser);
				metadata.put("modifyDate", "");
				metadata.put("modifyDate", "");
				updateService.updateManifestDoc("exportManifest", metadata);
				documentService.updateAuditInfo(newFileInfo.getID(), "ExportManifest", "Manifest", loggedinUser,
						"SUCCESS", "Exported successfully");
			}
			InputStreamResource resource = null;
			resource = new InputStreamResource(new FileInputStream(filename1));
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;")
					.header("fileName", filename + "_" + manifestSeqNumber + ".xls").body(resource);

		} catch (Exception e) {
			/*
			 * BoxAPIConnection api = new BoxAPIConnection(boxToken); BoxFile boxFile = new
			 * BoxFile(api, boxId);
			 */
			documentService.updateAuditInfo(boxId, "ExportManifest", "Manifest", loggedinUser, "FAILED",
					"Export failed");
			// boxFile.delete();
			// documentServiceDAO.deleteDoc(boxId, "exportManifest");
			log.error(e.getMessage(), e);
			return ResponseEntity.badRequest().header(HttpHeaders.CONTENT_DISPOSITION, "attachment;")
					.body(new InputStreamResource(null, "File Not Found"));
		} finally {
			File file = new File(filename1);
			if (file.delete()) {
				log.info("File deleted from the app server");
			} else {
				log.info("unable to delete the file from app server");
			}
		}

	}
}
