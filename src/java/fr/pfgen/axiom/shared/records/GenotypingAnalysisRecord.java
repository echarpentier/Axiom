package fr.pfgen.axiom.shared.records;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class GenotypingAnalysisRecord implements Serializable {

	private int id;
	private String genoName;
	private String folderPath;
	private String user;
	private Double dishQCLimit;
	private Double callRateLimit;
	private String libraryFiles;
	private String annotationFile;
	private Date executed;
	
	//getters, setters
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getGenoName() {
		return genoName;
	}
	public void setGenoName(String genoName) {
		this.genoName = genoName;
	}
	public String getFolderPath() {
		return folderPath;
	}
	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public Double getDishQCLimit() {
		return dishQCLimit;
	}
	public void setDishQCLimit(Double dishQCLimit) {
		this.dishQCLimit = dishQCLimit;
	}
	public Double getCallRateLimit() {
		return callRateLimit;
	}
	public void setCallRateLimit(Double callRateLimit) {
		this.callRateLimit = callRateLimit;
	}
	public Date getExecuted() {
		return executed;
	}
	public void setExecuted(Date executed) {
		this.executed = executed;
	}
	public String getLibraryFiles() {
		return libraryFiles;
	}
	public void setLibraryFiles(String libraryFiles) {
		this.libraryFiles = libraryFiles;
	}
	public String getAnnotationFile() {
		return annotationFile;
	}
	public void setAnnotationFile(String annotationFile) {
		this.annotationFile = annotationFile;
	}
}
