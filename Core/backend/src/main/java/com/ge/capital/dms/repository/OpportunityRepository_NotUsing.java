package com.ge.capital.dms.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ge.capital.dms.entity.DealDoc;

@Component
@Transactional
public interface OpportunityRepository_NotUsing extends CrudRepository<DealDoc,String>{
	@Query(value = "Select distinct party_name from dmsamerica.deal_doc where opportunity_id= ?1", nativeQuery = true)
	String getPartyName(String opportunityId);

	@Query(value = "Select distinct party_number from dmsamerica.deal_doc where opportunity_id= ?1", nativeQuery = true)
	String getPartyNumber(String opportunityId);
	
	@Query(value = "Select distinct credit_number from dmsamerica.deal_doc where opportunity_id= ?1", nativeQuery = true)
	String getCreditNumber(String opportunityId);

}
