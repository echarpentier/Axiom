package fr.pfgen.axiom.server.services;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import fr.pfgen.axiom.client.services.GenotypingAnalysisService;
import fr.pfgen.axiom.server.database.ConnectionPool;
import fr.pfgen.axiom.server.database.GenoAnalysisTable;
import fr.pfgen.axiom.server.utils.DatabaseUtils;
import fr.pfgen.axiom.server.utils.ServerUtils;
import fr.pfgen.axiom.shared.GenericGwtRpcList;
import fr.pfgen.axiom.shared.records.GenotypingAnalysisRecord;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class GenotypingAnalysisServiceImpl extends RemoteServiceServlet implements GenotypingAnalysisService{

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
	public List<GenotypingAnalysisRecord> fetch(Integer startRow, Integer endRow, String sortBy, Map<String, String> filterCriteria) {
		GenericGwtRpcList<GenotypingAnalysisRecord> outList = new GenericGwtRpcList<GenotypingAnalysisRecord>();
		
		String query = GenoAnalysisTable.constructQuery(sortBy,filterCriteria);
		
		outList.setTotalRows(DatabaseUtils.countRowInQuery(pool, query, true));
    	List<GenotypingAnalysisRecord> out = GenoAnalysisTable.getGenotypingAnalysis(pool,query,startRow,endRow);
    	
    	outList.addAll(out);
    	
    	return outList;
	}

	@Override
	public GenotypingAnalysisRecord add(GenotypingAnalysisRecord data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GenotypingAnalysisRecord update(GenotypingAnalysisRecord data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove(GenotypingAnalysisRecord data) {
		String genoPath = GenoAnalysisTable.removeGenoAnalysis(pool, data);
		if (genoPath!=null && !genoPath.isEmpty()){
			ServerUtils.deleteDirectory(new File(genoPath));
		}else{
			throw new RuntimeException("Cannot remove genotyping analysis "+data.getGenoName()+" from database !!");
		}
	}

	@Override
	public int nbSamplesInGenoRun(String genoName, String run) {
		String query = "SELECT * FROM ((genotyping_analysis ga JOIN genotyping_samples gs ON gs.geno_id=ga.geno_id) JOIN genotyping_runs gr ON gr.geno_sample_id=gs.geno_sample_id) WHERE geno_name=\""+genoName+"\" AND geno_run=\""+run+"\"";
		//ConnectionPool pool = (ConnectionPool)getServletContext().getAttribute("ConnectionPool");
		return DatabaseUtils.countRowInQuery(pool, query,false);
	}
	
	@Override
	public int nbSamplesInGenoAnalysis(String genoName){
		String query = "SELECT * FROM (genotyping_analysis ga JOIN genotyping_samples gs ON gs.geno_id=ga.geno_id) WHERE geno_name=\""+genoName+"\"";
		return DatabaseUtils.countRowInQuery(pool, query, false);
	}

	@Override
	public String download(String sortBy, Map<String, String> criterias) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> studiesLinkedToGenoAnalysis(int genoID) {
		return GenoAnalysisTable.getStudiesLinkedToGenoAnalysis(pool, genoID);
	}

	@Override
	public String getLibraryNameForGeno(String genoName) {
		return GenoAnalysisTable.getLibraryNameForGeno(pool, genoName);
	}
}
