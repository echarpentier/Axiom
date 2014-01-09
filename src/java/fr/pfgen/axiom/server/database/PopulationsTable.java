package fr.pfgen.axiom.server.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.pfgen.axiom.server.utils.SQLUtils;
import fr.pfgen.axiom.shared.records.PopulationRecord;

public class PopulationsTable {
	
	public static List<String> getPopulationNames(ConnectionPool pool){
		List<String> populationList = new ArrayList<String>();
		Statement stmt = null;
		ResultSet rs = null;
		Connection con = null;
		
		try{
			con = pool.getConnection();
			stmt = con.createStatement();
			String query = "SELECT population_name FROM populations ORDER BY created DESC";
			rs = stmt.executeQuery(query);
			while (rs.next()) {
				populationList.add(rs.getString("population_name"));
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
		return populationList;
	}
	
	public static List<PopulationRecord> getPopulations(ConnectionPool pool, Integer startRow, Integer endRow,	final String sortBy, Map<String, String> filterCriteria){
		List<PopulationRecord> list = new ArrayList<PopulationRecord> ();
		String query = "SELECT * FROM populations ";
		String critToString = "WHERE ";
		String rowsToString = "LIMIT ";
		String orderByString = "ORDER BY ";
		
		//filter criterias
		if (filterCriteria != null && filterCriteria.size() > 0) {
			List<String> critList = new ArrayList<String>();
			if (filterCriteria.containsKey("population_id")) {
				critList.add("population_id="+filterCriteria.get("population_id"));
			}else if (filterCriteria.containsKey("population_name")){
				critList.add("population_name=\""+filterCriteria.get("population_name")+"\"");
			}else if (filterCriteria.containsKey("user")){
				String[] user = filterCriteria.get("user").split("\\s");
				critList.add("user_id=(SELECT users.user_id FROM users JOIN populations ON users.user_id=populations.user_id WHERE firstname=\""+user[0]+"\" AND lastname=\""+user[1]+"\")");
			}
			
			for (Iterator<String> i = critList.iterator(); i.hasNext();) {
				String condition = i.next();
				critToString = critToString.concat(condition+" AND ");
			}
			if (critToString.endsWith(" AND ")){
				critToString = critToString.replaceAll("\\sAND\\s$", " ");
			}
			query = query.concat(critToString);
		}
		
		//Sort By
		if (!sortBy.isEmpty()){
			String[] orderByCrit = sortBy.split(",");
			for (String crit : orderByCrit) {
				if (crit.startsWith("-")){
					orderByString = orderByString.concat(crit.replaceFirst("-", "")+" DESC,");
				}else{
					orderByString = orderByString.concat(crit+" ASC,");
				}
			}
			if (orderByString.endsWith(",")){
				orderByString = orderByString.replaceAll(",$", " ");
			}
			query = query.concat(orderByString);
		}
		
		//Requested rows
		if (startRow != null && endRow != null && endRow >= startRow) {
			Integer size = endRow-startRow+1;
        	rowsToString = rowsToString.concat(startRow+","+size);
        	query = query.concat(rowsToString);
        }
		
		query = query.concat(";");
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		String query_u = "SELECT firstname,lastname FROM users WHERE user_id=?";
		PreparedStatement ps_u = null;
		ResultSet rs_u = null;
		try{
			con = pool.getConnection();
			ps_u = con.prepareStatement(query_u);
			stmt = con.createStatement();
			rs = stmt.executeQuery(query);
			while (rs.next()){
				PopulationRecord record = new PopulationRecord();
				record.setId(rs.getInt("population_id"));
				record.setPopulationName(rs.getString("population_name"));
				record.setCreated(rs.getDate("created"));
				
				ps_u.setInt(1, rs.getInt("user_id"));
				rs_u = ps_u.executeQuery();
				while (rs_u.next()){
						record.setUser(rs_u.getString("firstname")+" "+rs_u.getString("lastname"));
				}
				list.add(record);
			}
			ps_u.close();
			rs.close();
			stmt.close();
		}catch(SQLException e){
			throw new RuntimeException(e);
		}finally{
			SQLUtils.safeClose(rs_u);
			SQLUtils.safeClose(ps_u);
			SQLUtils.safeClose(rs);
			SQLUtils.safeClose(stmt);
			pool.close(con);
		}
		return list;
	}
	
	public static PopulationRecord addPopulation(ConnectionPool pool, PopulationRecord newPopulation){
		PopulationRecord record = new PopulationRecord();
		String[] user = newPopulation.getUser().split("\\s");
		int returnedKeyNewPopulation = -1;
		
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rsk = null;
		
		try{
			con = pool.getConnection();
			String query = "INSERT IGNORE INTO populations SET population_name=?,created=NOW(),user_id=(SELECT user_id FROM users WHERE firstname=? AND lastname=?);";
			ps = con.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, newPopulation.getPopulationName());
			ps.setString(2, user[0]);
			ps.setString(3, user[1]);
			ps.executeUpdate();
			rsk = ps.getGeneratedKeys();
			if (rsk.next()){
				returnedKeyNewPopulation = rsk.getInt(1);
			}else{
				throw new RuntimeException("Cannot insert new population");
			}
			rsk.close();
			ps.close();
		}catch(SQLException e){
			throw new RuntimeException(e);
		}finally{
			SQLUtils.safeClose(rsk);
			SQLUtils.safeClose(ps);
		}
		if (returnedKeyNewPopulation>-1){
			Statement stmt = null;
			ResultSet rs = null;
			try{
				stmt = con.createStatement();
				String query = "SELECT * FROM populations WHERE population_id="+returnedKeyNewPopulation+";";
				rs = stmt.executeQuery(query);
				if (rs.next()) {   
					do {  
						record.setId(rs.getInt("population_id"));
						record.setCreated(rs.getDate("created"));
						record.setPopulationName(newPopulation.getPopulationName());
						record.setUser(newPopulation.getUser());
					} while (rs.next());  
				} else {  
					rs.close();
					stmt.close();
					record = null;
				}
				rs.close();
				stmt.close();
			}catch(SQLException e){
				throw new RuntimeException(e);
			}finally{
				SQLUtils.safeClose(rs);
				SQLUtils.safeClose(ps);
				pool.close(con);
			}
		}else{
			record = null;
			pool.close(con);
		}
		return record;
	}
	
	public static void removePopulation(ConnectionPool pool, PopulationRecord record){
		
		String query = "DELETE FROM populations WHERE population_id=?";
		PreparedStatement ps = null;
		Connection con = null;
		
		
		try{
			con = pool.getConnection();
			ps = con.prepareStatement(query);
			ps.setInt(1, record.getId());
			ps.executeUpdate();
			ps.close();
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			SQLUtils.safeClose(ps);
			pool.close(con);
		}
	}
	
	public static PopulationRecord updatePopulation(ConnectionPool pool, PopulationRecord record){
		//TODO
		return record;
	}
}
