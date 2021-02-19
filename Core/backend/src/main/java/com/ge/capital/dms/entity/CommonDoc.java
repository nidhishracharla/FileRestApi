package com.ge.capital.dms.entity;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "common_doc")
public class CommonDoc implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private String docId;
	private String docName;
	private String docTitle;
	private String docVersionId;
	private String docType;
	private String legacyDocId;
	private String versionNum;
	private String versionLabel;
	private String folderRef;
	private String mimeType;
	private String docSource;
	private String isMigrated;
	private Timestamp migrationDate;
	private Timestamp retentionDate;
	private Timestamp createDate;
	private Timestamp modifyDate;
	private String creator;
	private String modifier;
	private String isCurrent;
	private String docState;
	private String retentionPolicy;
	private String isLocked;
	private String lockOwner;
	private String isDeleted;
	private String permName;
	private String realmName;
	private String ownerName;
	private String contentRef;
	private String contentTag;
	private String fileSize;

	public String getDocId() {
		return docId;
	}

	public void setDocId(String docId) {
		this.docId = docId;
	}

	public String getDocName() {
		return docName;
	}

	public void setDocName(String docName) {
		this.docName = docName;
	}

	public String getDocTitle() {
		return docTitle;
	}

	public void setDocTitle(String docTitle) {
		this.docTitle = docTitle;
	}

	public String getDocVersionId() {
		return docVersionId;
	}

	public void setDocVersionId(String docVersionId) {
		this.docVersionId = docVersionId;
	}

	public String getDocType() {
		return docType;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}

	public String getLegacyDocId() {
		return legacyDocId;
	}

	public void setLegacyDocId(String legacyDocId) {
		this.legacyDocId = legacyDocId;
	}

	public String getVersionNum() {
		return versionNum;
	}

	public void setVersionNum(String versionNum) {
		this.versionNum = versionNum;
	}

	public String getVersionLabel() {
		return versionLabel;
	}

	public void setVersionLabel(String versionLabel) {
		this.versionLabel = versionLabel;
	}

	public String getFolderRef() {
		return folderRef;
	}

	public void setFolderRef(String folderRef) {
		this.folderRef = folderRef;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getDocSource() {
		return docSource;
	}

	public void setDocSource(String docSource) {
		this.docSource = docSource;
	}

	public String getIsMigrated() {
		return isMigrated;
	}

	public void setIsMigrated(String isMigrated) {
		this.isMigrated = isMigrated;
	}

	public Date getMigrationDate() {
		return (Date) migrationDate.clone();
	}

	public void setMigrationDate(Timestamp migrationDate) {
		this.migrationDate = (Timestamp) migrationDate.clone();
	}

	public Date getRetentionDate() {
		return (Date) retentionDate.clone();
	}

	public void setRetentionDate(Timestamp retentionDate) {
		this.retentionDate = (Timestamp) retentionDate.clone();
	}

	public Date getCreateDate() {
		return (Date) createDate.clone();
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = (Timestamp) createDate.clone();
	}

	public Date getModifyDate() {
		return (Date) modifyDate.clone();
	}

	public void setModifyDate(Timestamp modifyDate) {
		this.modifyDate = (Timestamp) modifyDate.clone();
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

	public String getIsCurrent() {
		return isCurrent;
	}

	public void setIsCurrent(String isCurrent) {
		this.isCurrent = isCurrent;
	}

	public String getDocState() {
		return docState;
	}

	public void setDocState(String docState) {
		this.docState = docState;
	}

	public String getRetentionPolicy() {
		return retentionPolicy;
	}

	public void setRetentionPolicy(String retentionPolicy) {
		this.retentionPolicy = retentionPolicy;
	}

	public String getIsLocked() {
		return isLocked;
	}

	public void setIsLocked(String isLocked) {
		this.isLocked = isLocked;
	}

	public String getLockOwner() {
		return lockOwner;
	}

	public void setLockOwner(String lockOwner) {
		this.lockOwner = lockOwner;
	}

	public String getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(String isDeleted) {
		this.isDeleted = isDeleted;
	}

	public String getPermName() {
		return permName;
	}

	public void setPermName(String permName) {
		this.permName = permName;
	}

	public String getRealmName() {
		return realmName;
	}

	public void setRealmName(String realmName) {
		this.realmName = realmName;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public String getContentRef() {
		return contentRef;
	}

	public void setContentRef(String contentRef) {
		this.contentRef = contentRef;
	}

	public String getContentTag() {
		return contentTag;
	}

	public void setContentTag(String contentTag) {
		this.contentTag = contentTag;
	}

	public String getFileSize() {
		return fileSize;
	}

	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}

	private String environment;

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

}
