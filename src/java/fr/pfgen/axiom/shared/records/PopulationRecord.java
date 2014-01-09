package fr.pfgen.axiom.shared.records;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class PopulationRecord implements Serializable{

	private int id;
	private String populationName;
	private String user;
	private Date created;
	
	//getters, setters
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getPopulationName() {
		return populationName;
	}
	public void setPopulationName(String populationName) {
		this.populationName = populationName;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
}
