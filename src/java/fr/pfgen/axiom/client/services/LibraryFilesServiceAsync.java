package fr.pfgen.axiom.client.services;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LibraryFilesServiceAsync {
	void getLibraryFilesFolderNames(AsyncCallback<List<String>> callback) throws IllegalArgumentException;
}
