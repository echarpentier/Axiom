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
public class FamilyRecord implements Serializable{
    
    private int id;
    private String name;
    private String user;
    private Date created;
    private String propositus;

    public String getPropositus() {
        return propositus;
    }

    public void setPropositus(String propositus) {
        this.propositus = propositus;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
