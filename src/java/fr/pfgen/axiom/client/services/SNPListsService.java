package fr.pfgen.axiom.client.services;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath ("SNPListsService")
public interface SNPListsService extends RemoteService {
	String createNewList(String listName, List<String> snpList);
	List<String> getListNames();
	String getGraphForGenoAnalysis(String genoName, String listName, boolean plateColor, boolean priors, boolean posteriors, String annotFileName);
	String getGraphForStudy(String studyName, String listName, boolean plateColor, boolean ellipse, String subPopName, String annotFileName);
	Boolean deleteSNPList(String listName);
	List<String> getSNPinList(String valueAsString);
}
