package com.ge.capital.dms.fr.sle.controllers.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import com.ge.capital.dms.api.SearchApi;
import com.ge.capital.dms.api.SearchApiController;
import com.ge.capital.dms.fr.sle.config.DecodeSSO;
import com.ge.capital.dms.model.SearchIVO;
import com.ge.capital.dms.model.SearchOVO;
import com.ge.capital.dms.repository.DocSubTypeRepository;
import com.ge.capital.dms.service.SearchService;

import io.swagger.annotations.ApiParam;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2018-08-14T22:22:38.380+05:30")

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/secure")
public class SearchController implements SearchApi {

	private static final Logger log = Logger.getLogger(SearchApiController.class);

	@Autowired
	SearchService searchService;
	@Autowired
	DocSubTypeRepository docSubTypeRepository;
	@Autowired
	DecodeSSO decodeSSO;
	@SuppressWarnings("unused")
	private final ObjectMapper objectMapper;

	@SuppressWarnings("unused")
	private final HttpServletRequest request;

	@org.springframework.beans.factory.annotation.Autowired
	public SearchController(ObjectMapper objectMapper, HttpServletRequest request) {
		this.objectMapper = objectMapper;
		this.request = request;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/searchMetadata", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<SearchOVO> searchMetadataPost(HttpServletRequest httprequest,
			@ApiParam(value = "", required = true) @Valid @RequestBody SearchIVO metadata) {
		try {
			decodeSSO.getDecodedSSO(httprequest.getHeader("loggedinuser"));
			List<HashMap<String, String>> responseMetadataList = new ArrayList<HashMap<String, String>>();
			String docType = metadata.getDocType();
			Map<String, String> inputMetadataMap = (Map<String, String>) metadata.getMetadata();
			for (String key : inputMetadataMap.keySet()) {
				log.info("The value in " + key + " is :" + String.valueOf(inputMetadataMap.get(key)));
			}
			if (docType.equals("exportManifest")) {
				String arr[] = { "exportManifest1", "exportManifest2", "exportManifest3" };
				List<HashMap<String, String>> cart = new ArrayList<HashMap<String, String>>();

				for (int i = 0; i < arr.length; i++) {
					cart = searchService.searchMetadata(arr[i], inputMetadataMap);
					if (cart != null && cart.size() > 0)
						responseMetadataList.addAll(cart);
				}

			} else
				responseMetadataList = searchService.searchMetadata(docType, inputMetadataMap);

			SearchOVO searchOVO = new SearchOVO();
			searchOVO.setMetadataList(responseMetadataList);
			return new ResponseEntity<SearchOVO>(searchOVO, HttpStatus.OK);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return new ResponseEntity<SearchOVO>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/recentData", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<SearchOVO> getRecentSearchData(
			@ApiParam(value = "", required = true) @Valid @RequestBody String userId) {
		List<HashMap<String, String>> responseMetadataList = new ArrayList<HashMap<String, String>>();
		try {
			responseMetadataList = searchService.searchRecentMetadata(userId);
			SearchOVO searchOVO = new SearchOVO();
			searchOVO.setMetadataList(responseMetadataList);
			return new ResponseEntity<SearchOVO>(searchOVO, HttpStatus.OK);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return new ResponseEntity<SearchOVO>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(value = "/getSeqDropdown", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<List<String>> getSeqDropdownData(
			@ApiParam(value = "", required = true) @Valid @RequestBody String partyNumber) {
		List<String> responseMetadataList = new ArrayList<String>();
		try {
			
			log.info("Party Number " + partyNumber);
			responseMetadataList = searchService.getSequenceNumbers(partyNumber);

			return new ResponseEntity<List<String>>(responseMetadataList, HttpStatus.OK);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return new ResponseEntity<List<String>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public ResponseEntity<SearchOVO> searchMetadataPost(SearchIVO metadata) {
		// TODO Auto-generated method stub
		return null;
	}

}
