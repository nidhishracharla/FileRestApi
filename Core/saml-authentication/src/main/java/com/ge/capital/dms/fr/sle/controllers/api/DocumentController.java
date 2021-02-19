package com.ge.capital.dms.fr.sle.controllers.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy.Type;
import java.util.HashMap;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxFile;
import com.ge.capital.dms.api.DocumentApi;
import com.ge.capital.dms.fr.sle.config.DecodeSSO;
import com.ge.capital.dms.model.DocumentIVO;
import com.ge.capital.dms.model.DocumentOVO;
import com.ge.capital.dms.service.DocumentService;
import com.ge.capital.dms.service.SelectService;
import com.ge.capital.dms.utility.DmsUtilityConstants;
import com.ge.capital.dms.utility.DmsUtilityService;
import io.swagger.annotations.ApiParam;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2018-08-14T22:22:38.380+05:30")

@CrossOrigin(origins = "*", exposedHeaders = "fileName")
@RestController
@RequestMapping("/secure")

public class DocumentController implements DocumentApi {

	private final Logger log = Logger.getLogger(this.getClass());

	@Value("${download.path}")
	private String DIRECTORY;

	@Autowired
	private ServletContext servletContext;

	@Autowired
	DmsUtilityService dmsUtilityService;

	@Autowired
	DocumentService documentService;

	@Autowired
	DecodeSSO decodeSSO;

	@Autowired
	SelectService selectService;

