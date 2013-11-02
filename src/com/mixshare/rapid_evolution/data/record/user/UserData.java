package com.mixshare.rapid_evolution.data.record.user;

public class UserData {

	/////////////////
	// CONSTRUCTOR //
	/////////////////
	
	public UserData() { }
	public UserData(UserDataType userDataType, Object data) {
		this.userDataType = userDataType;
		this.data = data;
	}
	
	////////////
	// FIELDS //
	////////////
	
	private UserDataType userDataType;
	private Object data;
	
	/////////////
	// GETTERS //
	/////////////
	
	public UserDataType getUserDataType() { return userDataType; }
	public Object getData() { return data; }

	/////////////
	// SETTERS //
	/////////////
	
	public void setUserDataType(UserDataType userDataType) { this.userDataType = userDataType; }
	public void setData(Object data) { this.data = data; }
	
}
