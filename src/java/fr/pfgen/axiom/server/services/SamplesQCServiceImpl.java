package fr.pfgen.axiom.server.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import fr.pfgen.axiom.client.services.SamplesQCService;
import fr.pfgen.axiom.server.database.ConnectionPool;
import fr.pfgen.axiom.server.database.QcParamsTable;
import fr.pfgen.axiom.server.database.QcValuesTable;
import fr.pfgen.axiom.server.database.SamplesTable;
import fr.pfgen.axiom.server.utils.DatabaseUtils;
import fr.pfgen.axiom.server.utils.IOUtils;
import fr.pfgen.axiom.server.utils.ServerUtils;
import fr.pfgen.axiom.shared.GenericGwtRpcList;
import fr.pfgen.axiom.shared.records.SampleQCRecord;
import fr.pfgen.axiom.shared.records.SampleRecord;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class SamplesQCServiceImpl extends RemoteServiceServlet implements SamplesQCService {

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
	public SampleQCRecord add(SampleQCRecord data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SampleQCRecord update(SampleQCRecord data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove(SampleQCRecord data) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public List<SampleQCRecord> fetch(Integer startRow, Integer endRow, String sortBy, Map<String, String> filterCriteria) {
		
		GenericGwtRpcList<SampleQCRecord> outList = new GenericGwtRpcList<SampleQCRecord>();
	    
    	String query = QcValuesTable.constructQuery(sortBy,filterCriteria);
    	
    	outList.setTotalRows(DatabaseUtils.countRowInQuery(pool, query,false));
    	
    	List<SampleQCRecord> out = QcValuesTable.getSamplesQC(pool,query,startRow,endRow);
    	
    	outList.addAll(out);
    	
    	return outList;
	}
	
	@Override
	public int nbSamplesBelowQcInGenoAnalysis(String genoName) {
		String query = "SELECT s.sample_id,s.sample_name,pl.plate_name,p.population_name FROM (((((((samples s JOIN plates pl ON s.plate_id=pl.plate_id) LEFT JOIN samples_in_populations sp ON s.sample_id=sp.sample_id) LEFT JOIN populations p ON p.population_id = sp.population_id) JOIN genotyping_samples gs ON s.sample_id=gs.sample_id) JOIN genotyping_analysis ga ON gs.geno_id=ga.geno_id) JOIN qc_values qv ON qv.sample_id=s.sample_id) JOIN qc_params qp ON qp.qc_param_id=qv.qc_param_id) WHERE ga.geno_name=\""+genoName+"\" AND qp.qc_name=\"axiom_dishqc_DQC\" AND qv.qc_value<(SELECT dishQCLimit from genotyping_analysis WHERE geno_name=\""+genoName+"\")";
		return DatabaseUtils.countRowInQuery(pool,query,false);
	}

	@Override
	public List<String> getQcParams() {
		List<String> qcList = QcParamsTable.getQcParamsNames(pool);
		return qcList;
	}
	
	@Override
	public List<String> getUserParams() {
		List<String> qcList = QcParamsTable.getUserParamsNames(pool);
		return qcList;
	}

	@Override
	public synchronized String download(String sortBy, Map<String, String> criterias) {
		String query = QcValuesTable.constructQuery(null,criterias);
		List<SampleQCRecord> sampleList = QcValuesTable.getSamplesQC(pool,query,null,null);
		if (sampleList.size()<1){
			return null;
		}
		
		String outFilePath = "QCResults_"+ServerUtils.randomHexString()+".txt";;
		File tmpFolder = appFiles.get("temporaryFolder");
		if (!tmpFolder.exists()){
			if (!tmpFolder.mkdir()){
				throw new RuntimeException("Cannot create "+tmpFolder.getAbsolutePath());
			}
		}
		File outFile = new File(tmpFolder, outFilePath);
		PrintWriter pw = null;
		try{
			pw = new PrintWriter(new FileWriter(outFile));
			SampleQCRecord firstRecord = sampleList.get(0);
			//get an ordered list of qc names
			List<String> qcNameList = new ArrayList<String>();
			for (String qcName : firstRecord.getQcMap().keySet()) {
				qcNameList.add(qcName);
			}
			//print header in file
			pw.print("sampleID\tsampleName\tplateName\tpopulations\t");
			for (int i = 0; i < (qcNameList.size())-1; i++) {
				pw.print(qcNameList.get(i)+"\t");
			}
			pw.print(qcNameList.get(qcNameList.size()-1)+"\n");
			
			//print line for each sample
			for (SampleQCRecord record : sampleList) {
				pw.print(record.getSampleID()+"\t");
				pw.print(record.getSampleName()+"\t");
				pw.print(record.getPlateName()+"\t");
				if (record.getPopulationNames()!=null){
		        	StringBuilder popNames = new StringBuilder();
		        	for (String popName : record.getPopulationNames()) {
		        		popNames.append(popName+",");
		        	}
		        	String s = popNames.toString().replaceAll(",$", "");
		        	pw.print(s+"\t");
		        }else{
		        	pw.print("\t");
		        }
				
				for (int i = 0; i < (qcNameList.size())-1; i++) {
					pw.print(record.getQcMap().get(qcNameList.get(i))+"\t");
				}
				pw.print(record.getQcMap().get(qcNameList.get(qcNameList.size()-1))+"\n");
			}
			
			pw.flush();
			pw.close();
		}catch(IOException e){
			throw new RuntimeException(e);
		}finally{
			if (pw!=null) pw.flush();pw.close();
		}
		
		return outFile.getAbsolutePath();
	}

	@Override
	public Map<String, String> checkUploadedQCForPlate(String plateName, String filePath) {
		
		Map<String, String> crits = new HashMap<String, String>();
		crits.put("plate_name", plateName);
		String query = SamplesTable.constructQuery(null, crits);
		List<SampleRecord> samplesInDB = SamplesTable.getSamples(pool, query, null, null);
		return checkUploadedFile(samplesInDB, filePath);
	}	
	
	@Override
	public Map<String, String> checkUploadedQCForPopulation(String populationName, String filePath) {
		Map<String, String> crits = new HashMap<String, String>();
		crits.put("population_name", populationName);
		String query = SamplesTable.constructQuery(null, crits);
		List<SampleRecord> samplesInDB = SamplesTable.getSamples(pool, query, null, null);
		return checkUploadedFile(samplesInDB, filePath);
	}
		
	private Map<String, String> checkUploadedFile(List<SampleRecord> samplesInDB, String filePath){	
		File uploadedFile = new File(filePath);
		if (!uploadedFile.exists()){
			return null;
		}
		
		Map<String, SampleRecord> sampleName2sampleRecord = new HashMap<String, SampleRecord>();
		for (SampleRecord sampleRecord : samplesInDB) {
			sampleName2sampleRecord.put(sampleRecord.getSampleName(), sampleRecord);
		}
		List<String> paramNamesInDB = QcParamsTable.getUserParamsNames(pool);
		
		Map<String, String> errorsInFile = new HashMap<String, String>();
		
		BufferedReader br = null;
		Pattern tab = Pattern.compile("[\t]");
		
		String headerLine = ServerUtils.GetLineByIndex(1, uploadedFile);
		String[] header = tab.split(headerLine);
		Map<String, Integer> colName2colNumber = new HashMap<String, Integer>();
		for (int i=0;i<header.length;i++) {
			if (colName2colNumber.containsKey(header[i])){
				errorsInFile.put("Duplicate QC name (last found will be used):", header[i]);
			}
			colName2colNumber.put(header[i], i);
		}
		if (!colName2colNumber.containsKey("sampleName")){
			errorsInFile.put("No column named 'sampleName'", headerLine);
			return errorsInFile;
		}
		for (String paramName : colName2colNumber.keySet()) {
			if (paramName.equals("sampleName")){
				continue;
			}
			if (!paramNamesInDB.contains(paramName)){
				errorsInFile.put(paramName+" does not exist", "Parameter will be inserted in database");
			}
		}
		
		try{
			br = IOUtils.openFile(uploadedFile);
			String line;
			boolean headerFound = false;
			while((line=br.readLine())!=null){
				if (!headerFound){
					headerFound = true;
					continue;
				}
				if (line.isEmpty()) continue;
				String[] linesplit = tab.split(line);
				String sampleName = "";
				try{
					sampleName = linesplit[colName2colNumber.get("sampleName")];
					if (!sampleName2sampleRecord.containsKey(sampleName)){
						errorsInFile.put(sampleName+" does not exist in database", line);
					}
				}catch(IndexOutOfBoundsException e){
					errorsInFile.put("No sample name for line:", line);
				}
			}
			br.close();
		}catch(IOException e){
			e.printStackTrace();
			return null;
		}finally{
			IOUtils.safeClose(br);
		}
		
		return errorsInFile;
	}

	@Override
	public String addUserQCForPlate(String plateName, String filePath) {
		Map<String, String> crits = new HashMap<String, String>();
		crits.put("plate_name", plateName);
		String query = SamplesTable.constructQuery(null, crits);
		List<SampleRecord> samplesInDB = SamplesTable.getSamples(pool, query, null, null);
		return addUserQCInDB(samplesInDB, filePath);
	}
	
	@Override
	public String addUserQCForPopulation(String populationName, String filePath) {
		Map<String, String> crits = new HashMap<String, String>();
		crits.put("population_name", populationName);
		String query = SamplesTable.constructQuery(null, crits);
		List<SampleRecord> samplesInDB = SamplesTable.getSamples(pool, query, null, null);
		return addUserQCInDB(samplesInDB, filePath);
	}
	
	private String addUserQCInDB(List<SampleRecord> samplesInDB, String filePath){	
		File uploadedFile = new File(filePath);
		if (!uploadedFile.exists()){
			return null;
		}
		
		Map<String, SampleRecord> sampleName2sampleRecord = new HashMap<String, SampleRecord>();
		for (SampleRecord sampleRecord : samplesInDB) {
			sampleName2sampleRecord.put(sampleRecord.getSampleName(), sampleRecord);
		}
		List<String> paramNamesInDB = QcParamsTable.getUserParamsNames(pool);
				
		Pattern tab = Pattern.compile("[\t]");
		
		String headerLine = ServerUtils.GetLineByIndex(1, uploadedFile);
		String[] header = tab.split(headerLine);
		Map<String, Integer> colName2colNumber = new HashMap<String, Integer>();
		for (int i=0;i<header.length;i++) {
			colName2colNumber.put(header[i], i);
		}
		
		Set<String> paramList = new TreeSet<String>();
		for (String paramName : colName2colNumber.keySet()) {
			if (paramName.equals("sampleName")){
				continue;
			}
			if (!paramNamesInDB.contains(paramName)){
				paramList.add(paramName);
			}
		}
		
		if (!QcParamsTable.insertNewUserParam(pool, paramList)){
			return "Error: insertion in database failed";
		}
		
		BufferedReader br = null;
		Map<String, Map<Integer, String>> inserts = new HashMap<String, Map<Integer,String>>();
		for (String colName : colName2colNumber.keySet()) {
			if (colName.equals("sampleName")) continue;
			inserts.put(colName, new HashMap<Integer, String>());
		}
		
		try{
			br = IOUtils.openFile(uploadedFile);
			String line;
			boolean headerFound = false;
			int i = 0;
			while((line=br.readLine())!=null){
				i++;
				if (!headerFound){
					headerFound = true;
					continue;
				}
				if (line.isEmpty()) continue;
				String[] linesplit = tab.split(line);
				String sampleName = "";
				try{
					sampleName = linesplit[colName2colNumber.get("sampleName")];
				}catch(IndexOutOfBoundsException e){
					continue;
				}
				if (sampleName2sampleRecord.containsKey(sampleName)){
					for (String colName : colName2colNumber.keySet()) {
						if (colName.equals("sampleName")){
							continue;
						}
						try{
							String value = linesplit[colName2colNumber.get(colName)];
							if (value!=null && !value.isEmpty()){
								inserts.get(colName).put(sampleName2sampleRecord.get(sampleName).getSampleID(), value);
							}
						}catch(IndexOutOfBoundsException e){
							continue;
						}
					}
				}
			}
			br.close();
		}catch(IOException e){
			e.printStackTrace();
			return null;
		}finally{
			IOUtils.safeClose(br);
		}
		
		if (!QcParamsTable.insertUserQCValues(pool, inserts)){
			return "Error: insertion in database failed";
		}

		uploadedFile.delete();
		return "User QC successfully added to database";
	}

	@Override
	public List<String> getAllQcParams() {
		List<String> list = new ArrayList<String>();
		list.addAll(QcParamsTable.getQcParamsNames(pool));
		list.addAll(QcParamsTable.getUserParamsNames(pool));
		return list;
	}

	@Override
	public String makeQCGraphForPlate(String plateName, String xAxis, String yAxis) {
		Map<Integer, Map<String, String>> samples2qc = QcValuesTable.getQCParamsForPlate(pool, plateName, xAxis, yAxis);
		String qcFile = makeQCFile(samples2qc, xAxis, yAxis);
		return makeQCGraph(qcFile, plateName);
	}
	
	@Override
	public String makeQCGraphForPopulation(String populationName, String xAxis, String yAxis) {
		Map<Integer, Map<String, String>> samples2qc = QcValuesTable.getQCParamsForPopulation(pool, populationName, xAxis, yAxis);
		String qcFile = makeQCFile(samples2qc, xAxis, yAxis);
		return makeQCGraph(qcFile, populationName);
	}
	
	@Override
	public String makeQCTsvForPlate(String plateName, String xAxis, String yAxis) {
		Map<Integer, Map<String, String>> samples2qc = QcValuesTable.getQCParamsForPlate(pool, plateName, xAxis, yAxis);
		return makeQCFile(samples2qc, xAxis, yAxis);
	}
	
	@Override
	public String makeQCTsvForPopulation(String populationName, String xAxis, String yAxis) {
		Map<Integer, Map<String, String>> samples2qc = QcValuesTable.getQCParamsForPopulation(pool, populationName, xAxis, yAxis);
		return makeQCFile(samples2qc, xAxis, yAxis);
	}
	
	private String makeQCFile(Map<Integer, Map<String, String>> samples2qc, String xAxis, String yAxis){
		String outFilePath = "rInput_"+ServerUtils.randomHexString()+".txt";;
		File tmpFolder = appFiles.get("temporaryFolder");
		if (!tmpFolder.exists()){
			if (!tmpFolder.mkdir()){
				throw new RuntimeException("Cannot create "+tmpFolder.getAbsolutePath());
			}
		}
		File outFile = new File(tmpFolder, outFilePath);
		PrintWriter out = null;
		
		try{
			out = new PrintWriter(new FileWriter(outFile));
			List<String> paramNames = new ArrayList<String>(2);
			out.println("sampleID\t"+xAxis+"\t"+yAxis);
			paramNames.add(xAxis);
			paramNames.add(yAxis);
			for (Integer sampleID : samples2qc.keySet()) {
				out.print(sampleID);
				out.print("\t"+samples2qc.get(sampleID).get(xAxis));
				out.print("\t"+samples2qc.get(sampleID).get(yAxis));
				out.println();
			}
			out.close();
			return outFile.getAbsolutePath();
		}catch(IOException e){
			e.printStackTrace();
			return null;
		}finally{
			IOUtils.safeClose(out);
		}
	}
	
	private String makeQCGraph(String QCFilePath, String graphTitle){
	
		File tmpFolder = appFiles.get("temporaryFolder");
		if (!tmpFolder.exists()){
			if (!tmpFolder.mkdir()){
				throw new RuntimeException("Cannot create "+tmpFolder.getAbsolutePath());
			}
		}
		File QCFile = new File(QCFilePath);
		File graphFile = new File(tmpFolder, "Graph_"+ServerUtils.randomHexString()+".png");
		Process process = null;
		
		try{
			String[] cmd = {appFiles.get("RScriptBin").getAbsolutePath(),appFiles.get("RScripts")+"/QCGraph.R", QCFile.getAbsolutePath(), graphFile.getAbsolutePath(), graphTitle};
			
			ProcessBuilder procBuilder = new ProcessBuilder(cmd);
			process = procBuilder.start();
			process.waitFor();
			process.destroy();
			
			return graphFile.getAbsolutePath();
		
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}finally{
			if (process!=null){
				process.destroy();
			}
		}
	}
}
