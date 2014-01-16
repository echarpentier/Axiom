/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.pfgen.axiom.client.services;

import com.google.gwt.user.client.rpc.AsyncCallback;
import fr.pfgen.axiom.shared.records.FamilyRecord;
import java.util.List;

/**
 *
 * @author eric
 */
public interface FamiliesServiceAsync extends GenericGwtRpcServiceAsync<FamilyRecord>{
    void getFamiliesNames (AsyncCallback<List<String>> asyncCallback) throws IllegalArgumentException;;
}
