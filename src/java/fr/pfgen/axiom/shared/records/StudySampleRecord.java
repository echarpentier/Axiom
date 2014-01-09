package fr.pfgen.axiom.shared.records;

import java.io.Serializable;

@SuppressWarnings("serial")
public class StudySampleRecord implements Serializable{

	private int sampleID;
	private String sampleName;
	private int genoRunID;
	private String genoRun;
	private int studySampleID;
	
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
	public int getGenoRunID() {
		return genoRunID;
	}
	public void setGenoRunID(int genoRunID) {
		this.genoRunID = genoRunID;
	}
	public String getGenoRun() {
		return genoRun;
	}
	public void setGenoRun(String genoRun) {
		this.genoRun = genoRun;
	}
	public int getStudySampleID() {
		return studySampleID;
	}
	public void setStudySampleID(int studySampleID) {
		this.studySampleID = studySampleID;
	}
}
