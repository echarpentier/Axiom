package fr.pfgen.axiom.shared.records;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ArrayImageRecord implements Serializable{

	private int sampleID;
	private String image;
    private String name;
    private Integer coordX;
    private Integer coordY;
    private String serverPath;
    private String thumbnailPath;
    
    public String getThumbnailPath() {
		return thumbnailPath;
	}
	public void setThumbnailPath(String thumbnailPath) {
		this.thumbnailPath = thumbnailPath;
	}
	public int getSampleID() {
		return sampleID;
	}
	public void setSampleID(int sampleID) {
		this.sampleID = sampleID;
	}
	public String getServerPath(){
    	return serverPath;
    }
    public void setServerPath(String path){
    	this.serverPath = path;
    }
    public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getCoordX() {
		return coordX;
	}
	public void setCoordX(Integer coordX) {
		this.coordX = coordX;
	}
	public Integer getCoordY() {
		return coordY;
	}
	public void setCoordY(Integer coordY) {
		this.coordY = coordY;
	}
}
