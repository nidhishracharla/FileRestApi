package com.ge.capital.dms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ge.capital.dms.entity.DMSUserDetails;
import com.ge.capital.dms.entity.DocSubtype;
import com.ge.capital.dms.entity.DocuSignMessageStatus;
import com.ge.capital.dms.entity.ReportID;

@Component
@Transactional
public interface DocuSignStatusRepository extends CrudRepository<DocuSignMessageStatus, String> {
	
	@Query(value = "SELECT * from dmsamerica.docusign d where d.envelope_id=?1 and  d.docsign_filename=?2", nativeQuery = true)
	DocuSignMessageStatus getFileforEnvelopes(String Envelope,String docsignfilename);
	
	
}
