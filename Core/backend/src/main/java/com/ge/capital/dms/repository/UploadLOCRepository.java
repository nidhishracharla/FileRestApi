package com.ge.capital.dms.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ge.capital.dms.entity.DealDoc;

@Component
@Transactional
public interface UploadLOCRepository extends CrudRepository<DealDoc, String> {

	@Modifying
	@Query(value = "Update dmsamerica.deal_doc set credit_number= ?1 where opportunity_id= ?2  and lw_entity_type='Opportunity'", nativeQuery = true)
	void updateLocOppotunityID(String creditNumber, String opportunityId);

	@Modifying
	@Query(value = "Update dmsamerica.booking_report set read_only='true' where sequence_number= ?1", nativeQuery = true)
	void updateReaOnly(String sequenceNumber);
}
