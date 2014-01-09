package fr.pfgen.axiom.server.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import fr.pfgen.axiom.server.utils.SQLUtils;

public class AnnotationFilesTable {

	public static List<String> getAnnotationFilesNames(ConnectionPool pool) {
		List<String> list = new ArrayList<String>();
		Connection con = null;
		String query = "SELECT annot_name FROM annot_files";
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try{
			con = pool.getConnection();
			ps = con.prepareStatement(query);
			rs = ps.executeQuery();
			while (rs.next()){
				list.add(rs.getString("annot_name"));
			}
			rs.close();
			ps.close();
		} catch (SQLException e){
			throw new RuntimeException(e);
		} finally {
			SQLUtils.safeClose(rs);
			SQLUtils.safeClose(ps);
			pool.close(con);
		}
		return list;
	}
}
