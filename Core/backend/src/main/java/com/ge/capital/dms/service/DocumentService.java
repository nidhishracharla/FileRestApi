package com.ge.capital.dms.service;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.ge.capital.dms.dao.DocumentServiceDAO;
import com.ge.capital.dms.entity.DocType;

@Component
public class DocumentService {

	@Autowired
	DocumentServiceDAO documentServiceDAO;
	
	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
	public HashMap<String, String> getDocumentProperties(String docType, String documentId) {
		return documentServiceDAO.getDocumentProperties(docType, documentId);

	}
	
	public String deleteDoc(String docId, String docType) {
		
		return documentServiceDAO.deleteDoc(docId,docType);
	}	
	
	public void updateAuditInfo(String docId, String event, String docType, String loggedinUser, String status,
			String message) {
		// TODO Auto-generated method stub
		documentServiceDAO.updateAuditInfo(docId, event, docType, loggedinUser, status, message);
	}

	public List<DocType> getDocTypes() {
		// TODO Auto-generated method stub
		return documentServiceDAO.getDocTypes();
	}
	public String deleteDocuSignDoc(String docId) {
		
		return documentServiceDAO.deleteDocuSignDoc(docId);
	}
}
