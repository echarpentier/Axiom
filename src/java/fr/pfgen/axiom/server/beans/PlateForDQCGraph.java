package fr.pfgen.axiom.server.beans;

@Deprecated
public class PlateForDQCGraph {

	private int plateID;
	private String plateName;
	private int execute;
	
	public int getPlateID() {
		return plateID;
	}
	public void setPlateID(int plateID) {
		this.plateID = plateID;
	}
	public String getPlateName() {
		return plateName;
	}
	public void setPlateName(String plateName) {
		this.plateName = plateName;
	}
	public int getExecute() {
		return execute;
	}
	public void setExecute(int execute) {
		this.execute = execute;
	}
}
