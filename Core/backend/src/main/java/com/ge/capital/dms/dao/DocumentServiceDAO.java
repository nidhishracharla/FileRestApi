package com.ge.capital.dms.dao;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;

import org.hibernate.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import com.ge.capital.dms.entity.DMSAudit;
import com.ge.capital.dms.entity.DocType;
import com.ge.capital.dms.utility.DmsUtilityConstants;
import com.ge.capital.dms.utility.DmsUtilityService;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Component
@Configuration
@EnableTransactionManagement
public class DocumentServiceDAO {

	@PersistenceUnit
	private EntityManagerFactory emf;

	@Autowired
	private DmsUtilityService dmsUtilityService;

	private final Logger log = Logger.getLogger(this.getClass());

	@Transactional
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@SuppressFBWarnings(value = { "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", "DMI_INVOKING_TOSTRING_ON_ARRAY",
			"DLS_DEAD_LOCAL_STORE", "" }, justification = "let me just make the build pass")
	public HashMap<String, String> getDocumentProperties(String docType, String documentId) {

		Map docPropMap = null;
		EntityManager em = emf.createEntityManager();

		try {
			Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docTypeMappingResource);

			String selectKey = docType + ".docprop.select";
			String fromKey = docType + ".docprop.from";
			String whereKey = docType + ".docprop.where";

			String documentPropertiesQuery = props.getProperty(selectKey) + " " + props.getProperty(fromKey) + " "
					+ props.getProperty(whereKey);

			Query docPropQuery = em.createQuery(documentPropertiesQuery);
			docPropQuery.setParameter("documentId", documentId);
			List<Object[]> docPropLst = docPropQuery.getResultList();
			String respDocPropAttrString = props.getProperty(docType + ".docprop.result");
			if (respDocPropAttrString != null) {
				String respDocPropAttrArry[] = respDocPropAttrString.split(",");
				int respDocPropAttrSize = respDocPropAttrArry.length;

				// setting the document properties
				for (Object[] obj : docPropLst) {
					docPropMap = new HashMap<String, String>();
					for (int index = 0; index < respDocPropAttrSize; index++) {
						if (obj[index] instanceof Long)
							docPropMap.put(respDocPropAttrArry[index], (Long) obj[index]);
						else if (obj[index] instanceof Date)
							docPropMap.put(respDocPropAttrArry[index],
									new SimpleDateFormat("MM/dd/YYYY").format((java.util.Date) obj[index]));
						else if (obj[index] instanceof Timestamp) {
							docPropMap.put(respDocPropAttrArry[index],
									new SimpleDateFormat("MM/dd/YYYY HH:mm:ss").format((java.util.Date) obj[index]));
						} else if (obj[index] instanceof Float) {
							docPropMap.put(respDocPropAttrArry[index], String.valueOf(obj[index]));
						} else if (obj[index] instanceof Integer) {
							docPropMap.put(respDocPropAttrArry[index], String.valueOf(obj[index]));
						} else
							docPropMap.put(respDocPropAttrArry[index], (String) obj[index]);
					}
				}
			}
			return (HashMap<String, String>) docPropMap;
		} catch (Exception e) {
			// TODO: handle exception
			log.error(e.getMessage(), e);
		} finally {
			em.close();
		}
		return (HashMap<String, String>) docPropMap;

	}

	@Transactional
	public String deleteDoc(String docId, String docType) {
		EntityManager em = emf.createEntityManager();
		Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docIdMappingResource);
		String deleteKey = "deleteDoc.delete.clause";
		String fromKey = "deleteDoc.from.clause";
		String whereKey = "deleteDoc.where.clause";
		String deleteQuery = props.getProperty(deleteKey) + " " + props.getProperty(fromKey) + " "
				+ props.getProperty(whereKey);
		try {
			Query result = em.createQuery(deleteQuery);
			result.setParameter("docId", docId);
			em.getTransaction().begin();
			int count = result.executeUpdate() + deleteFromTable(docId, docType);
			log.info("Total rows updated :" + count);
			em.getTransaction().commit();
			return "deleted";
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return "Unable to Delete";
		} finally {
			em.close();
		}
	}
	
	@Transactional
	public String deleteDocuSignDoc(String docId) {
		EntityManager em = emf.createEntityManager();
		Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docIdMappingResource);
		String deleteKey = "deleteDoc.delete.clause";
		String fromKey = "deleteDoc.from.clause";
		String whereKey = "deleteDoc.where.clause";
		String deleteQuery = props.getProperty(deleteKey) + " " + props.getProperty(fromKey) + " "
				+ props.getProperty(whereKey);
		try {
			Query result = em.createQuery(deleteQuery);
			result.setParameter("docId", docId);
			em.getTransaction().begin();
			int count = result.executeUpdate() + deleteFromDocuSignTable(docId);
			log.info("Total rows delete from DocuSign table  :" + count);
			em.getTransaction().commit();
			return "deleted";
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return "Unable to Delete";
		} finally {
			em.close();
		}
	}

	@Transactional
	public int deleteFromTable(String docId, String docType) {
		EntityManager em = emf.createEntityManager();
		Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docIdMappingResource);
		try {
			String deleteKey = docType + ".delete.clause";
			String fromKey = docType + ".from.clause";
			String whereKey = docType + ".where.clause";
			String deleteQuery = props.getProperty(deleteKey) + " " + props.getProperty(fromKey) + " "
					+ props.getProperty(whereKey);
			Query result = em.createQuery(deleteQuery);
			result.setParameter("docId", docId);
			em.getTransaction().begin();
			int count = result.executeUpdate();
			log.info("Rows deleted from " + docType + " table is " + count);
			em.getTransaction().commit();
			return count;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			em.close();
		}
		return 0;
	}
	
	@Transactional
	public int deleteFromDocuSignTable(String docId) {
		EntityManager em = emf.createEntityManager();
		try {
			String deleteQuery = "DELETE FROM DocuSignMessageStatus WHERE docId=:docId";
			Query result = em.createQuery(deleteQuery);
			result.setParameter("docId", docId);
			em.getTransaction().begin();
			int count = result.executeUpdate();
			//log.info("Rows deleted from DocuSign  table is " + count);
			this.log.info("Deleted row from the DocuSign table with DocId : "+docId);
			em.getTransaction().commit();
			return count;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			em.close();
		}
		return 0;
	}

	@Transactional
	public void updateAuditInfo(String docId, String event, String docType, String loggedinUser, String status,
			String message) {
		// TODO Auto-generated method stub
		EntityManager em = emf.createEntityManager();
		try {
			em.joinTransaction();
			Session session = em.unwrap(org.hibernate.Session.class);
			// String timeStamp = new
			// SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
			DMSAudit audit = new DMSAudit();
			audit.setDocId(docId);
			audit.setDocType(docType);
			audit.setEvent(event);
			audit.setEventDetails(message);
			audit.setEventStatus(status);
			audit.setEventTime(new Timestamp(System.currentTimeMillis()));
			audit.setPerformer(loggedinUser);
			session.save(audit);
		} catch (Exception e) {
			System.out.println("Unable to update the audit");
			log.error(e.getMessage(), e);
		} finally {
			em.close();
		}
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public List<DocType> getDocTypes() {
		// TODO Auto-generated method stub
		EntityManager em = emf.createEntityManager();
		List<DocType> resultList = null;

		try {
			String searchQuery = "SELECT type_id,type_label,subtype_id,subtype_label from DocType ORDER BY type_label,subtype_label";
			Query selectQuery = em.createQuery(searchQuery);
			resultList = selectQuery.getResultList();

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			em.close();
		}
		return resultList;
	}

}
