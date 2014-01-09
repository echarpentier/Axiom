package fr.pfgen.axiom.client.services;

import java.util.List;
import java.util.Map;
import fr.pfgen.axiom.shared.records.ArrayImageRecord;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ArrayImagesServiceAsync {
	void fetch(Integer startRow, Integer endRow, String sortBy, Map<String, String> filterCriteria, AsyncCallback<List<ArrayImageRecord>> callback) throws IllegalArgumentException;
	void downloadThumbnailsPdf(String plateName,AsyncCallback<String> callback) throws IllegalArgumentException;
}