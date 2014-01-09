package fr.pfgen.axiom.server.utils;

public class GlobalDefs{
	private String axiomPath = null;
	private String axiomPathReplacementInDB = null;
	private String dataPath = null;
	private String dataPathReplacementInDB = null;
	private static GlobalDefs INSTANCE = null;

	private GlobalDefs(){
	
	}

	public String getAxiomPathReplacementInDB() {
		return axiomPathReplacementInDB;
	}
	
	public String getDataPathReplacementInDB(){
		return dataPathReplacementInDB;
	}

	public synchronized void setAxiomPathReplacementInDB(String axiomPathReplacementInDB) {
		if(this.axiomPathReplacementInDB!=null && !this.axiomPathReplacementInDB.equals(axiomPathReplacementInDB)) throw new IllegalStateException("The main axiom path replacement in DB cannot be changed once initialized !!");
		this.axiomPathReplacementInDB = axiomPathReplacementInDB;
	}
	
	public synchronized void setDataPathReplacementInDB(String dataPathReplacementInDB){
		if(this.dataPathReplacementInDB!=null && !this.dataPathReplacementInDB.equals(dataPathReplacementInDB)) throw new IllegalStateException("The data path replacement in DB cannot be changed once initialized !!");
		this.dataPathReplacementInDB = dataPathReplacementInDB;
	}

	public synchronized void setAxiomPath(String path){
		if(this.axiomPath!=null && !this.axiomPath.equals(path)) throw new IllegalStateException("The main Axiom path cannot be changed once initialized !!");
		this.axiomPath = path;
	}
	
	public synchronized void setDataPath(String path){
		if(this.dataPath!=null && !this.dataPath.equals(path)) throw new IllegalStateException("The data path cannot be changed once initialized !!");
		this.dataPath = path;
	}

	public String getAxiomPath(){
		return axiomPath;
	}
	
	public String getDataPath(){
		return dataPath;
	}

	public static GlobalDefs getInstance(){
		if(INSTANCE==null){
			synchronized (GlobalDefs.class){
				if(INSTANCE==null){
					INSTANCE = new GlobalDefs();
				}
			}
		}
		return INSTANCE;
	}
}
