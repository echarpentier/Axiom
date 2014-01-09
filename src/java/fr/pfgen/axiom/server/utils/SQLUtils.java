package fr.pfgen.axiom.server.utils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLUtils {

	public static void safeClose(PreparedStatement ps){
		try{
			if (ps!=null) ps.close();
		}catch (SQLException e){
			
		}
	}
	
	public static void safeClose(Statement s){
		try{
			if (s!=null) s.close();
		}catch (SQLException e){
			
		}
	}
	
	public static void safeClose(ResultSet rs){
		try{
			if (rs!=null) rs.close();
		}catch (SQLException e){
			
		}
	}
}
