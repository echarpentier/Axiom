package fr.pfgen.axiom.server.database;

import java.io.File;
import java.io.FileNotFoundException;
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
import fr.pfgen.axiom.server.utils.SQLUtils;
import fr.pfgen.axiom.shared.records.StudyRecord;

public class StudiesTable {

	public static String constructQuery(final String sortBy, Map<String, String> filterCriteria){
		
		String query = "SELECT * FROM studies";
		
		if (filterCriteria!=null && !filterCriteria.isEmpty()){
			Map<String,String> critMap = new Hashtable<String, String>();
			if (filterCriteria.containsKey("study_name")){
				critMap.put("study_name", filterCriteria.get("study_name"));
			}
			if (filterCriteria.containsKey("user_id")){
				critMap.put("user_id", filterCriteria.get("user_id"));
			}
			if (!critMap.isEmpty()){
				query = query.concat(DatabaseUtils.constructWhereClause(critMap));
			}
		}
		
		query = query.concat(DatabaseUtils.constructOrderByClause(sortBy));
		return query;
	}

	public static List<StudyRecord> getStudies(ConnectionPool pool, String query, Integer startRow, Integer endRow) {
		List<StudyRecord> studyList = new ArrayList<StudyRecord>();
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection con = null;
		try{
			con = pool.getConnection();
			ps = con.prepareStatement(query);
			rs = ps.executeQuery();
			while (rs.next()){
				StudyRecord record = new StudyRecord();
				record.setStudyID(rs.getInt("study_id"));
				record.setStudyName(rs.getString("study_name"));
				record.setStudyPath(DatabaseUtils.dbAxiomPathToRealPath(rs.getString("study_folder_path")));
				record.setStudyType(rs.getString("study_type"));
				record.setUserID(rs.getInt("user_id"));
				studyList.add(record);
			}
			rs.close();
			ps.close();
		}catch(SQLException e){
			throw new RuntimeException(e);
		}finally{
			if (rs!=null) try{rs.close();}catch(SQLException e){}
			if (ps!=null) try{ps.close();}catch(SQLException e){}
			pool.close(con);
		}
		
		return studyList;
	}

	public static StudyRecord addNewStudy(ConnectionPool pool, StudyRecord data) {
		
		Connection con = null;
		String insertQuery = "INSERT INTO studies (study_name,study_folder_path,user_id,study_type,created,description) VALUES (?,?,?,?,?,?)";
		PreparedStatement ps_insert = null;
		ResultSet rsk = null;
		
		try{
			con = pool.getConnection();
			ps_insert = con.prepareStatement(insertQuery,Statement.RETURN_GENERATED_KEYS);
			ps_insert.setString(1, data.getStudyName());
			ps_insert.setString(2, DatabaseUtils.realAxiomPathToDbPath(data.getStudyPath()));
			ps_insert.setInt(3, data.getUserID());
			ps_insert.setString(4, data.getStudyType());
			Timestamp sqlDate = DatabaseUtils.toDBDateFormat(new Date());
			ps_insert.setTimestamp(5, sqlDate);
                        ps_insert.setString(6, data.getDescription());
			ps_insert.executeUpdate();
			rsk = ps_insert.getGeneratedKeys();
			if (!rsk.next()){
				data = null;
			}else{
				data.setStudyID(rsk.getInt(1));
			}
			rsk.close();
			ps_insert.close();
		}catch(SQLException e){
			throw new RuntimeException(e);
		} catch (ParseException e){
			throw new RuntimeException(e);
		}finally{
			if (rsk!=null) try{rsk.close();}catch (SQLException e){}
			if (ps_insert!=null) try{ps_insert.close();}catch (SQLException e){}
			pool.close(con);
		}
		return data;
	}

