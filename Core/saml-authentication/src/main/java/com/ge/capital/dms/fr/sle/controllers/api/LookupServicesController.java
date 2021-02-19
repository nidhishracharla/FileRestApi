package com.ge.capital.dms.fr.sle.controllers.api;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ge.capital.dms.utility.CommonConstants;
import com.ge.capital.dms.exception.CustomGenericException;
import com.ge.capital.dms.fr.sle.config.DecodeSSO;
import com.ge.capital.dms.entity.CorporateList;
import com.ge.capital.dms.entity.CountyList;
import com.ge.capital.dms.entity.DocSubtype;
import com.ge.capital.dms.entity.DocType;
import com.ge.capital.dms.entity.ReportID;
import com.ge.capital.dms.repository.CorporateListRepository;
import com.ge.capital.dms.repository.CountyListRepository;
import com.ge.capital.dms.repository.DocSubTypeRepository;
import com.ge.capital.dms.repository.DocTypeRepository;
import com.ge.capital.dms.repository.ReportIDRepository;
import com.ge.capital.dms.service.DocumentService;

/**
 * 
 * @author JS431163
 *
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/lookup")
public class LookupServicesController {

	@Autowired
	DocTypeRepository docTypeRepository;
	@Autowired
	DecodeSSO decodeSSO;
	@Autowired
	CorporateListRepository corporateListRepository;
	@Autowired
	CountyListRepository countyListRepository;
	@Autowired
	DocSubTypeRepository docSubTypeRepository;
	@Autowired
	ReportIDRepository reportIDRepository;
	@Autowired
	DocumentService docDao;

	private final Logger logger = Logger.getLogger(this.getClass());

	/**
	 * GET /create --> Create a new doc type entry and save it in the database.
	 */
	@RequestMapping(value = "/createDocType/{type_subtype_id}/{type_id}/{type_label}/{subtype_id}/{subtype_label}", method = RequestMethod.GET)
	public void create(@PathVariable("type_subtype_id") int type_subtype_id, @PathVariable("type_id") int type_id,
			@PathVariable("type_label") String type_label, @PathVariable("subtype_id") int subtype_id,
			@PathVariable("subtype_label") String subtype_label) {

		logger.debug("createDocType service call begins");

		try {
			DocType docType = new DocType();

			docType.setType_id(type_id);
			docType.setType_label(type_label);
			docType.setSubtype_id(subtype_id);
			docType.setSubtype_label(subtype_label);

			docType = docTypeRepository.save(docType);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new CustomGenericException(CommonConstants.INTERNAL_SERVER_ERROR, e.getMessage());
		}

		logger.info("createDocType service call ends");

	}

	/**
	 * GET /create --> Create a new Corporation entry and save it in the database.
	 */
	@RequestMapping(value = "/createCorporateList/{corpVal}/{corpName}/{corpLabel}", method = RequestMethod.GET)
	public void createCorporateList(@PathVariable("corpVal") String corpVal, @PathVariable("corpName") String corpName,
			@PathVariable("corpLabel") String corpLabel) {

		logger.debug("create Corporation entry service call begins");

		try {
			CorporateList corporateList = new CorporateList();
			corporateList.setCorpVal(corpVal);
			corporateList.setCorpName(corpName);
			corporateList.setCorpLabel(corpLabel);
			corporateList = corporateListRepository.save(corporateList);

		} catch (Exception e) {
			logger.error("Exception occured :", e);
			throw new CustomGenericException(CommonConstants.INTERNAL_SERVER_ERROR, e.getMessage());
		}

		logger.info("create Corporation entry service call ends");

	}

	/**
	 * GET /create --> Create a new County entry and save it in the database.
	 */
	@RequestMapping(value = "/createCountyList/{countyCode}/{countyGroup}/{countyName}/{state}", method = RequestMethod.GET)
	public void createCountyList(@PathVariable("countyCode") String countyCode,
			@PathVariable("countyGroup") String countyGroup, @PathVariable("countyName") String countyName,
			@PathVariable("state") String state) {

		logger.info("create Corporation entry service call begins");

		try {
			CountyList countyList = new CountyList();
			countyList.setCountyCode(countyCode);
			countyList.setCountyGroup(countyGroup);
			countyList.setCountyName(countyName);
			countyList.setState(state);
			countyList = countyListRepository.save(countyList);

		} catch (Exception e) {
			logger.error("Exception occured :", e);

		}

		logger.debug("create Corporation entry service call ends");

	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
	@RequestMapping("/getAllSubTypes")
	public List<DocType> getAllSubTypes(HttpServletRequest request) {
		List<DocType> subTypeList = null;
		decodeSSO.getDecodedSSO(request.getHeader("loggedinuser"));
		try {
			subTypeList = docDao.getDocTypes();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return subTypeList;

	}

	/**
	 * GET /getCorporateList --> Get Corporation Values
	 */
	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
	@RequestMapping("/getCorporateList")
	public List<CorporateList> getCorporateList(HttpServletRequest request) {
		List<CorporateList> CorporateValuesList;
		decodeSSO.getDecodedSSO(request.getHeader("loggedinuser"));
		logger.debug("Service call for getting all the Corporation Values is initiated");
		try {
			CorporateValuesList = (List<CorporateList>) corporateListRepository.findAll();

		} catch (Exception e) {
			logger.error("Exception occured :", e);
			throw new CustomGenericException(CommonConstants.INTERNAL_SERVER_ERROR, e.getMessage());
		}
		logger.debug("Service call for getting all the Corporation Values is Ended");
		return CorporateValuesList;

	}

	/**
	 * GET /getCountyList --> Get CountyList Values
	 */
	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
	@RequestMapping("/getCountyList")
	public List<CountyList> getCountyList(HttpServletRequest request) {

		List<CountyList> CountyValuesList = null;
		decodeSSO.getDecodedSSO(request.getHeader("loggedinuser"));
		logger.debug("Service call for getting all the Corporation Values is initiated");
		try {
			CountyValuesList = (List<CountyList>) countyListRepository.findAll();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		logger.debug("Service call for getting all the Corporation Values is Ended");
		return CountyValuesList;

	}

	/**
	 * * GET /getDocSubtypeList --> Get DocSubtype List Values
	 */
	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
	@RequestMapping("/getDocSubtypeList")
	public List<DocSubtype> getDocSubtype(HttpServletRequest request) {
		List<DocSubtype> docSubTypeList = null;
		decodeSSO.getDecodedSSO(request.getHeader("loggedinuser"));
		try {
			docSubTypeList = (List<DocSubtype>) docSubTypeRepository.getAllDocSubTypes();

		} catch (Exception e) {
			logger.error("Exception occured :", e);
		}

		return docSubTypeList;

	}

	/**
	 * * GET /getReportIDList --> Get ReportID List Values
	 */
	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
	@RequestMapping("/getReportIDList")
	public List<ReportID> getGEReportLabel(HttpServletRequest request) {
		List<ReportID> reportIDList = null;
		decodeSSO.getDecodedSSO(request.getHeader("loggedinuser"));
		try {
			reportIDList = (List<ReportID>) reportIDRepository.findAll();

		} catch (Exception e) {
			logger.error("Exception occured :", e);

		}

		return reportIDList;

	}

}
