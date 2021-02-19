package com.ge.capital.dms.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;
import org.apache.log4j.Logger;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import com.ge.capital.dms.entity.DMSGroup;
import com.ge.capital.dms.utility.DmsUtilityConstants;
import com.ge.capital.dms.utility.DmsUtilityService;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Component
@Configuration
@EnableTransactionManagement
public class UserRealmDAO {

	@PersistenceUnit
	private EntityManagerFactory emf;

	@Autowired
	private DmsUtilityService dmsUtilityService;
	private final Logger logger = Logger.getLogger(this.getClass());
	@Transactional
	@SuppressWarnings("unchecked")
	@SuppressFBWarnings(value = { "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", "DMI_INVOKING_TOSTRING_ON_ARRAY",
			"DLS_DEAD_LOCAL_STORE", "" }, justification = "let me just make the build pass")
	public List<String> getRealm(String userId) {

		EntityManager em = emf.createEntityManager();
		Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docTypeMappingResource);
		List<String> resultMetadataList = null;
		try {		
		String selectKey = "dmsgroup.select.clause";
		String fromKey = "dmsgroup.from.clause";
		String whereKey = "dmsgroup.where.clause";
		String searchQuery = props.getProperty(selectKey) + " " + props.getProperty(fromKey) + " "
				+ props.getProperty(whereKey);
		
		Query selectQuery = em.createQuery(searchQuery);
		selectQuery.setParameter("userId", userId);
		resultMetadataList = selectQuery.getResultList();
		return resultMetadataList;
		}catch(Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			em.close();
		}
		return resultMetadataList;

	}

	@Transactional
	@SuppressWarnings("unchecked")
	public List<HashMap<String, String>> getDetails(String userId) {
		List<HashMap<String, String>> resultMetadataList = null;
		HashMap<String, String> resultMap = null;

		EntityManager em = emf.createEntityManager();
		Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docTypeMappingResource);
		try {
		String selectKey = "getDetails.select.clause";
		String fromKey = "getDetails.from.clause";
		String whereKey = "getDetails.where.clause";
		String searchQuery = props.getProperty(selectKey) + " " + props.getProperty(fromKey) + " "
				+ props.getProperty(whereKey);
		
		Query selectQuery = em.createQuery(searchQuery);
		selectQuery.setParameter("userId", userId);
		List<Object[]> searchResult = selectQuery.getResultList();
		resultMetadataList = new ArrayList<HashMap<String, String>>();

		String respMtdAttrString = props.getProperty("getDetails.metadata.respAttr");
		if (respMtdAttrString != null) {
			String respMtdAttrArry[] = respMtdAttrString.split(",");
			int respMtdAttrSize = respMtdAttrArry.length;

			for (Object[] obj : searchResult) {
				resultMap = new HashMap<String, String>();
				for (int index = 0; index < respMtdAttrSize; index++)
				{
					resultMap.put(respMtdAttrArry[index], (String) obj[index]);	
				}
					
				resultMetadataList.add(resultMap);
			}
			
			return resultMetadataList;
		}
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			//em.close();
			em.close();
		}
		
		return resultMetadataList;
	}

	@Transactional
	public void deleteUser(String all_users, String group_name) {

		EntityManager em = emf.createEntityManager();
		Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docTypeMappingResource);

		String deleteKey = "deleteUser.delete.clause";
		String fromKey = "deleteUser.from.clause";
		String whereKey = "deleteUser.where.clause";
		String deleteQuery = props.getProperty(deleteKey) + " " + props.getProperty(fromKey) + " "
				+ props.getProperty(whereKey);

		try {
			
			Query result = em.createQuery(deleteQuery);
			result.setParameter("all_users", all_users);
			result.setParameter("group_name", group_name);
			em.getTransaction().begin();
			int count = result.executeUpdate();
			logger.info("Number of rows updated : " + count);
			em.getTransaction().commit();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			em.close();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Transactional
	public String addUser(Map<String, String> userData) {
		String str = null;
		EntityManager em = emf.createEntityManager();
		try {
			

			String[] grpDetails = userData.get("group_name").split(",");
			em.joinTransaction();
			// em.persist(dmsGroup);

			Session session = em.unwrap(org.hibernate.Session.class);

			for(int i=0; i<grpDetails.length; i++)
			{
				DMSGroup dmsGroup = new DMSGroup();
				dmsGroup.setAll_User(userData.get("all_users"));
				dmsGroup.setGroup_id("cap_dms_"+userData.get("realm").toLowerCase()+"_"+grpDetails[i].toLowerCase());
				dmsGroup.setGroup_Name("Capital_DMS_"+userData.get("realm").toUpperCase()+"_"+grpDetails[i]);
				if(userData.get("realm").equals("ALL"))
					dmsGroup.setIsAdmin_Group("1");
				else
					dmsGroup.setIsAdmin_Group("0");
				dmsGroup.setIs_Active("1");
				if(userData.get("realm").equals("HEF_FINAL") || userData.get("realm").equals("HEF_INT")) {
					dmsGroup.setRealm("IFLEASING");
				} else {
					dmsGroup.setRealm(userData.get("realm"));
				}
				
				
				Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docTypeMappingResource);

				String selectKey = "getDetails.select.clause";
				String fromKey = "getDetails.from.clause";
				String searchQuery = props.getProperty(selectKey) + " " + props.getProperty(fromKey);
				
				
				Query selectQuery = em.createQuery(searchQuery);
				
				//List<DMSGroup> dms = (List<DMSGroup>) selectQuery.getResultList();
				List<HashMap<String, String>> resultMetadataList = null;
				HashMap<String, String> resultMap = null;
				List<Object[]> dms = selectQuery.getResultList();
				resultMetadataList = new ArrayList<HashMap<String, String>>();
				
				String respMtdAttrString = props.getProperty("getDetails.metadata.respAttr");
				if (respMtdAttrString != null) {
					String respMtdAttrArry[] = respMtdAttrString.split(",");
					int respMtdAttrSize = respMtdAttrArry.length;

					for (Object[] obj : dms) {
						resultMap = new HashMap<String, String>();
						for (int index = 0; index < respMtdAttrSize; index++)
							resultMap.put(respMtdAttrArry[index], (String) obj[index]);
						resultMetadataList.add(resultMap);
					}
				}
				
				Iterator itr = resultMetadataList.iterator();
				while(itr.hasNext()){
					HashMap<String, String> temp = (HashMap<String, String>) itr.next();
					if((temp.get("all_users").equals(dmsGroup.getAll_User())) && (temp.get("group_name").equals(dmsGroup.getGroup_Name()))) {
						
						throw new NonUniqueObjectException(null , "User already Exists");	
					}
				}
				session.save(dmsGroup);
				str = "User details created successfully..."; 
				
			}
			return str;
		} catch(NonUniqueObjectException e) {
			logger.error(e.getMessage(), e);
		str = "User Already Exists...";
		}
		catch(Exception e) {
		logger.error("Exception occured :"+e);
			str =  "Internal Server Error...Unable to add user";
		} finally {
			em.close();
		}
		return str;
	}
}
