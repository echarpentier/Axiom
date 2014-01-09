package fr.pfgen.axiom.server.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import fr.pfgen.axiom.client.services.CreateDQCGraphService;
import fr.pfgen.axiom.server.beans.SampleForDQCGraph;
import fr.pfgen.axiom.server.database.ConnectionPool;
import fr.pfgen.axiom.server.database.GenoAnalysisTable;
import fr.pfgen.axiom.server.database.SamplesTable;
import fr.pfgen.axiom.server.utils.IOUtils;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class CreateDQCGraphServiceImpl extends RemoteServiceServlet implements CreateDQCGraphService{

	private ConnectionPool pool;
	private Hashtable<String, File> appFiles; 
	
	@Override
	@SuppressWarnings("unchecked")
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		pool = (ConnectionPool)getServletContext().getAttribute("ConnectionPool");
		appFiles = (Hashtable<String, File>)getServletContext().getAttribute("ApplicationFiles");
	}
	
	@Override
	public synchronized String createDQCGraph() {
		List<SampleForDQCGraph> sampleList = SamplesTable.getSamplesForDQCGraph(pool);
		if (sampleList.isEmpty()){
			return null;
		}
		File dqcGraphFolder = new File(appFiles.get("analysisFile").getAbsolutePath(), "DQCGraph");
		if (!dqcGraphFolder.exists()){
			if (!dqcGraphFolder.mkdirs()){
				throw new RuntimeException("Cannot create "+dqcGraphFolder.getAbsolutePath());
			}
		}
		File samplesFile = new File(dqcGraphFolder, "samples.txt");
		File graph = new File(dqcGraphFolder, "DQCGraph.png");
		
		PrintWriter pw = null;
		try{
			pw = new PrintWriter(new FileWriter(samplesFile));
			pw.println("sampleID\tsampleName\tDQC\tplateID\tplateName");
			for (SampleForDQCGraph sample : sampleList) {
				pw.println(sample.getSample_id()+"\t"+sample.getSample_name()+"\t"+sample.getDqcValue()+"\t"+sample.getPlate_id()+"\t"+sample.getPlate_name());
			}
			pw.flush();
			pw.close();
		}catch(IOException e){
			throw new RuntimeException(e);
		}finally{
			IOUtils.safeClose(pw);
		}
		
		String[] cmd = {appFiles.get("RScriptBin").getAbsolutePath(),appFiles.get("RScripts").getAbsolutePath()+"/DQCValuesPerPlate.R", samplesFile.getAbsolutePath(), graph.getAbsolutePath()};
		
		Process process=null;
		try{
			ProcessBuilder procBuilder = new ProcessBuilder(cmd);
			procBuilder.redirectErrorStream(true);
			process = procBuilder.start();
		
			process.waitFor();
		}catch(IOException e ){
			throw new RuntimeException(e);
		}catch (InterruptedException e) {
			throw new RuntimeException(e);
		}finally{
			if (process!=null){process.destroy();}
		}
		
		return graph.getAbsolutePath();
	}

	@Override
	public synchronized String createParamBoxplot(List<String> genoNames) {
		File rInputFile = new File(appFiles.get("temporaryFolder"), "corr_"+Long.toHexString(Double.doubleToLongBits(Math.random()))+".txt");
		File rOutputPdf = new File(appFiles.get("temporaryFolder"), "box_"+Long.toHexString(Double.doubleToLongBits(Math.random()))+".pdf");
		PrintWriter pw = null;
		
		Process process = null;
		try{

			pw = new PrintWriter(new FileWriter(rInputFile));
			pw.println("genoName\treportFile\tcorrFile");
			for (String genoName : genoNames) {
				File runfolder;
				File genoFolder = new File(GenoAnalysisTable.getGenoPathFromName(pool, genoName));
				if (GenoAnalysisTable.checkIfSecondRunExist(pool, genoName)){
					runfolder = new File(genoFolder, "Second_run");
				}else{
					runfolder = new File(genoFolder, "First_run");
				}
				if (!runfolder.exists()){
					throw new RuntimeException("Cannot find folder "+runfolder.getAbsolutePath());
				}
				File reportFile = new File(runfolder, "AxiomGT1.report.txt");
				if (!reportFile.exists()){
					throw new RuntimeException("Cannot find "+reportFile.getAbsolutePath());
				}
				File corrFile = new File(genoFolder, "QCReport/samples_plates.txt");
				if (!corrFile.exists()){
					throw new RuntimeException("Cannot find "+corrFile.getAbsolutePath());
				}

				pw.println(genoName+"\t"+reportFile.getAbsolutePath()+"\t"+corrFile.getAbsolutePath());
			}
			pw.flush();
			pw.close();
			
			String[] cmdBox = {appFiles.get("RScriptBin").getAbsolutePath(),appFiles.get("RScripts").getAbsolutePath()+"/Box.R",rInputFile.getAbsolutePath(),rOutputPdf.getAbsolutePath()};
			ProcessBuilder procBuilder = new ProcessBuilder(cmdBox);
			procBuilder.redirectErrorStream(true);
			
			process = procBuilder.start();
			process.waitFor();
			process.destroy();
			if (rOutputPdf.exists()){
				return rOutputPdf.getAbsolutePath();
			}else{
				return null;
			}
		}catch(IOException e){
			e.printStackTrace();
			return null;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}finally{
			IOUtils.safeClose(pw);
			if (process!=null){process.destroy();}
		}
	}
}
