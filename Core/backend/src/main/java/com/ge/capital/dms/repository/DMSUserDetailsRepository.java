package com.ge.capital.dms.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import com.ge.capital.dms.entity.DMSUserDetails;

/**
 * @author VA460440
 */

@Component
@Transactional
public interface DMSUserDetailsRepository extends CrudRepository<DMSUserDetails, String> {

	@Query(value = "select * from dmsamerica.dms_user where user_id=?", nativeQuery = true)
	List<DMSUserDetails> findFirstNameAndLastname(String userId);

	@Query(value = "select * from dmsamerica.dms_user", nativeQuery = true)
	List<DMSUserDetails> findAllFirstNameAndLastname();
}
