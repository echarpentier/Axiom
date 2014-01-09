package fr.pfgen.axiom.client.services;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import fr.pfgen.axiom.shared.records.PlateRecord;

@RemoteServiceRelativePath("PlatesService")
public interface PlatesService extends GenericGwtRpcService<PlateRecord>  {
	List<String> getPlateNames();
	int getNumberOfPlatesInProject(String projectName);
}