package fr.pfgen.axiom.client.services;

import fr.pfgen.axiom.shared.records.UserRecord;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("loginService")
public interface LoginService extends RemoteService {
	UserRecord loginServer(String userName, String password) throws IllegalArgumentException;
	UserRecord checkUserLogin() throws IllegalArgumentException;
}
