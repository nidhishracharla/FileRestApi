package com.ge.capital.dms.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ge.capital.dms.entity.CornerstoneMessage;

@Component
@Transactional
public interface UploadContractRepository extends CrudRepository<CornerstoneMessage,Integer> {
	
	@Query(value="Select count(*) from cornerstone_message where entity_id= ?1 AND status= 'COMPLETED' and message_type in ('LoanUpdate','LeaseUpdate')",nativeQuery=true)
	Integer getCount(String contractNumber);
	
	@Query(value="Select count(*) from cornerstone_message where entity_id= ?1 AND status= 'COMPLETED' and message_type in ('LOCUpdate')",nativeQuery=true)
	Integer getCountOpportunity(String opportunityId);

}
