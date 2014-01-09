package fr.pfgen.axiom.client.services;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import fr.pfgen.axiom.shared.records.GenotypingQCRecord;
import fr.pfgen.axiom.shared.records.PedigreeRecord;
import fr.pfgen.axiom.shared.records.PedigreeState;

@RemoteServiceRelativePath("PedigreeService")
public interface PedigreeService extends GenericGwtRpcService<PedigreeRecord>{
	PedigreeState checkPedigreeState(String studyName, int userID);
	Map<String, String> checkUploadedPedigree(String studyName, int userID, String fileName);
	Map<String, List<String>> checkSamplesInPedigree(String studyName, int userID);
	Boolean validatePedigree(String studyName, int userID);
	Boolean invalidatePedigree(String studyName, int userID);
	Map<String, List<String>> checkIndividualsInPedigree(String studyName, int userID);
	Map<String, Map<String, String>> checkGendersInPedigree(String studyName, int userID);
	String visualizePedigree(String studyName, int userID);
	Map<String, List<GenotypingQCRecord>> checkDuplicateSamplesInCalls(String studyName);
	List<String> getSubPopNames(String studyName);
	Map<String, String> checkUploadedSubPopulation(String studyName, String subPopName, String filePath);
}
