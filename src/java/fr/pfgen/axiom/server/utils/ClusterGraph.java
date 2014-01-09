package fr.pfgen.axiom.server.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Deprecated
public class ClusterGraph {

	private static final String COL_PROBSET_ID="Probe Set ID";
	private static final String COL_DBSNP_RS_ID="dbSNP RS ID";
	private static final String COL_ALLELE_A="Allele A";
	private static final String COL_ALLELE_B="Allele B";
	private static final String COL_FREQUENCIES="Allele Frequencies";
	
	private Map<File, List<File>> chpFolders2chpFiles;
	private File calls;
	private File probesInfos;
	//private List<Integer> lineNumbers;
	private Map<String, String> newSampleName2plateName = new HashMap<String, String>();
	private boolean post = false;
	//private List<File> chpFilesInFolder;

	private Map<String, AnnotProbes> probeName2AnnotProbe = new HashMap<String, AnnotProbes>();
	private volatile Map<String, List<Calls>> probeName2Calls = new HashMap<String, List<Calls>>();
	private List<String> snpList = new ArrayList<String>();
	
	public ClusterGraph(File annotCSV, File models, Map<File, List<File>> chpFolders2chpFiles, File snpListFile, File callsFile, File probeInfosFile){
		this.chpFolders2chpFiles = chpFolders2chpFiles;
		this.calls = callsFile;
		this.probesInfos = probeInfosFile;
		
		readSnpFile(snpListFile);
		
		readCSV(annotCSV);
		
		readModels(models);
		
		if (this.chpFolders2chpFiles.size()==1){
			post = true;
			File chpFolder = chpFolders2chpFiles.keySet().toArray(new File[chpFolders2chpFiles.size()])[0];
			File posteriors = new File(chpFolder.getParentFile(), "AxiomGT1.snp-posteriors.txt");
			readPosteriors(posteriors);
		}
		
		readChpFiles();
		
		writeCalls();
	}
	
	private void readSnpFile(File snpListFile){
		
		BufferedReader br = null;
		
		try{
			br = IOUtils.openFile(snpListFile);
			String line;
			while((line=br.readLine())!=null){
				snpList.add(line);
			}
			br.close();
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			IOUtils.safeClose(br);
		}
	}
	
	private List<String> mappingSplit(String line) throws IOException{

		List<String> tokens=new ArrayList<String>(50);
		StreamTokenizer st=new StreamTokenizer(new StringReader(line));
		st.resetSyntax();
		st.whitespaceChars(',', ',');
		st.quoteChar('\"');
		while (st.nextToken() != StreamTokenizer.TT_EOF){
			String word = st.sval;
			tokens.add(word);
		}
		return tokens;
	}
	
	private class AnnotProbes{
		private String rsName;
		private char alleleA;
		private char alleleB;
		private double freqAlleleA;
		private double freqAlleleB;
		
		private double BBmeanX;
		private double BBvarX;
		private double BBmeanY;
		private double BBvarY;
		private double BBcovarXY;
		private double ABmeanX;
		private double ABvarX;
		private double ABmeanY;
		private double ABvarY;
		private double ABcovarXY;
		private double AAmeanX;
		private double AAvarX;
		private double AAmeanY;
		private double AAvarY;
		private double AAcovarXY;
		
		private double postBBmeanX;
		private double postBBvarX;
		private double postBBmeanY;
		private double postBBvarY;
		private double postBBcovarXY;
		private double postABmeanX;
		private double postABvarX;
		private double postABmeanY;
		private double postABvarY;
		private double postABcovarXY;
		private double postAAmeanX;
		private double postAAvarX;
		private double postAAmeanY;
		private double postAAvarY;
		private double postAAcovarXY;
		
