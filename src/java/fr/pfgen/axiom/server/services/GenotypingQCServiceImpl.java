package fr.pfgen.axiom.server.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import fr.pfgen.axiom.client.services.GenotypingQCService;
import fr.pfgen.axiom.server.database.ConnectionPool;
import fr.pfgen.axiom.server.database.GenoAnalysisTable;
import fr.pfgen.axiom.server.database.GenoQcParamsTable;
import fr.pfgen.axiom.server.database.GenoQcValuesTable;
import fr.pfgen.axiom.server.utils.DatabaseUtils;
import fr.pfgen.axiom.shared.GenericGwtRpcList;
import fr.pfgen.axiom.shared.records.GenotypingQCRecord;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class GenotypingQCServiceImpl extends RemoteServiceServlet implements GenotypingQCService{

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
	public GenotypingQCRecord add(GenotypingQCRecord data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GenotypingQCRecord update(GenotypingQCRecord data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove(GenotypingQCRecord data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<String> getGenoQcParams() {
		ConnectionPool pool = (ConnectionPool)getServletContext().getAttribute("ConnectionPool");
		List<String> qcList = GenoQcParamsTable.getGenoQcParamsNames(pool);
		return qcList;
	}
	
	@Override
	public List<GenotypingQCRecord> fetch(Integer startRow, Integer endRow, String sortBy, Map<String, String> filterCriteria) {
		GenericGwtRpcList<GenotypingQCRecord> outList = new GenericGwtRpcList<GenotypingQCRecord>();
	    
    	String query = GenoQcValuesTable.constructQuery(sortBy,filterCriteria);
    	
    	outList.setTotalRows(DatabaseUtils.countRowInQuery(pool, query,false));
    	
    	List<GenotypingQCRecord> out = GenoQcValuesTable.getSamplesQC(pool,query,startRow,endRow);
    	
    	outList.addAll(out);
    	
    	return outList;
	}

	@Override
	public synchronized String download(String sortBy, Map<String, String> criterias) {
		String query = GenoQcValuesTable.constructQuery(null,criterias);
		List<GenotypingQCRecord> sampleList = GenoQcValuesTable.getSamplesQC(pool,query,null,null);
		if (sampleList.size()<1){
			return null;
		}
		String outFilePath = "GenoQCResults_"+Long.toHexString(Double.doubleToLongBits(Math.random()))+".txt";
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
			GenotypingQCRecord firstRecord = sampleList.get(0);
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
			for (GenotypingQCRecord record : sampleList) {
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
	public boolean secondRunExistsInGenoAnalysis(String genoName) {
		return GenoAnalysisTable.checkIfSecondRunExist(pool,genoName);
	}
}
