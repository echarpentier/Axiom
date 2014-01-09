package fr.pfgen.axiom.client.services;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import fr.pfgen.axiom.shared.records.GenotypingQCRecord;
import fr.pfgen.axiom.shared.records.StudyRecord;
import fr.pfgen.axiom.shared.records.StudyWorkflowState;

@RemoteServiceRelativePath ("StudiesService")
public interface StudiesService extends GenericGwtRpcService<StudyRecord> {
	String addNewStudy(StudyRecord record);
	String addGenoAnalysisToStudy(String studyName, List<String> genoNameList);
	List<String> getStudyNames(String type);
	Boolean checkGenoSamplesForStudy(String studyName);
	StudyWorkflowState checkWorkflowState(String studyName);
	String addSamplesToStudy(String studyName, List<Integer> genoRunIdList);
	Boolean checkPlinkFilesInStudy(String studyName);
	String generatePlinkFilesForStudy(String studyName, String annotFileName, Map<String, GenotypingQCRecord> samplesChosen);
	String Affy2PlinkProgress(String studyName);
        StudyRecord getStudyInfos(String studyName);
}
