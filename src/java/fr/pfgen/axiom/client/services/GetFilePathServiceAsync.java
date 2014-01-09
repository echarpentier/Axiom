package fr.pfgen.axiom.client.services;

import com.google.gwt.user.client.rpc.AsyncCallback;

@Deprecated
public interface GetFilePathServiceAsync {
	void getDQCGraphPath(AsyncCallback<String> callback) throws IllegalArgumentException;
	void getPIGGraphPath(String genoName, AsyncCallback<String> callback) throws IllegalArgumentException;
}
