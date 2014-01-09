package fr.pfgen.axiom.client.services;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath ("UpdateDatabaseService")
public interface UpdateDatabaseService extends RemoteService {
	String updateDatabase() throws IllegalArgumentException;
}