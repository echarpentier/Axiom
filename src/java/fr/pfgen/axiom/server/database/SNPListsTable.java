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

public class SNPListsTable {

    public static List<String> getListNames(ConnectionPool pool) {
        List<String> list = new ArrayList<String>();

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String query = "SELECT snp_list_name FROM snp_lists";

        try {
            con = pool.getConnection();
            ps = con.prepareStatement(query);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("snp_list_name"));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            SQLUtils.safeClose(rs);
            SQLUtils.safeClose(ps);
            pool.close(con);
        }
        return list;
    }

    public static boolean createNewList(ConnectionPool pool, String listName, File snpListFile) {

        Connection con = null;
        String query = "INSERT INTO snp_lists (snp_list_name,snp_list_path) VALUES (?,?)";
        PreparedStatement ps = null;

        try {
            con = pool.getConnection();
            ps = con.prepareStatement(query);
            ps.setString(1, listName);
            ps.setString(2, DatabaseUtils.realAxiomPathToDbPath(snpListFile.getAbsolutePath()));
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            SQLUtils.safeClose(ps);
            pool.close(con);
        }

        return true;
    }

    public static String clusterGraphExistsForGeno(ConnectionPool pool, String genoName, String listName) {
        String query = "SELECT * FROM cg_geno_calls WHERE geno_id=(SELECT geno_id FROM genotyping_analysis WHERE geno_name=?) AND snp_list_id=(SELECT snp_list_id FROM snp_lists WHERE snp_list_name=?)";

        String path = "";

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = pool.getConnection();
            ps = con.prepareStatement(query);
            ps.setString(1, genoName);
            ps.setString(2, listName);
            rs = ps.executeQuery();
            if (rs.next()) {
                path = DatabaseUtils.dbAxiomPathToRealPath(rs.getString("calls_path"));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SQLUtils.safeClose(rs);
            SQLUtils.safeClose(ps);
            pool.close(con);
        }

        return path;
    }

    public static String clusterGraphExistsForStudy(ConnectionPool pool, String studyName, String listName) {
        String query = "SELECT * FROM cg_study_calls WHERE study_id=(SELECT study_id FROM studies WHERE study_name=?) AND snp_list_id=(SELECT snp_list_id FROM snp_lists WHERE snp_list_name=?)";

        String path = "";

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = pool.getConnection();
            ps = con.prepareStatement(query);
            ps.setString(1, studyName);
            ps.setString(2, listName);
            rs = ps.executeQuery();
            if (rs.next()) {
                path = DatabaseUtils.dbAxiomPathToRealPath(rs.getString("calls_path"));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SQLUtils.safeClose(rs);
            SQLUtils.safeClose(ps);
            pool.close(con);
        }

        return path;
    }

    public static boolean addNewGraphForGeno(ConnectionPool pool, String genoName, String listName, String path) {
        String query = "INSERT INTO cg_geno_calls (geno_id,snp_list_id,calls_path) VALUES ((SELECT geno_id FROM genotyping_analysis WHERE geno_name=?),(SELECT snp_list_id FROM snp_lists WHERE snp_list_name=?),?)";

        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = pool.getConnection();
            ps = con.prepareStatement(query);
            ps.setString(1, genoName);
            ps.setString(2, listName);
            ps.setString(3, DatabaseUtils.realAxiomPathToDbPath(path));
            ps.executeUpdate();
            ps.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            SQLUtils.safeClose(ps);
            pool.close(con);
        }
    }

    public static boolean addNewGraphForStudy(ConnectionPool pool, String studyName, String listName, String path) {
        String query = "INSERT INTO cg_study_calls (study_id,snp_list_id,calls_path) VALUES ((SELECT study_id FROM studies WHERE study_name=?),(SELECT snp_list_id FROM snp_lists WHERE snp_list_name=?),?)";

        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = pool.getConnection();
            ps = con.prepareStatement(query);
            ps.setString(1, studyName);
            ps.setString(2, listName);
            ps.setString(3, DatabaseUtils.realAxiomPathToDbPath(path));
            ps.executeUpdate();

            ps.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            SQLUtils.safeClose(ps);
            pool.close(con);
        }
    }

    public static File getSnpListFile(ConnectionPool pool, String listName) {
        String query = "SELECT snp_list_path FROM snp_lists WHERE snp_list_name=?";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = pool.getConnection();
            ps = con.prepareStatement(query);
            ps.setString(1, listName);
            rs = ps.executeQuery();
            if (rs.next()) {
                return new File(DatabaseUtils.dbAxiomPathToRealPath(rs.getString("snp_list_path")));
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            SQLUtils.safeClose(ps);
            pool.close(con);
        }
    }

    public static List<File> getCallFilesForList(ConnectionPool pool, String listName) {
        List<File> callFiles = new ArrayList<File>();
        
        
        List<String> tableNames = new ArrayList<String>(2);
        tableNames.add("cg_geno_calls");
        tableNames.add("cg_study_calls");

        for (String tableName : tableNames) {
            String query = "SELECT calls_path FROM "+tableName+" WHERE snp_list_id=(SELECT snp_list_id FROM snp_lists WHERE snp_list_name=?)";
            PreparedStatement ps = null;
            Connection con = null;
            ResultSet rs = null;

            try {
                con = pool.getConnection();
                ps = con.prepareStatement(query);
                ps.setString(1, listName);

                rs = ps.executeQuery();
                while (rs.next()) {
                    callFiles.add(new File(DatabaseUtils.dbAxiomPathToRealPath(rs.getString("calls_path"))));
                }
                rs.close();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            } finally {
                SQLUtils.safeClose(rs);
                SQLUtils.safeClose(ps);
                pool.close(con);
            }
        }

        return callFiles;
    }

    public static boolean deleteSNPList(ConnectionPool pool, String listName) {
        String query = "DELETE FROM snp_lists WHERE snp_list_name=?";
        PreparedStatement ps = null;
        Connection con = null;

        try {
            con = pool.getConnection();
            ps = con.prepareStatement(query);
            ps.setString(1, listName);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            SQLUtils.safeClose(ps);
            pool.close(con);
        }

        return true;
    }
}
