package fr.pfgen.axiom.client.services;

import fr.pfgen.axiom.shared.records.UserRecord;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>LoginService</code>.
 */
public interface LoginServiceAsync {
	void loginServer(String userName, String password, AsyncCallback<UserRecord> callback) throws IllegalArgumentException;
	void checkUserLogin(AsyncCallback<UserRecord> callback) throws IllegalArgumentException;
}
