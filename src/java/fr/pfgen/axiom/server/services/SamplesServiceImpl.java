package fr.pfgen.axiom.server.services;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import fr.pfgen.axiom.client.services.SamplesService;
import fr.pfgen.axiom.server.database.ConnectionPool;
import fr.pfgen.axiom.server.database.QcValuesTable;
import fr.pfgen.axiom.server.database.SamplesTable;
import fr.pfgen.axiom.server.utils.DatabaseUtils;
import fr.pfgen.axiom.shared.GenericGwtRpcList;
import fr.pfgen.axiom.shared.records.SampleRecord;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

@SuppressWarnings("serial")
public class SamplesServiceImpl extends RemoteServiceServlet implements SamplesService {

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
    public SampleRecord add (SampleRecord record) {
        SampleRecord newSample = SamplesTable.addSample(pool,record);
    	//items.add (newSample);
        return newSample;
    }

    @Override
    public synchronized SampleRecord update (SampleRecord record) {
        record = SamplesTable.updateSample(pool,record);
        return record;
    }

    @Override
    public void remove (SampleRecord record) {
    	SamplesTable.removeSample(pool,record.getSampleID());
    }

    @Override
    public List<SampleRecord> fetch (Integer startRow, Integer endRow,	final String sortBy, Map<String, String> filterCriteria) {
        
    	GenericGwtRpcList<SampleRecord> outList = new GenericGwtRpcList<SampleRecord>();
    
    	String query = SamplesTable.constructQuery(sortBy,filterCriteria);
    	
    	outList.setTotalRows(DatabaseUtils.countRowInQuery(pool,query,false));
    	
    	List<SampleRecord> out = SamplesTable.getSamples(pool,query,startRow,endRow);
    	
    	outList.addAll(out);
    	
    	return outList;
    }

	@Override
	public int nbSamplesInPopulation(String populationName) {
		String query = "SELECT * FROM ((samples s JOIN samples_in_populations sp ON s.sample_id=sp.sample_id) JOIN populations p on p.population_id=sp.population_id) WHERE population_name=\""+populationName+"\"";
		return DatabaseUtils.countRowInQuery(pool,query,false);
	}
	
	@Override
	public int nbSamplesInPlate(String plateName) {
		String query = "SELECT * FROM samples JOIN plates ON samples.plate_id=plates.plate_id WHERE plate_name=\""+plateName+"\"";
		return DatabaseUtils.countRowInQuery(pool,query,false);
	}
	
	@Override
	public int nbSamplesInProjectWithoutQC(String populationName) {
		String query = "SELECT * FROM ((samples s JOIN samples_in_populations sp ON s.sample_id=sp.sample_id) JOIN populations p on p.population_id=sp.population_id) WHERE NOT EXISTS (SELECT * FROM qc_values qc WHERE s.sample_id = qc.sample_id) AND population_name=\""+populationName+"\"";
		return DatabaseUtils.countRowInQuery(pool,query,false);
	}
	
	@Override
	public int nbSamplesInPlateWithoutQC(String plateName) {
		String query = "SELECT * FROM samples s JOIN plates p ON s.plate_id=p.plate_id WHERE NOT EXISTS (SELECT * FROM qc_values qc WHERE s.sample_id = qc.sample_id) AND plate_name=\""+plateName+"\"";
		return DatabaseUtils.countRowInQuery(pool,query,false);
	}
	
	@Override
	public int nbSamplesWithoutQC(List<Integer> sampleList){
		return QcValuesTable.countSamplesWithoutQC(pool,sampleList); 
	}

	@Override
	public String download(String sortBy, Map<String, String> criterias) {
		// TODO Auto-generated method stub
		return null;
	}
}