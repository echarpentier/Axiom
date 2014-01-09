package fr.pfgen.axiom.client.services;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface GenotypingServiceAsync {
	void performGenotyping(List<Integer> sampleIdList, String genotypingName, double dishQCLimit, double callRateLimit, int userID, String libraryFilesFolder, String annotationFile, AsyncCallback<String> callback) throws IllegalArgumentException;
	void getGenotypingNames(AsyncCallback<List<String>> callback) throws IllegalArgumentException;
}
