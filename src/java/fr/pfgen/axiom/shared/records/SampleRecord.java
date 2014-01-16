package fr.pfgen.axiom.shared.records;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class SampleRecord implements Serializable {

    private int sampleID;
    private String sampleName;
    private List<String> populationNames;
    private List<String> familyNames;
    private String plateName;
    private String samplePath;
    private int coordX;
    private int coordY;

    //getters,setters
    public List<String> getFamilyNames() {
        return familyNames;
    }

    public void setFamilyNames(List<String> familyNames) {
        this.familyNames = familyNames;
    }

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

    public List<String> getPopulationNames() {
        return populationNames;
    }

    public void setPopulationNames(List<String> populationNames) {
        this.populationNames = populationNames;
    }

    public String getPlateName() {
        return plateName;
    }

    public void setPlateName(String plateName) {
        this.plateName = plateName;
    }

    public String getSamplePath() {
        return samplePath;
    }

    public void setSamplePath(String samplePath) {
        this.samplePath = samplePath;
    }

    public int getCoordX() {
        return coordX;
    }

    public void setCoordX(int coordX) {
        this.coordX = coordX;
    }

    public int getCoordY() {
        return coordY;
    }

    public void setCoordY(int coordY) {
        this.coordY = coordY;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + sampleID;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SampleRecord other = (SampleRecord) obj;
        if (sampleID != other.sampleID) {
            return false;
        }
        return true;
    }
}
