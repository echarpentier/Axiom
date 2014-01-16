package fr.pfgen.axiom.server.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import fr.pfgen.axiom.server.utils.DatabaseUtils;
import fr.pfgen.axiom.server.utils.SQLUtils;
import fr.pfgen.axiom.shared.records.PlateRecord;

public class PlatesTable {

    public static List<String> getPlateNames(ConnectionPool pool) {
        List<String> plateList = new ArrayList<String>();

        String query = "SELECT plate_name FROM plates ORDER BY created DESC";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = pool.getConnection();
            ps = con.prepareStatement(query);
            rs = ps.executeQuery();
            while (rs.next()) {
                plateList.add(rs.getString("plate_name"));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            SQLUtils.safeClose(rs);
            SQLUtils.safeClose(ps);
            pool.close(con);
        }
        return plateList;
    }

    public static String constructQuery(String sortBy, Map<String, String> filterCriteria) {

        String query = new String();

        //default query
        query = "SELECT * FROM plates";

        if (filterCriteria != null && !filterCriteria.isEmpty()) {
            Map<String, String> critMap = new Hashtable<String, String>();
            if (filterCriteria.containsKey("population_name")) {
                query = "SELECT DISTINCT plates.* FROM (((samples JOIN plates ON samples.plate_id=plates.plate_id) JOIN samples_in_populations ON samples.sample_id=samples_in_populations.sample_id) JOIN populations ON samples_in_populations.population_id=populations.population_id)";
                critMap.put("population_name", filterCriteria.get("population_name"));
            }
            if (filterCriteria.containsKey("plate_name")) {
                critMap.put("plate_name", filterCriteria.get("plate_name"));
            }
            if (filterCriteria.containsKey("family_name")) {
                query = "SELECT DISTINCT plates.* FROM (((samples JOIN plates ON samples.plate_id=plates.plate_id) JOIN samples_in_families ON samples.sample_id=samples_in_families.sample_id) JOIN families ON samples_in_families.family_id=families.family_id)";
                critMap.put("family_name", filterCriteria.get("family_name"));
            }
            if (!critMap.isEmpty()) {
                query = query.concat(DatabaseUtils.constructWhereClause(critMap));
            }
        }
        if (sortBy != null && !sortBy.isEmpty()) {
            sortBy = sortBy.replaceAll("created", "plates.created");
            query = query.concat(DatabaseUtils.constructOrderByClause(sortBy));
        }

        return query;
    }

    public static List<PlateRecord> getPlates(ConnectionPool pool, String query, Integer startRow, Integer endRow) {
        String rowsToString = " LIMIT ";

        List<PlateRecord> list = new ArrayList<PlateRecord>();

        //Requested rows
        if (startRow != null && endRow != null && endRow >= startRow) {
            Integer size = endRow - startRow + 1;
            rowsToString = rowsToString.concat(startRow + "," + size);
            query = query.concat(rowsToString);
        }

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = pool.getConnection();
            ps = con.prepareStatement(query);
            rs = ps.executeQuery();
            while (rs.next()) {
                PlateRecord record = new PlateRecord();
                record.setName(rs.getString("plate_name"));
                record.setBarcode(rs.getString("plate_barcode"));
                record.setId(rs.getInt("plate_id"));
                record.setCreated(rs.getDate("created"));
                list.add(record);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            SQLUtils.safeClose(rs);
            SQLUtils.safeClose(ps);
            pool.close(con);
        }
        return list;
    }

    public static PlateRecord addPlate(ConnectionPool pool, PlateRecord newPlate) {

        return null;
    }

    public static void removePlate(ConnectionPool pool, int plateID) {
        PreparedStatement removeStmt = null;
        String sql = "DELETE FROM plates WHERE plate_id=?";
        Connection con = null;

        try {
            con = pool.getConnection();
            removeStmt = con.prepareStatement(sql);
            removeStmt.setInt(1, plateID);
            removeStmt.executeUpdate();
            removeStmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            SQLUtils.safeClose(removeStmt);
            pool.close(con);
        }
    }

    public static PlateRecord updatePlate(ConnectionPool pool, PlateRecord record) {
        Connection con = null;
        PreparedStatement updateStmt = null;
        Statement fetchStmt = null;
        ResultSet rs = null;
        try {
            con = pool.getConnection();
            if (record.getName() == null || record.getName().isEmpty()) {
                fetchStmt = con.createStatement();
                rs = fetchStmt.executeQuery("SELECT * FROM plates WHERE plate_id=" + record.getId());
                String plateName = "";
                while (rs.next()) {
                    plateName = rs.getString("plate_name");
                }
                record.setName(plateName);
            } else {
                fetchStmt = con.createStatement();
                rs = fetchStmt.executeQuery("SELECT * FROM plates");
                String newName = record.getName();
                String plateName = "";
                boolean alreadyExists = false;
                while (rs.next()) {
                    if (newName.equals(rs.getString("plate_name"))) {
                        alreadyExists = true;
                    }
                    if (record.getId() == rs.getInt("plate_id")) {
                        plateName = rs.getString("plate_name");
                    }
                }
                if (alreadyExists) {
                    record.setName(plateName);
                } else {
                    String query = "UPDATE IGNORE plates SET plate_name=? WHERE plate_id=?";
                    updateStmt = con.prepareStatement(query);
                    updateStmt.setString(1, record.getName());
                    updateStmt.setInt(2, record.getId());
                    updateStmt.executeUpdate();
                }
            }
            updateStmt.close();
            fetchStmt.close();
            rs.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            SQLUtils.safeClose(fetchStmt);
            SQLUtils.safeClose(updateStmt);
            SQLUtils.safeClose(rs);
            pool.close(con);
        }
        return record;
    }
}
