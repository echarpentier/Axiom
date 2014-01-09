package fr.pfgen.axiom.client.services;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import fr.pfgen.axiom.shared.records.GenotypingAnalysisRecord;

public interface GenotypingAnalysisServiceAsync extends GenericGwtRpcServiceAsync<GenotypingAnalysisRecord>{
	void nbSamplesInGenoRun(String genoName, String run, AsyncCallback<Integer> asyncCallback) throws IllegalArgumentException;
	void nbSamplesInGenoAnalysis(String genoName, AsyncCallback<Integer> asyncCallback) throws IllegalArgumentException;
	void studiesLinkedToGenoAnalysis(int genoID, AsyncCallback<List<String>> asyncCallback) throws IllegalArgumentException;
	void getLibraryNameForGeno(String genoName, AsyncCallback<String> asyncCallback);
}
