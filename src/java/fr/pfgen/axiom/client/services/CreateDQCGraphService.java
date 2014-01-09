package fr.pfgen.axiom.client.services;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath ("DQCGraphService")
public interface CreateDQCGraphService extends RemoteService{
	String createDQCGraph();
	String createParamBoxplot(List<String> genoNames);
}
