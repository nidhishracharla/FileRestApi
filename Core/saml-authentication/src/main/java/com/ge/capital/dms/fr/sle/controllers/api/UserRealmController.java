package com.ge.capital.dms.fr.sle.controllers.api;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ge.capital.dms.api.UpdateApiController;
import com.ge.capital.dms.exception.CustomGenericException;
import com.ge.capital.dms.fr.sle.config.DecodeSSO;
import com.ge.capital.dms.service.DocumentService;
//import com.ge.capital.dms.repository.UserRealmRepository;
//import com.ge.capital.dms.service.SearchService;
import com.ge.capital.dms.service.UserRealmService;
import com.ge.capital.dms.utility.CommonConstants;
import io.swagger.annotations.ApiParam;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/secure")
public class UserRealmController {
	
	@Autowired
	UserRealmService userRealmService;
	
	@Autowired
	DocumentService documentService;
	
	@Autowired
	DecodeSSO decodeSSO;
	private static final Logger log = Logger.getLogger(UpdateApiController.class);
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping("/deleteUser")
	public String deleteUser(HttpServletRequest httprequest, @ApiParam(value = "", required = true) @Valid @RequestBody String user) {
		String loggedinUser = decodeSSO.getDecodedSSO(httprequest.getHeader("loggedinuser"));
		
		String[] userDetails = user.split(",");
		String[] groupName = userDetails[0].split(":");
		String[] all_users = userDetails[3].split(":");
		
		
		try {
             userRealmService.deleteUser(all_users[1].replace('"',' ').trim(),groupName[1].replace('"',' ').trim());
             documentService.updateAuditInfo(all_users[1].replace('"',' ').trim(), "ManageUser", "deleteUser", loggedinUser, "SUCCESS",
						"User Removed successfully");
	
		}catch(Exception e) {
			documentService.updateAuditInfo(all_users[1].replace('"',' ').trim(), "ManageUser", "deleteUser", loggedinUser, "FAILED",
					"Unable to remove the user");
			log.error(e.getMessage(), e);
			throw new CustomGenericException(CommonConstants.INTERNAL_SERVER_ERROR, e.getMessage());
			
		}
		return null;		
	}
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@RequestMapping(value = "/addUser", method = RequestMethod.POST)
	@ResponseBody
	public String addUserDetails(HttpServletRequest httprequest,
			@ApiParam(value = "", required = true) @Valid @RequestBody String newUser) {
		String loggedinUser = decodeSSO.getDecodedSSO(httprequest.getHeader("loggedinuser"));
		
		//{"userSSO":"6676767","realm":"CORPTAX","role":["CONSUMER"]}
		
		String[] userDetails = newUser.split(",");
		
		
		String all_users = (userDetails[0].split(":"))[1].replace('"',' ').trim();
		String realm = (userDetails[1].split(":"))[1].replace('"',' ').trim();
		String group_name ="";
		for(int i=2;i<userDetails.length;i++)
		{
			if(i>2)
				group_name = group_name + userDetails[i].replace('"',' ').trim()+",";
			else
				group_name = group_name + (userDetails[2].split(":"))[1].replace('"',' ').trim()+",";
		}
		group_name = group_name.substring(2, group_name.length()-4);
		//String group_name = (userDetails[2].split(":"))[1];		
		Map<String, String> userData = new HashMap<String, String>();
		
		userData.put("all_users", all_users);
		userData.put("realm", realm);
		userData.put("group_name", group_name);
		String str = userRealmService.addUser(userData);
		documentService.updateAuditInfo(all_users, "ManageUser", "AddUser", loggedinUser, "SUCCESS",
				"User Added successfully");
		
		return str;
	}
}