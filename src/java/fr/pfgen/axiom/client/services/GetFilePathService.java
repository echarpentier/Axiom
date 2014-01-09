package fr.pfgen.axiom.client.services;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@Deprecated
@RemoteServiceRelativePath("FilePathService")
public interface GetFilePathService extends RemoteService{
	String getDQCGraphPath();
	String getPIGGraphPath(String genoName);
}
