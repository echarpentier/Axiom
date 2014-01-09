package fr.pfgen.axiom.client.services;

import java.util.List;
import java.util.Map;

import fr.pfgen.axiom.shared.records.SampleQCRecord;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath ("SamplesQCService")
public interface SamplesQCService extends GenericGwtRpcService<SampleQCRecord> {
	List<String> getQcParams();
	List<String> getUserParams();
	List<String> getAllQcParams();
	int nbSamplesBelowQcInGenoAnalysis(String genoName);
	Map<String, String> checkUploadedQCForPlate(String plateName, String filePath);
	String addUserQCForPlate(String plateName, String filePath);
	String makeQCGraphForPlate(String plateName, String xAxis, String yAxis);
	String addUserQCForPopulation(String populationName, String filePath);
	Map<String, String> checkUploadedQCForPopulation(String populationName, String filePath);
	String makeQCGraphForPopulation(String populationName, String xAxis, String yAxis);
	String makeQCTsvForPopulation(String populationName, String xAxis, String yAxis);
	String makeQCTsvForPlate(String plateName, String xAxis, String yAxis);
}
