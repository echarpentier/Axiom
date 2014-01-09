package fr.pfgen.axiom.server.database;

import java.sql.Connection;
import java.util.Date;

@Deprecated
public class ConnectionObject {
     private Connection con;
     private int useCount;
     private Date lastAccessTime;
     private boolean inUse;
 
     public Connection getConnection () {return con;}
     public int getUseCount () {return useCount;}
     public Date getLastAccessTime () {return lastAccessTime;}
     public boolean getInUse () {return inUse;}
 
     public void setConnection (Connection c) {con = c;}
     public void setUseCount (int u) {useCount = u;}
     public void setLastAccessTime (Date d) {lastAccessTime = d;}
     public void setInUse (boolean i) {inUse = i;}
 
     public boolean isAvailable () {return !inUse;}
}