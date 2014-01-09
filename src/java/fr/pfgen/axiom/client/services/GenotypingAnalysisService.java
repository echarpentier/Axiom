package fr.pfgen.axiom.client.services;

import java.util.List;

import fr.pfgen.axiom.shared.records.GenotypingAnalysisRecord;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath ("GenotypingAnalysisService")
public interface GenotypingAnalysisService extends GenericGwtRpcService<GenotypingAnalysisRecord>{
	int nbSamplesInGenoRun(String genoName, String run);
	int nbSamplesInGenoAnalysis(String genoName);
	List<String> studiesLinkedToGenoAnalysis(int genoID);
	String getLibraryNameForGeno(String genoName);
}
