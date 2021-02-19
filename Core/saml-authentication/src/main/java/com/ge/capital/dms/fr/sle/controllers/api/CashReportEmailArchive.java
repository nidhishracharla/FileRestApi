package com.ge.capital.dms.fr.sle.controllers.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import com.box.sdk.BoxItem.Info;
import com.ge.capital.dms.fr.sle.config.DecodeSSO;
import com.ge.capital.dms.service.SelectService;
import com.ge.capital.dms.service.UpdateService;
import com.ge.capital.dms.utility.DmsUtilityService;
import com.google.gson.Gson;

/**
 * @author GJ00557822
 */
@CrossOrigin(origins = "*", exposedHeaders = "fileName")
@RestController
@RequestMapping("/secure")
public class CashReportEmailArchive {
	private static final Logger log = Logger.getLogger(CashReportEmailArchive.class);

	@Value("${download.path}")
	private String DIRECTORY;

	@Autowired
	SelectService selectService;

	@Autowired
	UpdateService updateService;

	@Autowired
	DmsUtilityService dmsUtilityService;

	@Autowired
	DecodeSSO decodeSSO;

	@SuppressWarnings("unchecked")
	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
	@RequestMapping(value = "/cashEmailArchive")
	@ResponseBody
	public String cashReportArchive(HttpServletRequest httprequest, @RequestBody String folderId)
			throws IOException, ParseException {
		String response = "";
		String boxToken = dmsUtilityService.requestAccessToken();
		BoxAPIConnection api = new BoxAPIConnection(boxToken);

		BoxFolder mainParent = new BoxFolder(api, folderId);
		// log.info("Processing Folder : " + folderId);
		if (!mainParent.getInfo().getName().contains("Done")) {
			Info info = mainParent.getInfo();
			Map<String, BoxItem.Info> fileIdInfos = new HashMap<String, BoxItem.Info>();
			Map<String, String> fileIdNames = new HashMap<String, String>();
			List<HashMap<String, String>> metdataArray = new ArrayList<HashMap<String, String>>();
			for (BoxItem.Info childInfo : mainParent.getChildren()) {
				if (childInfo instanceof BoxFile.Info) {
					fileIdInfos.put(childInfo.getID(), childInfo);
					fileIdNames.put(childInfo.getName().trim(), childInfo.getID().trim());
					// log.info("FILE NAME ID :" + childInfo.getName() + " " + childInfo.getID());
					if (childInfo.getName().endsWith("json")) {
						BoxFile file = new BoxFile(api, childInfo.getID());
						file.download(new FileOutputStream(new File(DIRECTORY + "\\" + childInfo.getName())));
						JSONParser parser = new JSONParser();
						Object obj = parser.parse(new FileReader(DIRECTORY + "\\" + childInfo.getName()));
						JSONArray jsonArray = (JSONArray) obj;
						for (int i = 0; i < jsonArray.size(); i++) {
							HashMap<String, String> yourHashMap = new Gson().fromJson(jsonArray.get(i).toString(),
									HashMap.class);
							metdataArray.add(yourHashMap);
							// log.info("Metadata Map :" + yourHashMap);
						}
					}
				}

			}
			response = uploadToDMS(metdataArray, fileIdInfos, fileIdNames);
			String str = info.getName();
			info.setName(str + "-Done");
			mainParent.updateInfo((com.box.sdk.BoxFolder.Info) info);
		} else {
			response = "Folder already processed";
		}
		return response;

	}

	private String uploadToDMS(List<HashMap<String, String>> metadataArray, Map<String, Info> fileIdInfos,
			Map<String, String> fileIdNames) throws FileNotFoundException, IOException, ParseException {

		for (HashMap<String, String> finalMetadat : metadataArray) {
			finalMetadat.put("docId", fileIdNames.get(finalMetadat.get("Filename").trim()));
			finalMetadat.put("creator", finalMetadat.get("Email Sender"));
			// log.info("Processing Metadata : " + finalMetadat);
		}
		String response = updateService.saveToDB(metadataArray);
		return response;
	}

	/*
	 * String boxToken = dmsUtilityService.requestAccessToken(); BoxAPIConnection
	 * api = new BoxAPIConnection(boxToken);
	 * 
	 * BoxFolder bf = new BoxFolder(api, "124669568491");for( BoxItem.Info info:new
	 * BoxFolder(api,folderId).getChildren()) { if (info instanceof BoxFolder.Info)
	 * { boolean canMove = false; Map<String, BoxItem.Info> fileIdInfos = new
	 * HashMap<String, BoxItem.Info>(); Map<String, String> fileIdNames = new
	 * HashMap<String, String>(); List<HashMap<String, String>> metdataArray = new
	 * ArrayList<HashMap<String, String>>(); // BoxFolder.Info newFolderInfo =
	 * bf.createFolder(info.getName()); BoxFolder mainParent = new BoxFolder(api,
	 * info.getID()); if (!info.getName().contains("-Done")) {
	 * log.info("Processing Folder : " + info.getID()); for (BoxItem.Info childInfo
	 * : mainParent.getChildren()) { if (childInfo instanceof BoxFile.Info) {
	 * fileIdInfos.put(childInfo.getID(), childInfo);
	 * fileIdNames.put(childInfo.getName().trim(), childInfo.getID().trim()); if
	 * (childInfo.getName().endsWith("json")) {
	 * 
	 * BoxFile file = new BoxFile(api, childInfo.getID()); file.download(new
	 * FileOutputStream(new File(DIRECTORY + "\\" + childInfo.getName())));
	 * JSONParser parser = new JSONParser(); Object obj = parser.parse(new
	 * FileReader(DIRECTORY + "\\" + childInfo.getName())); JSONArray jsonArray =
	 * (JSONArray) obj; for (int i = 0; i < jsonArray.size(); i++) { HashMap<String,
	 * String> yourHashMap = new Gson().fromJson(jsonArray.get(i).toString(),
	 * HashMap.class); metdataArray.add(yourHashMap); } }
	 * 
	 * BoxFolder newChild = new BoxFolder(api, newFolderInfo.getID()); BoxFile filee
	 * = new BoxFile(api, childInfo.getID()); filee.move(newChild);
	 * 
	 * }
	 * 
	 * } String str = info.getName(); info.setName(str + "-Done");
	 * mainParent.updateInfo((com.box.sdk.BoxFolder.Info) info); } response =
	 * uploadToDMS(metdataArray, fileIdInfos, fileIdNames); } }
	 */
}
