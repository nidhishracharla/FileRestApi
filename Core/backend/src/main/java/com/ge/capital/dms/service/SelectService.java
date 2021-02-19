package com.ge.capital.dms.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ge.capital.dms.dao.SelectDAO;
import com.ge.capital.dms.entity.DocSubtype;
import com.ge.capital.dms.entity.DocSubtypeKeyword;
import com.ge.capital.dms.entity.DocuSignMessageStatus;
import com.ge.capital.dms.repository.DocSubTypeKeywordRepository;
import com.ge.capital.dms.repository.DocSubTypeRepository;
import com.ge.capital.dms.repository.DocuSignStatusRepository;

@Service
public class SelectService {
	
	@Autowired
	SelectDAO selectDAO;
	
	@Autowired
	DocSubTypeRepository docSubTypeRepository;
	
	@Autowired
	DocSubTypeKeywordRepository docSubTypeKeywordRepository;
	
	@Autowired
	DocuSignStatusRepository docuSignStatusRepository;


	public String getData(String opportunityId, String key) {
		// TODO Auto-generated method stub
		return selectDAO.getData(opportunityId,key);
	}


	public String isFileExists(String finalPackName) {
		// TODO Auto-generated method stub
		return selectDAO.isFileExists(finalPackName);
	}


	public HashMap<String, String> getDocMetadata(String contractNum) {
		
		return selectDAO.getDocMetadata(contractNum);
	}
	
	public HashMap<String, String> getDocMetadataforOpportunityID(String OpportunityID) {
		
		return selectDAO.getDocMetadataforOpportunityID(OpportunityID);
	}

	public HashMap<String, String> getDocMetadata(String contractNum, String OpportunityID) {
	
	return selectDAO.getDocMetadata(contractNum, OpportunityID);
	}


	public String getUserId(String login) {
		// TODO Auto-generated method stub
		return selectDAO.getUserId(login);
	}


	public String getFileName(String docId) {
		
		return selectDAO.getFileName(docId);
	}
	
	public List<DocSubtype> getDocSubtypes() {
		return docSubTypeRepository.getAllDocSubTypes();
	}
	
	public List<DocSubtype> getAllDocSubtypes() {
		return docSubTypeRepository.getDocSubTypes();
	}


	public String getReport(String sequenceNumber) {
		
		return selectDAO.getReport(sequenceNumber);
	}
	
	public DocuSignMessageStatus getFileforEnvelopes(String Envelopeid, String docsignfilename){
		
		return docuSignStatusRepository.getFileforEnvelopes(Envelopeid, docsignfilename);
	}

	public String getPermission(String sequenceNumber) {
		return selectDAO.getPermission(sequenceNumber);
	}
	
	public Iterable<DocSubtypeKeyword> getAllKeywords() {
		return docSubTypeKeywordRepository.findAll();
	}
	
}
