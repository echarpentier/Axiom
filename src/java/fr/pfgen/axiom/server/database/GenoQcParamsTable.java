package fr.pfgen.axiom.server.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class GenoQcParamsTable {

	public static List<String> getGenoQcParamsNames(ConnectionPool pool) {
		List<String> list = new ArrayList<String>();
		String query = "SELECT geno_qc_name FROM genotyping_qc_params";
		Connection con = null;
		ResultSet rs = null;
		Statement stmt = null;
		try{
			con = pool.getConnection();
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);
			while (rs.next()){
				list.add(rs.getString("geno_qc_name"));
			}
			rs.close();
			stmt.close();
		}catch (SQLException e){
			throw new RuntimeException(e);
		}finally{
			if (rs!=null){
				try{rs.close();}catch(SQLException e){throw new RuntimeException(e);}
			}
			if (stmt!=null){
				try{stmt.close();}catch(SQLException e){throw new RuntimeException(e);}
			}
			pool.close(con);
		}
		return list;
	}
}
