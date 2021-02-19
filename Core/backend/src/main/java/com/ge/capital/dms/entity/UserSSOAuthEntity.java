package com.ge.capital.dms.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;



@Entity
@Table(name="userssoauth")
public class UserSSOAuthEntity {
	
	@Id
	@Column(name = "usersso", unique = true, nullable = false)
	private String usersso;

	@Column(name="auth_token", unique = true, nullable = false)
	private String token;

	@Column(name="jsession_id")
	private String jsessionID;

	public UserSSOAuthEntity() {
		super();
	}

	public UserSSOAuthEntity(String usersso, String token,String jsession_id) {
		super();
		this.usersso = usersso;
		this.token = token;
		this.jsessionID = jsession_id;
	}

	public String getSessionId() {
		return jsessionID;
	}

	public void setSessionId(String sessionId) {
		this.jsessionID= sessionId;
	}
	public String getUsersso() {
		return usersso;
	}

	public void setUsersso(String usersso) {
		this.usersso = usersso;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserSSOAuthEntity other = (UserSSOAuthEntity) obj;
		if (token == null) {
			if (other.token != null)
				return false;
		} else if (!token.equals(other.token))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		return usersso.hashCode();
	}

	@Override
	public String toString() {
		return "UserSSOAuthEntity [usersso=" + usersso + ", token=" + token + "]";
	}

}
