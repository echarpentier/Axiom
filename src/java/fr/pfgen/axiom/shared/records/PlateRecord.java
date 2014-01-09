package fr.pfgen.axiom.shared.records;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class PlateRecord implements Serializable {
	
	private int id;
	private String barcode;
    private String name;
    private Date created;
    
    //getters, setters
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getBarcode() {
		return barcode;
	}
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
}