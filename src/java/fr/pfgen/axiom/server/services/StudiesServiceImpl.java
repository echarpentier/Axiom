package fr.pfgen.axiom.server.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import fr.pfgen.axiom.client.services.StudiesService;
import fr.pfgen.axiom.server.database.ConnectionPool;
import fr.pfgen.axiom.server.database.GenoAnalysisTable;
import fr.pfgen.axiom.server.database.GenoQcValuesTable;
import fr.pfgen.axiom.server.database.PedigreesTable;
import fr.pfgen.axiom.server.database.StudiesTable;
import fr.pfgen.axiom.server.database.StudySamplesTable;
import fr.pfgen.axiom.server.utils.Affy2Plink;
import fr.pfgen.axiom.server.utils.DatabaseUtils;
import fr.pfgen.axiom.server.utils.IOUtils;
import fr.pfgen.axiom.server.utils.ServerUtils;
import fr.pfgen.axiom.shared.GenericGwtRpcList;
import fr.pfgen.axiom.shared.records.GenotypingQCRecord;
import fr.pfgen.axiom.shared.records.PedigreeRecord;
import fr.pfgen.axiom.shared.records.StudyRecord;
import fr.pfgen.axiom.shared.records.StudyWorkflowState;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class StudiesServiceImpl extends RemoteServiceServlet implements StudiesService {

    private ConnectionPool pool;
    private Hashtable<String, File> appFiles;

    @Override
    @SuppressWarnings("unchecked")
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        pool = (ConnectionPool) getServletContext().getAttribute("ConnectionPool");
        appFiles = (Hashtable<String, File>) getServletContext().getAttribute("ApplicationFiles");
    }

    @Override
    public List<String> getStudyNames(String type) {
        return StudiesTable.getStudyNames(pool, type);
    }

    @Override
    public List<StudyRecord> fetch(Integer startRow, Integer endRow, String sortBy, Map<String, String> filterCriteria) {

        GenericGwtRpcList<StudyRecord> outList = new GenericGwtRpcList<StudyRecord>();

        String query = StudiesTable.constructQuery(sortBy, filterCriteria);

        outList.setTotalRows(DatabaseUtils.countRowInQuery(pool, query, false));

        List<StudyRecord> out = StudiesTable.getStudies(pool, query, startRow, endRow);

        outList.addAll(out);

        return outList;
    }

    @Override
    public StudyRecord add(StudyRecord data) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StudyRecord update(StudyRecord data) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void remove(StudyRecord data) {
        String studyPath = StudiesTable.removeStudy(pool, data);
        if (studyPath != null && !studyPath.isEmpty()) {
            ServerUtils.deleteDirectory(new File(studyPath));
        } else {
            throw new RuntimeException("Cannot remove study " + data.getStudyName() + " from database !!");
        }
    }

    @Override
    public String download(String sortBy, Map<String, String> filterCriteria) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String addNewStudy(StudyRecord record) {
        String studyName = record.getStudyName();
        String studyType = record.getStudyType();
        if (studyName == null || studyName.equals("")) {
            return "Error: incorrect study name";
        }

        if (studyType == null || studyType.equals("")) {
            return "Error: incorrect study type";
        }

        if (!studyType.equals("family") && !studyType.equals("case-control")) {
            return "Error: study type does not exist";
        }

        for (String name : StudiesTable.getStudyNames(pool, null)) {
            if (studyName.equals(name)) {
                return "Error: study name already exists";
            }
        }

        File studyFolder = new File(appFiles.get("analysisFile"), "study_" + studyName);
        record.setStudyPath(studyFolder.getAbsolutePath());
        record = StudiesTable.addNewStudy(pool, record);
        if (record != null) {
            if (!studyFolder.mkdir()) {
                throw new RuntimeException("Cannot create " + studyFolder.getAbsolutePath());
            }
        } else {
            return "Error: cannot insert study in database";
        }

        return "New study added !!";
    }

    @Override
    public String addGenoAnalysisToStudy(String studyName, List<String> genoNameList) {
        return StudiesTable.addGenoAnalysisToStudy(pool, studyName, genoNameList);
    }

    @Override
    public StudyWorkflowState checkWorkflowState(String studyName) {
        File studyFolder = StudiesTable.getStudyFolder(pool, studyName);
        StudyWorkflowState states = new StudyWorkflowState();
        //states.set
        states.setSamplesDone(checkGenoSamplesForStudy(studyName));
        File pedigreeFolder = new File(studyFolder, "Pedigree");
        File finalPedFile = new File(pedigreeFolder, "final_pedigree.txt");
        if (finalPedFile.exists()) {
            states.setPedigreeDone(true);
        } else {
            states.setPedigreeDone(false);
        }
        states.setPlinkFilesDone(checkPlinkFilesInStudy(studyName));


        return states;
    }

    @Override
    public Boolean checkGenoSamplesForStudy(String studyName) {
        return StudiesTable.checkGenoAnalysisInStudy(pool, studyName);
    }

    @Override
    public String addSamplesToStudy(String studyName, List<Integer> genoRunIdList) {
        File studyFolder = new File(appFiles.get("analysisFile"), "study_" + studyName);
        File samplesFolder = new File(studyFolder, "Samples");
        if (!samplesFolder.mkdir()) {
            throw new RuntimeException("Cannot create " + samplesFolder.getAbsolutePath());
        }
        String callback = StudySamplesTable.addSamplesToStudy(pool, studyName, genoRunIdList);
        if (callback == null || callback.isEmpty() || callback.startsWith("Error")) {
            ServerUtils.deleteDirectory(samplesFolder);
            return callback;
        }

        Map<String, String> criterias = new Hashtable<String, String>();
        criterias.put("study_name", studyName);
        String query = GenoQcValuesTable.constructQuery("sample_name", criterias);
        List<GenotypingQCRecord> samplesList = GenoQcValuesTable.getSamplesQC(pool, query, null, null);

        File samplesFile = new File(samplesFolder, "samples.txt");

        PrintWriter samplesPW = null;

        try {
            samplesPW = new PrintWriter(new FileWriter(samplesFile));
            samplesPW.println("sampleID\tsampleName\tplateName\tpopulations\tgenoID\tgenoName\tgenoRunID\trun\tcallrate");
            for (GenotypingQCRecord rec : samplesList) {
                String pops = "";
                if (rec.getPopulationNames() != null) {
                    StringBuilder popNames = new StringBuilder();
                    for (String popName : rec.getPopulationNames()) {
                        popNames.append(popName + ",");
                    }
                    pops = popNames.toString().replaceAll(",$", "");
                }
                String callrate = rec.getQcMap().get("call_rate");
                samplesPW.println(rec.getSampleID() + "\t" + rec.getSampleName() + "\t" + rec.getPlateName() + "\t" + pops + "\t" + rec.getGenoID() + "\t" + rec.getGenoName() + "\t" + rec.getGenoRunID() + "\t" + rec.getRun() + "\t" + callrate);
            }
            samplesPW.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (samplesPW != null) {
                samplesPW.close();
            }
        }


        return callback;
    }

    @Override
    public Boolean checkPlinkFilesInStudy(String studyName) {
        File studyFolder = StudiesTable.getStudyFolder(pool, studyName);
        File plinkFolder = new File(studyFolder, "Plink");
        if (!plinkFolder.exists()) {
            return false;
        }
        //File bed = new File(plinkFolder, studyName+".bed");
        //File bim = new File(plinkFolder, studyName+".bim");
        //File fam = new File(plinkFolder, studyName+".fam");
        //if (bed.exists() && bim.exists() && fam.exists()){
        File tped = new File(plinkFolder, studyName + ".tped");
        File tfam = new File(plinkFolder, studyName + ".tfam");
        if (tped.exists() && tfam.exists()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String generatePlinkFilesForStudy(String studyName, String annotFileName, Map<String, GenotypingQCRecord> samplesChosen) {
        File studyFolder = StudiesTable.getStudyFolder(pool, studyName);
        if (studyFolder == null) {
            return "Cannot find study folder for " + studyName;
        }

        File plinkFolder = new File(studyFolder, "Plink");
        if (!plinkFolder.exists()) {
            plinkFolder.mkdir();
        }

        File log = new File(plinkFolder, "log.txt");
        File sampleNamesToCallNames = new File(plinkFolder, "samples2calls.txt");
        if (log.exists() || sampleNamesToCallNames.exists()) {
            return "Plink files are already being generated";
        }

        Map<String, String> criterias = new HashMap<String, String>();
        criterias.put("study_name", studyName);

        //get all individual names in pedigree for this study
        String query = PedigreesTable.constructQuery(null, criterias);
        List<PedigreeRecord> pedRecList = PedigreesTable.getPedigreeRecordsInStudy(pool, query);

        //get all genotyped records in study
        query = GenoQcValuesTable.constructQuery(null, criterias);
        List<GenotypingQCRecord> genoQcRecList = GenoQcValuesTable.getSamplesQC(pool, query, null, null);

        //check if no more than one sample in calls correspond to individual ID and create map sample_name to geno_qc_record
        Map<String, GenotypingQCRecord> sampleName2genoQcRec = new HashMap<String, GenotypingQCRecord>();
        List<GenotypingQCRecord> tmpList = new ArrayList<GenotypingQCRecord>();
        for (PedigreeRecord pedRec : pedRecList) {
            tmpList.clear();
            for (GenotypingQCRecord genoQcRec : genoQcRecList) {
                if (pedRec.getIndividualID().equals(genoQcRec.getSampleName())) {
                    tmpList.add(genoQcRec);
                }
            }
            if (tmpList.size() > 1) {
                if (samplesChosen == null || !samplesChosen.containsKey(pedRec.getIndividualID())) {
                    throw new RuntimeException(pedRec.getIndividualID() + " is found more than once in calls and has not been chosen !!");
                }
                sampleName2genoQcRec.put(pedRec.getIndividualID(), samplesChosen.get(pedRec.getIndividualID()));
            } else if (tmpList.size() == 1) {
                sampleName2genoQcRec.put(pedRec.getIndividualID(), tmpList.get(0));
            }
        }

        //create map sample_name to sampleInCalls in order to create file of corresponding samples for Affy2Plink
        Map<String, SamplesInCalls> sampleName2sampleInCalls = new HashMap<String, SamplesInCalls>(sampleName2genoQcRec.size());
        for (String sampleName : sampleName2genoQcRec.keySet()) {
            GenotypingQCRecord rec = sampleName2genoQcRec.get(sampleName);
            SamplesInCalls sc = new SamplesInCalls();
            sc.sampleID = rec.getSampleID();
            File analysisFolder = new File(GenoAnalysisTable.getGenoPathFromName(pool, rec.getGenoName()));
            if (!analysisFolder.exists()) {
                return "Cannot find " + analysisFolder.getAbsolutePath();
            }
            sc.genoAnalysisPath = analysisFolder.getAbsolutePath();
            File callsFile;
            if (rec.getRun().equals("first")) {
                callsFile = new File(analysisFolder.getAbsolutePath() + "/First_run/AxiomGT1.calls.txt");
                if (!callsFile.exists()) {
                    callsFile = new File(analysisFolder.getAbsolutePath() + "/First_run/AxiomGT1.calls.txt.gz");
                    if (!callsFile.exists()) {
                        return "Cannot find callFile for first run of " + analysisFolder.getAbsolutePath();
                    }
                }
            } else if (rec.getRun().equals("second")) {
                callsFile = new File(analysisFolder.getAbsolutePath() + "/Second_run/AxiomGT1.calls.txt");
                if (!callsFile.exists()) {
                    callsFile = new File(analysisFolder.getAbsolutePath() + "/Second_run/AxiomGT1.calls.txt.gz");
                    if (!callsFile.exists()) {
                        return "Cannot find callFile for second run of " + analysisFolder.getAbsolutePath();
                    }
                }
            } else {
                return "Cannot find run for geno_run_id " + rec.getGenoRunID() + " in database";
            }
            sc.callsPath = callsFile.getAbsolutePath();

            sampleName2sampleInCalls.put(sampleName, sc);
        }

        Map<String, List<SamplesInCalls>> genoAnalysisPath2MapSampleNameTocalls = new HashMap<String, List<SamplesInCalls>>();
        for (String sampleName : sampleName2sampleInCalls.keySet()) {
            SamplesInCalls sc = sampleName2sampleInCalls.get(sampleName);
            if (genoAnalysisPath2MapSampleNameTocalls.containsKey(sc.genoAnalysisPath)) {
                genoAnalysisPath2MapSampleNameTocalls.get(sc.genoAnalysisPath).add(sc);
            } else {
                List<SamplesInCalls> l = new ArrayList<SamplesInCalls>();
                l.add(sc);
                genoAnalysisPath2MapSampleNameTocalls.put(sc.genoAnalysisPath, l);
            }
        }

        BufferedReader br = null;
        String line;
        Map<Integer, String> sampleName2SampleNewName = new HashMap<Integer, String>();
        for (String analysisPath : genoAnalysisPath2MapSampleNameTocalls.keySet()) {
            sampleName2SampleNewName.clear();
            try {
                br = IOUtils.openFile(new File(analysisPath + "/QCReport/samples_plates.txt"));
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("sampleID") || line.equals("")) {
                        continue;
                    }
                    String[] linesplit = line.split("\\t");
                    sampleName2SampleNewName.put(Integer.parseInt(linesplit[0]), linesplit[3]);
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtils.safeClose(br);
            }
            for (SamplesInCalls sc : genoAnalysisPath2MapSampleNameTocalls.get(analysisPath)) {
                sc.sampleNameInCalls = sampleName2SampleNewName.get(sc.sampleID);
            }
        }

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileWriter(sampleNamesToCallNames));
            for (String sampleName : sampleName2sampleInCalls.keySet()) {
                SamplesInCalls sc = sampleName2sampleInCalls.get(sampleName);
                String callsPath = sc.callsPath;
                String sampleNameInCalls = sc.sampleNameInCalls;
                pw.println(callsPath + "\t" + sampleName + "\t" + sampleNameInCalls);
            }
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.safeClose(pw);
        }

        File annotFile = new File(appFiles.get("affymetrixAnnotationFilesFolder"), annotFileName);
        String plinkFileName = studyName;
        File pedigree = new File(studyFolder.getAbsolutePath() + "/Pedigree/final_pedigree.txt");
        if (!pedigree.exists() || !pedigree.canRead()) {
            throw new RuntimeException("Cannot access " + pedigree.getAbsolutePath());
        }
        int geneticMapConsortium = 0;

        try {
            pw = new PrintWriter(new FileWriter(log), true);
            pw.println("Starting Affy2Plink");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Affy2PlinkThread t = new Affy2PlinkThread(annotFile, plinkFolder, plinkFileName, sampleNamesToCallNames, pedigree, geneticMapConsortium, pw);
        t.start();

        return "Affy2Plink launched !!";
    }

    public StudyRecord getStudyInfos(String studyName) {
        return StudiesTable.getStudyInfos(pool, studyName);
    }

    private class SamplesInCalls {

        public int sampleID;
        public String callsPath;
        public String genoAnalysisPath;
        public String sampleNameInCalls;
    }

    private class Affy2PlinkThread extends Thread {

        private File annotCSV;
        private File plinkFolder;
        private String plinkFileName;
        private File sampleNamesToCallNames;
        private File pedigree;
        private int geneticMapConsortium;
        private PrintWriter logPW;

        public Affy2PlinkThread(File annotCSV, File plinkFolder, String plinkFileName, File sampleNamesToCallNames, File pedigree, int geneticMapConsortium, PrintWriter logPW) {
            this.annotCSV = annotCSV;
            this.plinkFolder = plinkFolder;
            this.plinkFileName = plinkFileName;
            this.sampleNamesToCallNames = sampleNamesToCallNames;
            this.pedigree = pedigree;
            this.geneticMapConsortium = geneticMapConsortium;
            this.logPW = logPW;
        }

        @Override
        public void run() {
            try {
                new Affy2Plink(annotCSV, plinkFolder, plinkFileName, sampleNamesToCallNames, pedigree, geneticMapConsortium, logPW);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String Affy2PlinkProgress(String studyName) {
        File studyFolder = StudiesTable.getStudyFolder(pool, studyName);
        File plinkFolder = new File(studyFolder, "Plink");
        File logFile = new File(plinkFolder, "log.txt");

        if (!logFile.exists()) {
            return null;
        }

        BufferedReader br = null;
        String line;
        StringBuilder sb = new StringBuilder();
        try {
            br = new BufferedReader(new FileReader(logFile));
            while ((line = br.readLine()) != null) {
                sb.append(line.replaceAll("\\s", "&nbsp;").replaceAll("/", "&frasl;") + "<br>");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            IOUtils.safeClose(br);
        }
        return sb.toString();
    }
}
