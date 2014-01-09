package fr.pfgen.axiom.shared.records;

import java.io.Serializable;

@SuppressWarnings("serial")
public class UserRecord implements Serializable {
	
	private String loginText;
	private int userID;
	private String firstname;
	private String lastname;
	private String email;
	private String office_number;
	private String team;
	private String appID;
	private String appPw;
	private String status;
	
	//getters, setters
	public String getLoginText() {
		return loginText;
	}
	public void setLoginText(String loginText) {
		this.loginText = loginText;
	}
	public int getUserID() {
		return userID;
	}
	public void setUserID(int userID) {
		this.userID = userID;
	}
	public String getFirstname() {
		return firstname;
	}
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	public String getLastname() {
		return lastname;
	}
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getOffice_number() {
		return office_number;
	}
	public void setOffice_number(String office_number) {
		this.office_number = office_number;
	}
	public String getTeam() {
		return team;
	}
	public void setTeam(String team) {
		this.team = team;
	}
	public String getAppID() {
		return appID;
	}
	public void setAppID(String appID) {
		this.appID = appID;
	}
	public String getAppPw() {
		return appPw;
	}
	public void setAppPw(String appPw) {
		this.appPw = appPw;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}	
}
