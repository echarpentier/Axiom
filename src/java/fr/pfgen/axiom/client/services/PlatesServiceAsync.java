package fr.pfgen.axiom.client.services;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import fr.pfgen.axiom.shared.records.PlateRecord;

public interface PlatesServiceAsync extends GenericGwtRpcServiceAsync<PlateRecord> {

    void getPlateNames(AsyncCallback<List<String>> asyncCallback) throws IllegalArgumentException;

    void getNumberOfPlatesInProject(String projectName, AsyncCallback<Integer> asyncCallback) throws IllegalArgumentException;
}