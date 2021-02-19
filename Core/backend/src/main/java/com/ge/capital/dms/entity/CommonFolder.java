package com.ge.capital.dms.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.joda.time.DateTime;

@Entity
@Table(name = "common_folder")
public class CommonFolder {

	@Id
	private String folder_id;
	private String folder_name;
	private String realm_name;
	private String folder_ref;
	private String is_migrated;
	private String perm_name;
	private DateTime create_date;
	private DateTime modify_date;
	private String creator;
	private String modifier;
	private String child_ref;
	private String folder_path;

	public String getFolder_id() {
		return folder_id;
	}

	public void setFolder_id(String folder_id) {
		this.folder_id = folder_id;
	}

	public String getFolder_name() {
		return folder_name;
	}

	public void setFolder_name(String folder_name) {
		this.folder_name = folder_name;
	}

	public String getRealm_name() {
		return realm_name;
	}

	public void setRealm_name(String realm_name) {
		this.realm_name = realm_name;
	}

	public String getFolder_ref() {
		return folder_ref;
	}

	public void setFolder_ref(String folder_ref) {
		this.folder_ref = folder_ref;
	}

	public String getIs_migrated() {
		return is_migrated;
	}

	public void setIs_migrated(String is_migrated) {
		this.is_migrated = is_migrated;
	}

	public String getPerm_name() {
		return perm_name;
	}

	public void setPerm_name(String perm_name) {
		this.perm_name = perm_name;
	}

	public DateTime getCreate_date() {
		return create_date;
	}

	public void setCreate_date(DateTime create_date) {
		this.create_date = create_date;
	}

	public DateTime getModify_date() {
		return modify_date;
	}

	public void setModify_date(DateTime modify_date) {
		this.modify_date = modify_date;
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

	public String getChild_ref() {
		return child_ref;
	}

	public void setChild_ref(String child_ref) {
		this.child_ref = child_ref;
	}

	public String getFolder_path() {
		return folder_path;
	}

	public void setFolder_path(String folder_path) {
		this.folder_path = folder_path;
	}
}
