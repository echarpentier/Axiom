package fr.pfgen.axiom.client.services;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

import fr.pfgen.axiom.shared.records.SampleQCRecord;

public interface SamplesQCServiceAsync extends GenericGwtRpcServiceAsync<SampleQCRecord>{
	void getQcParams(AsyncCallback<List<String>> callback) throws IllegalArgumentException;
	void getUserParams(AsyncCallback<List<String>> asyncCallback) throws IllegalArgumentException;
	void getAllQcParams(AsyncCallback<List<String>> asyncCallback);
	void nbSamplesBelowQcInGenoAnalysis(String genoName, AsyncCallback<Integer> callback) throws IllegalArgumentException;
	void checkUploadedQCForPlate(String plateName, String filePath, AsyncCallback<Map<String, String>> asyncCallback) throws IllegalArgumentException;
	void addUserQCForPlate(String plateName, String filePath, AsyncCallback<String> asyncCallback) throws IllegalArgumentException;
	void makeQCGraphForPlate(String plateName, String xAxis, String yAxis, AsyncCallback<String> asyncCallback);
	void addUserQCForPopulation(String populationName, String filePath, AsyncCallback<String> asyncCallback) throws IllegalArgumentException;
	void checkUploadedQCForPopulation(String populationName, String filePath, AsyncCallback<Map<String, String>> asyncCallback) throws IllegalArgumentException;
	void makeQCGraphForPopulation(String populationName, String xAxis, String yAxis, AsyncCallback<String> asyncCallback) throws IllegalArgumentException;
	void makeQCTsvForPopulation(String populationName, String xAxis, String yAxis, AsyncCallback<String> asyncCallback) throws IllegalArgumentException;
	void makeQCTsvForPlate(String plateName, String xAxis, String yAxis, AsyncCallback<String> asyncCallback) throws IllegalArgumentException;
}
