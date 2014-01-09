package fr.pfgen.axiom.shared.records;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("serial")
public class SampleQCRecord implements Serializable{

	private int sampleID;
	private String sampleName;
	private String plateName;
	private List<String> populationNames;
	private HashMap<String, String> qcMap;
	private HashMap<String, String> userQcMap;
	
	//getters, setters
	public int getSampleID() {
		return sampleID;
	}
	public void setSampleID(int sampleID) {
		this.sampleID = sampleID;
	}
	public String getSampleName() {
		return sampleName;
	}
	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}
	public String getPlateName() {
		return plateName;
	}
	public void setPlateName(String plateName) {
		this.plateName = plateName;
	}
	public List<String> getPopulationNames() {
		return populationNames;
	}
	public void setPopulationNames(List<String> populationNames) {
		this.populationNames = populationNames;
	}
	public HashMap<String, String> getQcMap() {
		return qcMap;
	}
	public void setQcMap(HashMap<String, String> qcMap) {
		this.qcMap = qcMap;
	}
	public HashMap<String, String> getUserQcMap() {
		return userQcMap;
	}
	public void setUserQcMap(HashMap<String, String> userQcMap) {
		this.userQcMap = userQcMap;
	}
}
