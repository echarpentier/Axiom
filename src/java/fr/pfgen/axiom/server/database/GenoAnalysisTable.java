package fr.pfgen.axiom.server.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import fr.pfgen.axiom.server.utils.DatabaseUtils;
import fr.pfgen.axiom.server.utils.IOUtils;
import fr.pfgen.axiom.server.utils.SQLUtils;
import fr.pfgen.axiom.shared.records.GenotypingAnalysisRecord;

public class GenoAnalysisTable {
	
	public static int insertAnalysis(ConnectionPool pool, String genotypingName, int userID, String folderPath, double dishQCLimit, double callRateLimit, String libraryName, String annotName) {
		Connection con = null;
		PreparedStatement ps = null;
		String insertQuery = "INSERT IGNORE INTO genotyping_analysis SET geno_name=?,folder_path=?,user_id=?,executed=?,dishQCLimit=?,callRateLimit=?,library_id=(SELECT library_id FROM library_files WHERE library_name=?),annot_id=(SELECT annot_id FROM annot_files WHERE annot_name=?)";
		ResultSet rsGenKey = null;
		int genKey = -1;

		try{
			con = pool.getConnection();
			ps = con.prepareStatement(insertQuery,Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, genotypingName);
			ps.setString(2, DatabaseUtils.realAxiomPathToDbPath(folderPath));
			ps.setInt(3, userID);
			Timestamp sqlDate = DatabaseUtils.toDBDateFormat(new Date());
			ps.setTimestamp(4, sqlDate);
			ps.setDouble(5, dishQCLimit);
			ps.setDouble(6, callRateLimit);
			ps.setString(7, libraryName);
			ps.setString(8, annotName);
			ps.executeUpdate();

			rsGenKey = ps.getGeneratedKeys();

			if (rsGenKey.next()){
				genKey = rsGenKey.getInt(1);
			}else{
				throw new RuntimeException("Cannot return generated key while inserting new row in 'genotyping_analysis'");
			}
			rsGenKey.close();
			ps.close();
		} catch (SQLException e){
			throw new RuntimeException(e);
		} catch (ParseException e){
			throw new RuntimeException(e);
		} finally {
			if (rsGenKey!=null){
				try{rsGenKey.close();}catch (SQLException e){}
			}
			if (ps!=null){
				try{ps.close();}catch (SQLException e){}
			}
			pool.close(con);
		}
		return genKey;
	}

	public static List<String> getGenotypingNames(ConnectionPool pool) {
		List<String> list = new ArrayList<String>();
		Connection con = null;
		String query = "SELECT geno_name FROM genotyping_analysis ORDER BY executed DESC";
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try{
			con = pool.getConnection();
			ps = con.prepareStatement(query);
			rs = ps.executeQuery();
			while (rs.next()){
				list.add(rs.getString("geno_name"));
			}
			rs.close();
			ps.close();
		} catch (SQLException e){
			throw new RuntimeException(e);
		} finally {
			if (rs!=null){
				try{rs.close();}catch(SQLException e){}
			}
			if (ps!=null){
				try{ps.close();}catch(SQLException e){}
			}
			pool.close(con);
		}
		return list;
	}
	
