/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.pfgen.axiom.client.services;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import fr.pfgen.axiom.shared.records.FamilyRecord;
import java.util.List;

/**
 *
 * @author eric
 */
@RemoteServiceRelativePath ("FamiliesService")
public interface FamiliesService extends GenericGwtRpcService<FamilyRecord>{
    List<String> getFamiliesNames();
}
