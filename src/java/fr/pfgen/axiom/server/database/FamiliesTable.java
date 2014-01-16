/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.pfgen.axiom.server.database;

import fr.pfgen.axiom.server.utils.SQLUtils;
import fr.pfgen.axiom.shared.records.FamilyRecord;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author eric
 */
public class FamiliesTable {

    public static List<String> getFamiliesNames(ConnectionPool pool) {
        List<String> familiesNames = new ArrayList<String>();
        Statement stmt = null;
        ResultSet rs = null;
        Connection con = null;

        try {
            con = pool.getConnection();
            stmt = con.createStatement();
            String query = "SELECT family_name FROM families ORDER BY created DESC";
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                familiesNames.add(rs.getString("family_name"));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            SQLUtils.safeClose(rs);
            SQLUtils.safeClose(stmt);
            pool.close(con);
        }
        return familiesNames;
    }
    
    public static List<FamilyRecord> getFamilies(ConnectionPool pool, Integer startRow, Integer endRow, final String sortBy, Map<String, String> filterCriteria) {
        List<FamilyRecord> list = new ArrayList<FamilyRecord>();
        String query = "SELECT * FROM families ";
        String critToString = "WHERE ";
        String rowsToString = "LIMIT ";
        String orderByString = "ORDER BY ";

        //filter criterias
        if (filterCriteria != null && filterCriteria.size() > 0) {
            List<String> critList = new ArrayList<String>();
            if (filterCriteria.containsKey("family_id")) {
                critList.add("family_id=" + filterCriteria.get("family_id"));
            } else if (filterCriteria.containsKey("family_name")) {
                critList.add("family_name=\"" + filterCriteria.get("family_name") + "\"");
            } else if (filterCriteria.containsKey("user")) {
                String[] user = filterCriteria.get("user").split("\\s");
                critList.add("user_id=(SELECT users.user_id FROM users JOIN populations ON users.user_id=populations.user_id WHERE firstname=\"" + user[0] + "\" AND lastname=\"" + user[1] + "\")");
            }

            for (Iterator<String> i = critList.iterator(); i.hasNext();) {
                String condition = i.next();
                critToString = critToString.concat(condition + " AND ");
            }
            if (critToString.endsWith(" AND ")) {
                critToString = critToString.replaceAll("\\sAND\\s$", " ");
            }
            query = query.concat(critToString);
        }

        //Sort By
        if (!sortBy.isEmpty()) {
            String[] orderByCrit = sortBy.split(",");
            for (String crit : orderByCrit) {
                if (crit.startsWith("-")) {
                    orderByString = orderByString.concat(crit.replaceFirst("-", "") + " DESC,");
                } else {
                    orderByString = orderByString.concat(crit + " ASC,");
                }
            }
            if (orderByString.endsWith(",")) {
                orderByString = orderByString.replaceAll(",$", " ");
            }
            query = query.concat(orderByString);
        }

        //Requested rows
        if (startRow != null && endRow != null && endRow >= startRow) {
            Integer size = endRow - startRow + 1;
            rowsToString = rowsToString.concat(startRow + "," + size);
            query = query.concat(rowsToString);
        }

        query = query.concat(";");
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

        String query_u = "SELECT firstname,lastname FROM users WHERE user_id=?";
        PreparedStatement ps_u = null;
        ResultSet rs_u = null;
        try {
            con = pool.getConnection();
            ps_u = con.prepareStatement(query_u);
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                FamilyRecord record = new FamilyRecord();
                record.setId(rs.getInt("family_id"));
                record.setName(rs.getString("family_name"));
                record.setCreated(rs.getDate("created"));
                record.setPropositus(rs.getString("propositus"));

                ps_u.setInt(1, rs.getInt("user_id"));
                rs_u = ps_u.executeQuery();
                while (rs_u.next()) {
                    record.setUser(rs_u.getString("firstname") + " " + rs_u.getString("lastname"));
                }
                list.add(record);
            }
            ps_u.close();
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            SQLUtils.safeClose(rs_u);
            SQLUtils.safeClose(ps_u);
            SQLUtils.safeClose(rs);
            SQLUtils.safeClose(stmt);
            pool.close(con);
        }
        return list;
    }

    public static FamilyRecord addFamily(ConnectionPool pool, FamilyRecord newFamily) {
        FamilyRecord record = new FamilyRecord();
        String[] user = newFamily.getUser().split("\\s");
        int returnedKeyNewFamily = -1;

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rsk = null;

        try {
            con = pool.getConnection();
            String query = "INSERT IGNORE INTO families SET family_name=?,created=NOW(),user_id=(SELECT user_id FROM users WHERE firstname=? AND lastname=?),propositus=?;";
            ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, newFamily.getName());
            ps.setString(2, user[0]);
            ps.setString(3, user[1]);
            ps.setString(4, newFamily.getPropositus());
            ps.executeUpdate();
            rsk = ps.getGeneratedKeys();
            if (rsk.next()) {
                returnedKeyNewFamily = rsk.getInt(1);
            } else {
                throw new RuntimeException("Cannot insert new family");
            }
            rsk.close();
            ps.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            SQLUtils.safeClose(rsk);
            SQLUtils.safeClose(ps);
        }
        if (returnedKeyNewFamily > -1) {
            Statement stmt = null;
            ResultSet rs = null;
            try {
                stmt = con.createStatement();
                String query = "SELECT * FROM families WHERE family_id=" + returnedKeyNewFamily + ";";
                rs = stmt.executeQuery(query);
                if (rs.next()) {
                    do {
                        record.setId(rs.getInt("family_id"));
                        record.setCreated(rs.getDate("created"));
                        record.setName(newFamily.getName());
                        record.setUser(newFamily.getUser());
                        record.setPropositus(newFamily.getPropositus());
                    } while (rs.next());
                } else {
                    rs.close();
                    stmt.close();
                    record = null;
                }
                rs.close();
                stmt.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            } finally {
                SQLUtils.safeClose(rs);
                SQLUtils.safeClose(ps);
                pool.close(con);
            }
        } else {
            record = null;
            pool.close(con);
        }
        return record;
    }

    public static void removeFamily(ConnectionPool pool, FamilyRecord record) {

        String query = "DELETE FROM families WHERE family_id=?";
        PreparedStatement ps = null;
        Connection con = null;


        try {
            con = pool.getConnection();
            ps = con.prepareStatement(query);
            ps.setInt(1, record.getId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SQLUtils.safeClose(ps);
            pool.close(con);
        }
    }

    public static FamilyRecord updateFamily(ConnectionPool pool, FamilyRecord record) {
        //TODO
        return record;
    }
}
