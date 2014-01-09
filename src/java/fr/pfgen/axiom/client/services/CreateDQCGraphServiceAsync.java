package fr.pfgen.axiom.client.services;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface CreateDQCGraphServiceAsync {
	void createDQCGraph(AsyncCallback<String> callback) throws IllegalArgumentException;
	void createParamBoxplot(List<String> genoNames, AsyncCallback<String> asyncCallback) throws IllegalArgumentException;
}
