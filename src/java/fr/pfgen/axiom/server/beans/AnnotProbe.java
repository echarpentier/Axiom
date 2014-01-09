package fr.pfgen.axiom.server.beans;

public class AnnotProbe {

	private String probesetID;
	private String rsName;
	private String chr;
	private long pos;
	private char alleleA;
	private char alleleB;
	private double freqAlleleA;
	private double freqAlleleB;

	public char getAlleleA() {
		return alleleA;
	}
	public void setAlleleA(char alleleA) {
		this.alleleA = alleleA;
	}
	public char getAlleleB() {
		return alleleB;
	}
	public void setAlleleB(char alleleB) {
		this.alleleB = alleleB;
	}
	public String getChr() {
		return chr;
	}
	public void setChr(String chr) {
		this.chr = chr;
	}
	public double getFreqAlleleA() {
		return freqAlleleA;
	}
	public void setFreqAlleleA(double freqAlleleA) {
		this.freqAlleleA = freqAlleleA;
	}
	public double getFreqAlleleB() {
		return freqAlleleB;
	}
	public void setFreqAlleleB(double freqAlleleB) {
		this.freqAlleleB = freqAlleleB;
	}
	public long getPos() {
		return pos;
	}
	public void setPos(long pos) {
		this.pos = pos;
	}
	public String getProbesetID() {
		return probesetID;
	}
	public void setProbesetID(String probesetID) {
		this.probesetID = probesetID;
	}
	public String getRsName() {
		return rsName;
	}
	public void setRsName(String rsName) {
		this.rsName = rsName;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((probesetID == null) ? 0 : probesetID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final AnnotProbe other = (AnnotProbe) obj;
		if (probesetID == null) {
			if (other.probesetID != null)
				return false;
		} else if (!probesetID.equals(other.probesetID))
			return false;
		return true;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(this.probesetID+"\t");
		sb.append(this.rsName+"\t");
		sb.append(this.chr+"\t");
		sb.append(this.pos+"\t");
		sb.append(this.alleleA+"\t");
		sb.append(this.alleleB+"\t");
		sb.append(this.freqAlleleA+"\t");
		sb.append(this.freqAlleleB);
		
		return sb.toString();
	}
	
	public static String getHeaderLine(){
		StringBuilder sb = new StringBuilder();
		sb.append("probesetID\t");
		sb.append("rsName\t");
		sb.append("chr\t");
		sb.append("pos\t");
		sb.append("alleleA\t");
		sb.append("alleleB\t");
		sb.append("freqAlleleA\t");
		sb.append("freqAlleleB");
		
		return sb.toString();
	}
}
