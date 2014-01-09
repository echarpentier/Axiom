package fr.pfgen.axiom.client.services;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import fr.pfgen.axiom.shared.records.PopulationRecord;

@RemoteServiceRelativePath ("PopulationsService")
public interface PopulationsService extends GenericGwtRpcService<PopulationRecord> {
	List<String> getPopulationNames();
}