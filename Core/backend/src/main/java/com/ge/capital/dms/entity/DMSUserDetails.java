package com.ge.capital.dms.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author VA460440
 */

@Entity
@Table(name = "dms_user")
public class DMSUserDetails {

	@Column(name = "first_name", unique = false)
	private String firstName;

	@Column(name = "middle_name", unique = false)
	private String middleName;

	@Column(name = "last_name", unique = false)
	private String lastName;

	@Id
	@Column(name = "user_id", unique = true)
	private String userId;

	@Column(name = "user_source", unique = false)
	private String userSource;

	@Column(name = "realms", unique = false)
	private String realms;

	@Column(name = "is_active", unique = false)
	private int isActive;

	@Column(name = "email", unique = false)
	private String email;

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserSource() {
		return userSource;
	}

	public void setUserSource(String userSource) {
		this.userSource = userSource;
	}

	public String getRealms() {
		return realms;
	}

	public void setRealms(String realms) {
		this.realms = realms;
	}

	public int getIsActive() {
		return isActive;
	}

	public void setIsActive(int isActive) {
		this.isActive = isActive;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