	public static String addGenoAnalysisToStudy(ConnectionPool pool, String studyName, List<String> genoNameList) {
		String returnStatement;
		String query = "INSERT INTO study_samples (study_id,geno_id) VALUES ((SELECT study_id FROM studies WHERE study_name=?),(SELECT geno_id FROM genotyping_analysis WHERE geno_name=?))";
		Connection con = null;
		PreparedStatement ps = null;
		
		try{
			con = pool.getConnection();
			ps = con.prepareStatement(query);
			ps.setString(1, studyName);
			for (String genoName : genoNameList) {
				ps.setString(2, genoName);
				ps.executeUpdate();
			}
			ps.close();
		}catch(SQLException e){
			e.printStackTrace();
			returnStatement = "Error: cannot add genotyping analysis to study: "+e;
		}finally{
			SQLUtils.safeClose(ps);
			pool.close(con);
		}
		returnStatement = "Genotyping analysis added to study successfully";
		return returnStatement;
	}

	public static List<String> getStudyNames(ConnectionPool pool, String type) {
		List<String> studyList = new ArrayList<String>();
		Statement stmt = null;
		ResultSet rs = null;
		Connection con = null;
		String query;
		if (type==null || type.equals("")){
			query = "SELECT study_name FROM studies";
		}else{
			query = "SELECT study_name FROM studies WHERE study_type=\""+type+"\"";
		}
		
		try{
			con = pool.getConnection();
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				studyList.add(rs.getString("study_name"));
			}
			rs.close();
			stmt.close();
		}catch(SQLException e){
			throw new RuntimeException(e);
		}finally{
			SQLUtils.safeClose(rs);
			SQLUtils.safeClose(stmt);
			pool.close(con);
		}
		return studyList;
	}

	public static Boolean checkGenoAnalysisInStudy(ConnectionPool pool, String studyName) {
		Boolean isStudyAssociatedWithGenoAnalysis = null;
		
		String query = "SELECT DISTINCT ga.* FROM ((((studies s JOIN study_samples ss ON ss.study_id=s.study_id) JOIN genotyping_runs gr ON gr.geno_run_id=ss.geno_run_id) JOIN genotyping_samples gs ON gs.geno_sample_id=gr.geno_sample_id) JOIN genotyping_analysis ga ON ga.geno_id=gs.geno_id) WHERE study_name=?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection con = null;
		
		try{
			con = pool.getConnection();
			ps = con.prepareStatement(query);
			ps.setString(1, studyName);
			rs = ps.executeQuery();
			if (rs.next()){
				isStudyAssociatedWithGenoAnalysis = true;
			}else{
				isStudyAssociatedWithGenoAnalysis = false;
			}
			rs.close();
			ps.close();
		}catch(SQLException e){
			throw new RuntimeException(e);
		}finally{
			SQLUtils.safeClose(rs);
			SQLUtils.safeClose(ps);
			pool.close(con);
		}
		
		return isStudyAssociatedWithGenoAnalysis;
	}

	public static Map<String, String> getGenderForStudySamples(ConnectionPool pool, String studyName) {
		String query = "SELECT sample_name,geno_qc_value FROM ((((((studies s JOIN study_samples ss ON s.study_id=ss.study_id) JOIN genotyping_runs gr ON gr.geno_run_id=ss.geno_run_id) JOIN genotyping_samples gs ON gs.geno_sample_id=gr.geno_sample_id) JOIN samples sp ON sp.sample_id=gs.sample_id) JOIN genotyping_qc_values gv ON gv.geno_run_id=gr.geno_run_id) JOIN genotyping_qc_params gp ON gp.geno_qc_param_id=gv.geno_qc_param_id) WHERE study_name=? AND geno_qc_name=\"computed_gender\"";
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		Map<String, String> genderOfStudySamples = new Hashtable<String, String>();
		try{
			con = pool.getConnection();
			ps = con.prepareStatement(query);
			ps.setString(1, studyName);
			rs = ps.executeQuery();
			
			while(rs.next()){
				genderOfStudySamples.put(rs.getString("sample_name"), rs.getString("geno_qc_value"));
			}
			rs.close();
			ps.close();
			return genderOfStudySamples;
		}catch(SQLException e){
			e.printStackTrace();
			return null;
		}finally{
			SQLUtils.safeClose(rs);
			SQLUtils.safeClose(ps);
			pool.close(con);
		}
	}

	public static File getStudyFolder(ConnectionPool pool, String studyName) {
		String query = "SELECT study_folder_path FROM studies WHERE study_name=?";
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		File studyFolder = null;
		
		try{
			con = pool.getConnection();
			ps = con.prepareStatement(query);
			ps.setString(1, studyName);
			rs = ps.executeQuery();
			while(rs.next()){
				studyFolder = new File(DatabaseUtils.dbAxiomPathToRealPath(rs.getString("study_folder_path")));
				if (!studyFolder.exists()){
					throw new FileNotFoundException("Cannot find "+studyFolder.getAbsolutePath());
				}
			}
			rs.close();
			ps.close();
			return studyFolder;
		}catch (SQLException e){
			e.printStackTrace();
			return null;
		}catch (FileNotFoundException e){
			e.printStackTrace();
			return null;
		}finally{
			SQLUtils.safeClose(rs);
			SQLUtils.safeClose(ps);
			pool.close(con);
		}
	}

	public static String removeStudy(ConnectionPool pool, StudyRecord data) {
		String query = "DELETE FROM studies WHERE study_id=?";
		PreparedStatement ps = null;
		Connection con = null;
		try{
			con = pool.getConnection();
			ps = con.prepareStatement(query);
			ps.setInt(1, data.getStudyID());
			ps.executeUpdate();
			return data.getStudyPath();
		}catch(SQLException e){
			e.printStackTrace();
			return null;
		}finally{
			SQLUtils.safeClose(ps);
			pool.close(con);
		}
	}

	public static List<String> getLibraryUsedInStudy(ConnectionPool pool, String studyName) {
		String query = "SELECT DISTINCT lf.library_name FROM (((((studies s JOIN study_samples ss ON s.study_id=ss.study_id) JOIN genotyping_runs gr ON gr.geno_run_id=ss.geno_run_id) JOIN genotyping_samples gs ON gs.geno_sample_id=gr.geno_sample_id) JOIN genotyping_analysis ga ON ga.geno_id=gs.geno_id) JOIN library_files lf ON lf.library_id=ga.library_id) WHERE study_name=?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection con = null;
		List<String> libraryList = new ArrayList<String>();
		try{
			con = pool.getConnection();
			ps = con.prepareStatement(query);
			ps.setString(1, studyName);
			rs = ps.executeQuery();
			while (rs.next()) {
				libraryList.add(rs.getString("library_name"));
			}
			rs.close();
			ps.close();
			return libraryList;
		}catch(SQLException e){
			e.printStackTrace();
			return null;
		}finally{
			SQLUtils.safeClose(rs);
			SQLUtils.safeClose(ps);
			pool.close(con);
		}
	}
        
        public static StudyRecord getStudyInfos(ConnectionPool pool, String studyName){
            String query = "SELECT * FROM studies WHERE study_name=?";
		PreparedStatement ps = null;
                PreparedStatement psUser = null;
		ResultSet rs = null;
                ResultSet rsUser = null;
		Connection con = null;
		StudyRecord studyRecord = new StudyRecord();
		try{
			con = pool.getConnection();
			ps = con.prepareStatement(query);
                        psUser = con.prepareStatement("SELECT firstname,lastname FROM users WHERE user_id=?");
			ps.setString(1, studyName);
			rs = ps.executeQuery();
			while (rs.next()) {
				studyRecord.setStudyName(studyName);
                                studyRecord.setStudyType(rs.getString("study_type"));
                                studyRecord.setCreated(rs.getDate("created"));
                                if (rs.getString("description")==null || rs.getString("description").isEmpty()){
                                    studyRecord.setDescription("<i>no description entered</i>");
                                }else{
                                    
                                    studyRecord.setDescription(rs.getString("description"));
                                }
                                psUser.setInt(1, rs.getInt("user_id"));
                                rsUser = psUser.executeQuery();
                                while (rsUser.next()){
					studyRecord.setCreatedBy(rsUser.getString("firstname")+"&nbsp;"+rsUser.getString("lastname"));
				}
                                rsUser.close();
                                
			}
			rs.close();
			ps.close();
                        psUser.close();
			return studyRecord;
		}catch(SQLException e){
			e.printStackTrace();
			return null;
		}finally{
			SQLUtils.safeClose(rs);
                        SQLUtils.safeClose(rsUser);
			SQLUtils.safeClose(ps);
                        SQLUtils.safeClose(psUser);
			pool.close(con);
		}
        }
}
