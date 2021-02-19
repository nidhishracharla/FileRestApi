package com.ge.capital.dms.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
public class vw_dealdoc_icon {

	@Id
	private String doc_Id;
	private String doc_Name;
	private String doc_Title;
	private String legacy_Doc_Id;
	private String version_Num;
	private String doc_State;
	private String retention_Date;
	private String create_Date;
	private String modify_Date;
	private String creator;
	private String modifier;
	private String owner_Name;
	private String doc_subtype;
	private String welcome_package;
	private String syndication_package;
	private String physical_storage;
	private String phy_stg_not_sent;
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
	private String doc_Source;
	private String file_Size;
	private String copied_by;
	private String vin_no;
	private String envelope_Id;

	public String getDoc_Id() {
		return doc_Id;
	}

	public void setDoc_Id(String doc_Id) {
		this.doc_Id = doc_Id;
	}

	public String getDoc_Name() {
		return doc_Name;
	}

	public void setDoc_Name(String doc_Name) {
		this.doc_Name = doc_Name;
	}

	public String getDoc_Title() {
		return doc_Title;
	}

	public void setDoc_Title(String doc_Title) {
		this.doc_Title = doc_Title;
	}

	public String getLegacy_Doc_Id() {
		return legacy_Doc_Id;
	}

	public void setLegacy_Doc_Id(String legacy_Doc_Id) {
		this.legacy_Doc_Id = legacy_Doc_Id;
	}

	public String getVersion_Num() {
		return version_Num;
	}

	public void setVersion_Num(String version_Num) {
		this.version_Num = version_Num;
	}

	public String getDoc_State() {
		return doc_State;
	}

	public void setDoc_State(String doc_State) {
		this.doc_State = doc_State;
	}

	public String getRetention_Date() {
		return retention_Date;
	}

	public void setRetention_Date(String retention_Date) {
		this.retention_Date = retention_Date;
	}

	public String getCreate_Date() {
		return create_Date;
	}

	public void setCreate_Date(String create_Date) {
		this.create_Date = create_Date;
	}

	public String getModify_Date() {
		return modify_Date;
	}

	public void setModify_Date(String modify_Date) {
		this.modify_Date = modify_Date;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	public String getOwner_Name() {
		return owner_Name;
	}

	public void setOwner_Name(String owner_Name) {
		this.owner_Name = owner_Name;
	}

	public String getDoc_subtype() {
		return doc_subtype;
	}

	public void setDoc_subtype(String doc_subtype) {
		this.doc_subtype = doc_subtype;
	}

	public String getWelcome_package() {
		return welcome_package;
	}

	public void setWelcome_package(String welcome_package) {
		this.welcome_package = welcome_package;
	}

	public String getSyndication_package() {
		return syndication_package;
	}

	public void setSyndication_package(String syndication_package) {
		this.syndication_package = syndication_package;
	}

	public String getPhysical_storage() {
		return physical_storage;
	}

	public void setPhysical_storage(String physical_storage) {
		this.physical_storage = physical_storage;
	}

	public String getPhy_stg_not_sent() {
		return phy_stg_not_sent;
	}

	public void setPhy_stg_not_sent(String phy_stg_not_sent) {
		this.phy_stg_not_sent = phy_stg_not_sent;
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

	public String getDoc_Source() {
		return doc_Source;
	}

	public void setDoc_Source(String doc_Source) {
		this.doc_Source = doc_Source;
	}

	public String getFile_Size() {
		return file_Size;
	}

	public void setFile_Size(String file_Size) {
		this.file_Size = file_Size;
	}

	public String getCopied_by() {
		return copied_by;
	}

	public void setCopied_by(String copied_by) {
		this.copied_by = copied_by;
	}

	public String getVin_no() {
		return vin_no;
	}

	public void setVin_no(String vin_no) {
		this.vin_no = vin_no;
	}

	public String getEnvelope_Id() {
		return envelope_Id;
	}

	public void setEnvelope_Id(String envelope_Id) {
		this.envelope_Id = envelope_Id;
	}
}
