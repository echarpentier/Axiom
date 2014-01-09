package fr.pfgen.axiom.client.services;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath ("QCService")
public interface QCService extends RemoteService{
	String performQC();
	int NbSamplesWithoutQC();
	String QCProgress();
}
