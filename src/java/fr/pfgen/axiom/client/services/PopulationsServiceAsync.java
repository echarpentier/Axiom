package fr.pfgen.axiom.client.services;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import fr.pfgen.axiom.shared.records.PopulationRecord;

public interface PopulationsServiceAsync extends GenericGwtRpcServiceAsync<PopulationRecord>{
	void getPopulationNames (AsyncCallback<List<String>> asyncCallback) throws IllegalArgumentException;;
}