package fr.pfgen.axiom.server.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Affy2Plink {

	private static final String COL_PROBSET_ID="Probe Set ID";
	private static final String COL_DBSNP_RS_ID="dbSNP RS ID";
	private static final String COL_CHROMOSOME="Chromosome";
	private static final String COL_POSITION="Physical Position";
	private static final String COL_ALLELE_A="Allele A";
	private static final String COL_ALLELE_B="Allele B";
	private static final String COL_GENETIC_MAP="Genetic Map";
	
	private final File annotCSV;
	private final File plinkFolder;
	private final File sampleNamesToCallNames;
	private final String plinkFileName;
	private final File pedigree;
	private final RandomAccessFile raf;
	private final File rafFile;
	private final int geneticMapConsortium; //0=deCODE,1=Marshfield,2=SLM1,0=default
	
	private Map<String,PedigreeIndividual> indID2PedigreeIndividual = new HashMap<String,PedigreeIndividual>();
	private int indID2PedigreeIndividualSize;
	private PedigreeIndividual[] orderedPedIndividuals;
	private Map<String,AnnotProbes> affyID2annotProbe = new HashMap<String,AnnotProbes>(567096,0.90f);
	private int affyID2annotProbeSize;
	private AnnotProbes[] orderedAnnotProbes;
	private Map<File, Map<String, String>> samplesToFindInCalls = new HashMap<File, Map<String,String>>();
	
	public Affy2Plink(File annotCSV, File plinkFolder, String plinkFileName, File sampleNamesToCallNames, File pedigree, int geneticMapConsortium, PrintWriter logPW) throws IOException{
		if (!annotCSV.exists() || !annotCSV.canRead()){
			throw new FileNotFoundException("Cannot access "+annotCSV.getAbsolutePath());
		}
		if (!plinkFolder.exists()){
			if (!plinkFolder.mkdir()){
				throw new RuntimeException("Cannot create "+plinkFolder.getAbsolutePath());
			}
		}
		if (!sampleNamesToCallNames.exists()){
			throw new FileNotFoundException("Cannot access "+sampleNamesToCallNames.getAbsolutePath());
		}
		if (!pedigree.exists() || !pedigree.canRead()){
			throw new FileNotFoundException("Cannot access "+pedigree.getAbsolutePath());
		}
		this.annotCSV = annotCSV;
		this.plinkFolder = plinkFolder;
		this.sampleNamesToCallNames = sampleNamesToCallNames;
		this.plinkFileName = plinkFileName;
		this.pedigree = pedigree;
		this.geneticMapConsortium = geneticMapConsortium;
		
		//fill in indID2PedigreeIndividual
		logPW.println("reading ped...");
		readPedigree();
		orderedPedIndividuals = new PedigreeIndividual[indID2PedigreeIndividualSize];
		for (String indID : indID2PedigreeIndividual.keySet()) {
			orderedPedIndividuals[indID2PedigreeIndividual.get(indID).getColNumber()] = indID2PedigreeIndividual.get(indID);
		}
		
		//fill in samplesToFindInCalls
		logPW.println("reading samples_to_calls...");
		readSampleNamesToCallNames();
		
		//fill in affyID2annotProbe
		logPW.println("reading annotation file: "+annotCSV.getName()+"...");
		readAnnotCSV();
		orderedAnnotProbes = new AnnotProbes[affyID2annotProbeSize];
		for (String affyName : affyID2annotProbe.keySet()) {
			orderedAnnotProbes[affyID2annotProbe.get(affyName).getLineNumber()] = affyID2annotProbe.get(affyName);
		}
		
		//Create random access file
		logPW.println("initializing byte file...");
		rafFile = new File("/tmp/raf_"+ServerUtils.randomHexString());
		raf = new RandomAccessFile(rafFile, "rw");
		
		byte columns[]=new byte[indID2PedigreeIndividualSize];
		for(int i=0;i< columns.length;++i){
			columns[i]=(byte)-1;
		}
		
		//initialize table
		for (int i = 0; i < affyID2annotProbeSize; i++) {
			raf.write(columns);
		}
		
		//read calls
		for (File calls : samplesToFindInCalls.keySet()) {
			logPW.println("reading calls "+calls.getAbsolutePath()+"...");
			if (!calls.exists() || !calls.canRead()){
				throw new FileNotFoundException("Cannot access "+calls.getAbsolutePath());
			}
			readCalls(calls,samplesToFindInCalls.get(calls));
		}
		
		logPW.println("writing Plink files...");
		writeTfiles();
		
		logPW.println("DONE");
		raf.close();
		rafFile.delete();
		logPW.close();
	}
	
	private class PedigreeIndividual{
		private String famID;
		private String indID;
		private String fatherID;
		private String motherID;
		private int sex;
		private int status;
		private int colNumber;
		
		public String getFamID() {
			return famID;
		}
		public void setFamID(String famID) {
			this.famID = famID;
		}
		public String getIndID() {
			return indID;
		}
		public void setIndID(String indID) {
			this.indID = indID;
		}
		public String getFatherID() {
			return fatherID;
		}
		public void setFatherID(String fatherID) {
			this.fatherID = fatherID;
		}
		public String getMotherID() {
			return motherID;
		}
		public void setMotherID(String motherID) {
			this.motherID = motherID;
		}
		public int getSex() {
			return sex;
		}
		public void setSex(int sex) {
			this.sex = sex;
		}
		public int getStatus() {
			return status;
		}
		public void setStatus(int status) {
			this.status = status;
		}
		public int getColNumber() {
			return colNumber;
		}
		public void setColNumber(int colNumber) {
			this.colNumber = colNumber;
		}
	}
	
	private void readPedigree() throws IOException{
		BufferedReader in = IOUtils.openFile(pedigree);
		String line;
		int colNumber=0;
		while((line=in.readLine())!=null){
			if (line.startsWith("#") || line.isEmpty()){
				continue;
			}
			String[] lineSplit = line.split("\\t");
			if (lineSplit.length!=6){
				throw new RuntimeException("Expected 6 column in pedigree but got: "+line);
			}
			if (indID2PedigreeIndividual.containsKey(lineSplit[1])){
				throw new RuntimeException("Duplicate individual \""+lineSplit[1]+"\" in pedigree"+pedigree.getAbsolutePath());
			}
			
			PedigreeIndividual pedInd = new PedigreeIndividual();
			pedInd.setFamID(lineSplit[0]);
			pedInd.setIndID(lineSplit[1]);
			pedInd.setFatherID(lineSplit[2]);
			pedInd.setMotherID(lineSplit[3]);
			pedInd.setSex(Integer.parseInt(lineSplit[4]));
			pedInd.setStatus(Integer.parseInt(lineSplit[5]));
			pedInd.setColNumber(colNumber);
			indID2PedigreeIndividual.put(lineSplit[1], pedInd);
			colNumber++;
		}
		in.close();
		indID2PedigreeIndividualSize = indID2PedigreeIndividual.size();
	}

	private class AnnotProbes{
		private String affyProbeName;
		private String rsName;
		private String chromosome;
		private long bpPosition;
		private char alleleA;
		private char alleleB;
		private double geneticMap;
		private int lineNumber;
		
		public String getAffyProbeName() {
			return affyProbeName;
		}
		public void setAffyProbeName(String affyProbeName) {
			this.affyProbeName = affyProbeName;
		}
		public String getRsName() {
			return rsName;
		}
		public void setRsName(String rsName) {
			this.rsName = rsName;
		}
		public String getChromosome() {
			return chromosome;
		}
		public void setChromosome(String chromosome) {
			this.chromosome = chromosome;
		}
		public long getBpPosition() {
			return bpPosition;
		}
		public void setBpPosition(long bpPosition) {
			this.bpPosition = bpPosition;
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
		public double getGeneticMap() {
			return geneticMap;
		}
		public void setGeneticMap(double geneticMap) {
			this.geneticMap = geneticMap;
		}
		public int getLineNumber() {
			return lineNumber;
		}
		public void setLineNumber(int lineNumber) {
			this.lineNumber = lineNumber;
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
	
	private void readAnnotCSV() throws IOException{
		BufferedReader in = IOUtils.openFile(annotCSV);
		String line;
		List<String> header=null;
		int colIndexRsId=-1;
		int colChrom=-1;
		int colPosition=-1;
		int colProbSetId=-1;
		int colAlleleA=-1;
		int colAlleleB=-1;
		int colGeneticMap=-1;
		while((line=in.readLine())!=null){
			if(line.startsWith("#")) continue;
			header=mappingSplit(line);

			for(int i=0;i< header.size();++i){
				if(header.get(i).equals(COL_CHROMOSOME)){
					if(colChrom!=-1) throw new IOException("Duplicate "+COL_CHROMOSOME+" in "+line);
					colChrom=i;
				}else if(header.get(i).equals(COL_POSITION)){
					if(colPosition!=-1) throw new IOException("Duplicate "+COL_POSITION+" in "+line);
					colPosition=i;
				}else if(header.get(i).equals(COL_DBSNP_RS_ID)){
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
				}else if (header.get(i).equals(COL_GENETIC_MAP)){
					if (colGeneticMap!=-1) throw new IOException("Duplicate "+COL_GENETIC_MAP+" in "+line);
					colGeneticMap=i;
				}
			}
			if(colChrom==-1) throw new IOException("Cannot get "+COL_CHROMOSOME+" in "+line);
			if(colPosition==-1) throw new IOException("Cannot get "+COL_POSITION+" in "+line);
			if(colIndexRsId==-1) throw new IOException("Cannot get "+COL_DBSNP_RS_ID+" in "+line);
			if(colProbSetId==-1) throw new IOException("Cannot get "+COL_PROBSET_ID+" in "+line);
			if(colAlleleA==-1) throw new IOException("Cannot get "+COL_ALLELE_A+" in "+line);
			if(colAlleleB==-1) throw new IOException("Cannot get "+COL_ALLELE_B+" in "+line);
			if(colGeneticMap==-1) throw new IOException("Cannot get "+COL_GENETIC_MAP+" in "+line);
			break;
		}
		if(header==null) throw new IOException("header not found in "+annotCSV.getAbsolutePath());

		int lineNumber=0;
		while((line=in.readLine())!=null){
			List<String> tokens=mappingSplit(line);
			if(tokens.size()!=header.size()){
				throw new IOException("expected "+header.size()+" columns but got "+tokens.size()+" in "+line);
			}
			if (tokens.get(colIndexRsId).equals("---")){
				continue;
			}
			AnnotProbes marker=new AnnotProbes();
			marker.setAffyProbeName(tokens.get(colProbSetId));
			if(this.affyID2annotProbe.containsKey(marker.getAffyProbeName())){
				throw new IOException("?! duplicate probeSet Id "+marker.getAffyProbeName());
			}
			marker.setRsName(tokens.get(colIndexRsId));
			marker.setChromosome(tokens.get(colChrom));
			if (tokens.get(colPosition).equals("---")){
				marker.setBpPosition(0);
			}else{
				marker.setBpPosition(Long.parseLong(tokens.get(colPosition)));
			}
			if(tokens.get(colAlleleA).length()!=1) throw new IOException("?! bad alleleA "+tokens.get(colAlleleA));
			marker.setAlleleA(tokens.get(colAlleleA).charAt(0));
			if(tokens.get(colAlleleB).length()!=1) throw new IOException("?! bad alleleB "+tokens.get(colAlleleB));
			marker.setAlleleB(tokens.get(colAlleleB).charAt(0));
			marker.setGeneticMap(parseGeneticMap(tokens.get(colGeneticMap)));
			marker.setLineNumber(lineNumber);
			this.affyID2annotProbe.put(marker.getAffyProbeName(), marker);
			lineNumber++;
		}
		in.close();
		affyID2annotProbeSize = affyID2annotProbe.size();
	}
	
	private double parseGeneticMap(String gmLine) {
		String[] contentByCons = gmLine.split("///");
		if (contentByCons.length<geneticMapConsortium+1){
			return 0.0d;
		}
		String cons = contentByCons[geneticMapConsortium];
		String[] keep = cons.split("//");
		if (keep[0].equals("---")){
			return 0.0d;
		}
		return Double.parseDouble(keep[0]);
	}

	private void readSampleNamesToCallNames() throws IOException{
		BufferedReader in = IOUtils.openFile(sampleNamesToCallNames);
		String line;
		while((line=in.readLine())!=null){
			if (line.startsWith("axiom_calls") || line.isEmpty()){
				continue;
			}
			String[] lineSplit = line.split("\\t");
			if (lineSplit.length!=3){
				throw new RuntimeException("Expected 3 columns in "+sampleNamesToCallNames.getAbsolutePath()+" but got line :"+line);
			}
			File axiomCall = new File(lineSplit[0]);
			if (!axiomCall.exists() || !axiomCall.canRead()){
				throw new FileNotFoundException("Cannot read "+axiomCall.getAbsolutePath());
			}
			String sampleName = lineSplit[1];
			if (sampleName.isEmpty()){
				throw new RuntimeException("Empty sample_name in "+line);
			}
			if (samplesToFindInCalls.containsKey(axiomCall)  && samplesToFindInCalls.get(axiomCall).containsKey(sampleName)){
				throw new RuntimeException("Duplicate sample name "+sampleName+" in "+sampleNamesToCallNames.getAbsolutePath()+" for line "+line);
			}
			String sampleCel = lineSplit[2];
			if (sampleCel.isEmpty()){
				throw new RuntimeException("Empty sample_original_name in "+line);
			}
			if (samplesToFindInCalls.containsKey(axiomCall)  && samplesToFindInCalls.get(axiomCall).containsValue(sampleCel)){
				throw new RuntimeException("Duplicate sample cel "+sampleCel+" in "+sampleNamesToCallNames.getAbsolutePath()+" for line "+line);
			}
			if (samplesToFindInCalls.containsKey(axiomCall)){
				samplesToFindInCalls.get(axiomCall).put(sampleName, sampleCel);
			}else{
				Map<String, String> sampleName2Cel = new HashMap<String, String>();
				sampleName2Cel.put(sampleName, sampleCel);
				samplesToFindInCalls.put(axiomCall, sampleName2Cel);
			}
		}
	}
	
	private static String alleles2String(AnnotProbes marker,int opcode){
		String alleleString = "";
		switch(opcode){
			case -1: alleleString = "0 0";break;
			case 0: alleleString = marker.getAlleleA()+" "+marker.getAlleleA();break;
			case 1: alleleString = marker.getAlleleA()+" "+marker.getAlleleB();break;
			case 2: alleleString = marker.getAlleleB()+" "+marker.getAlleleB();break;
			default: throw new IllegalArgumentException("bad allele code: "+opcode);
		}
		return alleleString;
	}
	
	private void readCalls(File calls, Map<String, String> samplesToFind) throws IOException{
		BufferedReader in = IOUtils.openFile(calls);
		
		String line;
		String[] header=null;
		Map<String, Integer> sampleNames2colNumber = new HashMap<String, Integer>();

		while((line=in.readLine())!=null){
			if(line.startsWith("#")) continue;
			if(!line.startsWith("probeset_id")) throw new IOException("Expected line to start with probeset_id but got "+line);
			header = line.split("\\t");
			for(int i=1; i < header.length; i++){
				if (!samplesToFind.containsValue(header[i])){
					continue;
				}
				String celNameInCalls = header[i];
				for(String sampleName :samplesToFind.keySet()){
					if (celNameInCalls.equals(samplesToFind.get(sampleName))){
						sampleNames2colNumber.put(sampleName, i);
						break;
					}
				}
			}
			break;
		}
		if(header==null) throw new IOException("header not found in "+calls.getAbsolutePath());
		while((line=in.readLine())!=null){
			String tokens[] = line.split("\\t");
			if(tokens.length!=header.length){
				throw new IOException("expected "+header.length+" columns but got "+tokens.length+" in "+line);
			}
 			AnnotProbes probe=affyID2annotProbe.get(tokens[0]);
			if(probe!=null){
				int lineNumber = probe.getLineNumber();
				for (String sampleName : sampleNames2colNumber.keySet()){				
					int colNumber = indID2PedigreeIndividual.get(sampleName).getColNumber();
					int offset = colNumber+(lineNumber*indID2PedigreeIndividualSize);
					raf.seek(offset);
					int genotype=Integer.parseInt(tokens[sampleNames2colNumber.get(sampleName)]);
					switch(genotype){
						case -1: case 0: case 1: case 2: break;
						default: throw new IOException("Bad genotype code: "+genotype);
					}
					raf.writeByte((byte)genotype);
				}
			}
		}
		in.close();
	}
	
	private void writeTfiles() throws IOException{
		File tped = new File(plinkFolder, plinkFileName+".tped");
		File tfam = new File(plinkFolder, plinkFileName+".tfam");
		
		PrintWriter outped = new PrintWriter(new FileWriter(tped));
		PrintWriter outfam = new PrintWriter(new FileWriter(tfam));
		
		byte[] genotypes = new byte[indID2PedigreeIndividualSize];
		for (int i = 0; i < orderedAnnotProbes.length; i++) {
			AnnotProbes p = orderedAnnotProbes[i];
			outped.print(	p.getChromosome()+"\t"+
							p.getRsName()+"\t"+
							p.getGeneticMap()+"\t"+
							p.getBpPosition());
			raf.seek(i*indID2PedigreeIndividualSize);
			raf.readFully(genotypes);
			for (byte b : genotypes) {
				outped.print("\t"+alleles2String(p, b));
			}
			outped.print("\n");
		}
		
		for (int i = 0; i < orderedPedIndividuals.length; i++) {
			PedigreeIndividual p = orderedPedIndividuals[i];
			outfam.print(	p.getFamID()+"\t"+
							p.getIndID()+"\t"+
							p.getFatherID()+"\t"+
							p.getMotherID()+"\t"+
							p.getSex()+"\t"+
							p.getStatus()+"\n");
		}
		
		outped.flush();
		outped.close();
		outfam.flush();
		outfam.close();
	}
}
