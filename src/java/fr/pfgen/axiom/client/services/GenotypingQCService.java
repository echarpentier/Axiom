package fr.pfgen.axiom.client.services;

import java.util.List;

import fr.pfgen.axiom.shared.records.GenotypingQCRecord;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath ("GenotypingQCService")
public interface GenotypingQCService extends GenericGwtRpcService<GenotypingQCRecord>{
	List<String> getGenoQcParams();
	boolean secondRunExistsInGenoAnalysis(String genoName);
}
