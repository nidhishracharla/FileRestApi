package com.ge.capital.dms.entity;

import java.io.Serializable;
import java.sql.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "historical_orig_doc")
public class HistoricalOrigDoc implements Serializable {

	private static final long serialVersionUID = 4L;

	@Id
	private String docSubtype;
	private String welcomePackage;
	private String syndicationPackage;
	private String physicalStorage;
	private String phy_stg_not_sent;
	private String partyNumber;
	private String partyName;
	private String partyDealType;
	private String accountId;
	private String accountName;
	private String opportunityId;
	private String creditNumber;
	private String lobCode;
	private String legalEntityName;
	private String lwSequenceNum;
	private String contractDealType;
	private String lw_entity_type;
	private String migrationEntityType;
	private String origProposalId;
	private String origCustomerName;
	private String origTransactionId;
	private String origUserAdded;
	private Date origEfileCreateDate;
	private String origDocType;
	private Date origSiebelUpdateDt;
	private String origCustomerDockey;
	private String origDockey;
	private String origPlatform;
	private String origRegion;
	private String origEntityType;
	private String origEntityTypeId;
	private String origContractId;
	private String origCreditId;
	private String origDeleteFlag;
	private String origIsFinalDoc;
	private String origQuoteId;
	private String origSumId;
	private String origSumStatus;
	private String origSystemId;
	private String origRetentionCode;
	private String origStatus;
	private String origDocSource;
	private String origSourceType;
    private String filenetguid;
    private String documenttitle;
    private String docId;
    /*@ManyToOne
 	@JoinColumn(name = "docId")
 	private CommonDoc commonDoc;

 	public CommonDoc getCommonDoc() {
 		return commonDoc;
 	}*/

	public String getDocSubtype() {
		return docSubtype;
	}

	public void setDocSubtype(String docSubtype) {
		this.docSubtype = docSubtype;
	}

	public String getWelcomePackage() {
		return welcomePackage;
	}

	public void setWelcomePackage(String welcomePackage) {
		this.welcomePackage = welcomePackage;
	}

	public String getSyndicationPackage() {
		return syndicationPackage;
	}

	public void setSyndicationPackage(String syndicationPackage) {
		this.syndicationPackage = syndicationPackage;
	}

	public String getPhysicalStorage() {
		return physicalStorage;
	}

	public void setPhysicalStorage(String physicalStorage) {
		this.physicalStorage = physicalStorage;
	}

	public String getPhy_stg_not_sent() {
		return phy_stg_not_sent;
	}

	public void setPhy_stg_not_sent(String phy_stg_not_sent) {
		this.phy_stg_not_sent = phy_stg_not_sent;
	}

	public String getPartyNumber() {
		return partyNumber;
	}

	public void setPartyNumber(String partyNumber) {
		this.partyNumber = partyNumber;
	}

	public String getPartyName() {
		return partyName;
	}

	public void setPartyName(String partyName) {
		this.partyName = partyName;
	}

	public String getPartyDealType() {
		return partyDealType;
	}

