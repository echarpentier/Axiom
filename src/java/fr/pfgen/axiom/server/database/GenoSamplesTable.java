package fr.pfgen.axiom.server.database;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import fr.pfgen.axiom.server.utils.DatabaseUtils;
import fr.pfgen.axiom.server.utils.SQLUtils;
import fr.pfgen.axiom.shared.records.StudySampleRecord;

public class GenoSamplesTable {

	public static void insertGenoSamples(ConnectionPool pool, int genoAnalysisKey, List<Integer> sampleIDList){
		Connection con = null;
		PreparedStatement ps = null;
		String query = "INSERT IGNORE INTO genotyping_samples SET geno_id=?,sample_id=?";
		try{
			con = pool.getConnection();
			ps = con.prepareStatement(query);
			ps.setInt(1, genoAnalysisKey);
			for (Integer sampleID : sampleIDList) {
				ps.setInt(2, sampleID);
				ps.executeUpdate();
			}
			ps.close();
		}catch(SQLException e){
			throw new RuntimeException(e);
		}finally{
			if (ps!=null){
				try{ps.close();}catch(SQLException e){}
			}
			pool.close(con);
		}
	}

	public static List<File> getChpPathFromGenoRunId(ConnectionPool pool, List<StudySampleRecord> samplesList) {
		String query = "SELECT chp_path FROM genotyping_runs WHERE geno_run_id=?";
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		List<File> chpPaths = new ArrayList<File>();
		
		try{
			con = pool.getConnection();
			ps = con.prepareStatement(query);
			for (StudySampleRecord sample : samplesList) {
				ps.setInt(1, sample.getGenoRunID());
				rs = ps.executeQuery();
				while(rs.next()){
					chpPaths.add(new File(DatabaseUtils.dbAxiomPathToRealPath(rs.getString("chp_path"))));
				}
			}
			rs.close();
			ps.close();
			return chpPaths;
		}catch(SQLException e){
			e.printStackTrace();
			return null;
		}finally{
			SQLUtils.safeClose(rs);
			SQLUtils.safeClose(ps);
			pool.close(con);
		}
	}
}
