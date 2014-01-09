package fr.pfgen.axiom.client.services;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath ("AnnotationFilesService")
public interface AnnotationFilesService extends RemoteService {
	List<String> getAnnotationFilesNames();
}
