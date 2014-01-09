package fr.pfgen.axiom.server.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fr.pfgen.axiom.server.utils.DatabaseUtils;
import fr.pfgen.axiom.shared.records.GenotypingQCRecord;

public class GenoQcValuesTable {

	public static String constructQuery(String sortBy, Map<String, String> filterCriteria) {
		String query = "SELECT s.sample_id,s.sample_name,pl.plate_name,gr.geno_run_id,gr.geno_run,ga.geno_id,ga.geno_name FROM ((((samples s JOIN plates pl ON s.plate_id=pl.plate_id) JOIN genotyping_samples gs ON s.sample_id=gs.sample_id) JOIN genotyping_analysis ga ON gs.geno_id=ga.geno_id) JOIN genotyping_runs gr ON gs.geno_sample_id=gr.geno_sample_id)";
		
		//filter criterias
		if (filterCriteria != null && !filterCriteria.isEmpty()) {
			Map<String,String> critMap = new Hashtable<String, String>();
			if (filterCriteria.containsKey("callRateLimit")){
				query = "SELECT s.sample_id,s.sample_name,pl.plate_name,gr.geno_run_id,gr.geno_run,ga.geno_id,ga.geno_name FROM ((((((samples s JOIN plates pl ON s.plate_id=pl.plate_id) JOIN genotyping_samples gs ON s.sample_id=gs.sample_id) JOIN genotyping_analysis ga ON gs.geno_id=ga.geno_id) JOIN genotyping_runs gr ON gr.geno_sample_id=gs.geno_sample_id) JOIN genotyping_qc_values gv ON gv.geno_run_id=gr.geno_run_id) JOIN genotyping_qc_params gp ON gp.geno_qc_param_id=gv.geno_qc_param_id) WHERE gr.geno_run=\"first\" AND ga.geno_name=\""+filterCriteria.get("callRateLimit")+"\" AND gp.geno_qc_name=\"call_rate\" AND gv.geno_qc_value<(SELECT callRateLimit from genotyping_analysis WHERE geno_name=\""+filterCriteria.get("callRateLimit")+"\")";
			}
			if (filterCriteria.containsKey("sample_id")) {
				critMap.put("s.sample_id",filterCriteria.get("sample_id"));
			}
			if (filterCriteria.containsKey("sample_name")){
				critMap.put("s.sample_name",filterCriteria.get("sample_name"));
			}
			if (filterCriteria.containsKey("plate_name")){
				critMap.put("pl.plate_name",filterCriteria.get("plate_name"));
			}
			if (filterCriteria.containsKey("geno_name")){
				critMap.put("ga.geno_name",filterCriteria.get("geno_name"));
			}
			if (filterCriteria.containsKey("geno_run")){
				critMap.put("gr.geno_run", filterCriteria.get("geno_run"));
			}
			if (filterCriteria.containsKey("study_name")){
				query = "SELECT s.sample_id,s.sample_name,pl.plate_name,gr.geno_run_id,gr.geno_run,ga.geno_id,ga.geno_name FROM ((((((samples s JOIN plates pl ON s.plate_id=pl.plate_id) JOIN genotyping_samples gs ON s.sample_id=gs.sample_id) JOIN genotyping_analysis ga ON gs.geno_id=ga.geno_id) JOIN genotyping_runs gr ON gs.geno_sample_id=gr.geno_sample_id) JOIN study_samples ss ON ss.geno_run_id=gr.geno_run_id) JOIN studies st ON st.study_id=ss.study_id)";
				critMap.put("study_name", filterCriteria.get("study_name"));
			}
			if (!critMap.isEmpty()){
				query = query.concat(DatabaseUtils.constructWhereClause(critMap));
			}
		}
		
		//Sort By
		if (sortBy != null && !sortBy.isEmpty()){
			query = query.concat(DatabaseUtils.constructOrderByClause(sortBy));
		}
		
		return query;
	}

