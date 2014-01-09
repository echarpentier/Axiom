package fr.pfgen.axiom.client.services;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>QCService</code>.
 */
public interface QCServiceAsync {
	void performQC(AsyncCallback<String> callback) throws IllegalArgumentException;
	void NbSamplesWithoutQC(AsyncCallback<Integer> callback) throws IllegalArgumentException;
	void QCProgress(AsyncCallback<String> callback) throws IllegalArgumentException;
}