/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.pfgen.axiom.server.services;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import fr.pfgen.axiom.client.services.FamiliesService;
import fr.pfgen.axiom.server.database.ConnectionPool;
import fr.pfgen.axiom.server.database.FamiliesTable;
import fr.pfgen.axiom.shared.GenericGwtRpcList;
import fr.pfgen.axiom.shared.records.FamilyRecord;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 *
 * @author eric
 */
public class FamiliesServiceImpl extends RemoteServiceServlet implements FamiliesService {

    private ConnectionPool pool;
    //private Hashtable<String, File> appFiles; 

    //@SuppressWarnings("unchecked")
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        pool = (ConnectionPool) getServletContext().getAttribute("ConnectionPool");
        //appFiles = (Hashtable<String, File>)getServletContext().getAttribute("ApplicationFiles");
    }

    @Override
    public List<FamilyRecord> fetch(Integer startRow, Integer endRow, String sortBy, Map<String, String> filterCriteria) {
        GenericGwtRpcList<FamilyRecord> outList = new GenericGwtRpcList<FamilyRecord>();
    
    	List<FamilyRecord> out = FamiliesTable.getFamilies(pool,startRow,endRow,sortBy,filterCriteria);
    	
    	outList.addAll(out);
    	outList.setTotalRows(out.size());
    	
    	return outList;
    }

    @Override
    public FamilyRecord add(FamilyRecord data) {
        return FamiliesTable.addFamily(pool, data);
    }

    @Override
    public FamilyRecord update(FamilyRecord data) {
        return FamiliesTable.updateFamily(pool, data);
    }

    @Override
    public void remove(FamilyRecord data) {
        FamiliesTable.removeFamily(pool, data);
    }

    @Override
    public String download(String sortBy, Map<String, String> filterCriteria) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<String> getFamiliesNames() {
        return FamiliesTable.getFamiliesNames(pool);
    }
}