	@RequestMapping(value = "/documentProperties", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<DocumentOVO> documentPropertiesPost(HttpServletRequest request,
			@ApiParam(value = "", required = true) @Valid @RequestBody DocumentIVO documentIVO) {
		decodeSSO.getDecodedSSO(request.getHeader("loggedinuser"));
		HashMap<String, String> dopPropertiesMap;

		try {
			String docType = documentIVO.getDocType();
			String documentId = documentIVO.getDocId();

			dopPropertiesMap = documentService.getDocumentProperties(docType, documentId);
			DocumentOVO documentOVO = new DocumentOVO();
			documentOVO.setMetadataList(dopPropertiesMap);
			return new ResponseEntity<DocumentOVO>(documentOVO, HttpStatus.OK);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return new ResponseEntity<DocumentOVO>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@RequestMapping("/deleteDoc")
	public ResponseEntity<String> deleteDoc(HttpServletRequest request,
			@ApiParam(value = "", required = true) @Valid @RequestBody String[] document) {
		String loggedinUser = "";
		if (request.getHeader("loggedinuser") != null) {
			loggedinUser = decodeSSO.getDecodedSSO(request.getHeader("loggedinuser"));
		} else {
			loggedinUser = "DMS BATCH UTILITY";
		}
		/*
		 * AkanaToken akanaToken = dmsUtilityService.generateAkanaAccessToken();
		 * JSONObject boxTokenJSON =
		 * dmsUtilityService.generateBoxAccessToken(akanaToken.getAccess_token());
		 * String boxToken = boxTokenJSON.get("accessToken").toString();
		 */
		String boxToken = dmsUtilityService.requestAccessToken();

		if (!boxToken.isEmpty()) {

			BoxAPIConnection api = new BoxAPIConnection(boxToken);
			
//			  java.net.Proxy proxy = new java.net.Proxy(Type.HTTP, new InetSocketAddress(
//			  "PITC-Zscaler-Americas-Cincinnati3PR.proxy.corporate.ge.com", 80));
//			  api.setProxy(proxy);
			 
			for (int i = 0; i < document.length; i++) {
				String[] docDetails = document[i].split(",");
				String[] docType = docDetails[0].split(":");
				String[] docId = docDetails[1].split(":");
				String tempdocId = docId[1].replace('}', ' ').replace('"', ' ').trim();
				String tempDocType = docType[1].replace('}', ' ').replace('"', ' ').trim();
				try {
					BoxFile file = new BoxFile(api, tempdocId);
					file.delete();
					documentService.deleteDoc(tempdocId, tempDocType);
					if(tempDocType.equalsIgnoreCase("dealDoc")) {
						documentService.deleteDocuSignDoc(tempdocId);
					}
					documentService.updateAuditInfo(tempdocId, "deleteDoc", tempDocType, loggedinUser, "SUCCESS",
							"document deleted successfully");
					// return new ResponseEntity<String>("files deleted successfully",
					// HttpStatus.OK);

				} catch (Exception e) {
					documentService.updateAuditInfo(tempdocId, "deleteDoc", tempDocType, loggedinUser, "FAILED",
							"Failed to delete document with status" + HttpStatus.BAD_REQUEST);
					log.error(e.getMessage(), e);
					return new ResponseEntity<String>("File doesnot exist", HttpStatus.BAD_REQUEST);
				}
			}
		}

		return new ResponseEntity<String>("files deleted successfully", HttpStatus.OK);

	}

	@SuppressWarnings("unused")
	@RequestMapping(value = "/downloadDocument", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Resource> documentDownloadPost(HttpServletRequest request,
			@ApiParam(value = "", required = true) @Valid @RequestBody String boxId) {

		BoxFile file = null;
		MediaType mediaType = null;
		File _file = null;
		String fileName = null;
		BoxFile.Info info = null;
		FileOutputStream stream = null;

		InputStreamResource resource = null;
		String loggedinUser = decodeSSO.getDecodedSSO(request.getHeader("loggedinuser"));

		/*
		 * AkanaToken akanaToken = dmsUtilityService.generateAkanaAccessToken();
		 * JSONObject boxTokenJSON =
		 * dmsUtilityService.generateBoxAccessToken(akanaToken.getAccess_token());
		 * String boxToken = boxTokenJSON.get("accessToken").toString();
		 */
		String boxToken = dmsUtilityService.requestAccessToken();
		JSONObject jsonboxId = (JSONObject) JSONValue.parse(boxId);
		String box_Id = jsonboxId.get("boxId").toString();

		log.info("boxToken: " + boxToken);

		Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.applicationConstantsResource);

		if (!boxToken.isEmpty()) {
			BoxAPIConnection api = new BoxAPIConnection(boxToken);
			/*
			 * java.net.Proxy proxy = new java.net.Proxy(Type.HTTP, new InetSocketAddress(
			 * "PITC-Zscaler-Americas-Cincinnati3PR.proxy.corporate.ge.com", 80));
			 * api.setProxy(proxy);
			 */
			file = new BoxFile(api, box_Id);
			info = file.getInfo();
			fileName = info.getName();

			try {
				stream = new FileOutputStream(DIRECTORY + "\\" + fileName);
				file.download(stream);
				_file = new File(DIRECTORY + "\\" + fileName);
				resource = new InputStreamResource(new FileInputStream(_file));

				mediaType = DmsUtilityService.getMediaTypeForFileName(this.servletContext, _file);
				documentService.updateAuditInfo(box_Id, "download", "", loggedinUser, "SUCCESS",
						"user successfully downloaded the document");
			} catch (BoxAPIException e) {
				log.error(e.getMessage(), e);
			} catch (Exception e) {
				documentService.updateAuditInfo(box_Id, "download", "", loggedinUser, "FAILED",
						"user is not able to download the document");
				log.error(e.getMessage(), e);
			} finally {
				try {
					stream.close();

				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
				File dFile = new File(DIRECTORY + "\\" + fileName);
				if (dFile.delete()) {
					log.debug("File deleted from the app server");
				} else {
					log.debug(dFile.getAbsolutePath());
					log.debug("unable to delete the file from app server");
				}
			}

		}
		String finalFileName = selectService.getFileName(info.getID());
		return ResponseEntity.ok()

				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;").header("fileName", finalFileName)
				.contentType(mediaType).contentLength(_file.length()).body(resource);
	}

	

	@Override
	public ResponseEntity<Resource> documentDownloadPost(String boxId) {
		return null;
	}

	@Override
	public ResponseEntity<String> documentUploadPost(MultipartFile files) {
		return null;
	}

	@Override
	public ResponseEntity<DocumentOVO> documentPropertiesPost(DocumentIVO metadata) {
		// TODO Auto-generated method stub
		return null;
	}

}
