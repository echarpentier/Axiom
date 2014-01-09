package fr.pfgen.axiom.client.services;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SNPListsServiceAsync {
	void createNewList(String listName, List<String> snpList, AsyncCallback<String> callback) throws IllegalArgumentException;
	void getListNames(AsyncCallback<List<String>> asyncCallback) throws IllegalArgumentException;
	void getGraphForGenoAnalysis(String genoName, String listName, boolean plateColor, boolean priors, boolean posteriors, String annotFileName, AsyncCallback<String> asyncCallback) throws IllegalArgumentException;
	void getGraphForStudy(String studyName, String listName, boolean plateColor, boolean ellipse, String subPopName, String annotFileName, AsyncCallback<String> asyncCallback) throws IllegalArgumentException;
	void deleteSNPList(String listName, AsyncCallback<Boolean> asyncCallback) throws IllegalArgumentException;
	void getSNPinList(String valueAsString, AsyncCallback<List<String>> asyncCallback) throws IllegalArgumentException;
}
