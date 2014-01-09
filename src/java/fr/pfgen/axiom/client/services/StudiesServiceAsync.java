package fr.pfgen.axiom.client.services;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;

import fr.pfgen.axiom.shared.records.GenotypingQCRecord;
import fr.pfgen.axiom.shared.records.StudyRecord;
import fr.pfgen.axiom.shared.records.StudyWorkflowState;

public interface StudiesServiceAsync extends GenericGwtRpcServiceAsync<StudyRecord> {
	void addNewStudy(StudyRecord record, AsyncCallback<String> callback) throws IllegalArgumentException;
	void addGenoAnalysisToStudy(String studyName, List<String> genoNameList, AsyncCallback<String> asyncCallback) throws IllegalArgumentException;
	void getStudyNames(String type, AsyncCallback<List<String>> asyncCallback) throws IllegalArgumentException;
	void checkGenoSamplesForStudy(String studyName, AsyncCallback<Boolean> asyncCallback) throws IllegalArgumentException;
	void checkWorkflowState(String studyName, AsyncCallback<StudyWorkflowState> asyncCallback) throws IllegalArgumentException;
	void addSamplesToStudy(String studyName, List<Integer> genoRunIdList, AsyncCallback<String> callback) throws IllegalArgumentException;
	void checkPlinkFilesInStudy(String studyName, AsyncCallback<Boolean> callback) throws IllegalArgumentException;
	void generatePlinkFilesForStudy(String studyName, String annotFileName, Map<String, GenotypingQCRecord> samplesChosen, AsyncCallback<String> callback) throws IllegalArgumentException;
	void Affy2PlinkProgress(String studyName, AsyncCallback<String> asyncCallback) throws IllegalArgumentException;
        void getStudyInfos(String studyName, AsyncCallback<StudyRecord> asyncCallback) throws IllegalArgumentException;
}
