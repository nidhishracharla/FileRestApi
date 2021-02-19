
package com.ge.capital.dms.fr.sle.controllers.api;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.ge.capital.dms.fr.sle.config.DecodeSSO;
import com.ge.capital.dms.service.SelectService;
import com.ge.capital.dms.service.UpdateService;
import com.google.gson.Gson;

/**
 * @author GJ00557822
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/secure")
public class CopyDocuments {
	private static final Logger log = Logger.getLogger(CopyDocuments.class);

	@Autowired
	DecodeSSO decodeSSO;

	@Autowired
	SelectService selectService;

	@Autowired
	UpdateService updateService;

	@SuppressWarnings({ "unchecked" })
	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
	@RequestMapping(value = "/copyDocuments")
	@ResponseBody
	public Object copyDocuments(HttpServletRequest httprequest, @RequestBody Object request) {
		String response = "";
		String loggedinuser = decodeSSO.getDecodedSSO(httprequest.getHeader("loggedinuser"));
		String newSeqNum = (String) ((HashMap<String, Object>) request).get("newSeq");
		String welcomePack = (String) ((HashMap<String, Object>) request).get("welcomePackage");
		String finalPack = (String) ((HashMap<String, Object>) request).get("finalPackage");
		List<String> docIdArray = (List<String>) ((HashMap<String, Object>) request).get("docIdArray");
		HashMap<String, String> finalPackMetadata = new HashMap<String, String>();
		if (null != newSeqNum && !newSeqNum.isEmpty()) {
			if (docIdArray.size() > 0) {
				// get the metadata from db for newSeq
				finalPackMetadata = selectService.getDocMetadata(newSeqNum);
				if (finalPackMetadata.size() > 0) {
					for (String keys : finalPackMetadata.keySet()) {
						log.info(keys + " : " + finalPackMetadata.get(keys));
					}
					finalPackMetadata.put("welcomePackage", welcomePack);
					finalPackMetadata.put("finalPackage", finalPack);
					finalPackMetadata.put("creator", loggedinuser);
					for (String docId : docIdArray) {
						finalPackMetadata.put("dealDocId", docId.split(",")[0]);
						finalPackMetadata.put("documentSubType", docId.split(",")[1]);
						// DB Update
						try {
							updateService.copyDocuments(finalPackMetadata);
						} catch (Exception e) {
							response = e.getMessage();
							log.error(e.getMessage(), e);
						}
					}
					response = "Selected documents are copied to the new contract# " + newSeqNum;
				} else {
					response = "Unable to fetch metadata for the Contract# " + newSeqNum;
				}
				// return message saying no corresponding metadata found for the given seq#
				// number.
			} else {
				response = response + "Please select documents to be copied";
			}
		} else {
			response = response + "Please enter a valid Sequence Number";
		}
		Gson g = new Gson();
		String Jsonstr = g.toJson(response);
		return Jsonstr;

	}

	@SuppressWarnings({ "unchecked" })
	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
	@RequestMapping(value = "/deleteCopy")
	@ResponseBody
	public Object deleteCopy(HttpServletRequest httprequest, @RequestBody HashMap<String, String> request) {
		String response = "";
		String loggedinuser = decodeSSO.getDecodedSSO(httprequest.getHeader("loggedinuser"));
		response = updateService.deleteCopy(request);
		for (String keys : request.keySet()) {
			log.info(keys + " : " + request.get(keys));
		}
		Gson g = new Gson();
		String Jsonstr = g.toJson(response);
		return Jsonstr;

	}
}