		public String getRsName() {
			return rsName;
		}
		public void setRsName(String rsName) {
			this.rsName = rsName;
		}
		public char getAlleleA() {
			return alleleA;
		}
		public void setAlleleA(char alleleA) {
			this.alleleA = alleleA;
		}
		public char getAlleleB() {
			return alleleB;
		}
		public void setAlleleB(char alleleB) {
			this.alleleB = alleleB;
		}
		public double getFreqAlleleA() {
			return freqAlleleA;
		}
		public void setFreqAlleleA(double freqAlleleA) {
			this.freqAlleleA = freqAlleleA;
		}
		public double getFreqAlleleB() {
			return freqAlleleB;
		}
		public void setFreqAlleleB(double freqAlleleB) {
			this.freqAlleleB = freqAlleleB;
		}
		public double getBBmeanX() {
			return BBmeanX;
		}
		public void setBBmeanX(double bBmeanX) {
			BBmeanX = bBmeanX;
		}
		public double getBBvarX() {
			return BBvarX;
		}
		public void setBBvarX(double bBvarX) {
			BBvarX = bBvarX;
		}
		public double getBBmeanY() {
			return BBmeanY;
		}
		public void setBBmeanY(double bBmeanY) {
			BBmeanY = bBmeanY;
		}
		public double getBBvarY() {
			return BBvarY;
		}
		public void setBBvarY(double bBvarY) {
			BBvarY = bBvarY;
		}
		public double getBBcovarXY() {
			return BBcovarXY;
		}
		public void setBBcovarXY(double bBcovarXY) {
			BBcovarXY = bBcovarXY;
		}
		public double getABmeanX() {
			return ABmeanX;
		}
		public void setABmeanX(double aBmeanX) {
			ABmeanX = aBmeanX;
		}
		public double getABvarX() {
			return ABvarX;
		}
		public void setABvarX(double aBvarX) {
			ABvarX = aBvarX;
		}
		public double getABmeanY() {
			return ABmeanY;
		}
		public void setABmeanY(double aBmeanY) {
			ABmeanY = aBmeanY;
		}
		public double getABvarY() {
			return ABvarY;
		}
		public void setABvarY(double aBvarY) {
			ABvarY = aBvarY;
		}
		public double getABcovarXY() {
			return ABcovarXY;
		}
		public void setABcovarXY(double aBcovarXY) {
			ABcovarXY = aBcovarXY;
		}
		public double getAAmeanX() {
			return AAmeanX;
		}
		public void setAAmeanX(double aAmeanX) {
			AAmeanX = aAmeanX;
		}
		public double getAAvarX() {
			return AAvarX;
		}
		public void setAAvarX(double aAvarX) {
			AAvarX = aAvarX;
		}
		public double getAAmeanY() {
			return AAmeanY;
		}
		public void setAAmeanY(double aAmeanY) {
			AAmeanY = aAmeanY;
		}
		public double getAAvarY() {
			return AAvarY;
		}
		public void setAAvarY(double aAvarY) {
			AAvarY = aAvarY;
		}
		public double getAAcovarXY() {
			return AAcovarXY;
		}
		public void setAAcovarXY(double aAcovarXY) {
			AAcovarXY = aAcovarXY;
		}
		public double getPostBBmeanX() {
			return postBBmeanX;
		}
		public void setPostBBmeanX(double postBBmeanX) {
			this.postBBmeanX = postBBmeanX;
		}
		public double getPostBBvarX() {
			return postBBvarX;
		}
		public void setPostBBvarX(double postBBvarX) {
			this.postBBvarX = postBBvarX;
		}
		public double getPostBBmeanY() {
			return postBBmeanY;
		}
		public void setPostBBmeanY(double postBBmeanY) {
			this.postBBmeanY = postBBmeanY;
		}
		public double getPostBBvarY() {
			return postBBvarY;
		}
		public void setPostBBvarY(double postBBvarY) {
			this.postBBvarY = postBBvarY;
		}
		public double getPostBBcovarXY() {
			return postBBcovarXY;
		}
		public void setPostBBcovarXY(double postBBcovarXY) {
			this.postBBcovarXY = postBBcovarXY;
		}
		public double getPostABmeanX() {
			return postABmeanX;
		}
		public void setPostABmeanX(double postABmeanX) {
			this.postABmeanX = postABmeanX;
		}
		public double getPostABvarX() {
			return postABvarX;
		}
		public void setPostABvarX(double postABvarX) {
			this.postABvarX = postABvarX;
		}
		public double getPostABmeanY() {
			return postABmeanY;
		}
		public void setPostABmeanY(double postABmeanY) {
			this.postABmeanY = postABmeanY;
		}
		public double getPostABvarY() {
			return postABvarY;
		}
		public void setPostABvarY(double postABvarY) {
			this.postABvarY = postABvarY;
		}
		public double getPostABcovarXY() {
			return postABcovarXY;
		}
		public void setPostABcovarXY(double postABcovarXY) {
			this.postABcovarXY = postABcovarXY;
		}
		public double getPostAAmeanX() {
			return postAAmeanX;
		}
		public void setPostAAmeanX(double postAAmeanX) {
			this.postAAmeanX = postAAmeanX;
		}
		public double getPostAAvarX() {
			return postAAvarX;
		}
		public void setPostAAvarX(double postAAvarX) {
			this.postAAvarX = postAAvarX;
		}
		public double getPostAAmeanY() {
			return postAAmeanY;
		}
		public void setPostAAmeanY(double postAAmeanY) {
			this.postAAmeanY = postAAmeanY;
		}
		public double getPostAAvarY() {
			return postAAvarY;
		}
		public void setPostAAvarY(double postAAvarY) {
			this.postAAvarY = postAAvarY;
		}
		public double getPostAAcovarXY() {
			return postAAcovarXY;
		}
		public void setPostAAcovarXY(double postAAcovarXY) {
			this.postAAcovarXY = postAAcovarXY;
		}
	}
	
