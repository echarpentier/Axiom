package fr.pfgen.axiom.client.services;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import fr.pfgen.axiom.shared.records.SampleRecord;

public interface SamplesServiceAsync extends GenericGwtRpcServiceAsync<SampleRecord>{
	void nbSamplesInPopulation(String projectName, AsyncCallback<Integer> callback) throws IllegalArgumentException;
        void nbSamplesInFamily(String familyName, AsyncCallback<Integer> callback) throws IllegalArgumentException;
	void nbSamplesInPlate(String plateName, AsyncCallback<Integer> callback) throws IllegalArgumentException;
	void nbSamplesInProjectWithoutQC(String projectName, AsyncCallback<Integer> callback) throws IllegalArgumentException;
	void nbSamplesInPlateWithoutQC(String plateName, AsyncCallback<Integer> callback) throws IllegalArgumentException;
	void nbSamplesWithoutQC(List<Integer> sampleList, AsyncCallback<Integer> callback) throws IllegalArgumentException;
}