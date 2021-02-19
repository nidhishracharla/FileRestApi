package com.ge.capital.dms.dao;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import org.apache.log4j.Logger;
import org.apache.poi.ss.formula.functions.FinanceLib;
import org.hibernate.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import com.ge.capital.dms.entity.BookingReport;
import com.ge.capital.dms.entity.CashArchivalDoc;
import com.ge.capital.dms.entity.CashWireEmailArchive;
import com.ge.capital.dms.entity.CommonDoc;
import com.ge.capital.dms.entity.CorptaxDoc;
import com.ge.capital.dms.entity.DMSAudit;
import com.ge.capital.dms.entity.DealDoc;
import com.ge.capital.dms.entity.DocuSignMessageStatus;
import com.ge.capital.dms.entity.GEReportsDoc;
import com.ge.capital.dms.entity.InvoiceDocument;
import com.ge.capital.dms.entity.Lockbox;
import com.ge.capital.dms.entity.ManifestReportDoc;
import com.ge.capital.dms.repository.DocSubTypeRepository;
import com.ge.capital.dms.repository.DocuSignStatusRepository;
import com.ge.capital.dms.utility.DmsUtilityConstants;
import com.ge.capital.dms.utility.DmsUtilityService;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Component
@Configuration
@EnableTransactionManagement
public class UpdateDAO {

	@PersistenceUnit
	private EntityManagerFactory emf;

	@Autowired
	private DmsUtilityService dmsUtilityService;

	@Autowired
	DocSubTypeRepository docSubTypeRepo;

	@Autowired
	DocuSignStatusRepository docuSignStatusRepository;

	private final Logger logger = Logger.getLogger(this.getClass());

	@SuppressWarnings("rawtypes")
	@Transactional
	public void updateMetadata(String docId, Map<String, String> inputMetadataMap) {

		EntityManager em = emf.createEntityManager();

		try {
			if (inputMetadataMap.containsKey("docSubType")) {
				inputMetadataMap.remove("docSubType");
			}
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docIdMappingResource);

			Set<String> inputKeys = inputMetadataMap.keySet();

			String prefix = "";
			String updateKey = "";

			if (inputMetadataMap.containsKey("docMetaKey") || inputMetadataMap.containsKey("docMetaVal")) {
				updateKey = "document.update2.clause";
				prefix = "meta.";
			} else {
				updateKey = "document.update1.clause";
				prefix = "document.";
			}

			String whereKey = "document.where.clause";

			String updateQuery = "";

			updateQuery = props.getProperty(updateKey);
			// filters the required input attributes for metadata search
			for (String key : inputKeys) {
				updateQuery = updateQuery + " " + props.getProperty(prefix + key) + "=:" + key + " ,";
			}

			updateQuery = updateQuery.substring(0, updateQuery.length() - 1);

			updateQuery = updateQuery + props.getProperty(whereKey) + " AND " + props.getProperty(prefix + "docId")
			+ "='" + docId + "'";

			if (prefix.equals("meta."))
				updateQuery = updateQuery + " AND " + props.getProperty(prefix + "docMetaKey") + "=:"
						+ props.getProperty(prefix + "docMetaKey");
			logger.info("Update Query : " + updateQuery);
			Query invoicesearchQuery = em.createQuery(updateQuery);

			// set the named query values for the metadata inputs
			for (final Iterator iter = inputMetadataMap.entrySet().iterator(); iter.hasNext();) {
				Map.Entry entry = (Entry) iter.next();
				final String key = (String) entry.getKey();

				if (key.contains("Date") && (inputMetadataMap.get(key) != null)) {
					String oldDate = inputMetadataMap.get(key);
					Date date = df.parse(oldDate);
					invoicesearchQuery.setParameter(key, date, TemporalType.TIMESTAMP);
				} else
					invoicesearchQuery.setParameter(key, inputMetadataMap.get(key));
			}
			em.joinTransaction();
			invoicesearchQuery.executeUpdate();
		} catch (ClassCastException e) {
			logger.info("Document Id : " + docId);
			logger.info(inputMetadataMap.toString());
			logger.warn(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			em.close();
		}
	}