	private void readCSV(File annotCSV){
		
		BufferedReader in = null;
		
		try{
			in = IOUtils.openFile(annotCSV);
			String line;
			List<String> header=null;
			int colIndexRsId=-1;
			int colProbSetId=-1;
			int colAlleleA=-1;
			int colAlleleB=-1;
			int colFrequencies=-1;
			
			while((line=in.readLine())!=null){
				if(line.startsWith("#")) continue;
				header=mappingSplit(line);

				for(int i=0;i< header.size();++i){
					if(header.get(i).equals(COL_DBSNP_RS_ID)){
						if(colIndexRsId!=-1) throw new IOException("Duplicate "+COL_DBSNP_RS_ID+" in "+line);
						colIndexRsId=i;
					}else if(header.get(i).equals(COL_PROBSET_ID)){
						if(colProbSetId!=-1) throw new IOException("Duplicate "+COL_PROBSET_ID+" in "+line);
						colProbSetId=i;
					}else if(header.get(i).equals(COL_ALLELE_A)){
						if(colAlleleA!=-1) throw new IOException("Duplicate "+COL_ALLELE_A+" in "+line);
						colAlleleA=i;
					}else if(header.get(i).equals(COL_ALLELE_B)){
						if(colAlleleB!=-1) throw new IOException("Duplicate "+COL_ALLELE_B+" in "+line);
						colAlleleB=i;
					}else if(header.get(i).equals(COL_FREQUENCIES)){
						if(colFrequencies!=-1) throw new IOException("Duplicate "+COL_FREQUENCIES+" in "+line);
						colFrequencies=i;
					}
				}
				if(colIndexRsId==-1) throw new IOException("Cannot get "+COL_DBSNP_RS_ID+" in "+line);
				if(colProbSetId==-1) throw new IOException("Cannot get "+COL_PROBSET_ID+" in "+line);
				if(colAlleleA==-1) throw new IOException("Cannot get "+COL_ALLELE_A+" in "+line);
				if(colAlleleB==-1) throw new IOException("Cannot get "+COL_ALLELE_B+" in "+line);
				if(colFrequencies==-1) throw new IOException("Cannot get "+COL_FREQUENCIES+" in "+line);
				break;
			}
			if(header==null) throw new IOException("header not found in "+annotCSV.getAbsolutePath());
			int snpFound = snpList.size();
			System.out.println(snpFound+" snps to find");
			while((line=in.readLine())!=null && snpFound>0){
				List<String> tokens=mappingSplit(line);
				if(tokens.size()!=header.size()){
					throw new IOException("expected "+header.size()+" columns but got "+tokens.size()+" in "+line);
				}

				if (snpList.contains(tokens.get(colProbSetId)) || snpList.contains(tokens.get(colIndexRsId))){
					if (probeName2AnnotProbe.containsKey(tokens.get(colProbSetId))){
						throw new IOException("?! duplicate probeSet Id "+tokens.get(colProbSetId));
					}else{
						snpFound--;
						System.out.println(snpFound+" snps left to find");
						AnnotProbes probe = new AnnotProbes();
						probe.setRsName(tokens.get(colIndexRsId));
						probe.setAlleleA(tokens.get(colAlleleA).charAt(0));
						probe.setAlleleB(tokens.get(colAlleleB).charAt(0));
						probe.setFreqAlleleA(parseFreqLine(tokens.get(colFrequencies),"A", tokens.get(colIndexRsId)));
						probe.setFreqAlleleB(parseFreqLine(tokens.get(colFrequencies),"B", tokens.get(colIndexRsId)));
						probeName2AnnotProbe.put(tokens.get(colProbSetId), probe);
					}
				}
			}
			in.close();
		
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			IOUtils.safeClose(in);
		}
	}
	
