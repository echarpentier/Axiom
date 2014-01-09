package fr.pfgen.axiom.server.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import fr.pfgen.axiom.server.beans.AnnotProbe;
import fr.pfgen.axiom.server.beans.ModelProbe;

public class ClusterGraphModelAnnot {

	private List<String> snpList;
	private Map<String, File> models;
	private File annotFile;
	private File snpListFile;
	private File tmpModelFile;
	private Map<String, AnnotProbe> probeName2AnnotProbe;
	private Map<String, Map<String, ModelProbe>> probeName2ModelMap = new HashMap<String, Map<String,ModelProbe>>();

	public ClusterGraphModelAnnot(Map<String, File> models, File annotFile, File snpListFile, File tmpModelFile) {
		super();
		this.models = models;
		this.annotFile = annotFile;
		this.snpListFile = snpListFile;
		this.tmpModelFile = tmpModelFile;
	}

	public String createCgModelAnnotFile() {
		snpList = SnpListUtils.readSnpListFile(snpListFile);
		readAnnotFile();
		if (!models.isEmpty()){
			readModels();
		}
		writeFile();
		
		return tmpModelFile.getAbsolutePath();
	}
	
	private void writeFile(){
		PrintWriter pwModels = null;
		
		try{
			pwModels = new PrintWriter(new FileWriter(tmpModelFile));
			pwModels.print("affyProbeName\trsName\talleleA\talleleB\tfreqAlleleA\tfreqAlleleB");
			if (!models.isEmpty()){
				pwModels.println(	"\tmodelName\tBBmeanX\tBBmeanY\tBBvarX\tBBvarY\tBBcovarXY\t"+
									"ABmeanX\tABmeanY\tABvarX\tABvarY\tABcovarXY\t"+
									"AAmeanX\tAAmeanY\tAAvarX\tAAvarY\tAAcovarXY");
			}else{
				pwModels.println();
			}
			for (String probeName : probeName2AnnotProbe.keySet()){
				AnnotProbe annotProbe = probeName2AnnotProbe.get(probeName);
				if (!models.isEmpty()){
					Map<String, ModelProbe> model2ModelProbe = probeName2ModelMap.get(probeName);
					for (String modelName : model2ModelProbe.keySet()) {
						ModelProbe probe = model2ModelProbe.get(modelName);
						pwModels.println(	probeName+"\t"+annotProbe.getRsName()+"\t"+annotProbe.getAlleleA()+"\t"+annotProbe.getAlleleB()+"\t"+annotProbe.getFreqAlleleA()+"\t"+annotProbe.getFreqAlleleB()+"\t"+probe.getModelName()+"\t"+
											probe.getBBmeanX()+"\t"+probe.getBBmeanY()+"\t"+probe.getBBvarX()+"\t"+probe.getBBvarY()+"\t"+probe.getBBcovarXY()+"\t"+
											probe.getABmeanX()+"\t"+probe.getABmeanY()+"\t"+probe.getABvarX()+"\t"+probe.getABvarY()+"\t"+probe.getABcovarXY()+"\t"+
											probe.getAAmeanX()+"\t"+probe.getAAmeanY()+"\t"+probe.getAAvarX()+"\t"+probe.getAAvarY()+"\t"+probe.getAAcovarXY()
						);					
					}
				}else{
					pwModels.println(probeName+"\t"+annotProbe.getRsName()+"\t"+annotProbe.getAlleleA()+"\t"+annotProbe.getAlleleB()+"\t"+annotProbe.getFreqAlleleA()+"\t"+annotProbe.getFreqAlleleB());
				}
			}
			pwModels.close();
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			IOUtils.safeClose(pwModels);
		}
	}

	private void readAnnotFile(){
		AnnotFileReader annotReader = new AnnotFileReader(annotFile);
		probeName2AnnotProbe = annotReader.getAnnotations(snpList);
	}
	
	private void readModels(){
		for (String modelName : models.keySet()) {
			File modelFile = models.get(modelName);
			Map<String, ModelProbe> probeName2ModelProbe = readModelFile(modelFile, modelName);
			for (String probeName : probeName2ModelProbe.keySet()) {
				if (probeName2ModelMap.get(probeName)==null){
					Map<String, ModelProbe> model2ModelProbe = new HashMap<String, ModelProbe>();
					model2ModelProbe.put(modelName, probeName2ModelProbe.get(probeName));
					probeName2ModelMap.put(probeName, model2ModelProbe);
				}else{
					Map<String, ModelProbe> model2ModelProbe = probeName2ModelMap.get(probeName);
					model2ModelProbe.put(modelName, probeName2ModelProbe.get(probeName));
				}
			}
		}
	}
	
	private Map<String, ModelProbe> readModelFile(File modelFile, String modelName){
		BufferedReader in = null;
		Pattern tab = Pattern.compile("[\t]");
		Pattern comma = Pattern.compile("[,]");
		Pattern twoDots = Pattern.compile("[:]");
		Set<String> probesToFind = new HashSet<String>(probeName2AnnotProbe.keySet());
		Map<String, ModelProbe> probeName2ModelProbe = new HashMap<String, ModelProbe>();
		
		try{
			in = IOUtils.openFile(modelFile);

			String line;
			while((line=in.readLine())!=null && probesToFind.size()>0){
				if (line.startsWith("#") || line.startsWith("id")){
					continue;
				}
				String[] lineSplit = tab.split(line);
				if (lineSplit[0].contains(":")){
					lineSplit[0] = twoDots.split(lineSplit[0])[0];
				}
				if (probesToFind.contains(lineSplit[0])){
					ModelProbe probe = new ModelProbe();
					probesToFind.remove(lineSplit[0]);
					String[] BBsplit = comma.split(lineSplit[1]);
					String[] ABsplit = comma.split(lineSplit[2]);
					String[] AAsplit = comma.split(lineSplit[3]);
					
					probe.setProbesetID(lineSplit[0]);
					probe.setModelName(modelName);

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
					probeName2ModelProbe.put(lineSplit[0], probe);
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			IOUtils.safeClose(in);
		}
		
		return probeName2ModelProbe;
	}

}