	public static String constructQuery(String sortBy, Map<String, String> filterCriteria){
		String query = "SELECT * FROM genotyping_analysis ";
		
		//filter criterias
		if (filterCriteria != null && filterCriteria.size() > 0) {
			Map<String,String> critMap = new Hashtable<String, String>();
			if (filterCriteria.containsKey("geno_id")) {
				critMap.put("geno_id", filterCriteria.get("geno_id"));
			}
			if (filterCriteria.containsKey("geno_name")){
				critMap.put("geno_name", filterCriteria.get("geno_name"));
			}
			if (filterCriteria.containsKey("study_name")){
				query = "SELECT DISTINCT ga.* FROM ((((studies s JOIN study_samples ss ON ss.study_id=s.study_id) JOIN genotyping_runs gr ON gr.geno_run_id=ss.geno_run_id) JOIN genotyping_samples gs ON gs.geno_sample_id=gr.geno_sample_id) JOIN genotyping_analysis ga ON ga.geno_id=gs.geno_id)";
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

	public static List<GenotypingAnalysisRecord> getGenotypingAnalysis(ConnectionPool pool, String query, Integer startRow, Integer endRow) {
		
		List<GenotypingAnalysisRecord> list = new ArrayList<GenotypingAnalysisRecord> ();
		
		//Requested rows
		if (startRow != null && endRow != null && endRow >= startRow) {
			String rowsToString = "LIMIT ";
			Integer size = endRow-startRow;
        	rowsToString = rowsToString.concat(startRow+","+size);
        	query = query.replaceAll(";$", "");
        	query = query.concat(rowsToString+";");
		}
		
		PreparedStatement psGenoAnalysis = null;
		PreparedStatement psUser = null;
		PreparedStatement psAnnot = null;
		PreparedStatement psLibrary = null;
		ResultSet rsGeno = null;
		ResultSet rsUser = null;
		ResultSet rsAnnot = null;
		ResultSet rsLibrary = null;
		Connection con = null;
		try{
			con = pool.getConnection();
			psGenoAnalysis = con.prepareStatement(query);
			psUser = con.prepareStatement("SELECT firstname,lastname FROM users WHERE user_id=?");
			psAnnot = con.prepareStatement("SELECT annot_name FROM annot_files WHERE annot_id=?");
			psLibrary = con.prepareStatement("SELECT library_name FROM library_files WHERE library_id=?");
			rsGeno = psGenoAnalysis.executeQuery();
			while (rsGeno.next()){
				GenotypingAnalysisRecord record = new GenotypingAnalysisRecord();
				record.setId(rsGeno.getInt("geno_id"));
				record.setGenoName(rsGeno.getString("geno_name"));
				record.setFolderPath(DatabaseUtils.dbAxiomPathToRealPath(rsGeno.getString("folder_path")));
				record.setExecuted(rsGeno.getDate("executed"));
				record.setDishQCLimit(rsGeno.getDouble("dishQCLimit"));
				record.setCallRateLimit(rsGeno.getDouble("callRateLimit"));
				int userID = rsGeno.getInt("user_id");
				psUser.setInt(1, userID);
				rsUser = psUser.executeQuery();
				while (rsUser.next()){
					record.setUser(rsUser.getString("firstname")+" "+rsUser.getString("lastname"));
				}
				rsUser.close();
				psAnnot.setInt(1, rsGeno.getInt("annot_id"));
				rsAnnot = psAnnot.executeQuery();
				while (rsAnnot.next()){
					record.setAnnotationFile(rsAnnot.getString("annot_name"));
				}
				rsAnnot.close();
				psLibrary.setInt(1, rsGeno.getInt("library_id"));
				rsLibrary = psLibrary.executeQuery();
				while (rsLibrary.next()){
					record.setLibraryFiles(rsLibrary.getString("library_name"));
				}
				rsLibrary.close();
				list.add(record);
				
			}
			rsGeno.close();
			psUser.close();
			psAnnot.close();
			psLibrary.close();
			psGenoAnalysis.close();
		} catch (SQLException e){
			throw new RuntimeException(e);
		} finally {
			SQLUtils.safeClose(rsUser);
			SQLUtils.safeClose(rsGeno);
			SQLUtils.safeClose(psUser);
			SQLUtils.safeClose(psGenoAnalysis);
			SQLUtils.safeClose(psAnnot);
			SQLUtils.safeClose(psLibrary);
			SQLUtils.safeClose(rsLibrary);
			SQLUtils.safeClose(rsAnnot);
			pool.close(con);
		}
		return list;
	}

	public static String getGenoPathFromName(ConnectionPool pool, String genoName) {
		String query = "SELECT folder_path FROM genotyping_analysis WHERE geno_name=\""+genoName+"\"";
		String folderPath = null;
		
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		try{
			con = pool.getConnection();
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);
			while(rs.next()){
				folderPath = DatabaseUtils.dbAxiomPathToRealPath(rs.getString("folder_path"));
			}
			rs.close();
			stmt.close();
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			SQLUtils.safeClose(rs);
			SQLUtils.safeClose(stmt);
			pool.close(con);
		}
		
		return folderPath;
	}

	public static boolean checkIfSecondRunExist(ConnectionPool pool, String genoName) {
		String query = "SELECT EXISTS(SELECT 1 FROM ((genotyping_analysis ga JOIN genotyping_samples gs ON gs.geno_id=ga.geno_id) JOIN genotyping_runs gr ON gr.geno_sample_id=gs.geno_sample_id) WHERE geno_name=? and geno_run=?)";
		
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		Boolean secondRunExists = null;
		
		try{
			con = pool.getConnection();
			ps = con.prepareStatement(query);
			ps.setString(1, genoName);
			ps.setString(2, "second");
			rs = ps.executeQuery();
			if (rs.next()){
				if (rs.getBoolean(1)){
					secondRunExists = true;
				}else{
					secondRunExists = false;
				}
			}
			rs.close();
			ps.close();
		}catch(SQLException e){
			throw new RuntimeException(e);
		}finally{
			if (rs!=null)try{rs.close();}catch(SQLException e){}
			if (ps!=null)try{ps.close();}catch(SQLException e){}
			pool.close(con);
		}
		
		return secondRunExists;
	}

	public static String getCallsPath(ConnectionPool pool, int genoRunID) {
		String query = "SELECT folder_path,geno_run FROM ((genotyping_analysis ga JOIN genotyping_samples gs ON ga.geno_id=gs.geno_id) JOIN genotyping_runs gr ON gr.geno_sample_id=gs.geno_sample_id) WHERE geno_run_id=?";
		
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try{
			con = pool.getConnection();
			ps = con.prepareStatement(query);
			ps.setInt(1, genoRunID);
			rs = ps.executeQuery();
			if (rs.next()){
				String analysisPath = DatabaseUtils.dbAxiomPathToRealPath(rs.getString("folder_path"));
				String run = rs.getString("geno_run");
				File calls;
				if (run.equals("first")){
					calls = new File(analysisPath+"/First_run/AxiomGT1.calls.txt");
					if (!calls.exists()){
						calls = new File(analysisPath+"/First_run/AxiomGT1.calls.txt.gz");
						if (!calls.exists()){
							throw new RuntimeException("Cannot find call path of geno_run_id "+genoRunID);
						}
					}
				}else{
					calls = new File(analysisPath+"/Second_run/AxiomGT1.calls.txt");
					if (!calls.exists()){
						calls = new File(analysisPath+"/Second_run/AxiomGT1.calls.txt.gz");
						if (!calls.exists()){
							throw new RuntimeException("Cannot find call path of geno_run_id "+genoRunID);
						}
					}
				}
				return calls.getAbsolutePath();
			}else{
				throw new RuntimeException("Cannot find call path of geno_run_id "+genoRunID);
			}
		}catch(SQLException e){
			e.printStackTrace();
			return null;
		}finally{
			SQLUtils.safeClose(rs);
			SQLUtils.safeClose(ps);
			pool.close(con);
		}
	}

	public static String getSampleNewName(ConnectionPool pool, int sampleID, int genoID) {
		String query = "SELECT folder_path FROM genotyping_analysis WHERE geno_id=?";
		
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		BufferedReader br = null;
		try{
			con = pool.getConnection();
			ps = con.prepareStatement(query);
			ps.setInt(1, genoID);
			rs = ps.executeQuery();
			if (rs.next()){
				String analysisPath = DatabaseUtils.dbAxiomPathToRealPath(rs.getString("folder_path"));
				File samplePlates = new File(analysisPath+"/QCReport/samples_plates.txt");
				if (!samplePlates.exists()){
					throw new RuntimeException("Cannot find sample_plates.txt for geno analysis "+genoID);
				}
				br = IOUtils.openFile(samplePlates);
				String line;
				while((line=br.readLine())!=null){
					String[] linesplit = line.split("\\t");
					if (Integer.parseInt(linesplit[0])==sampleID){
						return linesplit[3];
					}
				}
				throw new RuntimeException("Cannot find sampleNewName in "+samplePlates.getAbsolutePath()+" for sample id "+sampleID);
			}else{
				throw new RuntimeException("Cannot find folder path for geno analysis "+genoID);
			}
		}catch(SQLException e){
			e.printStackTrace();
			return null;
		}catch(IOException e){
			e.printStackTrace();
			return null;
		}finally{
			IOUtils.safeClose(br);
			SQLUtils.safeClose(rs);
			SQLUtils.safeClose(ps);
			pool.close(con);
		}
	}

	public static List<String> getStudiesLinkedToGenoAnalysis(ConnectionPool pool, int genoID) {
		List<String> studyList = new ArrayList<String>();
		String query = "SELECT DISTINCT s.study_name FROM ((((genotyping_analysis ga JOIN genotyping_samples gs ON ga.geno_id=gs.geno_id) JOIN genotyping_runs gr ON gr.geno_sample_id=gs.geno_sample_id) JOIN study_samples ss ON gr.geno_run_id=ss.geno_run_id) JOIN studies s ON s.study_id=ss.study_id) WHERE ga.geno_id=?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection con = null;
		
		try{
			con = pool.getConnection();
			ps = con.prepareStatement(query);
			ps.setInt(1, genoID);
			rs = ps.executeQuery();
			while(rs.next()){
				studyList.add(rs.getString("study_name"));
			}
			rs.close();
			ps.close();
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			SQLUtils.safeClose(rs);
			SQLUtils.safeClose(ps);
			pool.close(con);
		}
		
		return studyList;
	}

	public static String removeGenoAnalysis(ConnectionPool pool, GenotypingAnalysisRecord data) {
		String query = "DELETE FROM genotyping_analysis WHERE geno_id=?";
		PreparedStatement ps = null;
		Connection con = null;
		try{
			con = pool.getConnection();
			ps = con.prepareStatement(query);
			ps.setInt(1, data.getId());
			ps.executeUpdate();
			return data.getFolderPath();
		}catch(SQLException e){
			e.printStackTrace();
			return null;
		}finally{
			SQLUtils.safeClose(ps);
			pool.close(con);
		}
	}

	public static String getLibraryNameForGeno(ConnectionPool pool, String genoName) {
		String query = "SELECT library_name FROM genotyping_analysis ga JOIN library_files lf ON ga.library_id=lf.library_id WHERE geno_name=?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection con = null;
		String libName = null;
		
		try{
			con = pool.getConnection();
			ps = con.prepareStatement(query);
			ps.setString(1, genoName);
			rs = ps.executeQuery();
			while(rs.next()){
				libName = rs.getString("library_name");
			}
			rs.close();
			ps.close();
		}catch (SQLException e) {
			e.printStackTrace();
			return null;
		}finally{
			SQLUtils.safeClose(ps);
			SQLUtils.safeClose(rs);
			pool.close(con);
		}
		return libName;
	}
}
