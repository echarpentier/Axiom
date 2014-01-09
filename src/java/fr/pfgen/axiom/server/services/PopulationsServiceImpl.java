package fr.pfgen.axiom.server.services;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import fr.pfgen.axiom.client.services.PopulationsService;
import fr.pfgen.axiom.server.database.ConnectionPool;
import fr.pfgen.axiom.server.database.PopulationsTable;
import fr.pfgen.axiom.shared.GenericGwtRpcList;
import fr.pfgen.axiom.shared.records.PopulationRecord;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

@SuppressWarnings("serial")
public class PopulationsServiceImpl extends RemoteServiceServlet implements PopulationsService {

	private ConnectionPool pool;
	//private Hashtable<String, File> appFiles; 
	
	//@SuppressWarnings("unchecked")
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		pool = (ConnectionPool)getServletContext().getAttribute("ConnectionPool");
		//appFiles = (Hashtable<String, File>)getServletContext().getAttribute("ApplicationFiles");
	}
    
    @Override
	public List<String> getPopulationNames() {
		return PopulationsTable.getPopulationNames(pool);
	}

    @Override
    public PopulationRecord add (PopulationRecord record) {
        PopulationRecord newPopulation = PopulationsTable.addPopulation(pool,record);
        return newPopulation;
    }

    @Override
    public PopulationRecord update (PopulationRecord record) {
        return PopulationsTable.updatePopulation(pool,record);
    }

    @Override
    public void remove (PopulationRecord record) {
    	PopulationsTable.removePopulation(pool,record);
    }

    @Override
    public List<PopulationRecord> fetch (Integer startRow, Integer endRow,	final String sortBy, Map<String, String> filterCriteria) {
        
    	GenericGwtRpcList<PopulationRecord> outList = new GenericGwtRpcList<PopulationRecord>();
    
    	List<PopulationRecord> out = PopulationsTable.getPopulations(pool,startRow,endRow,sortBy,filterCriteria);
    	
    	outList.addAll(out);
    	outList.setTotalRows(out.size());
    	
    	return outList;
    }

	@Override
	public String download(String sortBy, Map<String, String> criterias) {
		// TODO Auto-generated method stub
		return null;
	}

}