package com.ge.capital.dms.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "deal_doc")
public class DealDoc implements Serializable {

	private static final long serialVersionUID = 2L;

	@Id
	@JoinColumn(name = "deal_doc_id")
	private String deal_doc_id;
	private String doc_subtype;
	private Integer welcome_package;
	private Integer syndication_package;
	private Integer physical_storage;
	private Integer phy_stg_not_sent;
	private Integer final_package;
	private String party_number;
	private String party_name;
	private String party_deal_type;
	private String account_id;
	private String account_name;
	private String opportunity_id;
	private String credit_number;
	private String lob_code;
	private String legal_entity_name;
	private String lw_sequence_num;
	private String contract_deal_type;
	private String lw_entity_type;
	private String migration_entity_type;
	private String filenet_guid;
	private String copied_by;
	private Timestamp copied_on;
	private String vin_no;
	//private String docusign_filename;
	

	public Timestamp getCopiedOn() {
		return (Timestamp) copied_on.clone();
	}

	public void setCopiedOn(Timestamp copiedOn) {
		this.copied_on = (Timestamp) copiedOn.clone();
	}

	public String getCopiedBy() {
		return copied_by;
	}

	public void setCopiedBy(String copiedBy) {
		this.copied_by = copiedBy;
	}

	public String getFilenet_guid() {
		return filenet_guid;
	}

	public void setFilenet_guid(String filenet_guid) {
		this.filenet_guid = filenet_guid;
	}

	public String getDeal_doc_id() {
		return deal_doc_id;
	}

	public void setDeal_doc_id(String deal_doc_id) {
		this.deal_doc_id = deal_doc_id;
	}

	public String getDoc_subtype() {
		return doc_subtype;
	}

	public void setDoc_subtype(String doc_subtype) {
		this.doc_subtype = doc_subtype;
	}

	public Integer getWelcome_package() {
		return welcome_package;
	}

	public void setWelcome_package(Integer welcome_package) {
		this.welcome_package = welcome_package;
	}

	public Integer getSyndication_package() {
		return syndication_package;
	}

	public void setSyndication_package(Integer syndication_package) {
		this.syndication_package = syndication_package;
	}

	public Integer getPhysical_storage() {
		return physical_storage;
	}

	public void setPhysical_storage(Integer physical_storage) {
		this.physical_storage = physical_storage;
	}

	public Integer getPhy_stg_not_sent() {
		return phy_stg_not_sent;
	}

	public void setPhy_stg_not_sent(Integer phy_stg_not_sent) {
		this.phy_stg_not_sent = phy_stg_not_sent;
	}

	public Integer getFinal_package() {
		return final_package;
	}

	public void setFinal_package(Integer final_package) {
		this.final_package = final_package;
	}

	public String getParty_number() {
		return party_number;
	}

	public void setParty_number(String party_number) {
		this.party_number = party_number;
	}

	public String getParty_name() {
		return party_name;
	}

	public void setParty_name(String party_name) {
		this.party_name = party_name;
	}

	public String getParty_deal_type() {
		return party_deal_type;
	}

	public void setParty_deal_type(String party_deal_type) {
		this.party_deal_type = party_deal_type;
	}

	public String getAccount_id() {
		return account_id;
	}

	public void setAccount_id(String account_id) {
		this.account_id = account_id;
	}

	public String getAccount_name() {
		return account_name;
	}

	public void setAccount_name(String account_name) {
		this.account_name = account_name;
	}

	public String getOpportunity_id() {
		return opportunity_id;
	}

	public void setOpportunity_id(String opportunity_id) {
		this.opportunity_id = opportunity_id;
	}

	public String getCredit_number() {
		return credit_number;
	}

	public void setCredit_number(String credit_number) {
		this.credit_number = credit_number;
	}

	public String getLob_code() {
		return lob_code;
	}

	public void setLob_code(String lob_code) {
		this.lob_code = lob_code;
	}

	public String getLegal_entity_name() {
		return legal_entity_name;
	}

	public void setLegal_entity_name(String legal_entity_name) {
		this.legal_entity_name = legal_entity_name;
	}

	public String getLw_sequence_num() {
		return lw_sequence_num;
	}

	public void setLw_sequence_num(String lw_sequence_num) {
		this.lw_sequence_num = lw_sequence_num;
	}

	public String getContract_deal_type() {
		return contract_deal_type;
	}

	public void setContract_deal_type(String contract_deal_type) {
		this.contract_deal_type = contract_deal_type;
	}

	public String getLw_entity_type() {
		return lw_entity_type;
	}

	public void setLw_entity_type(String lw_entity_type) {
		this.lw_entity_type = lw_entity_type;
	}

	public String getMigration_entity_type() {
		return migration_entity_type;
	}

	public void setMigration_entity_type(String migration_entity_type) {
		this.migration_entity_type = migration_entity_type;
	}

	public String getVin_no() {
		return vin_no;
	}

	public void setVin_no(String vin_no) {
		this.vin_no = vin_no;
	}

}
