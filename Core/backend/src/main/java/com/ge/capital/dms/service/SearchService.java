package com.ge.capital.dms.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.ge.capital.dms.dao.SearchDAO;

@Component
public class SearchService {

	@Autowired
	SearchDAO searchDAO;

	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
	public List<HashMap<String, String>> searchMetadata(String docType, Map<String, String> inputMetadataMap) {
		return searchDAO.searchMetadata(docType, inputMetadataMap);

	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
	public List<HashMap<String, String>> searchRecentMetadata(String userId) {
		return searchDAO.searchRecentMetadata(userId);
	}

	public List<String> getSequenceNumbers(String partyNumber) {
		return searchDAO.getSequenceNumbers(partyNumber);
	}

}
