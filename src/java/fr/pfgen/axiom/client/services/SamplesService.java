package fr.pfgen.axiom.client.services;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import fr.pfgen.axiom.shared.records.SampleRecord;

@RemoteServiceRelativePath ("SamplesService")
public interface SamplesService extends GenericGwtRpcService<SampleRecord> {
	int nbSamplesInPopulation(String projectName);
	int nbSamplesInPlate(String plateName);
	int nbSamplesInProjectWithoutQC(String projectName);
	int nbSamplesInPlateWithoutQC(String plateName);
	int nbSamplesWithoutQC(List<Integer> sampleList);
}