	public synchronized static List<GenotypingQCRecord> getSamplesQC(ConnectionPool pool, String query, Integer startRow, Integer endRow) {
		List<GenotypingQCRecord> recordList = new ArrayList<GenotypingQCRecord>();
		
		//Requested rows
		if (startRow != null && endRow != null && endRow >= startRow) {
			String rowsToString = " LIMIT ";
			Integer size = endRow-startRow;
        	rowsToString = rowsToString.concat(startRow+","+size);
        	query = query.replaceAll(";$", "");
        	query = query.concat(rowsToString+";");
		}
		
		Connection con = null;
		Statement stmt = null;
		ResultSet rsSamples = null;
		
		PreparedStatement psQC = null;
		ResultSet rsQC = null;
		
		PreparedStatement ps_population = null;
		ResultSet rs_population = null;
		
		try{
			con = pool.getConnection();
			ps_population = con.prepareStatement("SELECT p.population_name FROM populations p JOIN samples_in_populations sp ON sp.population_id=p.population_id WHERE sp.sample_id=?");
			psQC = con.prepareStatement("SELECT gp.geno_qc_name,gv.geno_qc_value FROM ((genotyping_runs gr JOIN genotyping_qc_values gv ON gr.geno_run_id=gv.geno_run_id) JOIN genotyping_qc_params gp ON gp.geno_qc_param_id=gv.geno_qc_param_id) WHERE gr.geno_run_id=?");
			stmt = con.createStatement();
			rsSamples = stmt.executeQuery(query);
			while (rsSamples.next()){
				GenotypingQCRecord record = new GenotypingQCRecord();
				record.setSampleID(rsSamples.getInt("sample_id"));
				record.setSampleName(rsSamples.getString("sample_name"));
				record.setPlateName(rsSamples.getString("plate_name"));
				
				List<String> popNamesList = new ArrayList<String>();
				ps_population.setInt(1, rsSamples.getInt("sample_id"));
				rs_population = ps_population.executeQuery();
				while (rs_population.next()){
					popNamesList.add(rs_population.getString("population_name"));
				}
				record.setPopulationNames(popNamesList);
				rs_population.close();
				record.setGenoRunID(rsSamples.getInt("geno_run_id"));
				record.setRun(rsSamples.getString("geno_run"));
				record.setGenoID(rsSamples.getInt("geno_id"));
				record.setGenoName(rsSamples.getString("geno_name"));
				
				psQC.setInt(1, rsSamples.getInt("geno_run_id"));
				rsQC = psQC.executeQuery();
				HashMap<String, String> genoQCParams = new HashMap<String, String>();
				while (rsQC.next()){
					genoQCParams.put(rsQC.getString("geno_qc_name"), rsQC.getString("geno_qc_value"));
				}
				rsQC.close();
				record.setQcMap(genoQCParams);
				
				recordList.add(record);
			}
			ps_population.close();
			psQC.close();
			rsSamples.close();
			stmt.close();
		} catch (SQLException e){
			throw new RuntimeException(e);
		} finally {
			if (stmt!=null){
				try {stmt.close();} catch (SQLException e) {}
			}
			if (rs_population!=null){
				try {rs_population.close();} catch (SQLException e) {}
			}
			if (ps_population!=null){
				try {ps_population.close();} catch (SQLException e) {}
			}
			if (rsSamples!=null){
				try {rsSamples.close();} catch (SQLException e) {}
			}
			if (psQC!=null){
				try {psQC.close();} catch (SQLException e) {}
			}
			if (rsQC!=null){
				try {rsQC.close();} catch (SQLException e) {}
			}
			pool.close(con);
		}
		return recordList;
	}

	@Deprecated
	public static void insertGenoQcValues(Connection con, PreparedStatement ps, int genoSampleKey, Map<String, String> qcNameQcValueMap) {
		try{
			if (qcNameQcValueMap != null && !qcNameQcValueMap.isEmpty()){
				ps.setInt(3, genoSampleKey);
				for (String qcName : qcNameQcValueMap.keySet()) {
					ps.setString(1, qcNameQcValueMap.get(qcName));
					ps.setString(2, qcName);
					ps.execute();
				}
			}
		}catch(SQLException e){
			throw new RuntimeException(e);
		}
	}
	
