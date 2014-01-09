package fr.pfgen.axiom.client.services;

import java.util.List;
import java.util.Map;
import fr.pfgen.axiom.shared.records.ArrayImageRecord;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath ("ArrayImagesService")
public interface ArrayImagesService extends RemoteService  {
	List<ArrayImageRecord> fetch(Integer startRow, Integer endRow, String sortBy, Map<String, String> filterCriteria);
	String downloadThumbnailsPdf(String plateName);
}