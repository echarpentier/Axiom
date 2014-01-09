package fr.pfgen.axiom.server.services;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import fr.pfgen.axiom.client.services.PlatesService;
import fr.pfgen.axiom.server.database.ConnectionPool;
import fr.pfgen.axiom.server.database.PlatesTable;
import fr.pfgen.axiom.server.utils.DatabaseUtils;
import fr.pfgen.axiom.shared.GenericGwtRpcList;
import fr.pfgen.axiom.shared.records.PlateRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

@SuppressWarnings("serial")
public class PlatesServiceImpl extends RemoteServiceServlet implements PlatesService {

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
	public List<String> getPlateNames() {
    	return PlatesTable.getPlateNames(pool);
	}

    @Override
    public PlateRecord add (PlateRecord record) {
        PlateRecord newPlate = PlatesTable.addPlate(pool,record);
        return newPlate;
    }

    @Override
    public PlateRecord update (PlateRecord record) {
        record = PlatesTable.updatePlate(pool,record);
        return record;
    }

    @Override
    public void remove (PlateRecord record) {
    	PlatesTable.removePlate(pool,record.getId());
    }

    @Override
    public List<PlateRecord> fetch (Integer startRow, Integer endRow,	final String sortBy, Map<String, String> filterCriteria) {
    	GenericGwtRpcList<PlateRecord> outList = new GenericGwtRpcList<PlateRecord>();
    
    	String query = PlatesTable.constructQuery(sortBy,filterCriteria);
    	
    	//outList.setTotalRows(DatabaseUtils.countRowInQuery(pool,query));
    	
    	outList.setTotalRows(DatabaseUtils.countRowInQuery(pool, query, true));
    	List<PlateRecord> out = PlatesTable.getPlates(pool,query,startRow,endRow);
    	
    	outList.addAll(out);
    	
    	return outList;
    }

	@Override
	public int getNumberOfPlatesInProject(String populationName) {
		Map<String, String> crit = new HashMap<String, String>(1);
		crit.put("population_name", populationName);
		String query = PlatesTable.constructQuery(null, crit);
		return PlatesTable.getPlates(pool,query,null,null).size();
	}

	@Override
	public String download(String sortBy, Map<String, String> criterias) {
		// TODO Auto-generated method stub
		return null;
	}
}