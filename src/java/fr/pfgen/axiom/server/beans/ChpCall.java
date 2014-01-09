package fr.pfgen.axiom.server.beans;

public class ChpCall {

	private String sampleName;
	private String plateName;
	private String call;
	private double logRatio;
	private double strength;

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
	public String getCall() {
		return call;
	}
	public void setCall(String call) {
		this.call = call;
	}
	public double getLogRatio() {
		return logRatio;
	}
	public void setLogRatio(double logRatio) {
		this.logRatio = logRatio;
	}
	public double getStrength() {
		return strength;
	}
	public void setStrength(double strength) {
		this.strength = strength;
	}
}
