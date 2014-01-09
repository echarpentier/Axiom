package fr.pfgen.axiom.shared.records;

import java.io.Serializable;

@SuppressWarnings("serial")
public class PedigreeRecord implements Serializable{

	private int pedigreeID;
	private String familyID;
	private String individualID;
	private String fatherID;
	private String motherID;
	private int sex;
	private int status;
	private String studyName;
	private int userID;

	
	public int getPedigreeID() {
		return pedigreeID;
	}
	public void setPedigreeID(int pedigreeID) {
		this.pedigreeID = pedigreeID;
	}
	public String getFamilyID() {
		return familyID;
	}
	public void setFamilyID(String familyID) {
		this.familyID = familyID;
	}
	public String getIndividualID() {
		return individualID;
	}
	public void setIndividualID(String individualID) {
		this.individualID = individualID;
	}
	public String getFatherID() {
		return fatherID;
	}
	public void setFatherID(String fatherID) {
		this.fatherID = fatherID;
	}
	public String getMotherID() {
		return motherID;
	}
	public void setMotherID(String motherID) {
		this.motherID = motherID;
	}
	public int getSex() {
		return sex;
	}
	public void setSex(int sex) {
		this.sex = sex;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getStudyName() {
		return studyName;
	}
	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}
	public int getUserID() {
		return userID;
	}
	public void setUserID(int userID) {
		this.userID = userID;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((familyID == null) ? 0 : familyID.hashCode());
		result = prime * result
				+ ((individualID == null) ? 0 : individualID.hashCode());
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
		PedigreeRecord other = (PedigreeRecord) obj;
		if (familyID == null) {
			if (other.familyID != null)
				return false;
		} else if (!familyID.equals(other.familyID))
			return false;
		if (individualID == null) {
			if (other.individualID != null)
				return false;
		} else if (!individualID.equals(other.individualID))
			return false;
		return true;
	}
	
	
	
	/*static public List<String> check(Collection<PedigreeRecord> col){
		List<String> errorList = new ArrayList<String>();
		Map<String, PedigreeRecord> name2individual = new HashMap<String,PedigreeRecord>();
		for(PedigreeRecord rec:col){
			if(name2individual.containsKey(rec.getIndividualID())){
				errorList.add("Duplicate individual ID "+rec.getIndividualID());
				//throw new IllegalArgumentException("duplicate id");
			}
			name2individual.put(rec.getIndividualID(), rec);
		}
		for(String key:name2individual.keySet()){
			check(name2individual.get(key),name2individual,errorList);
		}
		Map<String, Integer> name2sqlkey=new HashMap<String, Integer>();
		for(String key:name2individual.keySet()){
			//insert(name2individual.get(key),name2individual,name2sqlkey);
		}
		return errorList;
	}
	
	static private void check(PedigreeRecord rec,Map<String, PedigreeRecord> name2individual, List<String> errorList){
		PedigreeRecord father;
		PedigreeRecord mother;
		if(rec.getFatherID()!=null && !rec.getFatherID().equals("0")){
			father=name2individual.get(rec.getFatherID());
			if(father==null){
				errorList.add("father used but not defined "+rec.getFatherID());
				return;
				//throw new IllegalArgumentException("father used but not defined "+rec.getFatherID());
			}
			if(father.getSex()!=1 && father.getSex()!=0){
				errorList.add("expected gender=1 for "+rec.getFatherID());
				return;
				//throw new IllegalArgumentException("expected gender=1 for "+rec.getFatherID());
			}
		}
		if(rec.getMotherID()!=null && !rec.getMotherID().equals("0")){
			mother=name2individual.get(rec.getMotherID());
			if(mother==null){
				errorList.add("mother used but not defined "+rec.getMotherID());
				return;
				//throw new IllegalArgumentException("mother used but not defined "+rec.getMotherID());
			}
			if(mother.getSex()!=2 && mother.getSex()!=0){
				errorList.add("expected gender=2 for "+rec.getMotherID());
				return;
				//throw new IllegalArgumentException("expected gender=2 for "+rec.getMotherID());
			}
		}
	}
	
	static int last_insert_id=0;
	
	static private void insert(PedigreeRecord rec,final Map<String, PedigreeRecord> name2individual, Map<String, Integer> name2id){
		if(name2id.containsKey(rec.getIndividualID())) return;//already inserted
		if(rec.getFatherID()!=null && !rec.getFatherID().equals("0")){
			insert(name2individual.get(rec.getFatherID()),name2individual,name2id);
		}
		if(rec.getMotherID()!=null && !rec.getMotherID().equals("0")){
			insert(name2individual.get(rec.getMotherID()),name2individual,name2id);
		}
		System.out.println("insert into database(name,fatherid,motherid)" +rec.getFatherID()+" "+name2id.get(rec.getFatherID())+" "+name2id.get(rec.getMotherID()));
		//int last_insert_id=0;///returned by sql...
		name2id.put(rec.getIndividualID(), last_insert_id++);
	}*/
}
