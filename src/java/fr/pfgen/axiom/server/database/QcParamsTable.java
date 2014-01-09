package fr.pfgen.axiom.server.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.pfgen.axiom.server.utils.SQLUtils;

public class QcParamsTable {

	public static List<String> getQcParamsNames(ConnectionPool pool) {
		List<String> list = new ArrayList<String>();
		String query = "SELECT qc_name FROM qc_params";
		Connection con = null;
		ResultSet rs = null;
		Statement stmt = null;
		try{
			con = pool.getConnection();
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);
			while (rs.next()){
				list.add(rs.getString("qc_name"));
			}
			rs.close();
			stmt.close();
		}catch (SQLException e){
			e.printStackTrace();
			return null;
		}finally{
			SQLUtils.safeClose(rs);
			SQLUtils.safeClose(stmt);
			pool.close(con);
		}
		return list;
	}

	public static List<String> getUserParamsNames(ConnectionPool pool) {
		List<String> list = new ArrayList<String>();
		String query = "SELECT user_qc_name FROM user_qc_params";
		Connection con = null;
		ResultSet rs = null;
		Statement stmt = null;
		try{
			con = pool.getConnection();
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);
			while (rs.next()){
				list.add(rs.getString("user_qc_name"));
			}
			rs.close();
			stmt.close();
		}catch (SQLException e){
			e.printStackTrace();
			return null;
		}finally{
			SQLUtils.safeClose(rs);
			SQLUtils.safeClose(stmt);
			pool.close(con);
		}
		return list;
	}

	public static boolean insertNewUserParam(ConnectionPool pool, Set<String> paramList) {
		String query = "INSERT INTO user_qc_params(user_qc_name) VALUES (?)";
		
		Connection con = null;
		PreparedStatement ps = null;
		
		try{
			con = pool.getConnection();
			ps = con.prepareStatement(query);
			for (String paramName : paramList) {
				ps.setString(1, paramName);
				ps.executeUpdate();
			}
			ps.close();
			return true;
		}catch(SQLException e){
			e.printStackTrace();
			return false;
		}finally{
			SQLUtils.safeClose(ps);
			pool.close(con);
		}
	}

	public static boolean insertUserQCValues(ConnectionPool pool, Map<String, Map<Integer, String>> inserts) {
		String query = "REPLACE INTO user_qc_values(user_qc_value,user_qc_param_id,sample_id) VALUES (?,(SELECT user_qc_param_id FROM user_qc_params WHERE user_qc_name=?),?)";
		Connection con = null;
		PreparedStatement ps = null;
		
		try{
			con = pool.getConnection();
			ps = con.prepareStatement(query);
			for (String paramName : inserts.keySet()) {
				ps.setString(2, paramName);
				for (Integer sampleID : inserts.get(paramName).keySet()) {
					ps.setString(1, inserts.get(paramName).get(sampleID));
					ps.setInt(3, sampleID);
					ps.executeUpdate();
				}
			}
			ps.close();
			return true;
		}catch(SQLException e){
			e.printStackTrace();
			return false;
		}finally{
			SQLUtils.safeClose(ps);
			pool.close(con);
		}
	}
}
