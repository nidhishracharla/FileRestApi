package com.ge.capital.dms.fr.sle.controllers.api;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ge.capital.dms.api.UpdateApi;
import com.ge.capital.dms.api.UpdateApiController;
import com.ge.capital.dms.model.UpdateIVO;
import com.ge.capital.dms.service.UpdateService;

import io.swagger.annotations.ApiParam;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2018-08-14T22:22:38.380+05:30")

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/secure")
public class UpdateController implements UpdateApi {

	private static final Logger log = Logger.getLogger(UpdateApiController.class);

	@Autowired
	UpdateService updateService;

	@SuppressWarnings("unused")
	private final ObjectMapper objectMapper;

	@SuppressWarnings("unused")
	private final HttpServletRequest request;

	@org.springframework.beans.factory.annotation.Autowired
	public UpdateController(ObjectMapper objectMapper, HttpServletRequest request) {
		this.objectMapper = objectMapper;
		this.request = request;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/updateMetadata", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<Void> updateMetadataPost(
			@ApiParam(value = "", required = true) @Valid @RequestBody List<UpdateIVO> multipleMetadata) {
		try {

			for (Iterator<UpdateIVO> i = multipleMetadata.iterator(); i.hasNext();) {

				UpdateIVO metadata = i.next();

				Map<String, String> inputMetadataMap = (Map<String, String>) metadata.getMetadata();

				String docId = inputMetadataMap.get("docId");
				String docType = inputMetadataMap.get("docType");
				String tablename = docType + "T";

				if (null != docId) {
					if (null != docType && !(docType.equalsIgnoreCase("Contract") || docType.equalsIgnoreCase("Party")
							|| docType.equalsIgnoreCase("Account") || docType.equalsIgnoreCase("Opportunity")
							|| docType.equalsIgnoreCase("LOC"))) {
						inputMetadataMap.remove("docType");
						updateService.updateDocumentTypeMetadata(docType, tablename, inputMetadataMap);
					} else
						updateService.updateMetadata(docId, inputMetadataMap);
				}

			}

			return new ResponseEntity<Void>(HttpStatus.OK);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<Void> updateMetadataPost(UpdateIVO metadata) {
		// TODO Auto-generated method stub
		return null;
	}
}