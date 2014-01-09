package fr.pfgen.axiom.client.services;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import fr.pfgen.axiom.shared.records.UserRecord;

@RemoteServiceRelativePath ("UsersService")
public interface UsersService extends GenericGwtRpcService<UserRecord>{

}
