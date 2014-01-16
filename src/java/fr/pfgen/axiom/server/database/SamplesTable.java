package fr.pfgen.axiom.server.database;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fr.pfgen.axiom.server.beans.SampleForDQCGraph;
import fr.pfgen.axiom.server.beans.SampleForGenoQcGraph;
import fr.pfgen.axiom.server.utils.DatabaseUtils;
import fr.pfgen.axiom.server.utils.SQLUtils;
import fr.pfgen.axiom.shared.records.ArrayImageRecord;
import fr.pfgen.axiom.shared.records.SampleRecord;

public class SamplesTable {

    public static String constructQuery(final String sortBy, Map<String, String> filterCriteria) {

        String query = new String();

        //default query (order by plate_name needs associated table plates)
        query = "SELECT samples.*,plate_name FROM (plates JOIN samples ON plates.plate_id=samples.plate_id)";

        if (filterCriteria != null && !filterCriteria.isEmpty()) {
            Map<String, String> critMap = new Hashtable<String, String>();
            if (filterCriteria.containsKey("noPopulation")) {
                query = "SELECT samples.*,plate_name FROM (samples JOIN plates ON samples.plate_id=plates.plate_id) WHERE NOT EXISTS (SELECT * FROM samples_in_populations WHERE samples.sample_id = samples_in_populations.sample_id)";
            }
            if (filterCriteria.containsKey("plate_name")) {
                //query = "SELECT * FROM (plates JOIN samples ON plates.plate_id=samples.plate_id)";
                critMap.put("plates.plate_name", filterCriteria.get("plate_name"));
            }
            if (filterCriteria.containsKey("population_name")) {
                query = "SELECT samples.*,population_name,plate_name FROM (((samples JOIN samples_in_populations ON samples.sample_id=samples_in_populations.sample_id) JOIN populations ON populations.population_id=samples_in_populations.population_id) JOIN plates on plates.plate_id=samples.plate_id)";
                critMap.put("populations.population_name", filterCriteria.get("population_name"));
            }
            if (filterCriteria.containsKey("family_name")) {
                query = "SELECT samples.*,family_name,plate_name FROM (((samples JOIN samples_in_families ON samples.sample_id=samples_in_families.sample_id) JOIN families ON families.family_id=samples_in_families.family_id) JOIN plates on plates.plate_id=samples.plate_id)";
                critMap.put("families.family_name", filterCriteria.get("family_name"));
            }
            if (!critMap.isEmpty()) {
                query = query.concat(DatabaseUtils.constructWhereClause(critMap));
            }
        }

        query = query.concat(DatabaseUtils.constructOrderByClause(sortBy));
        return query;
    }

    public static List<SampleRecord> getSamples(ConnectionPool pool, String query, Integer startRow, Integer endRow) {
        String rowsToString = " LIMIT ";

        List<SampleRecord> list = new ArrayList<SampleRecord>();

        //Requested rows
        if (startRow != null && endRow != null && endRow >= startRow) {
            Integer size = endRow - startRow;
            rowsToString = rowsToString.concat(startRow + "," + size);
            query = query.concat(rowsToString);
        }

        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

        PreparedStatement ps_plate = null;
        PreparedStatement ps_population = null;
        PreparedStatement ps_family = null;
        ResultSet rs_plate = null;
        ResultSet rs_population = null;
        ResultSet rs_family = null;

        try {
            con = pool.getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            ps_plate = con.prepareStatement("SELECT plate_name FROM plates WHERE plate_id=?");
            ps_population = con.prepareStatement("SELECT p.population_name FROM populations p JOIN samples_in_populations sp ON sp.population_id=p.population_id WHERE sp.sample_id=?");
            ps_family = con.prepareStatement("SELECT f.family_name FROM families f JOIN samples_in_families sf ON sf.family_id=f.family_id WHERE sf.sample_id=?");
            while (rs.next()) {
                SampleRecord record = new SampleRecord();
                record.setSampleID(rs.getInt("sample_id"));
                record.setSampleName(rs.getString("sample_name"));
                record.setCoordX(rs.getInt("plate_coordX"));
                record.setCoordY(rs.getInt("plate_coordY"));
                record.setSamplePath(DatabaseUtils.dbDataPathToRealPath(rs.getString("sample_path")));

                ps_plate.setInt(1, rs.getInt("plate_id"));
                rs_plate = ps_plate.executeQuery();
                if (rs_plate.next()) {
                    record.setPlateName(rs_plate.getString("plate_name"));
                } else {
                    throw new RuntimeException("Cannot retrieve plate_name from plate_id in database");
                }
                rs_plate.close();
                
                List<String> popNamesList = new ArrayList<String>();
                ps_population.setInt(1, rs.getInt("sample_id"));
                rs_population = ps_population.executeQuery();
                while (rs_population.next()) {
                    popNamesList.add(rs_population.getString("population_name"));
                }
                record.setPopulationNames(popNamesList);
                
                List<String> famNamesList = new ArrayList<String>();
                ps_family.setInt(1, rs.getInt("sample_id"));
                rs_family = ps_family.executeQuery();
                while (rs_family.next()) {
                    famNamesList.add(rs_family.getString("family_name"));
                }
                record.setFamilyNames(famNamesList);
                
                list.add(record);
            }

            rs.close();
            ps_population.close();
            ps_plate.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            SQLUtils.safeClose(rs_population);
            SQLUtils.safeClose(rs_plate);
            SQLUtils.safeClose(rs);
            SQLUtils.safeClose(ps_population);
            SQLUtils.safeClose(ps_plate);
            SQLUtils.safeClose(ps_family);
            SQLUtils.safeClose(rs_family);
            SQLUtils.safeClose(stmt);
            pool.close(con);
        }
        return list;
    }

