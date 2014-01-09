package fr.pfgen.axiom.shared.records;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("serial")
public class GenotypingQCRecord implements Serializable{

	private int sampleID;
	private int genoRunID;
	private int genoID;
	private String sampleName;
	private String plateName;
	private String genoName;
	private List<String> populationNames;
	private String run;
	private HashMap<String, String> qcMap;
	
	//getters, setters
	public int getSampleID() {
		return sampleID;
	}
	public void setSampleID(int sampleID) {
		this.sampleID = sampleID;
	}
	public int getGenoRunID() {
		return genoRunID;
	}
	public void setGenoRunID(int genoRunID) {
		this.genoRunID = genoRunID;
	}
	public int getGenoID() {
		return genoID;
	}
	public void setGenoID(int genoID) {
		this.genoID = genoID;
	}
	public String getGenoName() {
		return genoName;
	}
	public void setGenoName(String genoName) {
		this.genoName = genoName;
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
	public String getRun() {
		return run;
	}
	public void setRun(String run) {
		this.run = run;
	}
	public HashMap<String, String> getQcMap() {
		return qcMap;
	}
	public void setQcMap(HashMap<String, String> qcMap) {
		this.qcMap = qcMap;
	}
}
