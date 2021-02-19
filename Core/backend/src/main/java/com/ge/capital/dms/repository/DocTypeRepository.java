package com.ge.capital.dms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ge.capital.dms.entity.DocType;

@Component
@Transactional
public interface DocTypeRepository extends CrudRepository<DocType, String> {

	@Query(value="select * from dmsamerica.type_subtype_lookup ORDER BY type_label,subtype_label",nativeQuery=true)
	List<DocType> findDoctypeList();
}