	public void setPartyDealType(String partyDealType) {
		this.partyDealType = partyDealType;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getOpportunityId() {
		return opportunityId;
	}

	public void setOpportunityId(String opportunityId) {
		this.opportunityId = opportunityId;
	}

	public String getCreditNumber() {
		return creditNumber;
	}

	public void setCreditNumber(String creditNumber) {
		this.creditNumber = creditNumber;
	}

	public String getLobCode() {
		return lobCode;
	}

	public void setLobCode(String lobCode) {
		this.lobCode = lobCode;
	}

	public String getLegalEntityName() {
		return legalEntityName;
	}

	public void setLegalEntityName(String legalEntityName) {
		this.legalEntityName = legalEntityName;
	}

	public String getLwSequenceNum() {
		return lwSequenceNum;
	}

	public void setLwSequenceNum(String lwSequenceNum) {
		this.lwSequenceNum = lwSequenceNum;
	}

	public String getContractDealType() {
		return contractDealType;
	}

	public void setContractDealType(String contractDealType) {
		this.contractDealType = contractDealType;
	}

	public String getLw_entity_type() {
		return lw_entity_type;
	}

	public void setLw_entity_type(String lw_entity_type) {
		this.lw_entity_type = lw_entity_type;
	}

	public String getMigrationEntityType() {
		return migrationEntityType;
	}

	public void setMigrationEntityType(String migrationEntityType) {
		this.migrationEntityType = migrationEntityType;
	}

	public String getOrigProposalId() {
		return origProposalId;
	}

	public void setOrigProposalId(String origProposalId) {
		this.origProposalId = origProposalId;
	}

	public String getOrigCustomerName() {
		return origCustomerName;
	}

	public void setOrigCustomerName(String origCustomerName) {
		this.origCustomerName = origCustomerName;
	}

	public String getOrigTransactionId() {
		return origTransactionId;
	}

	public void setOrigTransactionId(String origTransactionId) {
		this.origTransactionId = origTransactionId;
	}

	public String getOrigUserAdded() {
		return origUserAdded;
	}

	public void setOrigUserAdded(String origUserAdded) {
		this.origUserAdded = origUserAdded;
	}

	public Date getOrigEfileCreateDate() {
		return (Date) origEfileCreateDate.clone();
	}

	public void setOrigEfileCreateDate(Date origEfileCreateDate) {
		this.origEfileCreateDate = (Date)origEfileCreateDate.clone();
	}

	public String getOrigDocType() {
		return origDocType;
	}

	public void setOrigDocType(String origDocType) {
		this.origDocType = origDocType;
	}

	public String getOrigCustomerDockey() {
		return origCustomerDockey;
	}

	public void setOrigCustomerDockey(String origCustomerDockey) {
		this.origCustomerDockey = origCustomerDockey;
	}

	public Date getOrigSiebelUpdateDt() {
		return (Date)origSiebelUpdateDt.clone();
	}

	public void setOrigSiebelUpdateDt(Date origSiebelUpdateDt) {
		this.origSiebelUpdateDt = (Date)origSiebelUpdateDt.clone();
	}

	public String getOrigDockey() {
		return origDockey;
	}

	public void setOrigDockey(String origDockey) {
		this.origDockey = origDockey;
	}

	public String getOrigPlatform() {
		return origPlatform;
	}

	public void setOrigPlatform(String origPlatform) {
		this.origPlatform = origPlatform;
	}

	public String getOrigRegion() {
		return origRegion;
	}

	public void setOrigRegion(String origRegion) {
		this.origRegion = origRegion;
	}

	public String getOrigEntityType() {
		return origEntityType;
	}

	public void setOrigEntityType(String origEntityType) {
		this.origEntityType = origEntityType;
	}

	public String getOrigEntityTypeId() {
		return origEntityTypeId;
	}

	public void setOrigEntityTypeId(String origEntityTypeId) {
		this.origEntityTypeId = origEntityTypeId;
	}

	public String getOrigContractId() {
		return origContractId;
	}

	public void setOrigContractId(String origContractId) {
		this.origContractId = origContractId;
	}

	public String getOrigCreditId() {
		return origCreditId;
	}

	public void setOrigCreditId(String origCreditId) {
		this.origCreditId = origCreditId;
	}

	public String getOrigDeleteFlag() {
		return origDeleteFlag;
	}

	public void setOrigDeleteFlag(String origDeleteFlag) {
		this.origDeleteFlag = origDeleteFlag;
	}

	public String getOrigIsFinalDoc() {
		return origIsFinalDoc;
	}

	public void setOrigIsFinalDoc(String origIsFinalDoc) {
		this.origIsFinalDoc = origIsFinalDoc;
	}

	public String getOrigQuoteId() {
		return origQuoteId;
	}

	public void setOrigQuoteId(String origQuoteId) {
		this.origQuoteId = origQuoteId;
	}

	public String getOrigSumId() {
		return origSumId;
	}

	public void setOrigSumId(String origSumId) {
		this.origSumId = origSumId;
	}

	public String getOrigSumStatus() {
		return origSumStatus;
	}

	public void setOrigSumStatus(String origSumStatus) {
		this.origSumStatus = origSumStatus;
	}

	public String getOrigSystemId() {
		return origSystemId;
	}

	public void setOrigSystemId(String origSystemId) {
		this.origSystemId = origSystemId;
	}

	public String getOrigRetentionCode() {
		return origRetentionCode;
	}

	public void setOrigRetentionCode(String origRetentionCode) {
		this.origRetentionCode = origRetentionCode;
	}

	public String getOrigStatus() {
		return origStatus;
	}

	public void setOrigStatus(String origStatus) {
		this.origStatus = origStatus;
	}

	public String getOrigDocSource() {
		return origDocSource;
	}

	public void setOrigDocSource(String origDocSource) {
		this.origDocSource = origDocSource;
	}

	public String getOrigSourceType() {
		return origSourceType;
	}

	public void setOrigSourceType(String origSourceType) {
		this.origSourceType = origSourceType;
	}

	public String getFilenetguid() {
		return filenetguid;
	}

	public void setFilenetguid(String filenetguid) {
		this.filenetguid = filenetguid;
	}

	public String getDocumenttitle() {
		return documenttitle;
	}

	public void setDocumenttitle(String documenttitle) {
		this.documenttitle = documenttitle;
	}

	public String getDocId() {
		return docId;
	}

	public void setDocId(String docId) {
		this.docId = docId;
	}

}
