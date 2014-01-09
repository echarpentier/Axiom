package fr.pfgen.axiom.shared.records;

import java.io.Serializable;

@SuppressWarnings("serial")
public class StudyWorkflowState implements Serializable{
	
	private boolean samplesDone;
	private boolean pedigreeDone;
	private boolean plinkFilesDone;
	private boolean allDone;
	
	
	public boolean isSamplesDone() {
		return samplesDone;
	}
	public void setSamplesDone(boolean samplesDone) {
		this.samplesDone = samplesDone;
	}
	public boolean isPedigreeDone() {
		return pedigreeDone;
	}
	public void setPedigreeDone(boolean pedigreeDone) {
		this.pedigreeDone = pedigreeDone;
	}
	public boolean isPlinkFilesDone() {
		return plinkFilesDone;
	}
	public void setPlinkFilesDone(boolean plinkFilesDone) {
		this.plinkFilesDone = plinkFilesDone;
	}
	public boolean isAllDone() {
		return allDone;
	}
	public void setAllDone(boolean allDone) {
		this.allDone = allDone;
	}
}
