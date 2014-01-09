package fr.pfgen.axiom.server.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import fr.pfgen.axiom.client.services.QCService;
import fr.pfgen.axiom.server.database.ConnectionPool;
import fr.pfgen.axiom.server.database.QcValuesTable;
import fr.pfgen.axiom.server.database.SamplesTable;
import fr.pfgen.axiom.server.utils.IOUtils;
import fr.pfgen.axiom.server.utils.ServerUtils;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class QCServiceImpl extends RemoteServiceServlet implements QCService {

    private static int nbFilesDone;
    private ConnectionPool pool;
    private Hashtable<String, File> appFiles;
    private File analysisFolder;

    @Override
    @SuppressWarnings("unchecked")
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        pool = (ConnectionPool) getServletContext().getAttribute("ConnectionPool");
        appFiles = (Hashtable<String, File>) getServletContext().getAttribute("ApplicationFiles");
        analysisFolder = new File(appFiles.get("analysisFile").getAbsolutePath() + "/QC_tmp");
    }

    private class QCRunningThread extends Thread {

        private ExecutorService threadExecutor;
        private File analysisFolder;

        public QCRunningThread(ExecutorService threadExecutor, File analysisFolder) {
            this.threadExecutor = threadExecutor;
            this.analysisFolder = analysisFolder;
        }

        @Override
        public void run() {
            try {
                threadExecutor.awaitTermination(12, TimeUnit.HOURS);
                ServerUtils.deleteDirectory(analysisFolder);
                removeQCAnalysisRunning();
            } catch (InterruptedException e) {
                e.printStackTrace();
                ServerUtils.deleteDirectory(analysisFolder);
                removeQCAnalysisRunning();
            }
        }
    }

    private static synchronized void incrementLog(PrintWriter log, int totalNbSamples) {
        nbFilesDone++;
        log.println("Processing " + nbFilesDone + " of " + totalNbSamples);
    }

    private class QCThread extends Thread {

        private File celFile;
        private File logFile;
        private File privateLogFile;
        private File qcFile;
        private Map<String, String> uniqueCelFiles;
        private final int totalNbSamples;

        public QCThread(File celFile, File logFile, File privateLogFile, File qcFile, Map<String, String> uniqueCelFiles, int totalNbSamples) {
            this.celFile = celFile;
            this.logFile = logFile;
            this.privateLogFile = privateLogFile;
            this.qcFile = qcFile;
            this.uniqueCelFiles = uniqueCelFiles;
            this.totalNbSamples = totalNbSamples;
        }

        @Override
        public void run() {
            Process process = null;
            BufferedReader stdout = null;
            PrintWriter privateLog = null;
            PrintWriter log = null;
            File libraryFilesFolder = new File(appFiles.get("affymetrixLibraryFilesFolder"), "axiomgwas_husnp_1_r4");
            if (!libraryFilesFolder.exists()) {
                throw new RuntimeException(libraryFilesFolder.getAbsolutePath() + " does not exist !");
            }
            File xmlConfigFile = new File(libraryFilesFolder, "Axiom_GW_Hu_SNP.r4.apt-geno-qc.AxiomQC1.xml");
            if (!xmlConfigFile.exists()) {
                throw new RuntimeException(xmlConfigFile.getAbsolutePath() + " does not exist !");
            }
            String[] cmd = {appFiles.get("APTbin").getAbsolutePath() + "/apt-geno-qc", "--analysis-files-path", libraryFilesFolder.getAbsolutePath(), "--xml-file", xmlConfigFile.getAbsolutePath(), "--cel-files", celFile.getAbsolutePath(), "--out-file", qcFile.getAbsolutePath()};
            try {
                ProcessBuilder procBuilder = new ProcessBuilder(cmd);
                procBuilder.redirectErrorStream(true);
                process = procBuilder.start();
                stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
                privateLog = new PrintWriter(new FileWriter(privateLogFile, true));
                log = new PrintWriter(new FileWriter(logFile, true));
                String line;

                while ((line = stdout.readLine()) != null) {
                    privateLog.println(line);
                    if (line.startsWith("Processing") && line.endsWith(".CEL")) {
                        incrementLog(log, totalNbSamples);
                        log.flush();
                    } else {
                        continue;
                    }
                    privateLog.flush();
                }
                privateLog.close();

                //Add results to database
                addQCresultsToDB(qcFile, uniqueCelFiles);

            } catch (IOException err) {
                //removeQCAnalysisRunning();
                throw new RuntimeException(err);
            } finally {
                if (privateLog != null) {
                    privateLog.flush();
                    privateLog.close();
                }
                if (log != null) {
                    log.flush();
                    log.close();
                }
                if (stdout != null) {
                    try {
                        stdout.close();
                    } catch (IOException err) {
                        throw new RuntimeException(err);
                    }
                }
                if (process != null) {
                    try {
                        process.destroy();
                    } catch (Exception err) {
                        throw new RuntimeException(err);
                    }
                }

                //delete files for this thread
                celFile.delete();
                privateLogFile.delete();
                qcFile.delete();
            }
        }

        private void addQCresultsToDB(File qcFile, Map<String, String> uniqueCelFiles) {
            Map<String, Map<String, String>> pathValuesMap = new Hashtable<String, Map<String, String>>();
            Map<Integer, String> indexQcNameMap = new Hashtable<Integer, String>();
            BufferedReader qc = null;
            try {
                qc = new BufferedReader(new FileReader(qcFile));
                String line;

                while ((line = qc.readLine()) != null) {
                    if (line.startsWith("#")) {
                        continue;
                    } else if (line.startsWith("cel_files")) {
                        String[] qcNames = line.split("\\t");
                        for (int i = 0; i < qcNames.length; i++) {
                            String qcName = qcNames[i];
                            indexQcNameMap.put(i, qcName);
                        }
                    } else {
                        Map<String, String> QcNameQcValueMap = new Hashtable<String, String>();
                        String[] qcValues = line.split("\\t");
                        for (int i = 0; i < qcValues.length; i++) {
                            String qcValue = qcValues[i];
                            QcNameQcValueMap.put(indexQcNameMap.get(i), qcValue);
                        }
                        String celName = QcNameQcValueMap.get("cel_files");
                        QcNameQcValueMap.remove("cel_files");
                        pathValuesMap.put(uniqueCelFiles.get(celName), QcNameQcValueMap);
                    }
                }
            } catch (IOException e) {
                //removeQCAnalysisRunning();
                throw new RuntimeException(e);
            } finally {
                if (qc != null) {
                    try {
                        qc.close();
                    } catch (IOException err) {
                    }
                }
            }
            QcValuesTable.insertQCValues(pool, pathValuesMap);
        }
    }

    @Override
    public synchronized String performQC() {
        /*synchronized (QCServiceImpl.class)
         {
         Set<Integer> analyses=(Set<Integer>)getServletContext().getAttribute("analyses-id");
         if(analyses==null) 
         {
         analyses=Collections.synchronizedSet(new HashSet<Integer>());
         getServletContext().setAttribute("analyses-id",analyses);
         }
         if(analyses.contains(1)) return "ENCOURS";
         analyses.add(1);
         }*/

        //security check if a QC analysis is already running
        if (checkMaxQCAnalysisRunning()) {
            addQCAnalysisRunning();
        } else {
            return "Maximum simultaneous QC analysis authorized is reached.\nCheck the workflow tab to see the progress.";
        }

        //create analyse folder
        analysisFolder.mkdirs();

        //get samples without QC
        List<File> samplesWithoutQC = SamplesTable.findSamplesWithoutQC(pool);
        //security check to see if list is empty
        if (samplesWithoutQC.isEmpty()) {
            ServerUtils.deleteDirectory(analysisFolder);
            removeQCAnalysisRunning();
            return "No samples found without QC";
        }

        final int totalSamples = samplesWithoutQC.size();
        final File logFile = new File(analysisFolder, "log.txt");
        List<QCThread> threads = new ArrayList<QCThread>();


        int i = 0;
        //execute apt-geno-qc by portion of 100 samples
        while (!samplesWithoutQC.isEmpty()) {
            List<File> samplesWithoutQCPortion = new ArrayList<File>(100);
            int startPoint = 0;
            int endPoint = 100;
            if (endPoint > samplesWithoutQC.size()) {
                endPoint = samplesWithoutQC.size();
            }
            for (int j = startPoint; j < endPoint; j++) {
                samplesWithoutQCPortion.add(samplesWithoutQC.get(j));
            }
            samplesWithoutQC.subList(startPoint, endPoint).clear();

            //create new celFile for samples with identical names (apt-geno-qc won't launch otherwise)
            while (!samplesWithoutQCPortion.isEmpty()) {
                Map<String, String> uniqueCelFiles = new Hashtable<String, String>();
                List<File> filesToRemove = new ArrayList<File>();
                for (File file : samplesWithoutQCPortion) {
                    if (uniqueCelFiles.containsKey(file.getName())) {
                        continue;
                    } else {
                        uniqueCelFiles.put(file.getName(), file.getAbsolutePath());
                        filesToRemove.add(file);
                    }
                }
                for (File file : filesToRemove) {
                    samplesWithoutQCPortion.remove(file);
                }

                //creating cel_list
                i++;
                final File celFile = new File(analysisFolder, "celFiles" + i + ".txt");
                BufferedWriter out = null;
                try {
                    out = new BufferedWriter(new FileWriter(celFile, true));
                    out.write("cel_files\n");
                    for (String celName : uniqueCelFiles.keySet()) {
                        out.write(uniqueCelFiles.get(celName) + "\n");
                    }
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    ServerUtils.deleteDirectory(analysisFolder);
                    removeQCAnalysisRunning();
                    return "Error\nCould not create celFiles.txt on server !!";
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException err) {
                            throw new RuntimeException(err);
                        }
                    }
                }

                //Creating log file for the stdout of apt-geno-qc and out file for report
                final File portionLogFile = new File(analysisFolder, "log" + i + ".txt");
                final File qcFile = new File(analysisFolder, "qc" + i + ".txt");
                QCThread th = new QCThread(celFile, logFile, portionLogFile, qcFile, uniqueCelFiles, totalSamples);
                threads.add(th);
            }
        }

        nbFilesDone = 0;

        ExecutorService threadExecutor = Executors.newFixedThreadPool(4);
        for (QCThread th : threads) {
            threadExecutor.execute(th);
        }
        threadExecutor.shutdown();

        QCRunningThread qcRunningThread = new QCRunningThread(threadExecutor, analysisFolder);
        qcRunningThread.start();

        return "QC launched on " + totalSamples + " samples.\nGo to workflow tab to see its progress.";
    }

    private boolean checkMaxQCAnalysisRunning() {

        Integer nbAnalysisRunning = (Integer) getServletContext().getAttribute("QCAnalysisRunning");
        int nbMaxQCAnalysisAuthorized = (Integer) getServletContext().getAttribute("MaxQCAnalysis");

        if (nbAnalysisRunning == null || nbAnalysisRunning < nbMaxQCAnalysisAuthorized) {
            return true;
        } else {
            return false;
        }
    }

    private void addQCAnalysisRunning() {
        Integer nbAnalysisRunning = (Integer) getServletContext().getAttribute("QCAnalysisRunning");
        if (nbAnalysisRunning == null) {
            getServletContext().setAttribute("QCAnalysisRunning", 1);
        } else {
            getServletContext().setAttribute("QCAnalysisRunning", nbAnalysisRunning++);
        }
    }

    private void removeQCAnalysisRunning() {
        Integer nbAnalysisRunning = (Integer) getServletContext().getAttribute("QCAnalysisRunning");
        getServletContext().setAttribute("QCAnalysisRunning", nbAnalysisRunning--);
    }

    @Override
    public int NbSamplesWithoutQC() {
        if (analysisFolder.exists()) {
            return -1;
        } else {
            return SamplesTable.findSamplesWithoutQC(pool).size();
        }
    }

    @Override
    public synchronized String QCProgress() {
        final File logFile = new File(analysisFolder, "log.txt");
        if (!logFile.exists()) {
            return "No QC analysis running";
        }

        BufferedReader br = null;
        String line = null, tmp;

        try {
            br = new BufferedReader(new FileReader(logFile));
            while ((tmp = br.readLine()) != null) {
                line = tmp;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            IOUtils.safeClose(br);
        }
        return line;
    }
}