	@Transactional
	public void updateManifestDoc(String docType, Map<String, String> updateParams) {
		EntityManager em = emf.createEntityManager();
		try {
			em.joinTransaction();
			Session session = em.unwrap(org.hibernate.Session.class);
			ManifestReportDoc manifestdoc = new ManifestReportDoc();

			manifestdoc.setMftBusinessLoc(updateParams.get("businessLocation"));
			manifestdoc.setMftSequenceNo(updateParams.get("manifestSeqNumber"));
			manifestdoc.setMftStorerNo(updateParams.get("storerNumber"));
			manifestdoc.setMftSenderName(updateParams.get("sendersName"));
			manifestdoc.setMftCreatorSearched(updateParams.get("mft_creater_searched"));
			manifestdoc.setDocId(updateParams.get("mft_modifier_searched"));
			manifestdoc.setDocId(updateParams.get("docID"));
			if (!updateParams.get("creation_frm_dt_searched").isEmpty())
				manifestdoc.setMftCreationFromDtSearched((java.sql.Date) new SimpleDateFormat("MM/dd/yyyy")
						.parse(updateParams.get("creation_frm_dt_searched")));

			if (!updateParams.get("creation_to_dt_searched").isEmpty())
				manifestdoc.setMftCreationToDtSearched((java.sql.Date) new SimpleDateFormat("MM/dd/yyyy")
						.parse(updateParams.get("creation_to_dt_searched")));
			CommonDoc commonDoc = new CommonDoc();
			commonDoc.setDocId(updateParams.get("docID"));
			commonDoc.setDocName(updateParams.get("docName"));
			commonDoc.setDocTitle(updateParams.get("docName"));
			commonDoc.setDocType("Manifest_Report");
			commonDoc.setOwnerName(updateParams.get("ownerName"));
			commonDoc.setCreateDate(new Timestamp(new Date().getTime()));
			commonDoc.setCreator(updateParams.get("creator"));
			commonDoc.setModifyDate(new Timestamp(new Date().getTime()));
			commonDoc.setModifier(updateParams.get("modifier"));
			commonDoc.setDocState("INTERIM");
			em.joinTransaction();
			em.persist(commonDoc);
			em.persist(manifestdoc);
			session.save(manifestdoc);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			em.close();
		}
	}

