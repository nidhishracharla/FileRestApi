package com.ge.capital.dms.fr.sle.model;

public class JSONExcelObject {
	private String Id;
	private String Item;
	private String Applies;
	private String AdditionalComments;
	private String Information;
	private String LWData;

	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}

	public String getItem() {
		return Item;
	}

	public void setItem(String item) {
		Item = item;
	}

	public String getApplies() {
		return Applies;
	}

	public void setApplies(String applies) {
		Applies = applies;
	}

	@Override
	public String toString() {
		return "Id=" + Id + ", Item=" + Item + ", Applies=" + Applies + ", AdditionalComments=" + AdditionalComments
				+ ", Information=" + Information + ", LWData=" + LWData;
	}

	public String getAdditionalComments() {
		return AdditionalComments;
	}

	public void setAdditionalComments(String additionalComments) {
		AdditionalComments = additionalComments;
	}

	public String getInformation() {
		return Information;
	}

	public void setInformation(String information) {
		Information = information;
	}

	public String getLWData() {
		return LWData;
	}

	public void setLWData(String lWData) {
		LWData = lWData;
	}

}