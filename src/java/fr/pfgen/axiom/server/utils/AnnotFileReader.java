package fr.pfgen.axiom.server.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.pfgen.axiom.server.beans.AnnotProbe;

public class AnnotFileReader {

	private File annotFile;
	private static final String COL_PROBSET_ID="Probe Set ID";
	private static final String COL_DBSNP_RS_ID="dbSNP RS ID";
	private static final String COL_CHROMOSOME="Chromosome";
	private static final String COL_POSITION="Physical Position";
	private static final String COL_ALLELE_A="Allele A";
	private static final String COL_ALLELE_B="Allele B";
	private static final String COL_FREQUENCIES="Allele Frequencies";
	
	public AnnotFileReader(File annotFile){
		if (annotFile.exists()){
			this.annotFile = annotFile;
		}else{
			throw new RuntimeException("Cannot find annotation file: "+annotFile.getAbsolutePath());
		}
	}
	
	public Map<String, AnnotProbe> getAnnotations(List<String> probesToFind){
		
		Map<String, AnnotProbe> probeName2probe = new HashMap<String, AnnotProbe>();
		
		BufferedReader br = null;
		
		try{
			br = new BufferedReader(new FileReader(annotFile));
			String line;
			List<String> header=null;
			int colIndexRsId=-1;
			int colChrom=-1;
			int colPosition=-1;
			int colProbSetId=-1;
			int colAlleleA=-1;
			int colAlleleB=-1;
			int colFrequencies=-1;
			while((line=br.readLine())!=null){
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
					}else if(header.get(i).equals(COL_FREQUENCIES)){
						if(colFrequencies!=-1) throw new IOException("Duplicate "+COL_FREQUENCIES+" in "+line);
						colFrequencies=i;
					}
				}
				if(colChrom==-1) throw new IOException("Cannot get "+COL_CHROMOSOME+" in "+line);
				if(colPosition==-1) throw new IOException("Cannot get "+COL_POSITION+" in "+line);
				if(colIndexRsId==-1) throw new IOException("Cannot get "+COL_DBSNP_RS_ID+" in "+line);
				if(colProbSetId==-1) throw new IOException("Cannot get "+COL_PROBSET_ID+" in "+line);
				if(colAlleleA==-1) throw new IOException("Cannot get "+COL_ALLELE_A+" in "+line);
				if(colAlleleB==-1) throw new IOException("Cannot get "+COL_ALLELE_B+" in "+line);
				if(colFrequencies==-1) throw new IOException("Cannot get "+COL_FREQUENCIES+" in "+line);
				break;
			}
			if(header==null) throw new IOException("header not found in "+annotFile.getAbsolutePath());

			Integer snpFound = null;
			if (probesToFind!=null && !probesToFind.isEmpty()){
				snpFound = probesToFind.size();
			}
			
			while(((line=br.readLine())!=null) && (snpFound==null || snpFound>0)){
				List<String> tokens=mappingSplit(line);
				if(tokens.size()!=header.size()){
					throw new IOException("expected "+header.size()+" columns but got "+tokens.size()+" in "+line);
				}
				
				if (probesToFind==null || probesToFind.isEmpty() || probesToFind.contains(tokens.get(colProbSetId)) || probesToFind.contains(tokens.get(colIndexRsId))){
					AnnotProbe marker = new AnnotProbe();
					marker.setProbesetID(tokens.get(colProbSetId));
					marker.setRsName(tokens.get(colIndexRsId));
					marker.setChr(tokens.get(colChrom));
					if (tokens.get(colPosition).equals("---")){
						marker.setPos(0);
					}else{
						marker.setPos(Long.parseLong(tokens.get(colPosition)));
					}
					if(tokens.get(colAlleleA).length()!=1) throw new IOException("?! bad alleleA "+tokens.get(colAlleleA));
					marker.setAlleleA(tokens.get(colAlleleA).charAt(0));
					if(tokens.get(colAlleleB).length()!=1) throw new IOException("?! bad alleleB "+tokens.get(colAlleleB));
					marker.setAlleleB(tokens.get(colAlleleB).charAt(0));
					marker.setFreqAlleleA(parseFreqLine(tokens.get(colFrequencies),"A", tokens.get(colIndexRsId)));
					marker.setFreqAlleleB(parseFreqLine(tokens.get(colFrequencies),"B", tokens.get(colIndexRsId)));
					probeName2probe.put(marker.getProbesetID(),marker);
					if (probesToFind!=null && !probesToFind.isEmpty()){
						snpFound--;
					}
				}
			}
			br.close();
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			IOUtils.safeClose(br);
		}
		
		return probeName2probe;
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
}
