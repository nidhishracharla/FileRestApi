package com.ge.capital.dms.repository;

import javax.transaction.Transactional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import com.ge.capital.dms.entity.UserDetails;
/** 
 * @author VA460440
 */

@Component
@Transactional
public interface UserDetailsRepository extends CrudRepository<UserDetails, String> {
	
	UserDetails findByUsernameAndPassword(String username,String password);

}
