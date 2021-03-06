package com.ge.capital.dms.entity;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "historical_hfs_doc")
public class HistoricalHfsDoc implements Serializable {

	private static final long serialVersionUID = 4L;

	@Id
	@JoinColumn(name = "hfsDocId")
	private String hfsDocId;
	private String docSubtype;
	private Integer welcomePackage;
	private Integer syndicationPackage;
	private Integer physicalStorage;
	private Integer phyStgNotSent;
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
	private String lwEntityType;
	private String migrationEntityType;
	private String hfsReferenceId;
	private String hfsSrNumber;
	private Timestamp hfsDocDate;
	private String submittalId;
	private String hfsEnvelopeId;
	private String hfsPlatform;
	
	@JoinColumn(name = "hfsDocId")
	private String hfsSubtypeId;
	private String hfsCustomerId;
	private String hfsTransactionId;
	private String hfsHold;
	private String hfsProgram;
	private String hfsDockey;
	private String hfsTakedownId;
	private Date hfsScanDate;
	private String hfsPgmSegment;
	private String hfsCustEligibility;
	private String hfsClassId;
	private String hfsTypeId;
	private String hfsCategoryId;
	private String hfsRefCustomerId;
	private String vin_no;

	public String getVin_no() {
		return vin_no;
	}

	public void setVin_no(String vin_no) {
		this.vin_no = vin_no;
	}

	public String getHfsDocId() {
		return hfsDocId;
	}

	public void setHfsDocId(String hfsDocId) {
		this.hfsDocId = hfsDocId;
	}

	public String getDocSubtype() {
		return docSubtype;
	}

	public void setDocSubtype(String docSubtype) {
		this.docSubtype = docSubtype;
	}

	public Integer getWelcomePackage() {
		return welcomePackage;
	}

	public void setWelcomePackage(Integer welcomePackage) {
		this.welcomePackage = welcomePackage;
	}

	public Integer getSyndicationPackage() {
		return syndicationPackage;
	}

	public void setSyndicationPackage(Integer syndicationPackage) {
		this.syndicationPackage = syndicationPackage;
	}

	public Integer getPhysicalStorage() {
		return physicalStorage;
	}

	public void setPhysicalStorage(Integer physicalStorage) {
		this.physicalStorage = physicalStorage;
	}

	public Integer getPhyStgNotSent() {
		return phyStgNotSent;
	}

	public void setPhyStgNotSent(Integer phyStgNotSent) {
		this.phyStgNotSent = phyStgNotSent;
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

	public String getLwEntityType() {
		return lwEntityType;
	}

	public void setLwEntityType(String lwEntityType) {
		this.lwEntityType = lwEntityType;
	}

	public String getMigrationEntityType() {
		return migrationEntityType;
	}

	public void setMigrationEntityType(String migrationEntityType) {
		this.migrationEntityType = migrationEntityType;
	}

	public String getHfsReferenceId() {
		return hfsReferenceId;
	}

	public void setHfsReferenceId(String hfsReferenceId) {
		this.hfsReferenceId = hfsReferenceId;
	}

	public String getHfsSrNumber() {
		return hfsSrNumber;
	}

	public void setHfsSrNumber(String hfsSrNumber) {
		this.hfsSrNumber = hfsSrNumber;
	}

	public Timestamp getHfsDocDate() {
		return (Timestamp) this.hfsDocDate.clone();
	}

	public void setHfsDocDate(Timestamp hfsDocDate) {
		this.hfsDocDate = (Timestamp) hfsDocDate.clone();
	}

	public String getSubmittalId() {
		return submittalId;
	}

	public void setSubmittalId(String submittalId) {
		this.submittalId = submittalId;
	}

	public String getHfsEnvelopeId() {
		return hfsEnvelopeId;
	}

	public void setHfsEnvelopeId(String hfsEnvelopeId) {
		this.hfsEnvelopeId = hfsEnvelopeId;
	}

	public String getHfsPlatform() {
		return hfsPlatform;
	}

	public void setHfsPlatform(String hfsPlatform) {
		this.hfsPlatform = hfsPlatform;
	}

	public String getHfsSubtypeId() {
		return hfsSubtypeId;
	}

	public void setHfsSubtypeId(String hfsSubtypeId) {
		this.hfsSubtypeId = hfsSubtypeId;
	}

	public String getHfsCustomerId() {
		return hfsCustomerId;
	}

	public void setHfsCustomerId(String hfsCustomerId) {
		this.hfsCustomerId = hfsCustomerId;
	}

	public String getHfsTransactionId() {
		return hfsTransactionId;
	}

	public void setHfsTransactionId(String hfsTransactionId) {
		this.hfsTransactionId = hfsTransactionId;
	}

	public String getHfsHold() {
		return hfsHold;
	}

	public void setHfsHold(String hfsHold) {
		this.hfsHold = hfsHold;
	}

	public String getHfsProgram() {
		return hfsProgram;
	}

	public void setHfsProgram(String hfsProgram) {
		this.hfsProgram = hfsProgram;
	}

	public String getHfsDockey() {
		return hfsDockey;
	}

	public void setHfsDockey(String hfsDockey) {
		this.hfsDockey = hfsDockey;
	}

	public String getHfsTakedownId() {
		return hfsTakedownId;
	}

	public void setHfsTakedownId(String hfsTakedownId) {
		this.hfsTakedownId = hfsTakedownId;
	}

	public Date getHfsScanDate() {
		return (Date) this.hfsScanDate.clone();
		// return hfsScanDate;
	}

	public void setHfsScanDate(Date hfsScanDate) {
		this.hfsScanDate = (Date) hfsScanDate.clone();
		// this.hfsScanDate = hfsScanDate;
	}

	public String getHfsPgmSegment() {
		return hfsPgmSegment;
	}

	public void setHfsPgmSegment(String hfsPgmSegment) {
		this.hfsPgmSegment = hfsPgmSegment;
	}

	public String getHfsCustEligibility() {
		return hfsCustEligibility;
	}

	public void setHfsCustEligibility(String hfsCustEligibility) {
		this.hfsCustEligibility = hfsCustEligibility;
	}

	public String getHfsClassId() {
		return hfsClassId;
	}

	public void setHfsClassId(String hfsClassId) {
		this.hfsClassId = hfsClassId;
	}

	public String getHfsTypeId() {
		return hfsTypeId;
	}

	public void setHfsTypeId(String hfsTypeId) {
		this.hfsTypeId = hfsTypeId;
	}

	public String getHfsRefCustomerId() {
		return hfsRefCustomerId;
	}

	public void setHfsRefCustomerId(String hfsRefCustomerId) {
		this.hfsRefCustomerId = hfsRefCustomerId;
	}

	public String getHfsCategoryId() {
		return hfsCategoryId;
	}

	public void setHfsCategoryId(String hfsCategoryId) {
		this.hfsCategoryId = hfsCategoryId;
	}

}
