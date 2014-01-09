package fr.pfgen.axiom.client.services;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath ("GenotypingService")
public interface GenotypingService extends RemoteService{
	String performGenotyping(List<Integer> sampleIdList, String genotypingName, double dishQCLimit, double callRateLimit, int userID, String libraryFilesFolder, String annotationFile);
	List<String> getGenotypingNames();
}
