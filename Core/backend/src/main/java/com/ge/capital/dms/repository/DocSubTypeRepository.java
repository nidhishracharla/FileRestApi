package com.ge.capital.dms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ge.capital.dms.entity.DocSubtype;
import com.ge.capital.dms.entity.DocType;

@Component
@Transactional
public interface DocSubTypeRepository extends CrudRepository<DocSubtype, String> {
	@Query(value = "Select is_final from dmsamerica.doc_subtype where doc_subtype= ?1", nativeQuery = true)
	String isFinal(String docSubType);

	@Query(value = "SELECT doc_subtype,is_final,keyword,include_welcomepkg,include_tiaapkg,primary_entitytype from dmsamerica.doc_subtype ORDER BY doc_subtype", nativeQuery = true)
	List<DocSubtype> getAllDocSubTypes();
	

	@Query(value = "SELECT DISTINCT (d.lw_sequence_num) from dmsamerica.deal_doc d WHERE d.credit_number IN (:creditNumbers)", nativeQuery = true)
	List<String> getSequenceNumbers(@Param("creditNumbers") List<String> creditNumbers);

	@Query(value = "SELECT * from dmsamerica.doc_subtype ORDER BY doc_subtype", nativeQuery = true)
	List<DocSubtype> getDocSubTypes();

}
