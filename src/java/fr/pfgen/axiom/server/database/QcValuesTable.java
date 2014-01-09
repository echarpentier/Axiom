package fr.pfgen.axiom.server.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import fr.pfgen.axiom.server.utils.DatabaseUtils;
import fr.pfgen.axiom.server.utils.SQLUtils;
import fr.pfgen.axiom.shared.records.SampleQCRecord;

public class QcValuesTable {

	public static synchronized void insertQCValues(ConnectionPool pool,Map<String, Map<String,String>> pathValuesMap){
		Connection connection = null;
		String sql = "INSERT INTO qc_values SET qc_value=?,qc_param_id=(SELECT qc_param_id FROM qc_params WHERE qc_name=?),sample_id=(SELECT sample_id FROM samples WHERE sample_path=?)";
		Map<String,String> QcValueMap;
		PreparedStatement ps = null;
		try {
			connection = pool.getConnection();
			ps = connection.prepareStatement(sql);
			for (String celPath : pathValuesMap.keySet()) {
				QcValueMap = pathValuesMap.get(celPath);
				for (String QcName : QcValueMap.keySet()) {
					ps.setString(1, QcValueMap.get(QcName));
					ps.setString(2, QcName);
					ps.setString(3, DatabaseUtils.realDataPathToDbPath(celPath));
					ps.execute();
				}
			}
			ps.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			SQLUtils.safeClose(ps);
			pool.close(connection);
		}
	}

