package fr.pfgen.axiom.client.services;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>LoginService</code>.
 */
public interface UpdateDatabaseServiceAsync {
	void updateDatabase(AsyncCallback<String> callback) throws IllegalArgumentException;
}