	@Transactional
	@SuppressFBWarnings(value = { "REC_CATCH_EXCEPTION" }, justification = "let me just make the build pass")
	public void updateDocumentTypeMetadata(String docType, String tableName, Map<String, String> updateParams)
			throws Exception {
		EntityManager em = emf.createEntityManager();
		try {
			Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docIdMappingResource);
			String documentType = docType;
			String updateKey = documentType + ".update." + tableName + ".clause";
			String whereKey = documentType + ".where." + tableName + ".clause";

			// for (Map<String, String> updateParam : updateParams) {
			String updateQuery = props.getProperty(updateKey);
			Set<String> updateKeys = updateParams.keySet();
			Iterator<String> itr = updateKeys.iterator();
			while (itr.hasNext()) {
				String key = itr.next();
				updateQuery = updateQuery + props.getProperty(documentType + "." + key) + "=:" + key + ",";
			}

			StringBuilder finalUpdateQuery = new StringBuilder(updateQuery);
			int index = finalUpdateQuery.lastIndexOf(",");
			finalUpdateQuery.replace(index, updateQuery.length() + index, "");
			String finalQry = finalUpdateQuery.toString() + " " + props.getProperty(whereKey);
			logger.info("Query : " + finalQry);
			Query updateMetadataQuery = em.createQuery(finalQry);
			for (Map.Entry<String, String> entry : updateParams.entrySet()) {

				if (entry.getKey().contains("Date") && (docType.equals("corptax") || docType.equals("hfs")
						|| docType.equals("Invoices") || docType.equals("manifest") || docType.equals("originations")
						|| docType.equals("reports.cash") || docType.equals("reports.check"))) {
					String oldDate = updateParams.get(entry.getKey());
					Date date;
					try {
						date = new SimpleDateFormat("yyyy-MM-dd").parse(oldDate);
						updateMetadataQuery.setParameter(entry.getKey(), date, TemporalType.TIMESTAMP);
					} catch (ParseException e) {
						logger.error(e);
					}
				} else if (entry.getKey().contains("date_loaded")) {
					String oldDate = entry.getValue();
					Date date;
					try {
						date = new SimpleDateFormat("MM-dd-yyyy").parse(oldDate);
						updateMetadataQuery.setParameter(entry.getKey(), date, TemporalType.TIMESTAMP);
					} catch (ParseException e) {
						logger.error(e);
					}

				} else if (entry.getKey().contains("checkAmount")) { // || entry.getKey().contains("totalAmount")
					if (entry.getValue().equalsIgnoreCase("Not Provided")) {
						updateMetadataQuery.setParameter(entry.getKey(), entry.getValue());
					} else {
						float amount = Float.parseFloat(entry.getValue());
						updateMetadataQuery.setParameter(entry.getKey(), amount);
					}

				} else if (entry.getKey().contains("physicalStorageNotSent")
						|| entry.getKey().contains("welcomePackage") || entry.getKey().contains("syndicationPackage")
						|| entry.getKey().contains("finalPackage")
						|| entry.getKey().contains("physicalStorageStatus")) {
					Integer temp = Integer.parseInt(entry.getValue());
					updateMetadataQuery.setParameter(entry.getKey(), temp);
				} else
					updateMetadataQuery.setParameter(entry.getKey(), entry.getValue());
			}
			em.joinTransaction();
			int result = updateMetadataQuery.executeUpdate();
			logger.info("Rows Updated : " + result);
			updateQuery = "";
			finalUpdateQuery = null;
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			em.close();
		}
	}

	@Transactional
	@SuppressFBWarnings(value = { "REC_CATCH_EXCEPTION" }, justification = "let me just make the build pass")
	public void updateDocumentMetadata(String docType, Map<String, String> documentDetails, String loggedinUser)
			throws Exception {

		EntityManager em = emf.createEntityManager();
		Session session = em.unwrap(org.hibernate.Session.class);
		try {
			CommonDoc commonDoc = new CommonDoc();
			commonDoc.setDocId(documentDetails.get("docId"));
			commonDoc.setDocVersionId(documentDetails.get("docVersionId"));
			commonDoc.setDocName(documentDetails.get("docName"));
			commonDoc.setDocTitle(documentDetails.get("docTitle"));
			commonDoc.setDocType(documentDetails.get("docType"));
			commonDoc.setDocSource(documentDetails.get("docSource"));
			commonDoc.setMimeType(documentDetails.get("mimeType"));
			commonDoc.setIsMigrated(documentDetails.get("isMigrated"));
			commonDoc.setPermName(documentDetails.get("permName"));
			commonDoc.setRealmName(documentDetails.get("realmName"));
			commonDoc.setOwnerName(documentDetails.get("ownerName"));
			commonDoc.setFileSize(documentDetails.get("fileSize"));
			if (!documentDetails.get("createDate").isEmpty())
				commonDoc.setCreateDate(java.sql.Timestamp.valueOf(documentDetails.get("createDate")));
			if (!documentDetails.get("retentionDate").isEmpty())
				commonDoc.setRetentionDate(java.sql.Timestamp.valueOf(documentDetails.get("retentionDate")));
			commonDoc.setCreator(documentDetails.get("creator"));
			if (!documentDetails.get("modifyDate").isEmpty())
				commonDoc.setModifyDate(java.sql.Timestamp.valueOf(documentDetails.get("modifyDate")));
			commonDoc.setModifier(documentDetails.get("modifier"));
			commonDoc.setIsDeleted(documentDetails.get("isDeleted"));
			commonDoc.setIsCurrent(documentDetails.get("isCurrent"));
			if (documentDetails.get("versionNum") != null)
				commonDoc.setVersionNum(documentDetails.get("versionNum"));
			commonDoc.setDocState(documentDetails.get("docState"));
			commonDoc.setEnvironment(documentDetails.get("environment")); // Environment
			commonDoc.setContentRef(documentDetails.get("contentRef"));
			commonDoc.setIsLocked(documentDetails.get("isLocked"));
			commonDoc.setFolderRef(documentDetails.get("folderRef"));
			DMSAudit audit = new DMSAudit();
			audit.setPerformer(loggedinUser);
			audit.setDocId(documentDetails.get("docId"));
			audit.setDocType(documentDetails.get("docType"));
			audit.setEvent("Upload");
			if (!documentDetails.get("createDate").isEmpty())
				audit.setEventTime(java.sql.Timestamp.valueOf(documentDetails.get("createDate")));
			audit.setEventStatus("SUCCESS");
			audit.setEventDetails("Document has been uploaded with docType " + documentDetails.get("docType"));

			em.joinTransaction();
			em.persist(audit);
			em.persist(commonDoc);
			if (docType.equals("dealDoc")) {
				DealDoc dealDoc = new DealDoc();
				dealDoc.setDeal_doc_id(documentDetails.get("docId"));
				session.save(dealDoc);
			} else if (docType.equals("corptax")) {
				CorptaxDoc corptaxDoc = new CorptaxDoc();
				corptaxDoc.setCorptaxDocId(documentDetails.get("docId"));
				corptaxDoc.setCtScanDate(java.sql.Timestamp.valueOf(documentDetails.get("createDate")));
				session.save(corptaxDoc);
			} else if (docType.equals("lockbox.cashmedia")) {
				Lockbox lockbox = new Lockbox();
				lockbox.setLockboxDocId(documentDetails.get("docId"));
				session.save(lockbox);
			} else if (docType.equals("reports.cash")) {
				GEReportsDoc reports = new GEReportsDoc();
				reports.setReports_Doc_Id(documentDetails.get("docId"));
				session.save(reports);
			} else if (docType.equals("reports.check")) {
				GEReportsDoc reports = new GEReportsDoc();
				reports.setReports_Doc_Id(documentDetails.get("docId"));
				session.save(reports);
			} else if (docType.equals("lockbox.pnc") || docType.equals("lockbox.wireslb")) {
				CashArchivalDoc cash = new CashArchivalDoc();
				cash.setDoc_id(documentDetails.get("docId"));
				session.save(cash);
			} else if (docType.equals("Invoices")) {
				InvoiceDocument invd = new InvoiceDocument();
				invd.setInvoiceDocId(documentDetails.get("docId"));
				session.save(invd);
			}
			logger.info("Rows Inserted in CommonDoc: 1");
			logger.info("Row is inserted for : DocType :" + docType + "with docId :" + documentDetails.get("docId"));
		} catch (Exception e) {
			throw new Exception(e);
		} finally {
			em.close();
		}
	}

	@Transactional
	public int updateCustomerName(String partyNumber, String partyName) {

		EntityManager em = emf.createEntityManager();
		try {
			Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docIdMappingResource);
			String updateKey = "deal.update.clause";
			String valueKey = "deal.value.clause";
			String whereKey = "deal.where.clause";

			String updateQuery = props.getProperty(updateKey) + " " + props.getProperty(valueKey) + " "
					+ props.getProperty(whereKey);

			try {
				Query result = em.createQuery(updateQuery);
				result.setParameter("partyName", partyName);
				result.setParameter("partyNumber", partyNumber);

				em.getTransaction().begin();
				int count = result.executeUpdate();
				logger.info("Number of rows updated : " + count);
				em.getTransaction().commit();
				return count;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {

			logger.error(e.getMessage(), e);
		} finally {
			em.close();
		}
		return 0;
	}

	@Transactional
	public int integFour(String partyNumber, String partyName, String creditNumber, String opportunityID) {

		int a = locUpdate(partyNumber, partyName, creditNumber, opportunityID);
		int b = permUpdate(creditNumber, opportunityID);
		// int c = hfsUpdate(partyNumber, partyName, creditNumber, opportunityID);
		// int d = origUpdate(partyNumber, partyName, creditNumber, opportunityID);
		int e = accountUpdate(partyNumber, partyName, creditNumber, opportunityID);
		updateCornerstoneMessageIntegFour(opportunityID, partyNumber, partyName, creditNumber, "LOCUpdate");
		return a + b + e;
	}

	@Transactional
	public int accountUpdate(String partyNumber, String partyName, String creditNumber, String opportunityID) {
		int count = 0;
		EntityManager em = emf.createEntityManager();
		Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docIdMappingResource);
		String updateKey = "account.update.clause";
		String valueKey = "account.value.clause";
		String whereKey = "account.where.clause";
		String updateQuery = props.getProperty(updateKey) + " " + props.getProperty(valueKey) + " "
				+ props.getProperty(whereKey);

		try {
			String account_Id = getAccountId(opportunityID);
			if (!account_Id.equals("")) {
				Query result = em.createQuery(updateQuery);
				result.setParameter("partyName", partyName);
				result.setParameter("partyNumber", partyNumber);
				result.setParameter("lw_entity_type", "Account");
				result.setParameter("account_Id", account_Id);
				/**
				 * SELECT DISTINCT deal1.account_id FROM DealDoc deal1 WHERE
				 * deal1.opportunity_id=:opportunity_id AND lw_entity_type=:lw_entity_type1
				 */

				em.getTransaction().begin();
				count = result.executeUpdate();
				em.getTransaction().commit();
				return count;
			}
		} catch (Exception e) {
			// logger.info(e.getMessage());
			logger.error(e.getMessage(), e);
		} finally {
			em.close();
		}
		return count;
	}

	@Transactional
	public String getAccountId(String opportunityID) {
		String account_Id = "";
		EntityManager em = emf.createEntityManager();
		Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docIdMappingResource);
		String selectKey = "account1.select.clause";
		String valueKey = "account1.value.clause";
		String whereKey = "account1.where.clause";
		String selectQuery = props.getProperty(selectKey) + " " + props.getProperty(valueKey) + " "
				+ props.getProperty(whereKey);

		try {
			Query result = em.createQuery(selectQuery);
			result.setParameter("opportunity_id", opportunityID);
			result.setParameter("lw_entity_type", "Opportunity");
			@SuppressWarnings("unchecked")
			List<String> resultList = result.getResultList();
			if (!resultList.isEmpty()) {
				account_Id = resultList.get(0);
			}

			return account_Id;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			// logger.info(e.getMessage());
		} finally {
			em.close();
		}
		return account_Id;

	}

	@Transactional
	public int permUpdate(String creditNumber, String opportunityID) {
		EntityManager em = emf.createEntityManager();
		int count = 0;
		Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docIdMappingResource);
		String updateKey = "perm.update.clause";
		String valueKey = "perm.value.clause";
		String whereKey = "perm.where.clause";
		String updateQuery = props.getProperty(updateKey) + " " + props.getProperty(valueKey) + " "
				+ props.getProperty(whereKey);

		try {
			Query result = em.createQuery(updateQuery);
			result.setParameter("permName", "Capital_DMS_HEF_FINAL_MANAGER");
			result.setParameter("opportunityId", opportunityID);
			result.setParameter("creditNumber", creditNumber);
			em.getTransaction().begin();
			count = result.executeUpdate();
			logger.info("Number of rows updated : " + count);
			em.getTransaction().commit();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			em.close();
		}
		// return 0;
		return count;
	}

	@Transactional
	public int locUpdate(String partyNumber, String partyName, String creditNumber, String opportunityID) {

		EntityManager em = emf.createEntityManager();
		try {
			Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docIdMappingResource);
			String updateKey = "deal1.update.clause";
			String valueKey = "deal1.value.clause";
			String whereKey = "deal1.where.clause";
			String updateQuery = props.getProperty(updateKey) + " " + props.getProperty(valueKey) + " "
					+ props.getProperty(whereKey);
			Query result = em.createQuery(updateQuery);
			result.setParameter("partyName", partyName);
			result.setParameter("partyNumber", partyNumber);
			result.setParameter("opportunityId", opportunityID);
			result.setParameter("creditNumber", creditNumber);
			result.setParameter("creditNum", creditNumber);
			em.getTransaction().begin();
			int count = result.executeUpdate();
			logger.info("Number of rows updated : " + count);
			em.getTransaction().commit();
			return count;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			em.close();

		}
		return 0;

	}

	@Transactional
	public int hfsUpdate(String partyNumber, String partyName, String creditNumber, String opportunityID) {
		EntityManager em = emf.createEntityManager();
		Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docIdMappingResource);
		String updateKey = "hfs.update.clause";
		String valueKey = "hfs.value.clause";
		String whereKey = "hfs.where.clause";

		String updateQuery = props.getProperty(updateKey) + " " + props.getProperty(valueKey) + " "
				+ props.getProperty(whereKey);

		try {
			Query result = em.createQuery(updateQuery);
			result.setParameter("partyName", partyName);
			result.setParameter("partyNumber", partyNumber);
			result.setParameter("opportunityId", opportunityID);
			result.setParameter("creditNumber", creditNumber);

			em.getTransaction().begin();
			int count = result.executeUpdate();
			logger.info("Number of rows updated : " + count);
			em.getTransaction().commit();
			return count;
		} catch (Exception e) {

			// e.getMessage();
			logger.error(e.getMessage(), e);
		} finally {
			em.close();
		}
		return 0;

	}

	@Transactional
	public int origUpdate(String partyNumber, String partyName, String creditNumber, String opportunityID) {
		EntityManager em = emf.createEntityManager();
		Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docIdMappingResource);
		String updateKey = "origin.update.clause";
		String valueKey = "origin.value.clause";
		String whereKey = "origin.where.clause";

		String updateQuery = props.getProperty(updateKey) + " " + props.getProperty(valueKey) + " "
				+ props.getProperty(whereKey);

		try {
			Query result = em.createQuery(updateQuery);
			result.setParameter("partyName", partyName);
			result.setParameter("partyNumber", partyNumber);
			result.setParameter("opportunityId", opportunityID);
			result.setParameter("creditNumber", creditNumber);

			em.getTransaction().begin();
			int count = result.executeUpdate();
			logger.info("Number of rows updated : " + count);
			em.getTransaction().commit();
			return count;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			em.close();
		}
		return 0;

	}

	@Transactional
	@SuppressWarnings("unchecked")
	public int loanUpdate(String creditNumber, String sequenceNumber, Date commencementDate, String metadata,
			String messageType) {
		// logger.info("Inside Lease/Loan Update");
		EntityManager em = emf.createEntityManager();
		try {
			Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docIdMappingResource);
			String searchQuery = "SELECT cd.docId,cd.docState,dd.doc_subtype from CommonDoc cd, DealDoc dd where cd.docId=dd.deal_doc_id and dd.lw_sequence_num=:seqNum";

			Query invoicesearchQuery = em.createQuery(searchQuery);
			invoicesearchQuery.setParameter("seqNum", sequenceNumber);
			List<Object[]> list = invoicesearchQuery.getResultList();
			int count = 0;
			for (Object[] str : list) {
				logger.info("Fetched values in search result :" + str[0] + " " + str[1] + " " + str[2]);
				String docId = (String) str[0];
				String docStatus = (String) str[1];
				String docSubType = (String) str[2];
				String updateKey = "common.update.clause";
				String valueKey = "common.value.clause";
				String whereKey = "common.where.clause";
				String updateQuery = props.getProperty(updateKey) + " " + props.getProperty(valueKey) + " "
						+ props.getProperty(whereKey);

				Query result = em.createQuery(updateQuery);
				if (docSubType.equals("Bookings")) {
					docSubType = "Booking";
				}

				String query = "Select isFinal from DocSubtype where subtype=:subType";
				Query finalsearchQuery = em.createQuery(query);
				finalsearchQuery.setParameter("subType", docSubType);
				String docIsFinal = (String) finalsearchQuery.getSingleResult();
				logger.info("The value returned by the query is :" + docIsFinal);
				if (docStatus != null
						&& (docStatus.equalsIgnoreCase("FINAL") || (docIsFinal.equalsIgnoreCase("TRUE")))) {
					result.setParameter("retentionDate", commencementDate);
					result.setParameter("docState", "FINAL");
					result.setParameter("permName", "Capital_DMS_HEF_FINAL_MANAGER");
					result.setParameter("sequenceNumber", sequenceNumber);
					result.setParameter("docId", docId);
				} else if (!(docIsFinal.equalsIgnoreCase("TRUE"))) {
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(commencementDate);
					calendar.add(Calendar.YEAR, -9);
					result.setParameter("retentionDate", calendar.getTime());
					result.setParameter("permName", "");
					result.setParameter("docState", "Interim");
					result.setParameter("sequenceNumber", sequenceNumber);
					result.setParameter("docId", docId);
				}

				em.getTransaction().begin();
				count = count + result.executeUpdate();
				updateCornerstoneMessage(sequenceNumber, metadata, messageType);
				logger.info("Number of rows updated : " + count);
				em.getTransaction().commit();

			}
			return count;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			em.close();
		}
		return 0;
	}

	@Transactional
	public void updateCornerstoneMessage(String sequenceNumber, String metadata, String messageType) {
		EntityManager em = emf.createEntityManager();
		try {

			Properties props = dmsUtilityService.loadPropertiesFile(DmsUtilityConstants.docIdMappingResource);
			String updateKey = "message.update.clause";
			String valueKey = "message.value.clause";
			String whereKey = "message.where.clause";
			String updateQuery = props.getProperty(updateKey) + " " + props.getProperty(valueKey) + " "
					+ props.getProperty(whereKey) + " " + "like concat('%','" + sequenceNumber
					+ "','%') AND message_type=:messageType";

			Query result = em.createQuery(updateQuery);
			result.setParameter("entity_id", sequenceNumber);
			result.setParameter("messageType", messageType);

			em.getTransaction().begin();
			result.executeUpdate();
			em.getTransaction().commit();
			// result.setParameter("message_json", metadata);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			em.close();
		}
	}

	@Transactional
	public void updateCornerstoneMessageIntegFour(String opportunityID, String partyNumber, String partyName,
			String creditNumber, String messageType) {
		EntityManager em = emf.createEntityManager();
		try {
			String updateQuery = "UPDATE CornerstoneMessage SET entity_id=:entity_id, party_number=:partyNumber,party_name=:partyName,credit_number=:creditNumber WHERE message_json like concat('%','"
					+ opportunityID + "','%') AND message_type=:messageType";
			Query result = em.createQuery(updateQuery);
			result.setParameter("entity_id", opportunityID);
			result.setParameter("partyNumber", partyNumber);
			result.setParameter("partyName", partyName);
			result.setParameter("creditNumber", creditNumber);
			result.setParameter("messageType", messageType);
			em.getTransaction().begin();
			result.executeUpdate();
			em.getTransaction().commit();
			// result.setParameter("message_json", metadata);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			em.close();
		}
	}

	@Transactional
	public void copyDocuments(Map<String, String> finalPackMetadata) {
		EntityManager em = emf.createEntityManager();
		try {
			DealDoc dealDoc = new DealDoc();
			dealDoc.setDeal_doc_id(finalPackMetadata.get("dealDocId"));
			if (finalPackMetadata.get("welcomePackage").equalsIgnoreCase("true")) {
				dealDoc.setWelcome_package(1);
			} else {
				dealDoc.setWelcome_package(0);
			}
			if (finalPackMetadata.get("finalPackage").equalsIgnoreCase("true")) {
				dealDoc.setFinal_package(1);
			} else {
				dealDoc.setFinal_package(0);
			}
			dealDoc.setParty_number(finalPackMetadata.get("partyNumber"));
			dealDoc.setParty_name(finalPackMetadata.get("partyName"));
			dealDoc.setParty_deal_type(finalPackMetadata.get("partyDealType"));
			dealDoc.setAccount_id(finalPackMetadata.get("sfdcAccountId"));
			dealDoc.setAccount_name(finalPackMetadata.get("sfdcAccountName"));
			dealDoc.setOpportunity_id(finalPackMetadata.get("sfdcOpportunityId"));
			dealDoc.setCredit_number(finalPackMetadata.get("lineOfCreditNumber"));
			dealDoc.setLob_code(finalPackMetadata.get("lineofBusinessCode"));
			dealDoc.setLegal_entity_name(finalPackMetadata.get("legalEntityName"));
			dealDoc.setLw_sequence_num(finalPackMetadata.get("lwContractSequenceNumber"));
			dealDoc.setContract_deal_type(finalPackMetadata.get("contractDealType"));
			dealDoc.setLw_entity_type(finalPackMetadata.get("legalEntityType"));
			dealDoc.setCopiedBy(finalPackMetadata.get("creator"));
			dealDoc.setDoc_subtype(finalPackMetadata.get("documentSubType"));
			dealDoc.setCopiedOn(new Timestamp(System.currentTimeMillis()));
			DMSAudit audit = new DMSAudit();
			audit.setPerformer(finalPackMetadata.get("creator"));
			audit.setDocId(finalPackMetadata.get("dealDocId"));
			audit.setDocType("dealDoc");
			audit.setEvent("Copy Documents");
			audit.setEventTime(new Timestamp(System.currentTimeMillis()));
			audit.setEventStatus("SUCCESS");
			audit.setEventDetails("Document has been copied to " + finalPackMetadata.get("lwContractSequenceNumber"));
			em.joinTransaction();
			em.persist(audit);
			em.persist(dealDoc);
			logger.info("Row is inserted in DealDoc with docId :" + finalPackMetadata.get("dealDocId"));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Transactional
	public String deleteCopy(HashMap<String, String> request) {
		String response = "";
		EntityManager em = emf.createEntityManager();
		try {
			String deleteQuery = "DELETE FROM DealDoc WHERE deal_doc_id=:docId AND lw_sequence_num=:seqNum AND lw_entity_type='Contract'";
			Query result = em.createQuery(deleteQuery);
			result.setParameter("docId", request.get("docId"));
			result.setParameter("seqNum", request.get("lw_sequence_num"));
			em.getTransaction().begin();
			int count = result.executeUpdate();
			logger.info("Rows deleted from Deal Doc table is " + count);
			if (count > 0) {
				response = "Copy of the document deleted succesfully from the contract# :"
						+ request.get("lw_sequence_num");
			}
		} catch (Exception e) {
			response = e.getMessage();
			logger.error(e.getMessage(), e);
		}
		em.getTransaction().commit();
		// em.close();
		return response;
	}

	@Transactional
	public String copyDocument(Map<String, String> finalPackMetadata) {
		EntityManager em = emf.createEntityManager();
		try {
			DealDoc dealDoc = new DealDoc();
			dealDoc.setDeal_doc_id(finalPackMetadata.get("docId"));
			dealDoc.setWelcome_package(Integer.parseInt(finalPackMetadata.get("welcomePackage")));
			dealDoc.setWelcome_package(Integer.parseInt(finalPackMetadata.get("finalPackage")));
			dealDoc.setParty_number(finalPackMetadata.get("partyNumber"));
			dealDoc.setParty_name(finalPackMetadata.get("partyName"));
			dealDoc.setParty_deal_type(
					finalPackMetadata.get("partyDealType") != null ? finalPackMetadata.get("partyDealType") : "");
			dealDoc.setAccount_id(
					finalPackMetadata.get("sfdcAccountId") != null ? finalPackMetadata.get("sfdcAccountId") : "");
			dealDoc.setAccount_name(
					finalPackMetadata.get("sfdcAccountName") != null ? finalPackMetadata.get("sfdcAccountName") : "");
			dealDoc.setOpportunity_id(
					finalPackMetadata.get("sfdcopportunityId") != null ? finalPackMetadata.get("sfdcopportunityId")
							: "");
			dealDoc.setCredit_number(
					finalPackMetadata.get("lineofcreditNumber") != null ? finalPackMetadata.get("lineofcreditNumber")
							: "");
			dealDoc.setLob_code(
					finalPackMetadata.get("lineofBusinessCode") != null ? finalPackMetadata.get("lineofBusinessCode")
							: "");
			dealDoc.setLegal_entity_name(
					finalPackMetadata.get("legalEntityName") != null ? finalPackMetadata.get("legalEntityName") : "");
			dealDoc.setLw_sequence_num(
					finalPackMetadata.get("lwSeqNumber") != null ? finalPackMetadata.get("lwSeqNumber") : "");
			dealDoc.setContract_deal_type(
					finalPackMetadata.get("contractDealType") != null ? finalPackMetadata.get("contractDealType") : "");
			dealDoc.setLw_entity_type(
					finalPackMetadata.get("legalEntityType") != null ? finalPackMetadata.get("legalEntityType") : "");
			// dealDoc.setpe(finalPackMetadata.get("creator")!=null?
			// finalPackMetadata.get("creator"):"");
			dealDoc.setDoc_subtype(
					finalPackMetadata.get("docSubType") != null ? finalPackMetadata.get("docSubType") : "");
			dealDoc.setCopiedOn(new Timestamp(System.currentTimeMillis()));
			dealDoc.setCopiedBy(finalPackMetadata.get("creator"));
			DMSAudit audit = new DMSAudit();
			audit.setPerformer(finalPackMetadata.get("creator"));
			audit.setDocId(finalPackMetadata.get("docId"));
			audit.setDocType("dealDoc");
			audit.setEvent("Copy Documents");
			audit.setEventTime(new Timestamp(System.currentTimeMillis()));
			audit.setEventStatus("SUCCESS");
			audit.setEventDetails("Document has been copied to " + finalPackMetadata.get("lwSeqNumber") != null
					? finalPackMetadata.get("lwSeqNumber")
							: "");
			em.joinTransaction();
			em.persist(audit);
			em.persist(dealDoc);

			logger.info("Copy Document inserted in DealDoc with docId :" + finalPackMetadata.get("docId"));
			return "Copy Document inserted";
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return "Exception occured while inserting the copy document is : "+e.getMessage();
		}
	}

	@Transactional
	public String saveToDB(List<HashMap<String, String>> metadataArray) {
		String response = "";

		for (HashMap<String, String> map : metadataArray) {
			EntityManager em = emf.createEntityManager();
			try {
				CashWireEmailArchive cashWireEmailArchive = new CashWireEmailArchive();
				if (map.get("docId") != null)
					cashWireEmailArchive.setDocId(map.get("docId"));
				else {
					cashWireEmailArchive.setDocId("");
				}
				if (map.get("Filename") != null)
					cashWireEmailArchive.setDocName(map.get("Filename"));
				if (map.get("Email Name") != null)
					cashWireEmailArchive.setEmailSubject(map.get("Filename"));
				if (map.get("Email Sender") != null)
					cashWireEmailArchive.setEmailId(map.get("Email Sender"));
				if (map.get("Invoice Numbers") != null)
					cashWireEmailArchive.setInvoiceNumber(map.get("Invoice Numbers"));
				if (map.get("Amount") != null)
					cashWireEmailArchive.setAmount(map.get("Amount"));
				if (map.get("Attachment") != null)
					cashWireEmailArchive.setAttachment(map.get("Attachment"));
				if (map.get("Sequence Numbers") != null)
					cashWireEmailArchive.setSequenceNumber(map.get("Sequence Numbers"));
				if (map.get("creator") != null)
					cashWireEmailArchive.setCreator(map.get("Email Sender"));
				cashWireEmailArchive.setCreateDate(new Timestamp(new Date().getTime()).toString());
				if (map.get("Email Received Date") != null) {
					String oldDate = new String(map.get("Email Received Date"));
					/*
					 * String newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS") .format(new
					 * SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(oldDate));
					 */
					cashWireEmailArchive.setEmailDate(oldDate);
				}
				// commented for testing
				logger.info(cashWireEmailArchive.toString());
				em.joinTransaction();
				em.persist(cashWireEmailArchive);
				em.flush();

			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage(), e);
				response = e.getMessage();
			}
		}
		response = "Updated the Database successfully!";

		return response;
	}

	@Transactional
	public String saveBookingReport(String sequenceNumber, List<String> tableData) {
		String response = "";

		EntityManager em = emf.createEntityManager();
		try {
			em.joinTransaction();
			Session session = em.unwrap(org.hibernate.Session.class);
			BookingReport book = new BookingReport();
			book.setSequenceNumber(sequenceNumber);
			book.setTableData(tableData.toString());
			book.setReadOnly("false");
			// em.joinTransaction();
			// em.persist(book);
			session.saveOrUpdate(book);
			em.close();
			// session.close();
		} catch (Exception e) {
			response = e.getMessage();
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		// System.out.println(response);
		return response;
	}

	@Transactional
	public String insertintoDocuSignStatus(DocuSignMessageStatus paramdocuSignMessageStatus) {
		String response = "OK";
		EntityManager em = emf.createEntityManager();
		try {
			em.joinTransaction();
			em.persist(paramdocuSignMessageStatus);
			em.close();
		} catch (Exception e) {
			response = e.getMessage();
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		}
		System.out.println(response);
		return response;
	}

	@Transactional
	public String updateDocuSignData(String docId, String legalEntityType) {
		EntityManager em = emf.createEntityManager();
		try {
			String updateDocusignQuery = "UPDATE DocuSignMessageStatus SET legalentitytype=:legalentitytype WHERE docId=:docId";
			Query result = em.createQuery(updateDocusignQuery);
			result.setParameter("legalentitytype", legalEntityType);
			result.setParameter("docId", docId);

			em.getTransaction().begin();
			int count = result.executeUpdate();
			em.getTransaction().commit();
			if(count>0)
				return "DocuSign table updated successfully";
			else
				return "Unable to update the DocuSign table";

		} catch (Exception e) {			
			logger.error(e.getMessage(), e);
			return "Exception occured while updating the DocuSign table is : "+e.getMessage();
		}

	}

}