	public static int countSamplesWithoutQC(ConnectionPool pool, List<Integer> sampleList) {
		Connection connection = null;
		String sql = "SELECT count(*) FROM qc_values WHERE sample_id=?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		int nbSamplesWoQC = 0;
		try {
			connection = pool.getConnection();
			ps = connection.prepareStatement(sql);
			for (Integer sampleID : sampleList) {
				ps.setInt(1, sampleID);
				rs = ps.executeQuery();
				if (rs.first()){
					if (rs.getInt(1)==0){
						nbSamplesWoQC++;
					}
				}else{
					nbSamplesWoQC++;
				}
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			SQLUtils.safeClose(rs);
			SQLUtils.safeClose(ps);
			pool.close(connection);
		}
		return nbSamplesWoQC;
	}
	
	public static String constructQuery(final String sortBy, Map<String, String> filterCriteria){
		String query = "SELECT s.sample_id,s.sample_name,pl.plate_name,p.population_name FROM ((samples s JOIN plates pl ON s.plate_id=pl.plate_id) LEFT JOIN samples_in_populations sp ON s.sample_id=sp.sample_id) LEFT JOIN populations p ON p.population_id=sp.population_id";
		
		//filter criterias
		if (filterCriteria != null && filterCriteria.size() > 0) {
			Map<String,String> critMap = new Hashtable<String, String>();
			if (filterCriteria.containsKey("dishQCLimit")){
				query = "SELECT s.sample_id,s.sample_name,pl.plate_name,p.population_name FROM (((((((samples s JOIN plates pl ON s.plate_id=pl.plate_id) LEFT JOIN samples_in_populations sp ON sp.sample_id=s.sample_id) LEFT JOIN populations p ON p.population_id=sp.population_id) JOIN genotyping_samples gs ON s.sample_id=gs.sample_id) JOIN genotyping_analysis ga ON gs.geno_id=ga.geno_id) JOIN qc_values qv ON qv.sample_id=s.sample_id) JOIN qc_params qp ON qp.qc_param_id=qv.qc_param_id) WHERE ga.geno_name=\""+filterCriteria.get("dishQCLimit")+"\" AND qp.qc_name=\"axiom_dishqc_DQC\" AND qv.qc_value<(SELECT dishQCLimit from genotyping_analysis WHERE geno_name=\""+filterCriteria.get("dishQCLimit")+"\") ";
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
			if (filterCriteria.containsKey("population_name")){
					critMap.put("p.population_name",filterCriteria.get("population_name"));
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
	
	public static List<SampleQCRecord> getSamplesQC(ConnectionPool pool, String query, Integer startRow, Integer endRow){
		List<SampleQCRecord> recordList = new ArrayList<SampleQCRecord>();
		
		//Requested rows
		if (startRow != null && endRow != null && endRow >= startRow) {
			String rowsToString = "LIMIT ";
			Integer size = endRow-startRow;
        	rowsToString = rowsToString.concat(startRow+","+size);
        	query = query.replaceAll(";$", "");
        	query = query.concat(rowsToString+";");
		}
		
		Connection con = null;
		Statement stmt = null;
		ResultSet rsSamples = null;
		ResultSet rsQC = null;
		ResultSet rsUserQC = null;
		PreparedStatement ps = null;
		PreparedStatement psUserQc = null;
		PreparedStatement ps_population = null;
		ResultSet rs_population = null;
		
		try{
			con = pool.getConnection();
			ps_population = con.prepareStatement("SELECT p.population_name FROM populations p JOIN samples_in_populations sp ON sp.population_id=p.population_id WHERE sp.sample_id=?");
			ps = con.prepareStatement("SELECT qp.qc_name,qv.qc_value FROM ((samples s JOIN qc_values qv ON s.sample_id=qv.sample_id) JOIN qc_params qp ON qp.qc_param_id=qv.qc_param_id) WHERE s.sample_id=?");
			psUserQc = con.prepareStatement("SELECT u_qp.user_qc_name,u_qv.user_qc_value FROM ((samples s JOIN user_qc_values u_qv ON s.sample_id=u_qv.sample_id) JOIN user_qc_params u_qp ON u_qp.user_qc_param_id=u_qv.user_qc_param_id) WHERE s.sample_id=?");
			stmt = con.createStatement();
			rsSamples = stmt.executeQuery(query);
			while (rsSamples.next()){
				SampleQCRecord record = new SampleQCRecord();
				record.setSampleID(rsSamples.getInt("sample_id"));
				record.setSampleName(rsSamples.getString("sample_name"));
				record.setPlateName(rsSamples.getString("plate_name"));
				ps.setInt(1, rsSamples.getInt("sample_id"));
				rsQC = ps.executeQuery();
				HashMap<String, String> QCParams = new HashMap<String, String>();
				while (rsQC.next()){
					QCParams.put(rsQC.getString("qc_name"), rsQC.getString("qc_value"));
				}
				record.setQcMap(QCParams);
				ps.clearParameters();
				rsQC.close();
				
				psUserQc.setInt(1, rsSamples.getInt("sample_id"));
				rsUserQC = psUserQc.executeQuery();
				HashMap<String, String> userQcParams = new HashMap<String, String>();
				while (rsUserQC.next()){
					userQcParams.put(rsUserQC.getString("user_qc_name"), rsUserQC.getString("user_qc_value"));
				}
				record.setUserQcMap(userQcParams);
				psUserQc.clearParameters();
				rsUserQC.close();
				
				List<String> popNamesList = new ArrayList<String>();
				ps_population.setInt(1, rsSamples.getInt("sample_id"));
				rs_population = ps_population.executeQuery();
				while (rs_population.next()){
					popNamesList.add(rs_population.getString("population_name"));
				}
				record.setPopulationNames(popNamesList);
				
				recordList.add(record);
			}
			ps.close();
			rsSamples.close();
			stmt.close();
		} catch (SQLException e){
			throw new RuntimeException(e);
		} finally {
			SQLUtils.safeClose(rs_population);
			SQLUtils.safeClose(ps_population);
			SQLUtils.safeClose(stmt);
			SQLUtils.safeClose(rsSamples);
			SQLUtils.safeClose(ps);
			SQLUtils.safeClose(rsQC);
			SQLUtils.safeClose(rsUserQC);
			SQLUtils.safeClose(psUserQc);
			pool.close(con);
		}
		return recordList;
	}
	
	public static Map<Integer, Map<String, String>> getQCParamsForPopulation(ConnectionPool pool, String populationName, String xAxis, String yAxis) {
		String samplesQuery = "SELECT s.sample_id FROM ((samples s JOIN samples_in_populations sp ON s.sample_id=sp.sample_id) JOIN populations p ON p.population_id=sp.population_id) WHERE p.population_name=?";
		return getQCParams(pool, samplesQuery, populationName, xAxis, yAxis);
	}
	
	public static Map<Integer, Map<String, String>> getQCParamsForPlate(ConnectionPool pool, String plateName, String xAxis, String yAxis) {
		String samplesQuery = "SELECT s.sample_id FROM samples s JOIN plates p ON s.plate_id=p.plate_id WHERE plate_name=?";
		return getQCParams(pool, samplesQuery, plateName, xAxis, yAxis);
	}

	private static Map<Integer, Map<String, String>> getQCParams(ConnectionPool pool, String samplesQuery, String name, String xAxis, String yAxis) {
		List<String> qcNames = QcParamsTable.getQcParamsNames(pool);
		List<String> userNames = QcParamsTable.getUserParamsNames(pool);
		//String samplesQuery = "SELECT s.sample_id FROM samples s JOIN plates p ON s.plate_id=p.plate_id WHERE plate_name=?";
		String qcParamsQuery = "SELECT qv.qc_value FROM qc_values qv JOIN qc_params qp ON qv.qc_param_id=qp.qc_param_id WHERE qp.qc_name=? AND qv.sample_id=?";
		String userParamsQuery = "SELECT uqv.user_qc_value FROM user_qc_values uqv JOIN user_qc_params uqp ON uqv.user_qc_param_id=uqp.user_qc_param_id WHERE uqp.user_qc_name=? AND uqv.sample_id=?";
	
		Connection con = null;
		PreparedStatement psSamples = null;
		PreparedStatement psQcParams = null;
		PreparedStatement psUserQcParams = null;
		ResultSet rs = null;
		
		try{
			con = pool.getConnection();
			psSamples = con.prepareStatement(samplesQuery);
			psSamples.setString(1, name);
			
			Map<Integer, Map<String, String>> sampleId2qc = new HashMap<Integer, Map<String,String>>();
			rs = psSamples.executeQuery();
			while (rs.next()){
				sampleId2qc.put(rs.getInt("sample_id"), new HashMap<String, String>());
			}
			if (sampleId2qc.isEmpty()){
				return sampleId2qc;
			}
			rs.close();
			psSamples.close();
			
			psQcParams = con.prepareStatement(qcParamsQuery);
			psUserQcParams = con.prepareStatement(userParamsQuery);
			
			List<String> paramsList = new ArrayList<String>(2);
			paramsList.add(xAxis);
			paramsList.add(yAxis);
			
			for (String param : paramsList) {
				if (qcNames.contains(param)){
					psQcParams.setString(1, param);
					for (Integer sampleID : sampleId2qc.keySet()) {
						psQcParams.setInt(2, sampleID);
						rs = psQcParams.executeQuery();
						while(rs.next()){
							sampleId2qc.get(sampleID).put(param, rs.getString("qc_value"));
						}
					}
				}else if (userNames.contains(param)){
					psUserQcParams.setString(1, param);
					for (Integer sampleID : sampleId2qc.keySet()) {
						psUserQcParams.setInt(2, sampleID);
						rs = psUserQcParams.executeQuery();
						while(rs.next()){
							sampleId2qc.get(sampleID).put(param, rs.getString("user_qc_value"));
						}
					}
				}
			}
			rs.close();
			psQcParams.close();
			psUserQcParams.close();
			return sampleId2qc;
		}catch(SQLException e){
			e.printStackTrace();
			return null;
		}finally{
			SQLUtils.safeClose(rs);
			SQLUtils.safeClose(psSamples);
			SQLUtils.safeClose(psQcParams);
			SQLUtils.safeClose(psUserQcParams);
			pool.close(con);
		}
	}
}
