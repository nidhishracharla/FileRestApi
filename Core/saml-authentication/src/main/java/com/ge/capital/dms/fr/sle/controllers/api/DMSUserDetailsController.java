package com.ge.capital.dms.fr.sle.controllers.api;

import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ge.capital.dms.entity.DMSUserDetails;
import com.ge.capital.dms.fr.sle.config.DecodeSSO;
import com.ge.capital.dms.repository.DMSUserDetailsRepository;
import com.ge.capital.dms.service.DocumentService;
import com.ge.capital.dms.service.UserRealmService;
import io.swagger.annotations.ApiParam;

/**
 * @author VA460440
 */

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/getDMSUserdetails")

public class DMSUserDetailsController {

	@Autowired
	DMSUserDetailsRepository dmsUserDetailsRepository;

	@Autowired
	UserRealmService userRealmService;

	@Autowired
	DocumentService documentService;

	@Autowired
	DecodeSSO decodeSSO;

	@Autowired
	HttpSession session;

	javax.servlet.http.Cookie[] cookie;

	private final Logger logger = Logger.getLogger(this.getClass());

	@RequestMapping(value = "/getByUserId", method = RequestMethod.POST)
	public List<DMSUserDetails> getByUserId(HttpServletRequest request, @RequestBody String ssoId) {
		List<DMSUserDetails> dmsUserdetails = null;
		try {
			String userSSO = decodeSSO.getDecodedSSO(ssoId);
			dmsUserdetails = (List<DMSUserDetails>) dmsUserDetailsRepository.findFirstNameAndLastname(userSSO);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return dmsUserdetails;

	}

	@RequestMapping("/userRealm")
	public List<String> getUserRealm(HttpServletRequest request,
			@ApiParam(value = "", required = true) @Valid @RequestBody String userId) {

		List<String> userRealmList = null;
		try {
			String userSSO = decodeSSO.getDecodedSSO(userId);
			decodeSSO.getDecodedSSO(request.getHeader("loggedinuser"));
			userRealmList = userRealmService.userRealm(userSSO);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

		}
		return userRealmList;

	}

	@RequestMapping("/userDetails")
	public List<HashMap<String, String>> getUserDetails(HttpServletRequest request,
			@ApiParam(value = "", required = true) @Valid @RequestBody String userId) {

		String decodedUserId = decodeSSO.getDecodedSSO(userId);
		List<HashMap<String, String>> userRealmList = null;
		try {
			logger.info("User ID : " + decodedUserId);

			userRealmList = userRealmService.userDetails(decodedUserId);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return userRealmList;

	}

	@RequestMapping(value = "/getAllUsers", method = RequestMethod.GET)
	public List<DMSUserDetails> getAllUserDetails(HttpServletRequest request) {
		decodeSSO.getDecodedSSO(request.getHeader("loggedinuser"));
		List<DMSUserDetails> dmsUserdetails = null;
		try {
			dmsUserdetails = (List<DMSUserDetails>) dmsUserDetailsRepository.findAllFirstNameAndLastname();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return dmsUserdetails;

	}

	@RequestMapping(value = "/trackSession", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public String sessionTrack(@RequestBody String userSSO) {
		String str = "";
		try {
			String decodedUserId = decodeSSO.getDecodedSSO(userSSO);
			documentService.updateAuditInfo("", "Login", "", decodedUserId, "SUCCESS",
					"User has been successfully loggedin");
			str = "User has been logged in";
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return str;
	}

}
