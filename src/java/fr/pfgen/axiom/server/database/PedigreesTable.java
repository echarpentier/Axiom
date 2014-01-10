package fr.pfgen.axiom.server.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import fr.pfgen.axiom.server.utils.DatabaseUtils;
import fr.pfgen.axiom.server.utils.IOUtils;
import fr.pfgen.axiom.server.utils.SQLUtils;
import fr.pfgen.axiom.shared.records.PedigreeRecord;

public class PedigreesTable {

    public static String getPedigreeState(ConnectionPool pool, String studyName, int userID) {
        String query = "SELECT * FROM pedigrees WHERE study_id=(SELECT study_id FROM studies WHERE study_name=?) AND user_id=?";

        String state = new String();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = pool.getConnection();
            ps = con.prepareStatement(query);
            ps.setString(1, studyName);
            ps.setInt(2, userID);
            rs = ps.executeQuery();
            while (rs.next()) {
                state = rs.getString("state");
            }
            rs.close();
            ps.close();
            return state;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            SQLUtils.safeClose(ps);
            SQLUtils.safeClose(rs);
            pool.close(con);
        }
    }

    public static Boolean addPedigreeToDB(ConnectionPool pool, File uploadedPedFile, String studyName, int userID) {
        Connection con = null;
        String pedQuery = "REPLACE INTO pedigrees (study_id,user_id,state) VALUES ((SELECT study_id FROM studies WHERE study_name=?),?,?)";
        String pedRecordQuery = "INSERT INTO pedigree_records (pedigree_id,family_id,individual_id,father_id,mother_id,sex,status) VALUES ((SELECT pedigree_id FROM pedigrees WHERE study_id=(SELECT study_id FROM studies WHERE study_name=?) AND user_id=?),?,?,?,?,?,?)";
        PreparedStatement pedPS = null;
        PreparedStatement pedRecordPS = null;
        BufferedReader br = null;
        Savepoint savepoint = null;

        try {
            con = pool.getConnection();
            pedPS = con.prepareStatement(pedQuery);
            pedRecordPS = con.prepareStatement(pedRecordQuery);

            pedPS.setString(1, studyName);
            pedPS.setInt(2, userID);
            pedPS.setString(3, "uploaded");

            savepoint = con.setSavepoint();
            pedPS.execute();

            br = new BufferedReader(new FileReader(uploadedPedFile));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#") || line.equals("") || line.startsWith("famID")) {
                    continue;
                } else {
                    String[] lineSplit = line.split("\\s+");

                    pedRecordPS.setString(1, studyName);
                    pedRecordPS.setInt(2, userID);
                    pedRecordPS.setString(3, lineSplit[0]);
                    pedRecordPS.setString(4, lineSplit[1]);
                    pedRecordPS.setString(5, lineSplit[2]);
                    pedRecordPS.setString(6, lineSplit[3]);
                    if (lineSplit[4].equals("male") || Integer.parseInt(lineSplit[4]) == 1) {
                        pedRecordPS.setString(7, "male");
                    } else if (lineSplit[4].equals("female") || Integer.parseInt(lineSplit[4]) == 2) {
                        pedRecordPS.setString(7, "female");
                    } else {
                        pedRecordPS.setString(7, "unknown");
                    }
                    if (lineSplit[5].equals("affected") || Integer.parseInt(lineSplit[5]) == 2) {
                        pedRecordPS.setString(8, "affected");
                    } else if (lineSplit[5].equals("unaffected") || Integer.parseInt(lineSplit[5]) == 1) {
                        pedRecordPS.setString(8, "unaffected");
                    } else {
                        pedRecordPS.setString(8, "unknown");
                    }
                    pedRecordPS.execute();
                }
            }
            br.close();
            pedPS.close();
            pedRecordPS.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                con.rollback(savepoint);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            try {
                con.rollback(savepoint);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            return false;
        } finally {
            IOUtils.safeClose(br);
            SQLUtils.safeClose(pedPS);
            SQLUtils.safeClose(pedRecordPS);
            pool.close(con);
        }
    }

    public static String constructQuery(String sortBy, Map<String, String> filterCriteria) {
        String query = new String();

        //default query (order by plate_name needs associated table plates)
        query = "SELECT pr.*,p.user_id,s.study_name FROM ((studies s JOIN pedigrees p ON s.study_id=p.study_id) JOIN pedigree_records pr ON pr.pedigree_id=p.pedigree_id)";

        if (filterCriteria != null && !filterCriteria.isEmpty()) {
            Map<String, String> critMap = new Hashtable<String, String>();
            if (filterCriteria.containsKey("user_id")) {
                critMap.put("p.user_id", filterCriteria.get("user_id"));
            }
            if (filterCriteria.containsKey("study_name")) {
                critMap.put("s.study_name", filterCriteria.get("study_name"));
            }
            if (filterCriteria.containsKey("status")) {
                critMap.put("pr.status", filterCriteria.get("status"));
            }
            if (!critMap.isEmpty()) {
                query = query.concat(DatabaseUtils.constructWhereClause(critMap));
            }
        }

        query = query.concat(DatabaseUtils.constructOrderByClause(sortBy));
        return query;
    }

    public static List<PedigreeRecord> getPedigreeRecordsInStudy(ConnectionPool pool, String query) {
        List<PedigreeRecord> recordList = new ArrayList<PedigreeRecord>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = pool.getConnection();
            ps = con.prepareStatement(query);
            //ps.setString(1, studyName);
            //ps.setInt(2, userID);
            rs = ps.executeQuery();
            while (rs.next()) {
                PedigreeRecord record = new PedigreeRecord();
                record.setUserID(rs.getInt("user_id"));
                record.setStudyName(rs.getString("study_name"));
                record.setPedigreeID(rs.getInt("pedigree_record_id"));
                record.setIndividualID(rs.getString("individual_id"));
                record.setFamilyID(rs.getString("family_id"));
                record.setFatherID(rs.getString("father_id"));
                record.setMotherID(rs.getString("mother_id"));
                record.setSex(sexDbToSexPedRecord(rs.getString("sex")));
                record.setStatus(statusDbToStatusPedRecord(rs.getString("status")));
                recordList.add(record);
            }
            rs.close();
            ps.close();
            return recordList;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            SQLUtils.safeClose(rs);
            SQLUtils.safeClose(ps);
            pool.close(con);
        }
    }

    private static int sexDbToSexPedRecord(String sex) {
        if (sex == null) {
            return 0;
        }
        if (sex.equalsIgnoreCase("male")) {
            return 1;
        }
        if (sex.equalsIgnoreCase("female")) {
            return 2;
        }
        return 0;
    }

    private static String sexPedRecordToSexDb(int sex) {
        if (sex == 1) {
            return "male";
        }
        if (sex == 2) {
            return "female";
        }
        return "unknown";
    }

    private static int statusDbToStatusPedRecord(String status) {
        if (status == null) {
            return 0;
        }
        if (status.equalsIgnoreCase("unaffected")) {
            return 1;
        }
        if (status.equalsIgnoreCase("affected")) {
            return 2;
        }
        return 0;
    }

    private static String statusPedRecordTostatusDb(int status) {
        if (status == 1) {
            return "unaffected";
        }
        if (status == 2) {
            return "affected";
        }
        return "unknown";
    }

    public static Boolean validatePedigree(ConnectionPool pool, String studyName, int userID) {
        String query = "UPDATE pedigrees SET state=state+1 WHERE study_id=(SELECT study_id FROM studies WHERE study_name=?) AND user_id=?";
        PreparedStatement ps = null;
        Connection con = null;

        try {
            con = pool.getConnection();
            ps = con.prepareStatement(query);
            ps.setString(1, studyName);
            ps.setInt(2, userID);
            ps.execute();
            ps.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            SQLUtils.safeClose(ps);
            pool.close(con);
        }
    }

    public static Boolean invalidatePedigree(ConnectionPool pool, String studyName, int userID) {
        String query = "UPDATE pedigrees SET state=state-1 WHERE study_id=(SELECT study_id FROM studies WHERE study_name=?) AND user_id=?";
        PreparedStatement ps = null;
        Connection con = null;

        try {
            con = pool.getConnection();
            ps = con.prepareStatement(query);
            ps.setString(1, studyName);
            ps.setInt(2, userID);
            ps.execute();
            ps.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            SQLUtils.safeClose(ps);
            pool.close(con);
        }
    }

    public static PedigreeRecord addNewRecord(ConnectionPool pool, PedigreeRecord record) {

        String query = "INSERT IGNORE INTO pedigree_records (pedigree_id,family_id,individual_id,father_id,mother_id,sex,status) VALUES"
                + " ((SELECT pedigree_id FROM pedigrees WHERE study_id=(SELECT study_id FROM studies WHERE study_name=?) AND user_id=?),?,?,?,?,?,?)";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = pool.getConnection();
            ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, record.getStudyName());
            ps.setInt(2, record.getUserID());
            ps.setString(3, record.getFamilyID());
            ps.setString(4, record.getIndividualID());
            ps.setString(5, record.getFatherID());
            ps.setString(6, record.getMotherID());
            ps.setString(7, sexPedRecordToSexDb(record.getSex()));
            ps.setString(8, statusPedRecordTostatusDb(record.getStatus()));

            ps.executeUpdate();

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                record.setPedigreeID(rs.getInt(1));
            } else {
                record = null;
            }
            rs.close();
            ps.close();
            return record;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            SQLUtils.safeClose(rs);
            SQLUtils.safeClose(ps);
            pool.close(con);
        }
    }

    public static void removeRecord(ConnectionPool pool, PedigreeRecord record) {
        String query = "DELETE FROM pedigree_records WHERE pedigree_record_id=?";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = pool.getConnection();
            ps = con.prepareStatement(query);
            ps.setInt(1, record.getPedigreeID());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SQLUtils.safeClose(ps);
            pool.close(con);
        }
    }

    public static PedigreeRecord updatedRecord(ConnectionPool pool, PedigreeRecord record) {
        String query = "UPDATE pedigree_records SET family_id=?,individual_id=?,father_id=?,mother_id=?,sex=?,status=? WHERE pedigree_record_id=?";
        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = pool.getConnection();
            ps = con.prepareStatement(query);
            ps.setString(1, record.getFamilyID());
            ps.setString(2, record.getIndividualID());
            ps.setString(3, record.getFatherID());
            ps.setString(4, record.getMotherID());
            ps.setString(5, sexPedRecordToSexDb(record.getSex()));
            ps.setString(6, statusPedRecordTostatusDb(record.getStatus()));
            ps.setInt(7, record.getPedigreeID());
            ps.executeUpdate();
            ps.close();
            return record;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            SQLUtils.safeClose(ps);
            pool.close(con);
        }
    }

    public static List<PedigreeRecord> getMaleButMother(ConnectionPool pool, String studyName, int userID) {
        String query = "SELECT * FROM pedigree_records pr1 WHERE sex=\"male\" AND EXISTS (SELECT * FROM pedigree_records pr2 WHERE pedigree_id=(SELECT pedigree_id FROM pedigrees WHERE study_id=(SELECT study_id FROM studies WHERE study_name=?) AND user_id=?) and pr2.mother_id=pr1.individual_id) and pedigree_id=(SELECT pedigree_id FROM pedigrees WHERE study_id=(SELECT study_id FROM studies WHERE study_name=?) AND user_id=?)";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<PedigreeRecord> pedList = new ArrayList<PedigreeRecord>();
        try {
            con = pool.getConnection();
            ps = con.prepareStatement(query);
            ps.setString(1, studyName);
            ps.setInt(2, userID);
            ps.setString(3, studyName);
            ps.setInt(4, userID);

            rs = ps.executeQuery();
            while (rs.next()) {
                PedigreeRecord rec = new PedigreeRecord();
                rec.setPedigreeID(rs.getInt("pedigree_record_id"));
                rec.setFamilyID(rs.getString("family_id"));
                rec.setIndividualID(rs.getString("individual_id"));
                rec.setFatherID(rs.getString("father_id"));
                rec.setMotherID(rs.getString("mother_id"));
                rec.setSex(sexDbToSexPedRecord(rs.getString("sex")));
                rec.setStatus(statusDbToStatusPedRecord(rs.getString("status")));
                rec.setStudyName(studyName);
                rec.setUserID(userID);
                pedList.add(rec);
            }
            rs.close();
            ps.close();
            return pedList;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            SQLUtils.safeClose(rs);
            SQLUtils.safeClose(ps);
            pool.close(con);
        }
    }

    public static List<PedigreeRecord> getFemaleButFather(ConnectionPool pool, String studyName, int userID) {
        String query = "SELECT * FROM pedigree_records pr1 WHERE sex=\"female\" AND EXISTS (SELECT * FROM pedigree_records pr2 WHERE pedigree_id=(SELECT pedigree_id FROM pedigrees WHERE study_id=(SELECT study_id FROM studies WHERE study_name=?) AND user_id=?) and pr2.father_id=pr1.individual_id) and pedigree_id=(SELECT pedigree_id FROM pedigrees WHERE study_id=(SELECT study_id FROM studies WHERE study_name=?) AND user_id=?)";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<PedigreeRecord> pedList = new ArrayList<PedigreeRecord>();
        try {
            con = pool.getConnection();
            ps = con.prepareStatement(query);
            ps.setString(1, studyName);
            ps.setInt(2, userID);
            ps.setString(3, studyName);
            ps.setInt(4, userID);

            rs = ps.executeQuery();
            while (rs.next()) {
                PedigreeRecord rec = new PedigreeRecord();
                rec.setPedigreeID(rs.getInt("pedigree_record_id"));
                rec.setFamilyID(rs.getString("family_id"));
                rec.setIndividualID(rs.getString("individual_id"));
                rec.setFatherID(rs.getString("father_id"));
                rec.setMotherID(rs.getString("mother_id"));
                rec.setSex(sexDbToSexPedRecord(rs.getString("sex")));
                rec.setStatus(statusDbToStatusPedRecord(rs.getString("status")));
                rec.setStudyName(studyName);
                rec.setUserID(userID);
                pedList.add(rec);
            }
            rs.close();
            ps.close();
            return pedList;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            SQLUtils.safeClose(rs);
            SQLUtils.safeClose(ps);
            pool.close(con);
        }
    }

    public static List<PedigreeRecord> getIndividualsNotDescribed(ConnectionPool pool, String studyName, int userID) {
        String queryPedID = "SELECT * FROM pedigrees WHERE study_id=(SELECT study_id FROM studies WHERE study_name=?) AND user_id=?";
        String query = "SELECT * FROM pedigree_records WHERE pedigree_id=? AND (mother_id!=\"0\" AND mother_id NOT IN (SELECT individual_id FROM pedigree_records WHERE pedigree_id=?) OR father_id!=\"0\" AND father_id NOT IN (SELECT individual_id FROM pedigree_records WHERE pedigree_id=?))";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<PedigreeRecord> pedList = new ArrayList<PedigreeRecord>();

        try {
            con = pool.getConnection();
            ps = con.prepareStatement(queryPedID);
            ps.setString(1, studyName);
            ps.setInt(2, userID);
            rs = ps.executeQuery();
            int pedID = -1;
            while (rs.next()) {
                pedID = rs.getInt(1);
            }
            rs.close();
            ps.close();
            if (pedID > -1) {
                ps = con.prepareStatement(query);
                ps.setInt(1, pedID);
                ps.setInt(2, pedID);
                ps.setInt(3, pedID);
                rs = ps.executeQuery();
                while (rs.next()) {
                    PedigreeRecord rec = new PedigreeRecord();
                    rec.setPedigreeID(rs.getInt("pedigree_record_id"));
                    rec.setFamilyID(rs.getString("family_id"));
                    rec.setIndividualID(rs.getString("individual_id"));
                    rec.setFatherID(rs.getString("father_id"));
                    rec.setMotherID(rs.getString("mother_id"));
                    rec.setSex(sexDbToSexPedRecord(rs.getString("sex")));
                    rec.setStatus(statusDbToStatusPedRecord(rs.getString("status")));
                    rec.setStudyName(studyName);
                    rec.setUserID(userID);
                    pedList.add(rec);
                }
                rs.close();
                ps.close();
                return pedList;
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            SQLUtils.safeClose(rs);
            SQLUtils.safeClose(ps);
            pool.close(con);
        }
    }

    public static Map<String, String> getGenderInPedigree(ConnectionPool pool, String studyName, int userID) {
        String query = "SELECT individual_id,sex FROM pedigree_records WHERE pedigree_id=(SELECT pedigree_id FROM pedigrees WHERE study_id=(SELECT study_id FROM studies WHERE study_name=?) AND user_id=?)";
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        Map<String, String> gendersInPedigree = new Hashtable<String, String>();
        try {
            con = pool.getConnection();
            ps = con.prepareStatement(query);
            ps.setString(1, studyName);
            ps.setInt(2, userID);
            rs = ps.executeQuery();

            while (rs.next()) {
                gendersInPedigree.put(rs.getString("individual_id"), rs.getString("sex"));
            }

            rs.close();
            ps.close();
            return gendersInPedigree;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            SQLUtils.safeClose(rs);
            SQLUtils.safeClose(ps);
            pool.close(con);
        }
    }

    public static void removeOtherPedigreesForStudy(ConnectionPool pool, String studyName, int userID) {
        String query = "DELETE FROM pedigrees WHERE study_id=(SELECT study_id FROM studies WHERE study_name=?) AND user_id!=?";

        Connection con = null;
        PreparedStatement ps = null;

        try {
            con = pool.getConnection();
            ps = con.prepareStatement(query);
            ps.setString(1, studyName);
            ps.setInt(2, userID);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SQLUtils.safeClose(ps);
            pool.close(con);
        }
    }

    public static List<String> getSubPopNamesForStudy(ConnectionPool pool, String studyName) {
        List<String> subPopList = new ArrayList<String>();

        String query = "SELECT subpedigree_name FROM subpedigrees WHERE pedigree_id=(SELECT pedigree_id FROM pedigrees WHERE study_id=(SELECT study_id FROM studies WHERE study_name=?))";

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = pool.getConnection();
            ps = con.prepareStatement(query);
            ps.setString(1, studyName);
            rs = ps.executeQuery();
            while (rs.next()) {
                subPopList.add(rs.getString("subpedigree_name"));
            }
            rs.close();
            ps.close();
            return subPopList;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            SQLUtils.safeClose(rs);
            SQLUtils.safeClose(ps);
            pool.close(con);
        }
    }

    public static boolean addSubPopToStudy(ConnectionPool pool, String studyName, String subPopName, List<Integer> subPedIds) {
        String insertSubPop = "INSERT INTO subpedigrees (subpedigree_name, pedigree_id) VALUES (?,(SELECT pedigree_id FROM pedigrees WHERE study_id=(SELECT study_id FROM studies WHERE study_name=?)))";
        String insertSubPopRecs = "INSERT INTO subpedigree_individuals (subpedigree_id,pedigree_record_id) VALUES (?,?)";

        PreparedStatement psSubPop = null;
        PreparedStatement psSubPopRecs = null;
        ResultSet rsk = null;
        Connection con = null;

        try {
            con = pool.getConnection();
            psSubPop = con.prepareStatement(insertSubPop, Statement.RETURN_GENERATED_KEYS);
            psSubPopRecs = con.prepareStatement(insertSubPopRecs);

            psSubPop.setString(1, subPopName);
            psSubPop.setString(2, studyName);
            psSubPop.executeUpdate();
            rsk = psSubPop.getGeneratedKeys();

            int key;
            if (rsk.next()) {
                key = rsk.getInt(1);
            } else {
                return false;
            }
            rsk.close();
            psSubPop.close();

            for (Integer pedRecId : subPedIds) {
                psSubPopRecs.setInt(1, key);
                psSubPopRecs.setInt(2, pedRecId);
                psSubPopRecs.executeUpdate();
            }

            psSubPopRecs.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            SQLUtils.safeClose(rsk);
            SQLUtils.safeClose(psSubPop);
            SQLUtils.safeClose(psSubPopRecs);
            pool.close(con);
        }
    }

    public static List<String> getIndNamesInStudyForSubpopulation(ConnectionPool pool, String subPopName) {
        String query = "SELECT pr.* FROM ((pedigree_records pr JOIN subpedigree_individuals si ON pr.pedigree_record_id=si.pedigree_record_id) JOIN subpedigrees s ON s.subpedigree_id=si.subpedigree_id) WHERE subpedigree_name=?";

        List<String> pedNamesList = new ArrayList<String>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            con = pool.getConnection();
            ps = con.prepareStatement(query);
            ps.setString(1, subPopName);
            rs = ps.executeQuery();
            while (rs.next()) {
                pedNamesList.add(rs.getString("individual_id"));
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

        return pedNamesList;
    }
}
