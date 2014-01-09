package fr.pfgen.axiom.server.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import fr.pfgen.axiom.server.beans.AnnotProbe;
import fr.pfgen.axiom.server.beans.ChpCall;

public class ClusterGraphCalls {
	
	private File callsFile;
	private Map<File, List<File>> chpFolders2chpFiles;
	private File snpListFile;
	private File annotFile;
	
	private Map<String, String> newSampleName2plateName = new HashMap<String, String>();
	private volatile Map<String, List<ChpCall>> probeName2Calls = new HashMap<String, List<ChpCall>>();
	private List<String> snpList;
	private Map<String, AnnotProbe> probeName2AnnotProbe;

	public ClusterGraphCalls(File callsFile, Map<File, List<File>> chpFolder2chpFiles, File snpListFile, File annotFile){
		this.callsFile = callsFile;
		this.chpFolders2chpFiles = chpFolder2chpFiles;
		this.snpListFile = snpListFile;
		this.annotFile = annotFile;
	}

	public String createCallsFile() {
		snpList = SnpListUtils.readSnpListFile(snpListFile);
		readAnnotFile();
		readChpFiles();
		writeCalls();
		return callsFile.getAbsolutePath();
	}
	
	private void readAnnotFile(){
		AnnotFileReader annotReader = new AnnotFileReader(annotFile);
		probeName2AnnotProbe = annotReader.getAnnotations(snpList);
		
	}
	
	private void readChpFiles(){
		
		for (File chpFolder : chpFolders2chpFiles.keySet()) {
			readSamplesPlates(chpFolder);
			List<File> chpFilesInFolder = chpFolders2chpFiles.get(chpFolder);
			
			List<Integer> lineNumbers = readFirstChpFile(chpFilesInFolder);
			List<ReadChpFilesThread> threads = new ArrayList<ReadChpFilesThread>();
			
			while (!chpFilesInFolder.isEmpty()){
				List<File> portion = new ArrayList<File>(100);
				int startPoint = 0;
				int endPoint = 100;
				if (endPoint>chpFilesInFolder.size()){
					endPoint = chpFilesInFolder.size();
				}
				for (int j = startPoint; j < endPoint; j++){
					portion.add(chpFilesInFolder.get(j));
				}
				chpFilesInFolder.subList(startPoint, endPoint).clear();
				ReadChpFilesThread t = new ReadChpFilesThread(portion,lineNumbers);
				threads.add(t);
			}
			
			ExecutorService threadExecutor = Executors.newFixedThreadPool(4);
			for (ReadChpFilesThread t : threads) {
				threadExecutor.execute(t);
			}
			threadExecutor.shutdown();
			
			try {
				threadExecutor.awaitTermination(2, TimeUnit.HOURS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void readSamplesPlates(File chpFolder){
		newSampleName2plateName.clear();
		
		File samplesPlatesFile = new File(chpFolder.getParentFile().getParentFile().getAbsolutePath()+"/QCReport/samples_plates.txt");
		if (!samplesPlatesFile.exists()){
			throw new RuntimeException("Cannot get 'samples_plates.txt' for chpFolder "+chpFolder.getAbsolutePath());
		}
		
		BufferedReader br = null;
		
		try{
			Pattern tab = Pattern.compile("[\t]");
			br = IOUtils.openFile(samplesPlatesFile);
			String line;
			while((line=br.readLine())!=null){
				if (line.startsWith("sampleID")){
					continue;
				}
				String[] lineSplit = tab.split(line);
				newSampleName2plateName.put(lineSplit[3], lineSplit[7]);
			}
		}catch(IOException e){
			throw new RuntimeException(e);
		}finally{
			IOUtils.safeClose(br);
		}
	}
	
	private List<Integer> readFirstChpFile(List<File> chpFiles){
		File firstFile = chpFiles.get(0);
		chpFiles.subList(0, 1).clear();
		String sampleName = firstFile.getName().replaceAll("\\.AxiomGT1\\.chp\\.txt\\.gz", "");
		
		List<Integer> lineNumbers = new ArrayList<Integer>(probeName2AnnotProbe.size());
		Pattern tab=Pattern.compile("[\t]");
		BufferedReader br = null;
		try{
			br = IOUtils.openFile(firstFile);
			int lineIndex = 1;
			String line;
			while((line = br.readLine())!=null){
				if (line.startsWith("#%")){
					continue;
				}
				String[] linesplit = tab.split(line);
				if (probeName2AnnotProbe.containsKey(linesplit[0])){
					ChpCall call = new ChpCall();
					call.setSampleName(sampleName);
					call.setPlateName(newSampleName2plateName.get(sampleName+".CEL"));
					call.setCall(linesplit[1]);
					call.setLogRatio(Double.parseDouble(linesplit[3]));
					call.setStrength(Double.parseDouble(linesplit[4]));
					List<ChpCall> callsList = probeName2Calls.get(linesplit[0]);
					if (callsList==null){
						callsList = Collections.synchronizedList(new ArrayList<ChpCall>());
						probeName2Calls.put(linesplit[0], callsList);
					}
					callsList.add(call);
					lineNumbers.add(lineIndex);
				}
				lineIndex++;
			}
			br.close();
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			IOUtils.safeClose(br);
		}
		return lineNumbers;
	}
	
	private class ReadChpFilesThread extends Thread{
		
		private List<File> portion;
		private List<Integer> lineNumbers;
		
		public ReadChpFilesThread(List<File> portion, List<Integer> lineNumbers){
		this.portion = portion;
		this.lineNumbers = lineNumbers;
		}
		
		@Override
		public void run(){
			Pattern tab = Pattern.compile("[\t]");
			for (File chp : portion) {
				String sampleName = chp.getName().replaceAll("\\.AxiomGT1\\.chp\\.txt\\.gz", "");
				List<String> list = ServerUtils.GetLinesByIndexOmitStartingWith(lineNumbers, chp, "#%");
				for (String c : list) {
					String[] linesplit = tab.split(c);
					ChpCall call = new ChpCall();
					call.setSampleName(sampleName);
					call.setPlateName(newSampleName2plateName.get(sampleName+".CEL"));
					call.setCall(linesplit[1]);
					call.setLogRatio(Double.parseDouble(linesplit[3]));
					call.setStrength(Double.parseDouble(linesplit[4]));
					List<ChpCall> callsList = probeName2Calls.get(linesplit[0]);
					if (callsList==null){
						throw new RuntimeException("Call list "+linesplit[0]+" doesn't exist in probeName2Calls");
					}
					callsList.add(call);
				}
			}
		}
	}
	
	private void writeCalls(){
		PrintWriter pwCalls = null;
		
		try{
			pwCalls = new PrintWriter(new FileWriter(callsFile));
			pwCalls.println("affyProbeName\tsampleName\tplateName\taffyCall\tlogRatio\tstrength");
			
			for (String probeName : probeName2Calls.keySet()){
				for (ChpCall c : probeName2Calls.get(probeName)) {
					pwCalls.println(probeName+"\t"+c.getSampleName()+"\t"+c.getPlateName()+"\t"+c.getCall()+"\t"+c.getLogRatio()+"\t"+c.getStrength());
				}
			}
			pwCalls.close();
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			IOUtils.safeClose(pwCalls);
		}
	}
}