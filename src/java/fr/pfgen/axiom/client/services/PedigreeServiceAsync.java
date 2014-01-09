package fr.pfgen.axiom.client.services;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

import fr.pfgen.axiom.shared.records.GenotypingQCRecord;
import fr.pfgen.axiom.shared.records.PedigreeRecord;
import fr.pfgen.axiom.shared.records.PedigreeState;

public interface PedigreeServiceAsync extends GenericGwtRpcServiceAsync<PedigreeRecord>{
	void checkPedigreeState(String studyName, int userID, AsyncCallback<PedigreeState> asyncCallback) throws IllegalArgumentException;
	void checkUploadedPedigree(String studyName, int userID, String fileName, AsyncCallback<Map<String, String>> asynCallback) throws IllegalArgumentException;
	void checkSamplesInPedigree(String studyName, int userID, AsyncCallback<Map<String, List<String>>> asyncCallback) throws IllegalArgumentException;
	void validatePedigree(String studyName, int userID, AsyncCallback<Boolean> asyncCallback) throws IllegalArgumentException;
	void invalidatePedigree(String studyName, int userID, AsyncCallback<Boolean> asyncCallback) throws IllegalArgumentException;
	void checkIndividualsInPedigree(String studyName, int userID, AsyncCallback<Map<String,List<String>>> asyncCallback) throws IllegalArgumentException;
	void checkGendersInPedigree(String studyName, int userID, AsyncCallback<Map<String, Map<String, String>>> asyncCallback);
	void visualizePedigree(String studyName, int userID, AsyncCallback<String> asyncCallback) throws IllegalArgumentException;
	void checkDuplicateSamplesInCalls(String studyName, AsyncCallback<Map<String, List<GenotypingQCRecord>>> asyncCallback) throws IllegalArgumentException;
	void getSubPopNames(String studyName, AsyncCallback<List<String>> asyncCallback) throws IllegalArgumentException;
	void checkUploadedSubPopulation(String studyName, String subPopName, String filePath, AsyncCallback<Map<String, String>> asyncCallback) throws IllegalArgumentException;
}
