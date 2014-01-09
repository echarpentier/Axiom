package fr.pfgen.axiom.shared.records;

import java.io.Serializable;

@SuppressWarnings("serial")
public class PedigreeState implements Serializable{

	private boolean fileUploaded;
	private boolean samplesFound;
	private boolean individualsDescribed;
	private boolean sexChecked;
	private boolean statusChecked;
	private boolean pedigreeChartChecked;
	private boolean alldone;
	
	public boolean isAlldone() {
		return alldone;
	}
	public void setAlldone(boolean alldone) {
		this.alldone = alldone;
	}
	public boolean isFileUploaded() {
		return fileUploaded;
	}
	public void setFileUploaded(boolean fileUploaded) {
		this.fileUploaded = fileUploaded;
	}
	public boolean isSexChecked() {
		return sexChecked;
	}
	public void setSexChecked(boolean sexChecked) {
		this.sexChecked = sexChecked;
	}
	public boolean isStatusChecked() {
		return statusChecked;
	}
	public void setStatusChecked(boolean statusChecked) {
		this.statusChecked = statusChecked;
	}
	public boolean isIndividualsDescribed() {
		return individualsDescribed;
	}
	public void setIndividualsDescribed(boolean individualsDescribed) {
		this.individualsDescribed = individualsDescribed;
	}
	public boolean isPedigreeChartChecked() {
		return pedigreeChartChecked;
	}
	public void setPedigreeChartChecked(boolean pedigreeChartChecked) {
		this.pedigreeChartChecked = pedigreeChartChecked;
	}
	public boolean isSamplesFound() {
		return samplesFound;
	}
	public void setSamplesFound(boolean samplesFound) {
		this.samplesFound = samplesFound;
	}
	public void setAllDone(){
		this.fileUploaded = true;
		this.individualsDescribed = true;
		this.pedigreeChartChecked = true;
		this.samplesFound = true;
		this.sexChecked = true;
		this.statusChecked = true;
		this.alldone = true;
	}
	public void setNoneDone(){
		this.fileUploaded = false;
		this.individualsDescribed = false;
		this.pedigreeChartChecked = false;
		this.samplesFound = false;
		this.sexChecked = false;
		this.statusChecked = false;
		this.alldone = false;
	}
}
