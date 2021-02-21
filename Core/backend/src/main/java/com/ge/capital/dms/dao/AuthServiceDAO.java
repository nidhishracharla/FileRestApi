package com.ge.capital.dms.dao;

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
public class AuthServiceDAO {
	
	
	
	@PersistenceUnit
	private EntityManagerFactory emf;

	private final Logger log = Logger.getLogger(this.getClass());
	@Transactional
	public String getToken(String jSessionID) {
		EntityManager em = emf.createEntityManager();
		String finalToken = "";
		try {
		String query = "SELECT token from UserSSOAuthEntity where jsessionID=:jSessionID";
		Query tokenQuery = em.createQuery(query);
		//tokenQuery.setParameter("usersso", userSSO);
		tokenQuery.setParameter("jSessionID", jSessionID);		
		finalToken = (String) tokenQuery.getResultList().get(0);
		//log.info("The token returned :"+ finalToken);
		return finalToken;
		} catch(Exception e) {
			log.error("Exception occured :" + e);
			
		} finally {
			em.close();
		}
		return finalToken;		
	}
	@Transactional
	public String getID(String apiToken) {
		// TODO Auto-generated method stub
		EntityManager em = emf.createEntityManager();
		String userSSO = "";
		try {
		String query = "SELECT usersso from UserSSOAuthEntity where token=:token";
		Query tokenQuery = em.createQuery(query);
		//tokenQuery.setParameter("usersso", userSSO);
		tokenQuery.setParameter("token", apiToken);
		userSSO = (String) tokenQuery.getResultList().get(0);
		//log.info("The SSO returned :"+ userSSO);
		return userSSO;
		} catch(Exception e) {
			log.error("Exception occured :" + e);			
		} finally {
			em.close();
		}
		return userSSO;
	}

}