	public static void insertGenoQc(ConnectionPool pool, LinkedHashMap<String, String> uniqueCelFiles, Map<String, String> changedCelFiles, File resultFolder, int genoAnalysisKey, String run) {
		Connection con = null;
		
		String insertGenoRun = "INSERT IGNORE INTO genotyping_runs SET geno_sample_id=(SELECT geno_sample_id FROM genotyping_samples WHERE geno_id=? AND sample_id=(SELECT sample_id FROM samples WHERE sample_path=?)),geno_run=?,chp_path=?";
		PreparedStatement psGenoRun = null;
		ResultSet rsGenoRunKey = null;
		
		String insertGenoQc = "INSERT IGNORE INTO genotyping_qc_values SET geno_qc_value=?,geno_qc_param_id=(SELECT geno_qc_param_id FROM genotyping_qc_params WHERE geno_qc_name=?),geno_run_id=?";
		PreparedStatement psGenoQc = null;
		
		File reportFile = new File(resultFolder, "AxiomGT1.report.txt");
		BufferedReader qcBF = null;
		
		try{
			con = pool.getConnection();
			psGenoRun = con.prepareStatement(insertGenoRun,Statement.RETURN_GENERATED_KEYS);
			psGenoQc = con.prepareStatement(insertGenoQc);
			
			//reverse changedCelFiles
			Map<String,String> reversedChangedCelFiles = new Hashtable<String, String>();
			for (String oldpath : changedCelFiles.keySet()) {
				reversedChangedCelFiles.put(changedCelFiles.get(oldpath), oldpath);
			}
			
			//read reportFile
			Map<Integer,String> indexQcNameMap = new Hashtable<Integer, String>();
			Map<String, Map<String,String>> celValuesMap = new Hashtable<String, Map<String,String>>();
			
			qcBF = new BufferedReader(new FileReader(reportFile));
			String line;

			while ((line = qcBF.readLine()) != null){
				if (line.startsWith("#")){
					continue;
				} else if (line.startsWith("cel_files")){
					String[] qcNames = line.split("\\t");
					for (int i = 0; i < qcNames.length; i++) {
						String qcName = qcNames[i];
						indexQcNameMap.put(i, qcName);
					}
				} else {
					Map<String,String> QcNameQcValueMap = new Hashtable<String, String>();
					String[] qcValues = line.split("\\t");
					for (int i = 0; i < qcValues.length; i++) {
						String qcValue = qcValues[i];
						QcNameQcValueMap.put(indexQcNameMap.get(i), qcValue);
					}
					String celName = QcNameQcValueMap.get("cel_files");
					QcNameQcValueMap.remove("cel_files");
					celValuesMap.put(celName, QcNameQcValueMap);
				}
			}
			qcBF.close();
			
			//insert into genotyping_runs and genotyping_qc_values
			psGenoRun.setInt(1, genoAnalysisKey);
			psGenoRun.setString(3, run);
			for (String celName : celValuesMap.keySet()) {
				String celPath = uniqueCelFiles.get(celName);
				if (reversedChangedCelFiles.containsKey(celPath)){
					celPath = reversedChangedCelFiles.get(celPath);
				}
				String chpPath = resultFolder.getAbsolutePath()+"/CHP/"+celName.replaceAll("\\.CEL", ".AxiomGT1.chp.txt.gz");
				psGenoRun.setString(2, DatabaseUtils.realDataPathToDbPath(celPath));
				psGenoRun.setString(4, DatabaseUtils.realAxiomPathToDbPath(chpPath));
				
				psGenoRun.executeUpdate();
				
				//get key of new row in genotyping_runs
				rsGenoRunKey = psGenoRun.getGeneratedKeys();
				int genoRunKey;
				if (rsGenoRunKey.next()){
					genoRunKey = rsGenoRunKey.getInt(1);
				}else{
					throw new RuntimeException("Cannot return generated key while inserting new row in 'genotyping_runs'");
				}
				rsGenoRunKey.close();
				
				psGenoQc.setInt(3, genoRunKey);
				for (String qcName : celValuesMap.get(celName).keySet()) {
					psGenoQc.setString(1, celValuesMap.get(celName).get(qcName));
					psGenoQc.setString(2, qcName);
					psGenoQc.executeUpdate();
				}
			}
			psGenoQc.close();
			psGenoRun.close();
		}catch(IOException e){
			throw new RuntimeException(e);
		}catch(SQLException e){
			throw new RuntimeException(e);
		}finally{
			if (psGenoQc!=null){
				try{psGenoQc.close();}catch(SQLException e){}
			}
			if (psGenoRun!=null){
				try{psGenoRun.close();}catch(SQLException e){}
			}
			if (rsGenoRunKey!=null){
				try{rsGenoRunKey.close();}catch(SQLException e){}
			}
			pool.close(con);
		}
	}
}
