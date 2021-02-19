package com.ge.capital.dms.fr.sle.controllers.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ge.capital.dms.entity.UserDetails;
import com.ge.capital.dms.repository.UserDetailsRepository;

/**
 * @author VA460440
 */

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/login")
public class LoginController {

	@Autowired
	UserDetailsRepository userDetailsRepository;
	
	@RequestMapping(value = "/validate", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<UserDetails> validateLogin(HttpServletRequest req, @RequestBody UserDetails userDetails) {
		HttpSession session = req.getSession(true);
		userDetails = userDetailsRepository.findByUsernameAndPassword(userDetails.getUsername(),
				userDetails.getPassword());
		session.setAttribute("UserDetails", userDetails);
		if (userDetails == null) {
			throw new UsernameNotFoundException("This user doesnot exists...Enter valid Credentials");
		}
		return new ResponseEntity<UserDetails>(userDetails, HttpStatus.OK);

	}
	
	

}
