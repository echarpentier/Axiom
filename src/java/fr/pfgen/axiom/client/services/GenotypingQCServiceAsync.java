package fr.pfgen.axiom.client.services;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import fr.pfgen.axiom.shared.records.GenotypingQCRecord;

public interface GenotypingQCServiceAsync extends GenericGwtRpcServiceAsync<GenotypingQCRecord>{
	void getGenoQcParams(AsyncCallback<List<String>> callback) throws IllegalArgumentException;
	void secondRunExistsInGenoAnalysis(String genoName, AsyncCallback<Boolean> callback) throws IllegalArgumentException;
}