	private void readModels(File models){
		BufferedReader in = null;
		Pattern tab = Pattern.compile("[\t]");
		Pattern comma = Pattern.compile("[,]");
		Set<String> probesToFind = new HashSet<String>(probeName2AnnotProbe.keySet());
		
		try{
			in = IOUtils.openFile(models);

			String line;
			while((line=in.readLine())!=null && probesToFind.size()>0){
				if (line.startsWith("#") || line.startsWith("id")){
					continue;
				}
				String[] lineSplit = tab.split(line);
				if (probesToFind.contains(lineSplit[0])){
					AnnotProbes probe = probeName2AnnotProbe.get(lineSplit[0]);
					probesToFind.remove(lineSplit[0]);
					String[] BBsplit = comma.split(lineSplit[1]);
					String[] ABsplit = comma.split(lineSplit[2]);
					String[] AAsplit = comma.split(lineSplit[3]);

					probe.setBBmeanX(Double.parseDouble(BBsplit[0]));
					probe.setBBmeanY(Double.parseDouble(BBsplit[4]));
					probe.setBBvarX(Double.parseDouble(BBsplit[1]));
					probe.setBBvarY(Double.parseDouble(BBsplit[5]));
					probe.setBBcovarXY(Double.parseDouble(BBsplit[6]));

					probe.setABmeanX(Double.parseDouble(ABsplit[0]));
					probe.setABmeanY(Double.parseDouble(ABsplit[4]));
					probe.setABvarX(Double.parseDouble(ABsplit[1]));
					probe.setABvarY(Double.parseDouble(ABsplit[5]));
					probe.setABcovarXY(Double.parseDouble(ABsplit[6]));

					probe.setAAmeanX(Double.parseDouble(AAsplit[0]));
					probe.setAAmeanY(Double.parseDouble(AAsplit[4]));
					probe.setAAvarX(Double.parseDouble(AAsplit[1]));
					probe.setAAvarY(Double.parseDouble(AAsplit[5]));
					probe.setAAcovarXY(Double.parseDouble(AAsplit[6]));
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			IOUtils.safeClose(in);
		}
	}
	
	private void readPosteriors(File posteriors){
		BufferedReader in = null;
		Pattern tab = Pattern.compile("[\t]");
		Pattern comma = Pattern.compile("[,]");
		Set<String> probesToFind = new HashSet<String>(probeName2AnnotProbe.keySet());
		
		try{
			in = IOUtils.openFile(posteriors);

			String line;
			while((line=in.readLine())!=null && probesToFind.size()>0){
				if (line.startsWith("#") || line.startsWith("id")){
					continue;
				}
				String[] lineSplit = tab.split(line);
				if (probesToFind.contains(lineSplit[0])){
					AnnotProbes probe = probeName2AnnotProbe.get(lineSplit[0]);
					probesToFind.remove(lineSplit[0]);
					String[] BBsplit = comma.split(lineSplit[1]);
					String[] ABsplit = comma.split(lineSplit[2]);
					String[] AAsplit = comma.split(lineSplit[3]);

					probe.setPostBBmeanX(Double.parseDouble(BBsplit[0]));
					probe.setPostBBmeanY(Double.parseDouble(BBsplit[4]));
					probe.setPostBBvarX(Double.parseDouble(BBsplit[1]));
					probe.setPostBBvarY(Double.parseDouble(BBsplit[5]));
					probe.setPostBBcovarXY(Double.parseDouble(BBsplit[6]));

					probe.setPostABmeanX(Double.parseDouble(ABsplit[0]));
					probe.setPostABmeanY(Double.parseDouble(ABsplit[4]));
					probe.setPostABvarX(Double.parseDouble(ABsplit[1]));
					probe.setPostABvarY(Double.parseDouble(ABsplit[5]));
					probe.setPostABcovarXY(Double.parseDouble(ABsplit[6]));

					probe.setPostAAmeanX(Double.parseDouble(AAsplit[0]));
					probe.setPostAAmeanY(Double.parseDouble(AAsplit[4]));
					probe.setPostAAvarX(Double.parseDouble(AAsplit[1]));
					probe.setPostAAvarY(Double.parseDouble(AAsplit[5]));
					probe.setPostAAcovarXY(Double.parseDouble(AAsplit[6]));
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			IOUtils.safeClose(in);
		}
	}
	
	private double parseFreqLine(String freqLine, String allele, String rs){
		String[] populationsFreq = freqLine.split("///");
		String caucasianPop = "";
		for (String pop : populationsFreq) {
			if (pop.toLowerCase().contains("caucasian")){
				caucasianPop = pop;
			}
		}
		if (caucasianPop.isEmpty()) return 0;
		String[] caucasianLine = caucasianPop.split("//");
		if (allele.equals("A")){
			return Double.parseDouble(caucasianLine[0]);
		}else{
			return Double.parseDouble(caucasianLine[1]);
		}
	}
	
	private void readChpFiles(){
		
		for (File chpFolder : chpFolders2chpFiles.keySet()) {
			System.out.println("reading CHPs: "+chpFolder.getAbsolutePath());
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
	
	private class Calls{
		private String sampleName;
		private String plateName;
		private String call;
		private double logRatio;
		private double strength;
	
		public String getSampleName() {
			return sampleName;
		}
		public void setSampleName(String sampleName) {
			this.sampleName = sampleName;
		}
		public String getPlateName() {
			return plateName;
		}
		public void setPlateName(String plateName) {
			this.plateName = plateName;
		}
		public String getCall() {
			return call;
		}
		public void setCall(String call) {
			this.call = call;
		}
		public double getLogRatio() {
			return logRatio;
		}
		public void setLogRatio(double logRatio) {
			this.logRatio = logRatio;
		}
		public double getStrength() {
			return strength;
		}
		public void setStrength(double strength) {
			this.strength = strength;
		}
	}
	
	private List<Integer> readFirstChpFile(List<File> chpFiles){
		File firstFile = chpFiles.get(0);
		chpFiles.subList(0, 1).clear();
		String sampleName = firstFile.getName().replaceAll("\\.AxiomGT1\\.chp\\.txt\\.gz", "");
		
		List<Integer> lineNumbers = new ArrayList<Integer>(snpList.size());
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
					Calls call = new Calls();
					call.setSampleName(sampleName);
					call.setPlateName(newSampleName2plateName.get(sampleName+".CEL"));
					call.setCall(linesplit[1]);
					call.setLogRatio(Double.parseDouble(linesplit[3]));
					call.setStrength(Double.parseDouble(linesplit[4]));
					List<Calls> callsList = probeName2Calls.get(linesplit[0]);
					if (callsList==null){
						callsList = Collections.synchronizedList(new ArrayList<Calls>());
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
					Calls call = new Calls();
					call.setSampleName(sampleName);
					call.setPlateName(newSampleName2plateName.get(sampleName+".CEL"));
					call.setCall(linesplit[1]);
					call.setLogRatio(Double.parseDouble(linesplit[3]));
					call.setStrength(Double.parseDouble(linesplit[4]));
					List<Calls> callsList = probeName2Calls.get(linesplit[0]);
					if (callsList==null){
						throw new RuntimeException("Call list "+linesplit[0]+" doesn't exist in probeName2Calls");
					}
					callsList.add(call);
				}
			}
		}
	}
	
	private String printAlleles(AnnotProbes marker, String call){
		if (call.equals("AA")){
			return marker.getAlleleA()+""+marker.getAlleleA();
		}else if (call.equals("AB")){
			return marker.getAlleleA()+""+marker.getAlleleB();
		}else if (call.equals("BB")){
			return marker.getAlleleB()+""+marker.getAlleleB();
		}else if (call.equals("NC")){
			return "NC";
		}else{
			throw new IllegalArgumentException("bad allele code: "+call);
		}
	}
	
	private void writeCalls(){
		PrintWriter pwCalls = null;
		PrintWriter pwInfos = null;
		
		try{
			pwCalls = new PrintWriter(new FileWriter(calls));
			pwInfos = new PrintWriter(new FileWriter(probesInfos));
			pwCalls.println("affyProbeName\tsampleName\tplateName\taffyCall\talleles\tlogRatio\tstrength");
			pwInfos.print("affyProbeName\trsName\tfreqAlleleA\tfreqAlleleB\t"+
							"BBmeanX\tBBmeanY\tBBvarX\tBBvarY\tBBcovarXY\t"+
							"ABmeanX\tABmeanY\tABvarX\tABvarY\tABcovarXY\t"+
							"AAmeanX\tAAmeanY\tAAvarX\tAAvarY\tAAcovarXY");
			if (post){
				pwInfos.print("\t");
				pwInfos.print(
						"postBBmeanX\tpostBBmeanY\tpostBBvarX\tpostBBvarY\tpostBBcovarXY\t"+
						"postABmeanX\tpostABmeanY\tpostABvarX\tpostABvarY\tpostABcovarXY\t"+
						"postAAmeanX\tpostAAmeanY\tpostAAvarX\tpostAAvarY\tpostAAcovarXY");
			}
			pwInfos.println();
			for (String probeName : probeName2Calls.keySet()){
				for (Calls c : probeName2Calls.get(probeName)) {
					pwCalls.println(probeName+"\t"+c.getSampleName()+"\t"+c.getPlateName()+"\t"+c.getCall()+"\t"+printAlleles(probeName2AnnotProbe.get(probeName), c.getCall())+"\t"+c.getLogRatio()+"\t"+c.getStrength());
				}
				AnnotProbes probe = probeName2AnnotProbe.get(probeName);
				pwInfos.print(probeName+"\t"+probe.getRsName()+"\t"+probe.getFreqAlleleA()+"\t"+probe.getFreqAlleleB()+"\t"+
								probe.getBBmeanX()+"\t"+probe.getBBmeanY()+"\t"+probe.getBBvarX()+"\t"+probe.getBBvarY()+"\t"+probe.getBBcovarXY()+"\t"+
								probe.getABmeanX()+"\t"+probe.getABmeanY()+"\t"+probe.getABvarX()+"\t"+probe.getABvarY()+"\t"+probe.getABcovarXY()+"\t"+
								probe.getAAmeanX()+"\t"+probe.getAAmeanY()+"\t"+probe.getAAvarX()+"\t"+probe.getAAvarY()+"\t"+probe.getAAcovarXY());
				if (post){
					pwInfos.print("\t");
					pwInfos.print(
							probe.getPostBBmeanX()+"\t"+probe.getPostBBmeanY()+"\t"+probe.getPostBBvarX()+"\t"+probe.getPostBBvarY()+"\t"+probe.getPostBBcovarXY()+"\t"+
							probe.getPostABmeanX()+"\t"+probe.getPostABmeanY()+"\t"+probe.getPostABvarX()+"\t"+probe.getPostABvarY()+"\t"+probe.getPostABcovarXY()+"\t"+
							probe.getPostAAmeanX()+"\t"+probe.getPostAAmeanY()+"\t"+probe.getPostAAvarX()+"\t"+probe.getPostAAvarY()+"\t"+probe.getPostAAcovarXY());
				}
				pwInfos.println();
			}
			pwCalls.close();
			pwInfos.close();
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			IOUtils.safeClose(pwCalls);
			IOUtils.safeClose(pwInfos);
		}
	}
}
