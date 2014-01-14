/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.pfgen.axiom.shared.records;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author eric
 */
public class RunningGenotypingAnalysis implements Serializable {

    public enum GenoAnaRunningStatus{
        STARTING("Initializing..."),
        FIRST("First genotyping run in progress..."),
        SECOND("Second genotyping run in progress..."),
        METRICS("Creating metrics..."),
        DONE("Finished !!");
        
        private final String label;

        private GenoAnaRunningStatus(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
        
        public static GenoAnaRunningStatus getEnumFromLabel(String label){
            if (label.trim().equalsIgnoreCase("Initializing...")){
                return GenoAnaRunningStatus.STARTING;
            }else if(label.trim().equalsIgnoreCase("First genotyping run in progress...")){
                return GenoAnaRunningStatus.FIRST;
            }else if(label.trim().equalsIgnoreCase("Second genotyping run in progress...")){
                return GenoAnaRunningStatus.SECOND;
            }else if(label.trim().equalsIgnoreCase("Creating metrics...")){
                return GenoAnaRunningStatus.METRICS;
            }else if(label.trim().equalsIgnoreCase("Finished !!")){
                return GenoAnaRunningStatus.DONE;
            }else{
                return null;
            }       
        }
    }
    
    private String name;
    private GenoAnaRunningStatus status;
    private String user;
    private Date startDate;
    private Date endDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GenoAnaRunningStatus getStatus() {
        return status;
    }

    public void setStatus(GenoAnaRunningStatus status) {
        this.status = status;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RunningGenotypingAnalysis other = (RunningGenotypingAnalysis) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }
}