    public static SampleRecord addSample(ConnectionPool pool, SampleRecord newSample) {
        return null;
    }

    public static void removeSample(ConnectionPool pool, int plateID) {
    }

    public static SampleRecord updateSample(ConnectionPool pool, SampleRecord record) {
        //update populations
        String removeFromPop = "DELETE FROM samples_in_populations WHERE sample_id=?";
        String query = "INSERT IGNORE INTO samples_in_populations (sample_id,population_id) VALUES (?,(SELECT population_id FROM populations WHERE population_name=?))";
        String getSample = "SELECT population_name FROM ((samples s JOIN samples_in_populations sp ON s.sample_id=sp.sample_id) JOIN populations p ON p.population_id=sp.population_id) WHERE s.sample_id=?";
        PreparedStatement ps_remove = null;
        PreparedStatement ps_insert = null;
        PreparedStatement ps_get = null;
        ResultSet rs_get = null;
        Connection con = null;
        try {
            con = pool.getConnection();
            ps_remove = con.prepareStatement(removeFromPop);
            ps_remove.setInt(1, record.getSampleID());
            ps_remove.executeUpdate();
            ps_remove.close();
            ps_get = con.prepareStatement(getSample);
            ps_insert = con.prepareStatement(query);
            ps_insert.setInt(1, record.getSampleID());
            for (String popName : record.getPopulationNames()) {
                if (popName != null && !popName.isEmpty()) {
                    ps_insert.setString(2, popName);
                    ps_insert.executeUpdate();
                }
            }
            ps_insert.close();
            ps_get.setInt(1, record.getSampleID());
            rs_get = ps_get.executeQuery();
            List<String> popNames = new ArrayList<String>();
            while (rs_get.next()) {
                popNames.add(rs_get.getString("population_name"));
            }
            record.setPopulationNames(popNames);
            ps_get.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            SQLUtils.safeClose(ps_remove);
            SQLUtils.safeClose(ps_insert);
            SQLUtils.safeClose(ps_get);
            SQLUtils.safeClose(rs_get);
            pool.close(con);
        }
        
        //update families
        String removeFromFam = "DELETE FROM samples_in_families WHERE sample_id=?";
        String queryFam = "INSERT IGNORE INTO samples_in_families (sample_id,family_id) VALUES (?,(SELECT family_id FROM families WHERE family_name=?))";
        String getSampleFam = "SELECT family_name FROM ((samples s JOIN samples_in_families sf ON s.sample_id=sf.sample_id) JOIN families f ON f.family_id=sf.family_id) WHERE s.sample_id=?";
        PreparedStatement ps_removeFam = null;
        PreparedStatement ps_insertFam = null;
        PreparedStatement ps_getFam = null;
        ResultSet rs_getFam = null;
        Connection conFam = null;
        try {
            conFam = pool.getConnection();
            ps_removeFam = conFam.prepareStatement(removeFromFam);
            ps_removeFam.setInt(1, record.getSampleID());
            ps_removeFam.executeUpdate();
            ps_removeFam.close();
            ps_getFam = conFam.prepareStatement(getSampleFam);
            ps_insertFam = conFam.prepareStatement(queryFam);
            ps_insertFam.setInt(1, record.getSampleID());
            for (String famName : record.getFamilyNames()) {
                if (famName != null && !famName.isEmpty()) {
                    ps_insertFam.setString(2, famName);
                    ps_insertFam.executeUpdate();
                }
            }
            ps_insertFam.close();
            ps_getFam.setInt(1, record.getSampleID());
            rs_getFam = ps_getFam.executeQuery();
            List<String> famNames = new ArrayList<String>();
            while (rs_getFam.next()) {
                famNames.add(rs_getFam.getString("family_name"));
            }
            record.setFamilyNames(famNames);
            ps_getFam.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            SQLUtils.safeClose(ps_removeFam);
            SQLUtils.safeClose(ps_insertFam);
            SQLUtils.safeClose(ps_getFam);
            SQLUtils.safeClose(rs_getFam);
            pool.close(conFam);
        }
   
        
        return record;
    }

