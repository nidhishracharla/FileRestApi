package com.ge.capital.dms.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "dms_group")

public class DMSGroup implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "group_name")
	private String group_name;

	@Column(name = "group_id")
	private String group_id;
	
	@Column(name = "realm")
	private String realm;
	
	@Column(name = "all_users")
	private String all_users;
	
	@Column(name = "is_admin_group")
	private String is_admin_group;
	
	@Column(name = "is_active")
	private String is_active;

	public String getGroup_Name() {
		return group_name;
	}

	public void setGroup_Name(String group_name) {
		this.group_name = group_name;
	}
	
	public String getGroup_id() {
		return group_id;
	}

	public void setGroup_id(String group_id) {
		this.group_id = group_id;
	}
	
	public String getRealm() {
		return realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}
	
	public String getAll_User() {
		return all_users;
	}

	public void setAll_User(String all_users) {
		this.all_users = all_users;
	}
	
	public String getIsAdmin_Group() {
		return is_admin_group;
	}

	public void setIsAdmin_Group(String is_admin_group) {
		this.is_admin_group = is_admin_group;
	}
	
	public String getIs_Active() {
		return is_active;
	}

	public void setIs_Active(String is_active) {
		this.is_active = is_active;
	}
}
