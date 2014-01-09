package fr.pfgen.axiom.server.beans;

public class SampleForDQCGraph {

	private int plate_id;
	private String sample_name;
	private int sample_id;
	private double dqcValue;
	private String plate_name;
	
	
	public int getPlate_id() {
		return plate_id;
	}
	public void setPlate_id(int plate_id) {
		this.plate_id = plate_id;
	}
	public String getSample_name() {
		return sample_name;
	}
	public void setSample_name(String sample_name) {
		this.sample_name = sample_name;
	}
	public int getSample_id() {
		return sample_id;
	}
	public void setSample_id(int sample_id) {
		this.sample_id = sample_id;
	}
	public double getDqcValue() {
		return dqcValue;
	}
	public void setDqcValue(double dqcValue) {
		this.dqcValue = dqcValue;
	}
	public String getPlate_name() {
		return plate_name;
	}
	public void setPlate_name(String plate_name) {
		this.plate_name = plate_name;
	}
	
}
