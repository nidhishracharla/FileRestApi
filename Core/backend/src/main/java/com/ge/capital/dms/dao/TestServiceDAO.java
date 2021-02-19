//TestServiceDAO
package com.ge.capital.dms.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

@Component
@Configuration
@EnableTransactionManagement
public class TestServiceDAO {
	
	@PersistenceUnit
	private EntityManagerFactory emf;

	private final Logger log = Logger.getLogger(this.getClass());
	
	@Transactional
	@SuppressWarnings("rawtypes")
	public String dbMonitor() {
		EntityManager em = emf.createEntityManager();
		String status = "";
		try {
		String query = "SELECT firstName from DMSUserDetails where userId=:userId";
		Query tokenQuery = em.createQuery(query);
		//tokenQuery.setParameter("usersso", userSSO);
		tokenQuery.setParameter("userId", "444444444");		
		List result =  tokenQuery.getResultList();
		//log.info("The token returned :"+ finalToken);
		if(!(result.isEmpty())) {
			status = "WORKING";
		} else {
			status = "Result set is empty";
		}
		return status;
		} catch(Exception e) {
			log.error("Exception occured :" + e);
			status = "Exception occured : "+e.getMessage();
		} finally {
			em.close();
		}
		return status;		
	}

	
}
