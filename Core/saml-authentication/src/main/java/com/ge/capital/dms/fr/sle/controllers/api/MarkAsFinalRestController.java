package com.ge.capital.dms.fr.sle.controllers.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

import com.ge.capital.dms.model.MarkAsFinalIVO;


@Controller
public class MarkAsFinalRestController {	
	
	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
	public void markAsFinalUpdate(String URL_MarkasFinal_Update, List<MarkAsFinalIVO> multipleMetadata)
	{
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.postForObject(URL_MarkasFinal_Update, multipleMetadata, ResponseEntity.class);
	}

}
