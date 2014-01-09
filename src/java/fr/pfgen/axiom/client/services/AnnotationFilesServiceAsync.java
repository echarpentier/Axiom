package fr.pfgen.axiom.client.services;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AnnotationFilesServiceAsync {
	void getAnnotationFilesNames(AsyncCallback<List<String>> callback) throws IllegalArgumentException;
}
