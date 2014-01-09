package fr.pfgen.axiom.server.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import fr.pfgen.axiom.server.utils.DatabaseUtils;
import fr.pfgen.axiom.server.utils.SQLUtils;
import fr.pfgen.axiom.shared.records.StudySampleRecord;

public class StudySamplesTable {

	public static String addSamplesToStudy(ConnectionPool pool, String studyName, List<Integer> genoRunIdList) {
		String query = "INSERT IGNORE INTO study_samples (study_id,geno_run_id) VALUES ((SELECT study_id FROM studies WHERE study_name=?),?)";
		String result = new String();
		
		Connection con = null;
		PreparedStatement ps = null;
		
		try{
			con = pool.getConnection();
			ps = con.prepareStatement(query);
			ps.setString(1, studyName);
			for (Integer genoRunId : genoRunIdList) {
				ps.setInt(2, genoRunId);
				ps.executeUpdate();
			}
			ps.close();
			result = "Samples added successfully !";
		}catch(SQLException e){
			result = "Error: cannot add sample to study\n"+e;
		}finally{
			SQLUtils.safeClose(ps);
			pool.close(con);
		}
		
		return result;
	}

	public static List<StudySampleRecord> getSamplesInStudy(ConnectionPool pool, String studyName) {
		List<StudySampleRecord> studySamplesRecords = new ArrayList<StudySampleRecord>();
		String query = "SELECT s.sample_id,s.sample_name,gr.geno_run_id,gr.geno_run,ss.study_sample_id FROM ((((samples s JOIN genotyping_samples gs ON s.sample_id=gs.sample_id) JOIN genotyping_runs gr ON gs.geno_sample_id=gr.geno_sample_id) JOIN study_samples ss ON ss.geno_run_id=gr.geno_run_id) JOIN studies st on st.study_id=ss.study_id) WHERE study_name=?";
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try{
			con = pool.getConnection();
			ps = con.prepareStatement(query);
			ps.setString(1, studyName);
			rs = ps.executeQuery();
			while (rs.next()){
				StudySampleRecord newRec = new StudySampleRecord();
				newRec.setSampleID(rs.getInt("sample_id"));
				newRec.setSampleName(rs.getString("sample_name"));
				newRec.setGenoRunID(rs.getInt("geno_run_id"));
				newRec.setGenoRun(rs.getString("geno_run"));
				newRec.setStudySampleID(rs.getInt("study_sample_id"));
				studySamplesRecords.add(newRec);
			}
			rs.close();
			ps.close();
		}catch(SQLException e){
			throw new RuntimeException(e);
		}finally{
			DatabaseUtils.safeClose(rs);
			DatabaseUtils.safeClose(ps);
			pool.close(con);
		}
		
		return studySamplesRecords;
	}

}
