package fr.pfgen.axiom.server.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import fr.pfgen.axiom.client.services.SNPListsService;
import fr.pfgen.axiom.server.database.ConnectionPool;
import fr.pfgen.axiom.server.database.GenoAnalysisTable;
import fr.pfgen.axiom.server.database.GenoSamplesTable;
import fr.pfgen.axiom.server.database.PedigreesTable;
import fr.pfgen.axiom.server.database.SNPListsTable;
import fr.pfgen.axiom.server.database.StudiesTable;
import fr.pfgen.axiom.server.database.StudySamplesTable;
import fr.pfgen.axiom.server.utils.ClusterGraphCalls;
import fr.pfgen.axiom.server.utils.ClusterGraphModelAnnot;
import fr.pfgen.axiom.server.utils.IOUtils;
import fr.pfgen.axiom.server.utils.SnpListUtils;
import fr.pfgen.axiom.shared.records.StudySampleRecord;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class SNPListsServiceImpl extends RemoteServiceServlet implements
		SNPListsService {

	private ConnectionPool pool;
	private Hashtable<String, File> appFiles;

	@Override
	@SuppressWarnings("unchecked")
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		pool = (ConnectionPool) getServletContext().getAttribute(
				"ConnectionPool");
		appFiles = (Hashtable<String, File>) getServletContext().getAttribute(
				"ApplicationFiles");
	}

	@Override
	public String createNewList(String listName, List<String> snpList) {
		if (listName == null || listName.isEmpty()) {
			return "Error: You must provide a name for the new snp list.";
		}
		if (!listName.matches("^[a-zA-Z0-9_-]{3,15}$")) {
			return "Error: Name must contain characters \"a-z\" \"A-Z\" \"0-9\" \"-\" \"_\" only. 3-15 characters.";
		}
		List<String> listNames = SNPListsTable.getListNames(pool);
		if (listNames.contains(listName)) {
			return "Error: Name already exists.";
		}
		if (snpList == null || snpList.isEmpty()) {
			return "Error: Empty list.";
		}
		for (String snp : snpList) {
			if (!snp.matches("^AX-\\d+$") && !snp.matches("^rs\\d+$")) {
				return "Error: " + snp + " is not a valid snp.";
			}
		}
		List<String> newSnpList = new ArrayList<String>();
		for (String snp : snpList) {
			if (!newSnpList.contains(snp)){
				newSnpList.add(snp);
			}
		}
		
		File snpListsFolder = new File(appFiles.get("analysisFile"), "SNPLists");
		if (!snpListsFolder.exists()){
			if (!snpListsFolder.mkdirs()){
				return "Error: cannot create folder: "+snpListsFolder.getAbsolutePath();
			}
		}
		
		File snpListFile = new File(snpListsFolder, listName + ".txt");
		
		if (!SnpListUtils.writeSnpList(snpListFile, newSnpList)){
			return "Error: cannot write snp list file on server";
		}

		if (SNPListsTable.createNewList(pool, listName, snpListFile)) {
			return "SNP list has been created !!";
		} else {
			return "Error: Could not create snp list.";
		}
	}

	@Override
	public List<String> getListNames() {
		return SNPListsTable.getListNames(pool);
	}

	@Override
	public String getGraphForGenoAnalysis(String genoName, String listName, boolean plateColor, boolean priors, boolean posteriors, String annotFileName) {
		String cgCallsPath = SNPListsTable.clusterGraphExistsForGeno(pool, genoName, listName);
		File genoFolder = new File(GenoAnalysisTable.getGenoPathFromName(pool, genoName));
		File snpListFile = SNPListsTable.getSnpListFile(pool, listName);
		if (snpListFile == null) {
			throw new RuntimeException("Cannot get snp_list_path for snp_list " + listName);
		}
		boolean e = GenoAnalysisTable.checkIfSecondRunExist(pool,genoName);
		File runFolder;
		if (e) {
			runFolder = new File(genoFolder, "Second_run");
		} else {
			runFolder = new File(genoFolder, "First_run");
		}
		File annotFile = new File(appFiles.get("affymetrixAnnotationFilesFolder"), annotFileName);
		if (!annotFile.exists() || !annotFile.canRead()){
			throw new RuntimeException("Cannot read "+annotFile.getAbsolutePath());
		}
		
		if (cgCallsPath == null || cgCallsPath.isEmpty()) {
			cgCallsPath = createCgCallsFileForGeno(genoFolder, runFolder, snpListFile, genoName, listName, annotFile);
			if (cgCallsPath != null && !cgCallsPath.isEmpty()) {
				SNPListsTable.addNewGraphForGeno(pool, genoName, listName, cgCallsPath);
			}
		}
		
		String graphSubTitle = "Genotyping analysis: " + genoName + " / SNP list: " + listName;
		String annotFileUsed = "Annotation file: " + annotFileName;

		Map<String, File> models = new HashMap<String, File>();
		if (priors || posteriors) {
			if (priors) {
				String libraryUsed = GenoAnalysisTable.getLibraryNameForGeno(pool, genoName);
				File libraryFilesFolder = new File(appFiles.get("affymetrixLibraryFilesFolder"),libraryUsed);
				File modelFile = libraryFilesFolder.listFiles(new FilenameFilter() {

					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith("AxiomGT1.models");
					}
				})[0];
				models.put("priors", modelFile);
			}
			if (posteriors) {
				File posteriorsFile = runFolder.listFiles(new FilenameFilter() {

					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith("AxiomGT1.snp-posteriors.txt");
					}
				})[0];
				
				models.put("posteriors", posteriorsFile);
			}
		}
		
		File tmpModelFile;
		try{
			tmpModelFile = File.createTempFile("CG_probes_", ".txt", appFiles.get("temporaryFolder"));
		}catch(IOException exception){
			throw new RuntimeException("Cannot create temporary file: \"GC_probes_xxx.txt\" in "+appFiles.get("temporaryFolder").getAbsolutePath());
		}
			
		ClusterGraphModelAnnot cgModelAnnot = new ClusterGraphModelAnnot(models, annotFile, snpListFile, tmpModelFile);
		String cgModelAnnotPath = cgModelAnnot.createCgModelAnnotFile();
		
		return makeGraph(cgCallsPath, cgModelAnnotPath, graphSubTitle, annotFileUsed, plateColor, priors, posteriors);
	}
	
	@Override
	public String getGraphForStudy(String studyName, String listName, boolean plateColor, boolean priors, String subPopName, String annotFileName) {
		String cgCallsPath = SNPListsTable.clusterGraphExistsForStudy(pool, studyName, listName);
		File studyFolder = StudiesTable.getStudyFolder(pool, studyName);
		File snpListFile = SNPListsTable.getSnpListFile(pool, listName);
		if (snpListFile == null) {
			throw new RuntimeException("Cannot get snp_list_path for snp_list " + listName);
		}
		
		File annotFile = new File(appFiles.get("affymetrixAnnotationFilesFolder"), annotFileName);
		if (!annotFile.exists() || !annotFile.canRead()){
			throw new RuntimeException("Cannot read "+annotFile.getAbsolutePath());
		}
		
		if (cgCallsPath == null || cgCallsPath.isEmpty()) {
			cgCallsPath = createCgCallsFileForStudy(studyFolder, snpListFile, studyName, listName, annotFile);
			if (cgCallsPath != null && !cgCallsPath.isEmpty()) {
				SNPListsTable.addNewGraphForStudy(pool, studyName, listName, cgCallsPath);
			}
		}
		
		String graphSubTitle = "Study: " + studyName + " / SNP list: " + listName;
		String annotFileUsed = "Annotation file: " + annotFileName;

		Map<String, File> models = new HashMap<String, File>();
		if (priors) {
			List<String> libraries = StudiesTable.getLibraryUsedInStudy(pool, studyName);
			if (libraries == null || libraries.isEmpty()){
				throw new RuntimeException("No library files found for study "+studyName);
			}
			for(String library : libraries){
				File libraryFilesFolder = new File(appFiles.get("affymetrixLibraryFilesFolder"),library);
				File modelFile = libraryFilesFolder.listFiles(new FilenameFilter() {
				
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith("AxiomGT1.models");
					}
				})[0];
				models.put("priors "+library, modelFile);
			}
		}
		
		File tmpModelFile;
		try{
			tmpModelFile = File.createTempFile("CG_probes_", ".txt", appFiles.get("temporaryFolder"));
		}catch(IOException exception){
			throw new RuntimeException("Cannot create temporary file: \"GC_probes_xxx.txt\" in "+appFiles.get("temporaryFolder").getAbsolutePath());
		}
			
		ClusterGraphModelAnnot cgModelAnnot = new ClusterGraphModelAnnot(models, annotFile, snpListFile, tmpModelFile);
		String cgModelAnnotPath = cgModelAnnot.createCgModelAnnotFile();
		
		if (subPopName != null) {
			BufferedReader br = null;
			PrintWriter pw = null;
			File newCallsFile;

			List<String> subPopRecs = PedigreesTable.getIndNamesInStudyForSubpopulation(pool, subPopName);
			try {
				newCallsFile = File.createTempFile("CG_calls_", ".txt", appFiles.get("temporaryFolder"));
				br = new BufferedReader(new FileReader(cgCallsPath));
				pw = new PrintWriter(new FileWriter(newCallsFile));
				String line;
				String header = "";
				Pattern tab = Pattern.compile("[\t]");
				while ((line = br.readLine()) != null) {
					if (line.startsWith("affyProbeName")) {
						header = line;
						pw.println(header);
					}
					String[] linesplit = tab.split(line);
					String indName = GetSampleName(linesplit[1]);
					if (subPopRecs.contains(indName)) {
						pw.println(line);
					}
				}
				br.close();
				pw.close();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} finally {
				IOUtils.safeClose(br);
				IOUtils.safeClose(pw);
			}
			cgCallsPath = newCallsFile.getAbsolutePath();
		}
		
		return makeGraph(cgCallsPath, cgModelAnnotPath, graphSubTitle, annotFileUsed, plateColor, priors, false);
	}
	
	private String makeGraph(String cgCallsPath, String cgModelAnnotPath, String graphSubTitle, String annotFileUsed, boolean plateColor, boolean priors, boolean posteriors) {
		
		File clusterGraph;
		try{
			clusterGraph = File.createTempFile("CG_", ".pdf", appFiles.get("temporaryFolder"));
		}catch(IOException exception){
			throw new RuntimeException("Cannot create temporary file: \"GC_xxx.txt\" in "+appFiles.get("temporaryFolder").getAbsolutePath());
		}

		String plateColor2String;
		if (plateColor) {
			plateColor2String = "true";
		} else {
			plateColor2String = "false";
		}
		String priors2String;
		if (priors) {
			priors2String = "true";
		} else {
			priors2String = "false";
		}
		String posteriors2String;
		if (posteriors) {
			posteriors2String = "true";
		} else {
			posteriors2String = "false";
		}
		
		List<String> cmd = Arrays.asList(appFiles.get("RScriptBin").getAbsolutePath(), appFiles.get("RScripts") + "/ClusterGraph.R", cgCallsPath, cgModelAnnotPath, clusterGraph.getAbsolutePath(), graphSubTitle, annotFileUsed, plateColor2String, priors2String, posteriors2String);

		Process process = null;

		try {
			ProcessBuilder procBuilder = new ProcessBuilder(cmd.toArray(new String[cmd.size()]));
			process = procBuilder.start();
			process.waitFor();
			process.destroy();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (process != null) {
				try {
					process.destroy();
				} catch (Exception err) {
				}
			}
		}

		return clusterGraph.getAbsolutePath();
	}

	private String createCgCallsFileForGeno(File genoFolder, File runFolder, File snpListFile, String genoName, String listName, File annotFile) {

		File chpFolder = new File(runFolder, "CHP");
		
		if (!chpFolder.exists()) {
			throw new RuntimeException("Cannot find appropriate chp folder in " + genoFolder.getAbsolutePath());
		}
		Map<File, List<File>> chpFolder2chpFiles = new HashMap<File, List<File>>();
		List<File> chpFiles = new ArrayList<File>(Arrays.asList(chpFolder.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				if (name.endsWith(".AxiomGT1.chp.txt.gz")) {
					return true;
				} else {
					return false;
				}
			}
		})));

		chpFolder2chpFiles.put(chpFolder, chpFiles);
		File cgFolder = new File(genoFolder, "CG");
		if (!cgFolder.exists()) {
			cgFolder.mkdir();
		}

		File callsFile = new File(cgFolder, "CG_calls_" + listName + ".txt");

		ClusterGraphCalls cgCalls = new ClusterGraphCalls(callsFile, chpFolder2chpFiles, snpListFile, annotFile);
		return cgCalls.createCallsFile();
	}

	private String createCgCallsFileForStudy(File studyFolder, File snpListFile, String studyName, String listName, File annotFile) {
		Map<File, List<File>> chpFolder2chpFiles = new HashMap<File, List<File>>();

		List<StudySampleRecord> studySamples = StudySamplesTable.getSamplesInStudy(pool, studyName);
		List<File> chpPaths = GenoSamplesTable.getChpPathFromGenoRunId(pool, studySamples);
		if (chpPaths == null || chpPaths.isEmpty()) {
			throw new RuntimeException(
					"Cannot get chp paths for samples in study " + studyName);
		}
		for (File chp : chpPaths) {
			if (chpFolder2chpFiles.containsKey(chp.getParentFile())) {
				chpFolder2chpFiles.get(chp.getParentFile()).add(chp);
			} else {
				List<File> chpList = new ArrayList<File>();
				chpList.add(chp);
				chpFolder2chpFiles.put(chp.getParentFile(), chpList);
			}
		}

		File cgFolder = new File(studyFolder, "CG");
		if (!cgFolder.exists()) {
			cgFolder.mkdir();
		}

		File callsFile = new File(cgFolder, "CG_calls_" + listName + ".txt");
	
		ClusterGraphCalls cgCalls = new ClusterGraphCalls(callsFile, chpFolder2chpFiles, snpListFile, annotFile);
		return cgCalls.createCallsFile();
	}

	@Override
	public Boolean deleteSNPList(final String listName) {
		/*
		List<String> graphPaths = SNPListsTable.getGraphPathsForList(pool, listName);
		if (graphPaths == null) {
			return false;
		}

		if (!graphPaths.isEmpty()) {
			for (String graph : graphPaths) {
				File[] files2delete = new File(graph)
						.listFiles(new FilenameFilter() {

							@Override
							public boolean accept(File dir, String name) {
								if (name.startsWith("CG_")
										&& name.contains("_" + listName + ".")) {
									return true;
								} else {
									return false;
								}
							}
						});
				for (File file : files2delete) {
					if (!file.delete()) {
						return false;
					}
				}
			}
		}
		*/
            
            File listFile = SNPListsTable.getSnpListFile(pool, listName);
            if (listFile.exists()) {
                listFile.delete();
            }
            
            List<File> callFilesToDelete = SNPListsTable.getCallFilesForList(pool, listName);
            for (File file : callFilesToDelete) {
                if(file.exists()){
                    file.delete();
                }
            }
                    
            return SNPListsTable.deleteSNPList(pool, listName);
	}

	@Override
	public List<String> getSNPinList(String listName) {
		File listFile = SNPListsTable.getSnpListFile(pool, listName);
		return SnpListUtils.readSnpListFile(listFile);
	}

	private static String GetSampleName(String celName) {
		String sampleName;
		sampleName = celName.replaceFirst("\\.CEL$", "");
		if (sampleName.matches("^\\d+__")) {
			sampleName = sampleName.replaceFirst("^\\d+__", "");
		}
		if (sampleName.matches("^.*_[A-H][0-1][0-9]$")) {
			sampleName = sampleName.replaceFirst("_[A-H][0-1][0-9]$", "");
		} else if (sampleName.matches("^[A-H][0-1][0-9]_.*$")) {
			sampleName = sampleName.replaceFirst("^[A-H][0-1][0-9]_", "");
		}

		return sampleName;
	}
}
