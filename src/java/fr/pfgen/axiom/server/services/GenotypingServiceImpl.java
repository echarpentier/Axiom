package fr.pfgen.axiom.server.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import fr.pfgen.axiom.client.services.GenotypingService;
import fr.pfgen.axiom.server.beans.AnnotProbe;
import fr.pfgen.axiom.server.beans.SampleForGenoQcGraph;
import fr.pfgen.axiom.server.database.ConnectionPool;
import fr.pfgen.axiom.server.database.GenoAnalysisTable;
import fr.pfgen.axiom.server.database.GenoQcValuesTable;
import fr.pfgen.axiom.server.database.GenoSamplesTable;
import fr.pfgen.axiom.server.database.SamplesTable;
import fr.pfgen.axiom.server.utils.AnnotFileReader;
import fr.pfgen.axiom.server.utils.IOUtils;
import fr.pfgen.axiom.server.utils.ServerUtils;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import fr.pfgen.axiom.server.database.UsersTable;
import fr.pfgen.axiom.shared.records.RunningGenotypingAnalysis;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

@SuppressWarnings("serial")
public class GenotypingServiceImpl extends RemoteServiceServlet implements GenotypingService {

    private ConnectionPool pool;
    private Hashtable<String, File> appFiles;

    @SuppressWarnings("unchecked")
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        pool = (ConnectionPool) getServletContext().getAttribute("ConnectionPool");
        appFiles = (Hashtable<String, File>) getServletContext().getAttribute("ApplicationFiles");
    }

    private class chpToTxtThread extends Thread {

        private File chpFolder;

        public chpToTxtThread(File chpFolder) {
            this.chpFolder = chpFolder;
        }

        @Override
        public void run() {
            File[] chpPaths = chpFolder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if (name.endsWith(".AxiomGT1.chp")) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            File chpFile = new File(chpFolder, "chpFiles.txt");
            PrintWriter pw = null;

            try {
                pw = new PrintWriter(new FileWriter(chpFile));
                pw.println("chp_files");

                for (File chp : chpPaths) {
                    pw.println(chp.getAbsolutePath());
                }
                pw.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtils.safeClose(pw);
            }

            String[] cmd = {appFiles.get("APTbin").getAbsolutePath() + "/apt-chp-to-txt", "--chp-files", chpFile.getAbsolutePath(), "--out-dir", chpFolder.getAbsolutePath()};
            Process process = null;
            FileOutputStream outt = null;
            InputStream inn = null;
            File stdoutFile = new File(chpFolder, "stdout.txt");
            try {
                ProcessBuilder procBuilder = new ProcessBuilder(cmd);
                procBuilder.redirectErrorStream(true);

                process = procBuilder.start();
                inn = process.getInputStream();
                outt = new FileOutputStream(stdoutFile);
                byte buffer[] = new byte[2048];
                int nRead;
                while ((nRead = inn.read(buffer)) != -1) {
                    outt.write(buffer, 0, nRead);
                }
                inn.close();
                outt.close();
                process.waitFor();
                process.destroy();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                IOUtils.safeClose(inn);
                IOUtils.safeClose(outt);
                if (process != null) {
                    try {
                        process.destroy();
                    } catch (Exception err) {
                    }
                }
            }

            File[] chpTxt = chpFolder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if (name.endsWith(".AxiomGT1.chp.txt")) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            if (chpTxt.length == chpPaths.length) {
                for (File chp : chpPaths) {
                    chp.delete();
                }
                File logFile = new File(chpFolder, "apt-chp-to-txt.log");
                if (logFile.exists()) {
                    logFile.delete();
                }
                if (stdoutFile.exists()) {
                    stdoutFile.delete();
                }
                chpFile.delete();
            } else {
                throw new RuntimeException("CHPs have not all been transformed to TXT in " + chpFolder.getAbsolutePath());
            }

            GZIPOutputStream out = null;
            FileInputStream in = null;

            try {
                for (File txt : chpTxt) {
                    File gzipTxt = new File(chpFolder, txt.getName() + ".gz");
                    out = new GZIPOutputStream(new FileOutputStream(gzipTxt));
                    in = new FileInputStream(txt);
                    IOUtils.copyTo(in, out);
                    in.close();
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtils.safeClose(in);
                IOUtils.safeClose(out);
            }

            File[] chpZip = chpFolder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if (name.endsWith("AxiomGT1.chp.txt.gz")) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            if (chpTxt.length == chpZip.length) {
                for (File chptxt : chpTxt) {
                    chptxt.delete();
                }
            } else {
                throw new RuntimeException("CHP.TXTs have not all been gzipped in " + chpFolder.getAbsolutePath());
            }
        }
    }

    private class GenotypingThread extends Thread {

        private File finalAnalysisFolder;
        private final List<Integer> sampleIDList;
        private String genotypingName;
        private double dishQCLimit;
        private double callRateLimit;
        private int userID;
        private File annotationFile;
        private File libraryFilesFolder;
        private File xmlGenoConfigFile;
        private RunningGenotypingAnalysis runningAnalysis;

        public GenotypingThread(File finalAnalysisFolder, List<Integer> sampleIDList, String genotypingName, double dishQCLimit, double callRateLimit, int userID, String libraryFilesFolder, String annotationFile, RunningGenotypingAnalysis runningAnalysis) {
            this.finalAnalysisFolder = finalAnalysisFolder;
            this.sampleIDList = sampleIDList;
            this.genotypingName = genotypingName;
            this.dishQCLimit = dishQCLimit;
            this.callRateLimit = callRateLimit;
            this.userID = userID;
            this.runningAnalysis = runningAnalysis;
            File libFilesFol = new File(appFiles.get("affymetrixLibraryFilesFolder"), libraryFilesFolder);
            if (!libFilesFol.exists() || !libFilesFol.canRead()) {
                throw new RuntimeException("Cannot read " + libFilesFol.getAbsolutePath());
            }
            this.libraryFilesFolder = libFilesFol;

            File xmlFile = libFilesFol.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith("apt-probeset-genotype.AxiomGT1.xml");
                }
            })[0];
            if (!xmlFile.exists() || !xmlFile.canRead()) {
                throw new RuntimeException("Cannot read " + xmlFile.getAbsolutePath());
            }
            this.xmlGenoConfigFile = xmlFile;

            File annotFile = new File(appFiles.get("affymetrixAnnotationFilesFolder"), annotationFile);
            if (!annotFile.exists() || !annotFile.canRead()) {
                throw new RuntimeException("Cannot read " + annotFile.getAbsolutePath());
            }
            this.annotationFile = annotFile;
        }

        @Override
        public void run() {
            final File logFile = new File(finalAnalysisFolder, "log.txt");
            final File stdoutFile = new File(finalAnalysisFolder, "stdout.txt");

            Process process = null;
            BufferedReader stdoutBR = null;
            BufferedReader reportBR = null;
            PrintWriter logPW = null;
            PrintWriter celFilePW = null;
            PrintWriter s_pPW = null;
            PrintWriter pw = null;
            File renamedCelsDir = null;
            File celFile;

            InputStream in = null;
            OutputStream out = null;

            try {
                //get all samples specified
                logPW = new PrintWriter(new FileWriter(logFile, false), true);
                logPW.println("GENOTYPING ANALYSIS: " + genotypingName);
                logPW.println("Starting time: " + new Date());
                logPW.println("DishQC specified: " + dishQCLimit);
                logPW.println("CallRate specified: " + callRateLimit);

                int genoAnalysisKey = insertGenotypingAnalysisInDB(genotypingName, userID, finalAnalysisFolder, dishQCLimit, callRateLimit, libraryFilesFolder, annotationFile);

                logPW.println("Collecting samples for genotyping...");
                List<File> samples = SamplesTable.getSamplesFromIDs(pool, sampleIDList);
                logPW.println("\tfound " + samples.size() + " CEL files.");
                logPW.println("DONE");

                insertGenotypingSamplesInDB(genoAnalysisKey);

                //rename samples that have same name
                logPW.println("Renaming cels to avoid identical names...");
                LinkedHashMap<String, String> uniqueCelFiles = new LinkedHashMap<String, String>();
                Map<String, String> changedCelFiles = new Hashtable<String, String>();
                int i = 0;
                for (File file : samples) {
                    i++;
                    if (uniqueCelFiles.containsKey(file.getName())) {
                        String oldPath = file.getAbsolutePath();
                        String newPath = finalAnalysisFolder.getAbsolutePath() + "/renamedCels/" + i + "__" + file.getName();
                        changedCelFiles.put(oldPath, newPath);
                        uniqueCelFiles.put(i + "__" + file.getName(), newPath);
                    } else {
                        uniqueCelFiles.put(file.getName(), file.getAbsolutePath());
                    }
                }
                if (changedCelFiles.size() > 0) {
                    renamedCelsDir = new File(finalAnalysisFolder, "renamedCels");
                    if (!renamedCelsDir.mkdir()) {
                        throw new RuntimeException("Cannot create " + renamedCelsDir.getAbsolutePath());
                    }
                    for (String oldPath : changedCelFiles.keySet()) {
                        IOUtils.copy(oldPath, changedCelFiles.get(oldPath));
                        logPW.println("\tcopying " + oldPath + " to " + changedCelFiles.get(oldPath));
                    }
                }

                //writing "complete_cel_file_list.txt" containing all files chosen by user
                logPW.println("Writing 'complete_cel_file_list.txt' in main analysis folder...");
                celFile = new File(finalAnalysisFolder, "complete_cel_file_list.txt");
                celFilePW = new PrintWriter(new FileWriter(celFile));
                celFilePW.println("cel_files");
                for (String celName : uniqueCelFiles.keySet()) {
                    celFilePW.println(uniqueCelFiles.get(celName));
                }
                celFilePW.flush();
                celFilePW.close();
                logPW.println("DONE");

                //writing "samples_plates.txt" in QCReport folder
                logPW.println("Writing \"samples_plates.txt\" in \"QCReport\" folder...");
                File qc_report_folder = new File(finalAnalysisFolder, "QCReport");
                if (!qc_report_folder.mkdir()) {
                    throw new RuntimeException("Cannot create " + qc_report_folder.getAbsolutePath());
                }
                File samples_plates_file = new File(qc_report_folder, "samples_plates.txt");
                s_pPW = new PrintWriter(new FileWriter(samples_plates_file));
                List<SampleForGenoQcGraph> genoSampleList = SamplesTable.getSamplesForGenoQcGraph(pool, genoAnalysisKey, uniqueCelFiles, changedCelFiles);
                s_pPW.println("sampleID\tsampleName\tsampleOriginalName\tsampleNewName\tsampleOriginalPath\tsampleNewPath\tplateID\tplateName\tplateOriginalName");
                for (SampleForGenoQcGraph s : genoSampleList) {
                    s_pPW.println(s.getSampleID() + "\t" + s.getSampleName() + "\t" + s.getSampleOriginalName() + "\t" + s.getSampleNewName() + "\t" + s.getSampleOriginalPath() + "\t" + s.getSampleNewPath() + "\t" + s.getPlateID() + "\t" + s.getPlateName() + "\t" + s.getPlateOriginalName());
                }
                s_pPW.flush();
                s_pPW.close();
                logPW.println("DONE");

                //checking dishqc limit
                logPW.println("Checking for DishQC limit entered...");
                if (dishQCLimit > 0) {
                    logPW.println("\tdishQC of " + dishQCLimit + " specified by user.");
                    celFile = new File(finalAnalysisFolder, "cel_file_list_passQC.txt");
                    logPW.println("\t" + celFile.getName() + " will be used for the run.");
                    logPW.println("\tchecking for samples with DISHQC<" + dishQCLimit + "...");

                    //get samples below dishQC value
                    Map<File, Double> samplesBadQC = SamplesTable.getSamplesWithBadQC(pool, sampleIDList, dishQCLimit);
                    logPW.println("\tfound " + samplesBadQC.size() + " samples below dishQC limit.");
                    if (samplesBadQC.size() > 0) {
                        //remove samples with bad dishqc before writing new celFile.txt
                        logPW.println("\tremoving samples below dishQC limit.");
                        for (File cel : samplesBadQC.keySet()) {
                            if (changedCelFiles.containsKey(cel.getAbsolutePath())) {
                                File f = new File(changedCelFiles.get(cel.getAbsolutePath()));
                                logPW.println("\t\t" + f.getAbsolutePath() + " = " + samplesBadQC.get(cel));
                                uniqueCelFiles.remove(f.getName());
                            } else {
                                logPW.println("\t\t" + cel.getAbsolutePath() + " = " + samplesBadQC.get(cel));
                                uniqueCelFiles.remove(cel.getName());
                            }
                        }
                    }
                    logPW.println("\twriting '" + celFile.getName() + "'...");
                    celFilePW = new PrintWriter(new FileWriter(celFile));
                    celFilePW.println("cel_files");
                    for (String celName : uniqueCelFiles.keySet()) {
                        celFilePW.println(uniqueCelFiles.get(celName));
                    }
                    celFilePW.flush();
                    celFilePW.close();
                    logPW.println("DONE");
                } else {
                    logPW.println("\tno DishQC limit specified by user.");
                    logPW.println("\t" + celFile.getName() + " will be used for the run.");
                }

                //First genotyping
                File resultFolder = new File(finalAnalysisFolder, "First_run");
                if (!resultFolder.mkdir()) {
                    throw new RuntimeException("Cannot create " + resultFolder.getAbsolutePath());
                }
                if (callRateLimit > 0) {
                    logPW.println("Performing 1st genotyping...");
                } else {
                    logPW.println("Performing unique genotyping...");
                }
                runningAnalysis.setStatus(RunningGenotypingAnalysis.GenoAnaRunningStatus.FIRST);
                File reportFile = new File(resultFolder, "AxiomGT1.report.txt");
                File chpFolder = new File(resultFolder, "CHP");
                if (!chpFolder.mkdir()) {
                    throw new RuntimeException("Cannot create " + chpFolder.getAbsolutePath());
                }
                String[] cmd = {appFiles.get("APTbin").getAbsolutePath() + "/apt-probeset-genotype", "--analysis-files-path", libraryFilesFolder.getAbsolutePath(), "--xml-file", xmlGenoConfigFile.getAbsolutePath(), "--cel-files", celFile.getAbsolutePath(), "--out-dir", resultFolder.getAbsolutePath(), "--cc-chp-output", "--cc-chp-out-dir", chpFolder.getAbsolutePath(), "--write-models"};

                //launch genotyping (first or unique)
                logPW.println("\t" + Arrays.toString(cmd));
                ProcessBuilder procBuilder = new ProcessBuilder(cmd);
                procBuilder.redirectErrorStream(true);
                process = procBuilder.start();

                in = process.getInputStream();
                out = new FileOutputStream(stdoutFile);
                IOUtils.copyTo(in, out);
                in.close();
                out.close();
                process.destroy();

                logPW.println("DONE");
                chpToTxtThread t = new chpToTxtThread(chpFolder);
                t.start();

                logPW.println("Inserting results in database...");
                insertGenotypingQcInDB(uniqueCelFiles, changedCelFiles, resultFolder, genoAnalysisKey, "first");
                logPW.println("DONE");

                //second genotyping if necessary
                if (callRateLimit > 0) {
                    logPW.println("Checking if second genotyping is necessary...");

                    logPW.println("\tcall rate of " + callRateLimit + " specified by user.");
                    //check if second genotyping is necessary
                    Map<String, String> samplesWithBadCallRate = new Hashtable<String, String>();
                    int callRateIndex = -1;

                    reportBR = new BufferedReader(new FileReader(reportFile));
                    String line;
                    while ((line = reportBR.readLine()) != null) {
                        if (line.startsWith("#")) {
                            continue;
                        }
                        if (line.startsWith("cel_files")) {
                            String[] header = line.split("\\t");
                            for (i = 0; i < header.length; i++) {
                                if (header[i].equals("call_rate")) {
                                    callRateIndex = i;
                                    break;
                                }
                            }
                            continue;
                        }
                        String[] QCline = line.split("\\t");
                        if (Double.parseDouble(QCline[callRateIndex]) < callRateLimit) {
                            samplesWithBadCallRate.put(QCline[0], QCline[callRateIndex]);
                        }
                    }
                    reportBR.close();

                    logPW.println("\tfound " + samplesWithBadCallRate.size() + " samples with bad call rate.");

                    if (samplesWithBadCallRate.size() > 0) {
                        //print bad call rate samples in log and remove from list
                        logPW.println("\tremoving samples below call rate limit.");
                        for (String celName : samplesWithBadCallRate.keySet()) {
                            logPW.println("\t\t" + uniqueCelFiles.get(celName) + " = " + samplesWithBadCallRate.get(celName));
                            uniqueCelFiles.remove(celName);
                        }

                        String celFileName = celFile.getName().replaceAll("\\.txt$", "");
                        celFile = new File(celFile.getParent(), celFileName + "_passCR.txt");
                        celFilePW = new PrintWriter(new FileWriter(celFile), false);
                        celFilePW.println("cel_files");
                        logPW.println("\twriting '" + celFile.getName() + "'...");
                        for (String celName : uniqueCelFiles.keySet()) {
                            celFilePW.println(uniqueCelFiles.get(celName));
                        }
                        celFilePW.flush();
                        celFilePW.close();

                        logPW.println("DONE\nPerforming second genotyping...");
                        runningAnalysis.setStatus(RunningGenotypingAnalysis.GenoAnaRunningStatus.SECOND);
                        resultFolder = new File(finalAnalysisFolder, "Second_run");
                        if (!resultFolder.mkdir()) {
                            throw new RuntimeException("Cannot create " + resultFolder.getAbsolutePath());
                        }
                        reportFile = new File(resultFolder, "AxiomGT1.report.txt");
                        chpFolder = new File(resultFolder, "CHP");
                        if (!chpFolder.mkdir()) {
                            throw new RuntimeException("Cannot create " + chpFolder.getAbsolutePath());
                        }
                        String[] cmdSecondGenotyping = {appFiles.get("APTbin").getAbsolutePath() + "/apt-probeset-genotype", "--analysis-files-path", libraryFilesFolder.getAbsolutePath(), "--xml-file", xmlGenoConfigFile.getAbsolutePath(), "--cel-files", celFile.getAbsolutePath(), "--out-dir", resultFolder.getAbsolutePath(), "--cc-chp-output", "--cc-chp-out-dir", chpFolder.getAbsolutePath(), "--write-models"};
                        logPW.println("\t" + Arrays.toString(cmdSecondGenotyping));

                        procBuilder = new ProcessBuilder(cmdSecondGenotyping);
                        procBuilder.redirectErrorStream(true);

                        process = procBuilder.start();
                        in = process.getInputStream();
                        out = new FileOutputStream(stdoutFile, true);
                        IOUtils.copyTo(in, out);
                        in.close();
                        out.close();
                        process.destroy();

                        t = new chpToTxtThread(chpFolder);
                        t.start();

                        logPW.println("Inserting results in database...");
                        insertGenotypingQcInDB(uniqueCelFiles, changedCelFiles, resultFolder, genoAnalysisKey, "second");
                        logPW.println("DONE");

                    } else {
                        logPW.println("DONE");
                    }
                }

                logPW.println("Creating probeAnnot.txt...");
                AnnotFileReader annotReader = new AnnotFileReader(annotationFile);
                Map<String, AnnotProbe> annotProbes = annotReader.getAnnotations(null);
                File annotProbeFile = new File(qc_report_folder.getAbsolutePath() + "/annotProbes.txt");
                try {
                    pw = new PrintWriter(new FileWriter(annotProbeFile));
                    pw.println(AnnotProbe.getHeaderLine());
                    for (String probeName : annotProbes.keySet()) {
                        pw.println(annotProbes.get(probeName).toString());
                    }
                    pw.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    IOUtils.safeClose(pw);
                }
                logPW.println("DONE");

                logPW.println("Creating metrics.txt...");
                runningAnalysis.setStatus(RunningGenotypingAnalysis.GenoAnaRunningStatus.METRICS);
                String[] cmdMetrics = {appFiles.get("RScriptBin").getAbsolutePath(), appFiles.get("RScripts").getAbsolutePath() + "/CreateMetrics.R", resultFolder.getAbsolutePath() + "/AxiomGT1.snp-posteriors.txt", resultFolder.getAbsolutePath() + "/AxiomGT1.calls.txt", annotProbeFile.getAbsolutePath(), qc_report_folder.getAbsolutePath() + "/metrics.txt"};
                procBuilder = new ProcessBuilder(cmdMetrics);
                procBuilder.redirectErrorStream(true);

                process = procBuilder.start();
                stdoutBR = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = stdoutBR.readLine()) != null) {
                    logPW.println("\t" + line);
                }
                logPW.println("DONE");

                logPW.println("Ending time: " + new Date());
                logPW.println("ALL DONE");
                logPW.close();

            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                IOUtils.safeClose(celFilePW);
                IOUtils.safeClose(logPW);
                IOUtils.safeClose(pw);
                IOUtils.safeClose(stdoutBR);
                IOUtils.safeClose(in);
                IOUtils.safeClose(out);
                IOUtils.safeClose(reportBR);
                if (process != null) {
                    try {
                        process.destroy();
                    } catch (Exception err) {
                        throw new RuntimeException(err);
                    }
                }
                if (renamedCelsDir != null && renamedCelsDir.exists()) {
                    ServerUtils.deleteDirectory(renamedCelsDir);
                }
                removeGenoAnalysisRunning();
                runningAnalysis.setEndDate(new Date());
                runningAnalysis.setStatus(RunningGenotypingAnalysis.GenoAnaRunningStatus.DONE);
            }
        }

        private int insertGenotypingAnalysisInDB(String genotypingName, int userID, File finalAnalysisFolder, double dishQCLimit, double callRateLimit, File libraryFilesFolder, File annotationFile) {
            return GenoAnalysisTable.insertAnalysis(pool, genotypingName, userID, finalAnalysisFolder.getAbsolutePath(), dishQCLimit, callRateLimit, libraryFilesFolder.getName(), annotationFile.getName());
        }

        private void insertGenotypingSamplesInDB(int genoAnalysisKey) {

            GenoSamplesTable.insertGenoSamples(pool, genoAnalysisKey, sampleIDList);
        }

        private void insertGenotypingQcInDB(LinkedHashMap<String, String> uniqueCelFiles, Map<String, String> changedCelFiles, File resultFolder, int genoAnalysisKey, String run) {
            GenoQcValuesTable.insertGenoQc(pool, uniqueCelFiles, changedCelFiles, resultFolder, genoAnalysisKey, run);
        }
    }

    @Override
    public synchronized String performGenotyping(List<Integer> sampleIdList, String genotypingName, double dishQCLimit, double callRateLimit, int userID, String libraryFilesFolder, String annotationFile) {

        if (isMaxGenoAnalysisRunningNotReached()) {
            addGenoAnalysisRunning();
        } else {
            return "Maximum simultaneous genotyping analysis authorized is reached.\nCheck the workflow tab to see the progress and wait for one to finish.";
        }

        final File mainAnalysisFolder = appFiles.get("analysisFile");
        final File finalAnalysisFolder = new File(mainAnalysisFolder.getAbsolutePath() + "/genotyping_" + genotypingName);
        if (finalAnalysisFolder.exists()) {
            removeGenoAnalysisRunning();
            return "This name has already been used in a previous genotyping. Please choose another one.";
        } else {
            finalAnalysisFolder.mkdir();
        }

        RunningGenotypingAnalysis runningAnalysis = new RunningGenotypingAnalysis();
        runningAnalysis.setName(genotypingName);
        runningAnalysis.setStartDate(new Date());
        runningAnalysis.setStatus(RunningGenotypingAnalysis.GenoAnaRunningStatus.STARTING);
        runningAnalysis.setUser(UsersTable.getUserNameFromId(pool, userID));
        addRunningAnalysisToList(runningAnalysis);

        GenotypingThread th = new GenotypingThread(finalAnalysisFolder, sampleIdList, genotypingName, dishQCLimit, callRateLimit, userID, libraryFilesFolder, annotationFile, runningAnalysis);
        th.start();

        return "Genotyping launched.\nGo to workflow tab to see its progress.";
    }

    private boolean isMaxGenoAnalysisRunningNotReached() {
        Integer nbAnalysisRunning = (Integer) getServletContext().getAttribute("GenoAnalysisRunning");
        int nbMaxGenoAnalysisAuthorized = (Integer) getServletContext().getAttribute("MaxGenoAnalysis");
        if (nbAnalysisRunning == null || nbAnalysisRunning < nbMaxGenoAnalysisAuthorized) {
            return true;
        } else {
            return false;
        }
    }

    private void addGenoAnalysisRunning() {
        Integer nbAnalysisRunning = (Integer) getServletContext().getAttribute("GenoAnalysisRunning");
        if (nbAnalysisRunning == null) {
            getServletContext().setAttribute("GenoAnalysisRunning", 1);
        } else {
            getServletContext().setAttribute("GenoAnalysisRunning", nbAnalysisRunning++);
        }
    }

    private void removeGenoAnalysisRunning() {
        Integer nbAnalysisRunning = (Integer) getServletContext().getAttribute("GenoAnalysisRunning");
        getServletContext().setAttribute("GenoAnalysisRunning", nbAnalysisRunning--);
    }

    @Override
    public List<String> getGenotypingNames() {
        return GenoAnalysisTable.getGenotypingNames(pool);
    }

    public List<RunningGenotypingAnalysis> getRunningGenoAnalysis() {
        List<RunningGenotypingAnalysis> list = (List<RunningGenotypingAnalysis>) getServletContext().getAttribute("runningGenoAnalysisList");
        if (list == null || list.isEmpty()) {
            return null;
        } else {
            Collections.sort(list, new Comparator<RunningGenotypingAnalysis>() {
                @Override
                public int compare(RunningGenotypingAnalysis o1, RunningGenotypingAnalysis o2) {
                    return o1.getStartDate().compareTo(o2.getStartDate());
                }
            });
            return list;
        }
    }

    public synchronized void removeRunningAnalysisFromList(String name) {
        RunningGenotypingAnalysis anaToRemove = new RunningGenotypingAnalysis();
        anaToRemove.setName(name);
        List<RunningGenotypingAnalysis> list = (List<RunningGenotypingAnalysis>) getServletContext().getAttribute("runningGenoAnalysisList");
        list.remove(anaToRemove);
        getServletContext().removeAttribute("runningGenoAnalysisList");
        getServletContext().setAttribute("runningGenoAnalysisList", list);
    }

    public void addRunningAnalysisToList(RunningGenotypingAnalysis analysis) {
        List<RunningGenotypingAnalysis> list = (List<RunningGenotypingAnalysis>) getServletContext().getAttribute("runningGenoAnalysisList");
        if (list == null) {
            list = new ArrayList<RunningGenotypingAnalysis>();
        }
        list.add(analysis);
        getServletContext().removeAttribute("runningGenoAnalysisList");
        getServletContext().setAttribute("runningGenoAnalysisList", list);
    }
}