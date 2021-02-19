package com.ge.capital.dms.dao;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class SelectDAO {

	@PersistenceUnit
	private EntityManagerFactory emf;

	private final Logger log = Logger.getLogger(this.getClass());

	@Transactional
	public String getData(String opportunityId, String key) {
		// TODO Auto-generated method stub
		String value = "";
		EntityManager em = emf.createEntityManager();
		try {
			String selectQuery = "select " + key
					+ " from CornerstoneMessage where entity_id=:opportunityId and message_type=:messageType";
			log.info("select query : " + selectQuery);
			Query dealSearchQuery = em.createQuery(selectQuery);
			dealSearchQuery.setParameter("opportunityId", opportunityId);
			dealSearchQuery.setParameter("messageType", "LOCUpdate");
			value = (String) dealSearchQuery.getResultList().get(0);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			em.close();
		}
		return value;
	}

	@Transactional
	public String isFileExists(String finalPackName) {
		String result = "";
		EntityManager em = emf.createEntityManager();
		try {
			String selectQuery = "select docId from CommonDoc where docName=:finalPackName";
			log.info("select query : " + selectQuery);
			Query dealSearchQuery = em.createQuery(selectQuery);
			dealSearchQuery.setParameter("finalPackName", finalPackName);
			dealSearchQuery.setFirstResult(0);
			dealSearchQuery.setMaxResults(1);
			if (dealSearchQuery.getResultList().size() > 0) {
				result = (String) dealSearchQuery.getResultList().get(0);
			}
		} catch (Exception e) {
			result = "";
			log.error(e.getMessage(), e);
		} finally {
			em.close();
		}

		return result;
	}

	@Transactional
	public HashMap<String, String> getDocMetadata(String contractNum) {
		HashMap<String, String> resultMetadataList = new HashMap<String, String>();
		EntityManager em = emf.createEntityManager();
		try {
			//String searchQuery = "SELECT DealDoc.lw_sequence_num,DealDoc.party_number,DealDoc.party_name,DealDoc.contract_deal_type,DealDoc.opportunity_id,DealDoc.lw_entity_type,DealDoc.credit_number,DealDoc.account_id,DealDoc.legal_entity_name,DealDoc.lob_code,CommonDoc.creator,DealDoc.party_deal_type,DealDoc.account_name from DealDoc DealDoc,CommonDoc CommonDoc WHERE DealDoc.deal_doc_id = CommonDoc.docId AND DealDoc.lw_sequence_num=:contractNum ORDER BY CommonDoc.modifyDate desc";
			//String searchQuery = "SELECT DealDoc.lw_sequence_num,DealDoc.party_number,DealDoc.party_name,DealDoc.contract_deal_type,DealDoc.opportunity_id,DealDoc.lw_entity_type,DealDoc.credit_number,DealDoc.account_id,DealDoc.legal_entity_name,DealDoc.lob_code,CommonDoc.creator,DealDoc.party_deal_type,DealDoc.account_name from DealDoc DealDoc,CommonDoc CommonDoc WHERE DealDoc.deal_doc_id = CommonDoc.docId AND DealDoc.lw_entity_type='Contract' AND DealDoc.lw_sequence_num=:contractNum AND CommonDoc.docSource is NULL ORDER BY CommonDoc.modifyDate desc";
			String searchQuery = "SELECT DealDoc.lw_sequence_num,DealDoc.party_number,DealDoc.party_name,DealDoc.contract_deal_type,DealDoc.opportunity_id,DealDoc.lw_entity_type,DealDoc.credit_number,DealDoc.account_id,DealDoc.legal_entity_name,DealDoc.lob_code,CommonDoc.creator,DealDoc.party_deal_type,DealDoc.account_name from DealDoc DealDoc,CommonDoc CommonDoc WHERE DealDoc.deal_doc_id = CommonDoc.docId AND DealDoc.lw_entity_type='Contract' AND DealDoc.lw_sequence_num=:contractNum AND (CommonDoc.docSource IS NULL OR CommonDoc.docSource NOT IN('DocuSign')) ORDER BY CommonDoc.modifyDate desc";
			Query invoicesearchQuery = em.createQuery(searchQuery);
			invoicesearchQuery.setParameter("contractNum", contractNum);
			invoicesearchQuery.setFirstResult(0);

			invoicesearchQuery.setMaxResults(1);
			List<Object[]> invSrchResLst = invoicesearchQuery.getResultList();
			String respMtdAttrArry[] = "lwContractSequenceNumber,partyNumber,partyName,contractDealType,sfdcOpportunityId,legalEntityType,lineOfCreditNumber,sfdcAccountId,legalEntityName,lineofBusinessCode,creator,partyDealType,sfdcAccountName"
					.split(",");
			int respMtdAttrSize = respMtdAttrArry.length;
			for (Object[] obj : invSrchResLst) {
				for (int index = 0; index < respMtdAttrSize; index++) {
					resultMetadataList.put(respMtdAttrArry[index], (String) obj[index]);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return resultMetadataList;
	}

	@Transactional
	public HashMap<String, String> getDocMetadataforOpportunityID(String OpportunityID) {
		HashMap<String, String> resultMetadataList = new HashMap<String, String>();
		EntityManager em = emf.createEntityManager();
		try {
			//String searchQuery = "SELECT DealDoc.lw_sequence_num,DealDoc.party_number,DealDoc.party_name,DealDoc.contract_deal_type,DealDoc.opportunity_id,DealDoc.lw_entity_type,DealDoc.credit_number,DealDoc.account_id,DealDoc.legal_entity_name,DealDoc.lob_code,CommonDoc.creator,DealDoc.party_deal_type,DealDoc.account_name from DealDoc DealDoc,CommonDoc CommonDoc WHERE DealDoc.deal_doc_id = CommonDoc.docId AND DealDoc.opportunity_id=:OpportunityID AND DealDoc.lw_entity_type in ('Opportunity' ,'Contract')  ORDER BY CommonDoc.modifyDate desc";
			//String searchQuery = "SELECT DealDoc.lw_sequence_num,DealDoc.party_number,DealDoc.party_name,DealDoc.contract_deal_type,DealDoc.opportunity_id,DealDoc.lw_entity_type,DealDoc.credit_number,DealDoc.account_id,DealDoc.legal_entity_name,DealDoc.lob_code,CommonDoc.creator,DealDoc.party_deal_type,DealDoc.account_name from DealDoc DealDoc,CommonDoc CommonDoc WHERE DealDoc.deal_doc_id = CommonDoc.docId AND DealDoc.opportunity_id=:OpportunityID AND DealDoc.lw_entity_type in ('Opportunity' ,'Contract') AND CommonDoc.docSource is NULL ORDER BY CommonDoc.modifyDate desc";
			String searchQuery = "SELECT DealDoc.lw_sequence_num,DealDoc.party_number,DealDoc.party_name,DealDoc.contract_deal_type,DealDoc.opportunity_id,DealDoc.lw_entity_type,DealDoc.credit_number,DealDoc.account_id,DealDoc.legal_entity_name,DealDoc.lob_code,CommonDoc.creator,DealDoc.party_deal_type,DealDoc.account_name from DealDoc DealDoc,CommonDoc CommonDoc WHERE DealDoc.deal_doc_id = CommonDoc.docId AND DealDoc.opportunity_id=:OpportunityID AND DealDoc.lw_entity_type in ('Opportunity' ,'Contract') AND (CommonDoc.docSource IS NULL OR CommonDoc.docSource NOT IN('DocuSign')) ORDER BY CommonDoc.modifyDate desc";
			Query invoicesearchQuery = em.createQuery(searchQuery);
			invoicesearchQuery.setParameter("OpportunityID", OpportunityID);
			invoicesearchQuery.setFirstResult(0);

			invoicesearchQuery.setMaxResults(1);
			List<Object[]> invSrchResLst = invoicesearchQuery.getResultList();
			String respMtdAttrArry[] = "lwContractSequenceNumber,partyNumber,partyName,contractDealType,sfdcOpportunityId,legalEntityType,lineOfCreditNumber,sfdcAccountId,legalEntityName,lineofBusinessCode,creator,partyDealType,sfdcAccountName"
					.split(",");
			int respMtdAttrSize = respMtdAttrArry.length;
			for (Object[] obj : invSrchResLst) {
				for (int index = 0; index < respMtdAttrSize; index++) {
					resultMetadataList.put(respMtdAttrArry[index], (String) obj[index]);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return resultMetadataList;
	}

	@Transactional
	public HashMap<String, String> getDocMetadata(String contractNum,String OpportunityID) {
		HashMap<String, String> resultMetadataList = new HashMap<String, String>();
		EntityManager em = emf.createEntityManager();
		try {
			//String searchQuery = "SELECT DealDoc.lw_sequence_num,DealDoc.party_number,DealDoc.party_name,DealDoc.contract_deal_type,DealDoc.opportunity_id,DealDoc.lw_entity_type,DealDoc.credit_number,DealDoc.account_id,DealDoc.legal_entity_name,DealDoc.lob_code,CommonDoc.creator,DealDoc.party_deal_type,DealDoc.account_name from DealDoc DealDoc,CommonDoc CommonDoc WHERE DealDoc.deal_doc_id = CommonDoc.docId AND DealDoc.lw_sequence_num=:contractNum AND DealDoc.opportunity_id=:OpportunityID ORDER BY CommonDoc.modifyDate desc";
			//String searchQuery = "SELECT DealDoc.lw_sequence_num,DealDoc.party_number,DealDoc.party_name,DealDoc.contract_deal_type,DealDoc.opportunity_id,DealDoc.lw_entity_type,DealDoc.credit_number,DealDoc.account_id,DealDoc.legal_entity_name,DealDoc.lob_code,CommonDoc.creator,DealDoc.party_deal_type,DealDoc.account_name from DealDoc DealDoc,CommonDoc CommonDoc WHERE DealDoc.deal_doc_id = CommonDoc.docId AND DealDoc.lw_sequence_num=:contractNum AND DealDoc.opportunity_id=:OpportunityID AND DealDoc.lw_entity_type in ('Opportunity' ,'Contract') AND CommonDoc.docSource is NULL ORDER BY CommonDoc.modifyDate desc";
			//String searchQuery = "SELECT DealDoc.lw_sequence_num,DealDoc.party_number,DealDoc.party_name,DealDoc.contract_deal_type,DealDoc.opportunity_id,DealDoc.lw_entity_type,DealDoc.credit_number,DealDoc.account_id,DealDoc.legal_entity_name,DealDoc.lob_code,CommonDoc.creator,DealDoc.party_deal_type,DealDoc.account_name from DealDoc DealDoc,CommonDoc CommonDoc WHERE DealDoc.deal_doc_id = CommonDoc.docId AND DealDoc.lw_sequence_num=:contractNum OR DealDoc.opportunity_id=:OpportunityID AND DealDoc.lw_entity_type in ('Opportunity' ,'Contract') AND CommonDoc.docSource is NULL ORDER BY CommonDoc.modifyDate desc";
			//String searchQuery = "SELECT DealDoc.lw_sequence_num,DealDoc.party_number,DealDoc.party_name,DealDoc.contract_deal_type,DealDoc.opportunity_id,DealDoc.lw_entity_type,DealDoc.credit_number,DealDoc.account_id,DealDoc.legal_entity_name,DealDoc.lob_code,CommonDoc.creator,DealDoc.party_deal_type,DealDoc.account_name from DealDoc DealDoc,CommonDoc CommonDoc WHERE DealDoc.deal_doc_id = CommonDoc.docId AND DealDoc.lw_sequence_num=:contractNum OR DealDoc.opportunity_id=:OpportunityID AND DealDoc.lw_entity_type in ('Opportunity' ,'Contract') AND (CommonDoc.docSource IS NULL OR CommonDoc.docSource NOT IN('DocuSign')) ORDER BY CommonDoc.modifyDate desc";
			String searchQuery = "SELECT DealDoc.lw_sequence_num,DealDoc.party_number,DealDoc.party_name,DealDoc.contract_deal_type,DealDoc.opportunity_id,DealDoc.lw_entity_type,DealDoc.credit_number,DealDoc.account_id,DealDoc.legal_entity_name,DealDoc.lob_code,CommonDoc.creator,DealDoc.party_deal_type,DealDoc.account_name from DealDoc DealDoc,CommonDoc CommonDoc WHERE DealDoc.deal_doc_id = CommonDoc.docId AND DealDoc.lw_sequence_num=:contractNum AND DealDoc.opportunity_id=:OpportunityID AND DealDoc.lw_entity_type in ('Opportunity' ,'Contract') AND (CommonDoc.docSource IS NULL OR CommonDoc.docSource NOT IN('DocuSign')) ORDER BY CommonDoc.modifyDate desc";
			Query invoicesearchQuery = em.createQuery(searchQuery);
			invoicesearchQuery.setParameter("contractNum", contractNum);
			invoicesearchQuery.setParameter("OpportunityID", OpportunityID);
			invoicesearchQuery.setFirstResult(0);

			invoicesearchQuery.setMaxResults(1);
			List<Object[]> invSrchResLst = invoicesearchQuery.getResultList();
			String respMtdAttrArry[] = "lwContractSequenceNumber,partyNumber,partyName,contractDealType,sfdcOpportunityId,legalEntityType,lineOfCreditNumber,sfdcAccountId,legalEntityName,lineofBusinessCode,creator,partyDealType,sfdcAccountName"
					.split(",");
			int respMtdAttrSize = respMtdAttrArry.length;
			for (Object[] obj : invSrchResLst) {
				for (int index = 0; index < respMtdAttrSize; index++) {
					resultMetadataList.put(respMtdAttrArry[index], (String) obj[index]);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return resultMetadataList;
	}

	@Transactional
	public String getUserId(String login) {
		String result = "";
		EntityManager em = emf.createEntityManager();
		try {
			String selectQuery = "select userId from DMSUserDetails where email=:email";
			log.info("select query : " + selectQuery);
			Query dealSearchQuery = em.createQuery(selectQuery);
			dealSearchQuery.setParameter("email", login);
			result = (String) dealSearchQuery.getResultList().get(0);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			em.close();
		}

		return result;
	}

	@Transactional
	public String getFileName(String docId) {
		String result = "";
		EntityManager em = emf.createEntityManager();
		try {
			String selectQuery = "select docName from CommonDoc where docId=:docId";
			log.info("select query : " + selectQuery);
			Query dealSearchQuery = em.createQuery(selectQuery);
			dealSearchQuery.setParameter("docId", docId);
			if (dealSearchQuery.getResultList().size() > 0)
				result = (String) dealSearchQuery.getResultList().get(0);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			em.close();
		}

		return result;

	}

	@Transactional
	public String getReport(String sequenceNumber) {
		String result = "";
		EntityManager em = emf.createEntityManager();
		try {
			String selectQuery = "select tableData from BookingReport where sequenceNumber=:sequenceNumber";
			log.info("select query : " + selectQuery);
			Query dealSearchQuery = em.createQuery(selectQuery);
			dealSearchQuery.setParameter("sequenceNumber", sequenceNumber);
			if (dealSearchQuery.getResultList().size() > 0)
				result = (String) dealSearchQuery.getResultList().get(0);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			em.close();
		}

		return result;
	}

	@Transactional
	public String getPermission(String sequenceNumber) {
		String result = "";
		EntityManager em = emf.createEntityManager();
		try {
			String selectQuery = "select readOnly from BookingReport where sequenceNumber=:sequenceNumber";
			log.info("select query : " + selectQuery);
			Query dealSearchQuery = em.createQuery(selectQuery);
			dealSearchQuery.setParameter("sequenceNumber", sequenceNumber);
			if (dealSearchQuery.getResultList().size() > 0)
				result = (String) dealSearchQuery.getResultList().get(0);
			else
				result = "false";
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			em.close();
		}

		return result;
	}

}
