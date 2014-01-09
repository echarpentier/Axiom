package fr.pfgen.axiom.server.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import fr.pfgen.axiom.client.services.PedigreeService;
import fr.pfgen.axiom.server.database.ConnectionPool;
import fr.pfgen.axiom.server.database.GenoQcValuesTable;
import fr.pfgen.axiom.server.database.PedigreesTable;
import fr.pfgen.axiom.server.database.StudiesTable;
import fr.pfgen.axiom.server.database.StudySamplesTable;
import fr.pfgen.axiom.server.utils.IOUtils;
import fr.pfgen.axiom.shared.GenericGwtRpcList;
import fr.pfgen.axiom.shared.records.GenotypingQCRecord;
import fr.pfgen.axiom.shared.records.PedigreeRecord;
import fr.pfgen.axiom.shared.records.PedigreeState;
import fr.pfgen.axiom.shared.records.StudySampleRecord;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class PedigreeServiceImpl extends RemoteServiceServlet implements PedigreeService{
	
	private ConnectionPool pool;
	private Hashtable<String, File> appFiles; 
	
	@Override
	@SuppressWarnings("unchecked")
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		pool = (ConnectionPool)getServletContext().getAttribute("ConnectionPool");
		appFiles = (Hashtable<String, File>)getServletContext().getAttribute("ApplicationFiles");
	}
	
	@Override
	public List<PedigreeRecord> fetch(Integer startRow, Integer endRow, String sortBy, Map<String, String> filterCriteria) {		
		GenericGwtRpcList<PedigreeRecord> outList = new GenericGwtRpcList<PedigreeRecord>();
	    
		String query = PedigreesTable.constructQuery(sortBy, filterCriteria);
		
    	List<PedigreeRecord> out = PedigreesTable.getPedigreeRecordsInStudy(pool, query);
    	
    	outList.addAll(out);
    	outList.setTotalRows(out.size());
    	
    	return outList;
	}

	@Override
	public synchronized PedigreeRecord add(PedigreeRecord record) {
		return PedigreesTable.addNewRecord(pool, record);
	}

	@Override
	public synchronized PedigreeRecord update(PedigreeRecord record) {
		return PedigreesTable.updatedRecord(pool, record);
	}

	@Override
	public synchronized void remove(PedigreeRecord record) {
		PedigreesTable.removeRecord(pool, record);
	}

	@Override
	public String download(String sortBy, Map<String, String> filterCriteria) {
		String query = PedigreesTable.constructQuery(sortBy, filterCriteria);
		List<PedigreeRecord> pedRecords = PedigreesTable.getPedigreeRecordsInStudy(pool, query);
		File tmpFolder = appFiles.get("temporaryFolder");
		String downloadFilePath = "pedigree_"+Long.toHexString(Double.doubleToLongBits(Math.random()))+".txt";
		File downloadFile = new File(tmpFolder, downloadFilePath);
		
		PrintWriter pw = null;
		
		try{
			pw = new PrintWriter(new FileWriter(downloadFile));
			for (PedigreeRecord record : pedRecords) {
				pw.print(record.getFamilyID()+"\t");
				pw.print(record.getIndividualID()+"\t");
				pw.print(record.getFatherID()+"\t");
				pw.print(record.getMotherID()+"\t");
				pw.print(record.getSex()+"\t");
				pw.print(record.getStatus()+"\n");
			}
			pw.close();
			return downloadFile.getAbsolutePath();
		}catch(IOException e){
			e.printStackTrace();
			return null;
		}finally{
			IOUtils.safeClose(pw);
		}
	}

	@Override
	public PedigreeState checkPedigreeState(String studyName, int userID) {
		PedigreeState pedState = new PedigreeState();
		File studyFolder = StudiesTable.getStudyFolder(pool, studyName);
		File pedigreeFolder = new File(studyFolder, "Pedigree");
		File finalPed = new File(pedigreeFolder, "final_pedigree.txt");
		if (finalPed.exists()){
			pedState.setAllDone();
			return pedState;
		}
		
		String state = PedigreesTable.getPedigreeState(pool, studyName, userID);
		
		if (state==null || state.equals("")){
			pedState.setNoneDone(); //none done
			return pedState;
		}else{
			pedState.setAllDone(); //assume all done
		}
		
		//return all done if final ped exists
		if (!state.equals("final")){
			pedState.setAlldone(false);
		}else{
			return pedState;
		}
		
		if (!state.equals("status")){
			pedState.setStatusChecked(false);
		}else{
			return pedState;
		}
		
		if (!state.equals("sex")){
			pedState.setSexChecked(false);
		}else{
			return pedState;
		}
		
		if (!state.equals("individuals")){
			pedState.setIndividualsDescribed(false);
		}else{
			return pedState;
		}
		
		if (!state.equals("samples")){
			pedState.setSamplesFound(false);
		}else{
			return pedState;
		}
		
		return pedState;
	}
	
	@Override
	public Map<String, String> checkUploadedPedigree(final String studyName, final int userID, final String fileName) {
		File uploadedPedFile = new File(fileName);
		Map<String, String> okMap = isPedigreeWellFormatted(uploadedPedFile);
		if (!okMap.isEmpty()){
			return okMap;
		}else{
			Boolean ok = PedigreesTable.addPedigreeToDB(pool, uploadedPedFile, studyName, userID);
			if (ok==null || !ok) {
				okMap.put("Database error", "Cannot insert pedigree in database !");
				uploadedPedFile.delete();
			}
			return okMap;
		}
	}
	
	private class IndInPed{
		private String famID;
		private String indID;
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((famID == null) ? 0 : famID.hashCode());
			result = prime * result + ((indID == null) ? 0 : indID.hashCode());
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
			IndInPed other = (IndInPed) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (famID == null) {
				if (other.famID != null)
					return false;
			} else if (!famID.equals(other.famID))
				return false;
			if (indID == null) {
				if (other.indID != null)
					return false;
			} else if (!indID.equals(other.indID))
				return false;
			return true;
		}
		private PedigreeServiceImpl getOuterType() {
			return PedigreeServiceImpl.this;
		}
	}
	
	private Map<String, String> isPedigreeWellFormatted(File pedigreeFile){
		
		BufferedReader pedBR = null;
		Map<String, String> okMap = new HashMap<String, String>();
		List<IndInPed> indList = new ArrayList<PedigreeServiceImpl.IndInPed>();
		
		String line = "";
		try{
			pedBR = new BufferedReader(new FileReader(pedigreeFile));

			while((line = pedBR.readLine()) != null) {
				if (line.equals("") || line.startsWith("famID")){
					continue;
				}
				String[] lineSplit = line.split("\\t");
				if (lineSplit.length!=6){
					okMap.put(line, "Wrong number of columns");
					break;
				}
				for (int i = 0; i < lineSplit.length; i++) {
					if (lineSplit[i].equals("")){
						okMap.put(line, "Empty field in column "+(i+1));
						break;
					}
				}
				IndInPed ind = new IndInPed();
				ind.famID = lineSplit[0];
				ind.indID = lineSplit[1];
				if (indList.contains(ind)){
					okMap.put(line, "Duplicate individual in pedigree");
					break;
				}else{
					indList.add(ind);
				}
				
				int sex = Integer.parseInt(lineSplit[4]);
				if (sex!=1 && sex!=2 && sex !=0){
					okMap.put(line, "Wrong gender value");
					break;
				}
				
				int status = Integer.parseInt(lineSplit[5]);
				if (status!=1 && status!=2 && status !=0 && status!=-9){
					okMap.put(line, "Wrong status value");
					break;
				}
			}
			pedBR.close();
			return okMap;
		}catch(IOException e){
			e.printStackTrace();
			okMap.put(line, "Cannot open pedigree");
			return okMap;
		}catch(NumberFormatException e){
			e.printStackTrace();
			okMap.put(line, "Wrong gender or status value");
			return okMap;
		}finally{
			IOUtils.safeClose(pedBR);
		}
	}
	
	@Override
	public Map<String, List<String>> checkSamplesInPedigree(String studyName, int userID) {
		List<String> pedSamples = new ArrayList<String>();
		Map<String, String> filterCriteria = new HashMap<String, String>();
		filterCriteria.put("study_name", studyName);
		filterCriteria.put("user_id", String.valueOf(userID));
		String query = PedigreesTable.constructQuery(null, filterCriteria);
		List<PedigreeRecord> pedigreeRecords = PedigreesTable.getPedigreeRecordsInStudy(pool, query);
		for (PedigreeRecord pedRecord : pedigreeRecords) {
			pedSamples.add(pedRecord.getIndividualID());
		}
		
		List<String> studySamples = new ArrayList<String>();
		List<StudySampleRecord> studySamplesRecords = StudySamplesTable.getSamplesInStudy(pool, studyName);
		for (StudySampleRecord ssRecord : studySamplesRecords) {
			studySamples.add(ssRecord.getSampleName());
		}
		
		List<String> notExistsInStudySamples = new ArrayList<String>();
		List<String> notExistsInPedSamples = new ArrayList<String>();
		List<String> commonSamples = new ArrayList<String>();
		for (String pedSample : pedSamples) {
			if (!studySamples.contains(pedSample)){
				notExistsInStudySamples.add(pedSample);
			}else{
				commonSamples.add(pedSample);
			}
		}
		for (String studySample : studySamples) {
			if (!pedSamples.contains(studySample)){
				notExistsInPedSamples.add(studySample);
			}
		}
		Map<String, List<String>> lists = new HashMap<String, List<String>>();
		lists.put("isNotInPed", notExistsInPedSamples);
		lists.put("isNotInStudy", notExistsInStudySamples);
		lists.put("commonSamples", commonSamples);
		
		return lists;
	}

	@Override
	public Boolean validatePedigree(String studyName, int userID) {
		String state = PedigreesTable.getPedigreeState(pool, studyName, userID);
		if (state.equals("status")){
			File studyFolder = new File(appFiles.get("analysisFile"), "study_"+studyName);
			File pedFolder = new File(studyFolder, "Pedigree");
			if (!pedFolder.exists()){
				pedFolder.mkdir();
			}
			File pedFile = new File(pedFolder, "final_pedigree.txt");
			if (writePedigreeFile(studyName,userID,pedFile.getAbsolutePath(),false)){
				PedigreesTable.removeOtherPedigreesForStudy(pool, studyName, userID);
				return PedigreesTable.validatePedigree(pool, studyName, userID);
			}else{
				return null;
			}
		}else{
			return PedigreesTable.validatePedigree(pool, studyName, userID);
		}
	}
	
	private Boolean writePedigreeFile(String studyName, int userID, String fileName, Boolean header) {
		File pedFile = new File(fileName);
		
		Map<String, String> filterCriteria = new HashMap<String, String>();
		filterCriteria.put("study_name", studyName);
		filterCriteria.put("user_id", String.valueOf(userID));
		String query = PedigreesTable.constructQuery(null, filterCriteria);
		List<PedigreeRecord> pedRecords = PedigreesTable.getPedigreeRecordsInStudy(pool, query);
		
		PrintWriter pw = null;
		
		try{
			pw = new PrintWriter(new FileWriter(pedFile));
			if (header!=null && header){
				pw.println("famID\tindID\tdadID\tmomID\tsex\taffected");
			}
			for (PedigreeRecord rec : pedRecords) {
				pw.println(rec.getFamilyID()+"\t"+rec.getIndividualID()+"\t"+rec.getFatherID()+"\t"+rec.getMotherID()+"\t"+rec.getSex()+"\t"+rec.getStatus());
			}
			pw.close();
			return true;
		}catch(IOException e){
			e.printStackTrace();
			return false;
		}finally{
			IOUtils.safeClose(pw);
		}
	}

	@Override
	public Boolean invalidatePedigree(String studyName, int userID) {
		return PedigreesTable.invalidatePedigree(pool, studyName, userID);
	}

	@Override
	public Map<String, List<String>> checkIndividualsInPedigree(String studyName, int userID) {
		List<String> maleButMother = new ArrayList<String>();
		List<String> femaleButFather = new ArrayList<String>();
		List<String> indNotDescribed = new ArrayList<String>();
		
		List<PedigreeRecord> maleButMotherPeds = PedigreesTable.getMaleButMother(pool, studyName, userID);
		List<PedigreeRecord> femaleButFatherPeds = PedigreesTable.getFemaleButFather(pool, studyName, userID);
		List<PedigreeRecord> indNotDescribedPeds = PedigreesTable.getIndividualsNotDescribed(pool, studyName, userID);
		
		for (PedigreeRecord rec : maleButMotherPeds) {
			maleButMother.add(rec.getIndividualID());
		}
		for (PedigreeRecord rec : femaleButFatherPeds) {
			femaleButFather.add(rec.getIndividualID());
		}
		for (PedigreeRecord rec : indNotDescribedPeds) {
			indNotDescribed.add(rec.getIndividualID());
		}
		
		Map<String, List<String>> checkIndMap = new HashMap<String, List<String>>();
		checkIndMap.put("maleButMother", maleButMother);
		checkIndMap.put("femaleButFather", femaleButFather);
		checkIndMap.put("indNotDescribed", indNotDescribed);
		return checkIndMap;
	}

	@Override
	public Map<String, Map<String, String>> checkGendersInPedigree(String studyName, int userID) {
		Map<String, String> genderForStudySamples = StudiesTable.getGenderForStudySamples(pool, studyName);
		Map<String, String> genderInPedigree = PedigreesTable.getGenderInPedigree(pool, studyName, userID);
		
		Map<String, Map<String, String>> expectedGenders = new HashMap<String, Map<String, String>>();
		if (genderForStudySamples!=null && genderInPedigree!=null){
			for (String ind : genderInPedigree.keySet()) {
				if (genderForStudySamples.containsKey(ind)){
					if (!genderInPedigree.get(ind).equals(genderForStudySamples.get(ind))){
						if (!genderForStudySamples.get(ind).equals("unknown")){
							Map<String, String> diffMap = new HashMap<String, String>();
							diffMap.put("pedigreeGender", genderInPedigree.get(ind));
							diffMap.put("computedGender", genderForStudySamples.get(ind));
							expectedGenders.put(ind, diffMap);
						}
					}
				}
			}
		}else{
			return null;
		}
		return expectedGenders;
	}

	@Override
	public String visualizePedigree(String studyName, int userID) {
		
		File pedFile = new File(appFiles.get("temporaryFolder"), "Ped_"+Long.toHexString(Double.doubleToLongBits(Math.random()))+".txt");
		
		if (writePedigreeFile(studyName, userID, pedFile.getAbsolutePath(), true)){
			File pedPdf = new File(appFiles.get("temporaryFolder"), "Ped_"+Long.toHexString(Double.doubleToLongBits(Math.random()))+".pdf");
			Process process = null;
			try{
				String[] cmd = {appFiles.get("RScriptBin").getAbsolutePath(),appFiles.get("RScripts").getAbsolutePath()+"/KinshipGraph.R",pedFile.getAbsolutePath(),pedPdf.getAbsolutePath()};
				ProcessBuilder procBuilder = new ProcessBuilder(cmd);
				procBuilder.redirectErrorStream(true);
			
				process = procBuilder.start();
				process.waitFor();
				if (process.exitValue()==0){
					return pedPdf.getAbsolutePath();
				}else{
					return null;
				}
			}catch(IOException e){
				e.printStackTrace();
				return null;
			}catch(InterruptedException e) {
				e.printStackTrace();
				return null;
			}finally{
				if (process!=null){ process.destroy();}
			}
		}else{
			return null;
		}
	}

	@Override
	public Map<String, List<GenotypingQCRecord>> checkDuplicateSamplesInCalls(String studyName) {
		Map<String, String> criterias = new HashMap<String, String>();
		criterias.put("study_name", studyName);
		
		//get all individual names in pedigree for this study
		String query = PedigreesTable.constructQuery(null, criterias);
		List<PedigreeRecord> pedRecList = PedigreesTable.getPedigreeRecordsInStudy(pool, query);
		
		//get all records in study
		query = GenoQcValuesTable.constructQuery(null, criterias);
		List<GenotypingQCRecord> genoQcRecList = GenoQcValuesTable.getSamplesQC(pool, query, null, null);
		
		//affect list of records if more than one record with identical sample name
		Map<String, List<GenotypingQCRecord>> list = new HashMap<String, List<GenotypingQCRecord>>();
		List<GenotypingQCRecord> tmpList = new ArrayList<GenotypingQCRecord>();
		for (PedigreeRecord pedRec : pedRecList) {
			tmpList.clear();
			for (GenotypingQCRecord genoQcRec : genoQcRecList) {
				if (pedRec.getIndividualID().equals(genoQcRec.getSampleName())){
					tmpList.add(genoQcRec);
				}
			}
			if (tmpList.size()>1){
				List<GenotypingQCRecord> duplicateList = new ArrayList<GenotypingQCRecord>();
				duplicateList.addAll(tmpList);
				list.put(pedRec.getIndividualID(), duplicateList);
			}
		}
		
		return list;
	}

	@Override
	public List<String> getSubPopNames(String studyName) {
		return PedigreesTable.getSubPopNamesForStudy(pool, studyName);
	}

	@Override
	public Map<String, String> checkUploadedSubPopulation(String studyName, String subPopName, String filePath) {
		File uploadedPedFile = new File(filePath);
		Map<String, String> okMap = isPedigreeWellFormatted(uploadedPedFile);
		if (!okMap.isEmpty()){
			return okMap;
		}else{
			Map<String, String> crits = new HashMap<String, String>();
			String query = PedigreesTable.constructQuery(null, crits);
			List<PedigreeRecord> pedRecList = PedigreesTable.getPedigreeRecordsInStudy(pool, query);
			
			BufferedReader br = null;
			String line = "";
			Pattern space = Pattern.compile("[\\s]");
			List<Integer> subPedIds = new ArrayList<Integer>();
			try{
				br = new BufferedReader(new FileReader(uploadedPedFile));
				
				while((line=br.readLine())!=null){
					if (line.isEmpty()) continue;
					if (line.startsWith("famID")) continue;
					String linesplit[] = space.split(line);
					if (linesplit.length!=6){
						okMap.put(line, "wrong number of columns in line");
						return okMap;
					}
					PedigreeRecord rec = new PedigreeRecord();
					rec.setFamilyID(linesplit[0]);
					rec.setIndividualID(linesplit[1]);
					
					int index = pedRecList.indexOf(rec);
					if (index==-1){
						okMap.put(line, "individual does not exist in full pedigree");
						return okMap;
					}else{
						subPedIds.add(pedRecList.get(index).getPedigreeID());
					}
				}
				br.close();
			}catch(IOException e){
				e.printStackTrace();
				okMap.put(line , "Error while reading uploaded pedigree");
				return okMap;
			}finally{
				IOUtils.safeClose(br);
			}
			
			boolean ok = PedigreesTable.addSubPopToStudy(pool, studyName, subPopName, subPedIds);
			if (!ok){
				okMap.put("Database insert error", "Could not insert subpopulation in database");
			}
			return okMap;
		}
	}
}
