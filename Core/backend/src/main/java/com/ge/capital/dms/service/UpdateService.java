package com.ge.capital.dms.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.ge.capital.dms.dao.UpdateDAO;
import com.ge.capital.dms.entity.DocuSignMessageStatus;
import com.ge.capital.dms.repository.UploadLOCRepository;

@Component
public class UpdateService {

	@Autowired
	UpdateDAO updateDAO;

	@Autowired
    UploadLOCRepository uploadLOCRepository;
    
	public void updateMetadata(String docId, Map<String, String> inputMetadataMap) {

		updateDAO.updateMetadata(docId, inputMetadataMap);
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
	public void updateDocumentMetadata(String docType, Map<String, String> documentDetails, String loggedinUser)
			throws Exception {

		updateDAO.updateDocumentMetadata(docType, documentDetails, loggedinUser);
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
	public void updateDocumentTypeMetadata(String docType, String tableName, Map<String, String> updateParams)
			throws Exception {

		updateDAO.updateDocumentTypeMetadata(docType, tableName, updateParams);
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
	public void updateManifestDoc(String docType, Map<String, String> updateParams) throws Exception {

		updateDAO.updateManifestDoc(docType, updateParams);
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
	public int updateCustomerName(String partyNumber, String partyName) {

		return updateDAO.updateCustomerName(partyNumber, partyName);
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
	public int locUpdate(String partyNumber, String partyName, String creditNumber, String opportunityID) {

		return updateDAO.integFour(partyNumber, partyName, creditNumber, opportunityID);
	}

	@PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_USER')")
	public int loanUpdate(String creditNumber, String sequenceNumber, Date commencementDate, String metadata,
			String messageType) {
		try {
			return updateDAO.loanUpdate(creditNumber, sequenceNumber, commencementDate, metadata, messageType);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public void copyDocuments(Map<String, String> finalPackMetadata) {
		updateDAO.copyDocuments(finalPackMetadata);
	}

	public String copyDocument(Map<String, String> finalPackMetadata) {
		return updateDAO.copyDocument(finalPackMetadata);
	}

	public String deleteCopy(HashMap<String, String> request) {

		return updateDAO.deleteCopy(request);
	}

	public String saveToDB(List<HashMap<String, String>> metadataArray) {

		return updateDAO.saveToDB(metadataArray);
	} // update service

	public String saveBookingReport(String sequenceNumber, List<String> tableData) {
		return updateDAO.saveBookingReport(sequenceNumber, tableData);

	}
	
	public String updateDocuSignData(String docid, String legalEntityType) {
		return updateDAO.updateDocuSignData( docid, legalEntityType);
	}
	
	public String insertDocuSignStatus(DocuSignMessageStatus status) {
		return updateDAO.insertintoDocuSignStatus(status);
	}
	public void updateBookingReport(String sequenceNumber) {
	       
        uploadLOCRepository.updateReaOnly(sequenceNumber);
    }
	

}