    public static List<ArrayImageRecord> getArrayImageRecord(ConnectionPool pool, String arrayImagePath, Integer startRow, Integer endRow, final String sortBy, Map<String, String> filterCriteria) {
        List<ArrayImageRecord> list = new ArrayList<ArrayImageRecord>();

        String plateName = filterCriteria.get("plateName");

        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            con = pool.getConnection();
            stmt = con.createStatement();
            String query = "SELECT sample_id,sample_name,sample_original_name,plate_coordX,plate_coordY,sample_path,plate_name,plate_original_name FROM samples JOIN plates ON samples.plate_id=plates.plate_id WHERE plate_name=\"" + plateName + "\" ORDER BY plate_coordX,plate_coordY;";
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                ArrayImageRecord record = new ArrayImageRecord();
                String thumbnailName = rs.getString("sample_original_name") + ".png";
                String thumbnailPath = arrayImagePath + "/" + rs.getString("plate_original_name") + "/" + thumbnailName;
                String imagePath = DatabaseUtils.dbDataPathToRealPath(rs.getString("sample_path")).replaceAll("\\.CEL$", ".JPG");
                record.setThumbnailPath(thumbnailPath);
                record.setSampleID(rs.getInt("sample_id"));
                record.setImage("imageProvider?file=" + thumbnailPath);
                record.setName(rs.getString("sample_name"));
                record.setServerPath(imagePath);
                record.setCoordX(rs.getInt("plate_coordX"));
                record.setCoordY(rs.getInt("plate_coordY"));
                list.add(record);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            SQLUtils.safeClose(rs);
            SQLUtils.safeClose(stmt);
            pool.close(con);
        }
        return list;
    }

    public static List<File> findSamplesWithoutQC(ConnectionPool pool) {
        List<File> samplesWithoutQC = new ArrayList<File>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String query = "SELECT sample_path FROM samples s WHERE NOT EXISTS (SELECT * FROM qc_values qc WHERE s.sample_id = qc.sample_id)";
        Connection con = null;
        try {
            con = pool.getConnection();
            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                samplesWithoutQC.add(new File(DatabaseUtils.dbDataPathToRealPath(rs.getString("sample_path"))));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            SQLUtils.safeClose(rs);
            SQLUtils.safeClose(stmt);
            pool.close(con);
        }
        return samplesWithoutQC;
    }

    public static Map<File, Double> getSamplesWithBadQC(ConnectionPool pool, List<Integer> sampleIDList, double dishQCLimit) {
        Map<File, Double> samples = new Hashtable<File, Double>();

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String query = "SELECT sample_path,qc_value FROM ((samples s JOIN qc_values qv ON s.sample_id=qv.sample_id) JOIN qc_params qp ON qp.qc_param_id=qv.qc_param_id) WHERE qc_name=\"axiom_dishqc_DQC\" AND qc_value<" + dishQCLimit + " AND s.sample_id=?";

        try {
            con = pool.getConnection();
            stmt = con.prepareStatement(query);
            for (Integer sampleID : sampleIDList) {
                stmt.setInt(1, sampleID);
                rs = stmt.executeQuery();
                while (rs.next()) {
                    samples.put(new File(DatabaseUtils.dbDataPathToRealPath(rs.getString("sample_path"))), Double.parseDouble(rs.getString("qc_value")));
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            SQLUtils.safeClose(rs);
            SQLUtils.safeClose(stmt);
            pool.close(con);
        }
        return samples;
    }

    public static List<File> getSamplesFromIDs(ConnectionPool pool, List<Integer> sampleIDList) {
        List<File> samples = new ArrayList<File>(sampleIDList.size());

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String query = "SELECT sample_path FROM samples WHERE sample_id=?";

        try {
            con = pool.getConnection();
            stmt = con.prepareStatement(query);
            for (Integer sampleID : sampleIDList) {
                stmt.setInt(1, sampleID);
                rs = stmt.executeQuery();
                while (rs.next()) {
                    samples.add(new File(DatabaseUtils.dbDataPathToRealPath(rs.getString("sample_path"))));
                }
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            SQLUtils.safeClose(rs);
            SQLUtils.safeClose(stmt);
            pool.close(con);
        }
        return samples;
    }

    public static List<SampleForDQCGraph> getSamplesForDQCGraph(ConnectionPool pool) {
        List<SampleForDQCGraph> sampleList = new ArrayList<SampleForDQCGraph>();
        Statement stmt = null;
        ResultSet rs = null;
        Connection con = null;

        String query = "SELECT s.sample_id,s.sample_name,pl.plate_id,pl.plate_name,qv.qc_value FROM (((samples s JOIN qc_values qv ON s.sample_id=qv.sample_id) JOIN qc_params qp ON qp.qc_param_id=qv.qc_param_id) JOIN plates pl ON s.plate_id=pl.plate_id) WHERE qc_name=\"axiom_dishqc_DQC\" ORDER BY pl.created ASC";

        try {
            con = pool.getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                SampleForDQCGraph bean = new SampleForDQCGraph();
                bean.setSample_id(rs.getInt("sample_id"));
                bean.setSample_name(rs.getString("sample_name"));
                bean.setPlate_id(rs.getInt("plate_id"));
                bean.setDqcValue(rs.getDouble("qc_value"));
                bean.setPlate_name(rs.getString("plate_name"));
                sampleList.add(bean);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            SQLUtils.safeClose(rs);
            SQLUtils.safeClose(stmt);
            pool.close(con);
        }
        return sampleList;
    }

    public static List<SampleForGenoQcGraph> getSamplesForGenoQcGraph(ConnectionPool pool, int genoAnalysisKey, LinkedHashMap<String, String> uniqueCelFiles, Map<String, String> changedCelFiles) {
        List<SampleForGenoQcGraph> sampleList = new ArrayList<SampleForGenoQcGraph>();
        Map<String, String> reversedChangedCelFiles = new Hashtable<String, String>(changedCelFiles.size());
        for (String oldPath : changedCelFiles.keySet()) {
            reversedChangedCelFiles.put(changedCelFiles.get(oldPath), oldPath);
        }

        Connection con = null;
        String query = "SELECT sample_id,sample_name,sample_original_name,sample_path,pl.plate_id,plate_name,plate_original_name FROM samples s JOIN plates pl ON s.plate_id=pl.plate_id WHERE sample_path=?";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = pool.getConnection();
            ps = con.prepareStatement(query);
            for (String newCelName : uniqueCelFiles.keySet()) {
                String samplePath;
                if (reversedChangedCelFiles.containsKey(uniqueCelFiles.get(newCelName))) {
                    samplePath = reversedChangedCelFiles.get(uniqueCelFiles.get(newCelName));
                } else {
                    samplePath = uniqueCelFiles.get(newCelName);
                }
                ps.setString(1, DatabaseUtils.realDataPathToDbPath(samplePath));
                rs = ps.executeQuery();
                while (rs.next()) {
                    SampleForGenoQcGraph sample = new SampleForGenoQcGraph();
                    sample.setSampleID(rs.getInt("sample_id"));
                    sample.setSampleName(rs.getString("sample_name"));
                    sample.setSampleOriginalName(rs.getString("sample_original_name"));
                    sample.setSampleNewName(newCelName);
                    sample.setSampleOriginalPath(samplePath);
                    sample.setSampleNewPath(uniqueCelFiles.get(newCelName));
                    sample.setPlateID(rs.getInt("plate_id"));
                    sample.setPlateName(rs.getString("plate_name"));
                    sample.setPlateOriginalName(rs.getString("plate_original_name"));
                    sampleList.add(sample);
                }
                rs.close();
            }
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            SQLUtils.safeClose(rs);
            SQLUtils.safeClose(ps);
            pool.close(con);
        }
        return sampleList;
    }
}
