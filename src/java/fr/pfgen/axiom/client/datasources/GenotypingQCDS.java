package fr.pfgen.axiom.client.datasources;

import java.util.ArrayList;
import java.util.List;

import fr.pfgen.axiom.client.services.GenotypingQCService;
import fr.pfgen.axiom.client.services.GenotypingQCServiceAsync;
import fr.pfgen.axiom.shared.records.GenotypingQCRecord;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.fields.DataSourceIntegerField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.widgets.grid.ListGridRecord;

public class GenotypingQCDS extends GenericGwtRpcDataSource<GenotypingQCRecord,ListGridRecord,GenotypingQCServiceAsync> {

	/*private static GenotypingQCDS instance;
	
	// forces to use the singleton through getInstance();
	private GenotypingQCDS(){
	};
	
	public static GenotypingQCDS getInstance(){
		if (instance == null){
			instance = new GenotypingQCDS();
		}
		return (instance);
	}*/
	
	@Override
	public void copyValues (ListGridRecord from, GenotypingQCRecord to) {
		/*to.setSampleID(from.getAttributeAsInt("sample_id"));
        to.setSampleName(from.getAttributeAsString ("sample_name"));
        to.setProjectName(from.getAttributeAsString ("project_name"));
        to.setPlateName(from.getAttributeAsString ("plate_name"));
        to.setSamplePath(from.getAttribute("sample_path"));
        to.setCoordX(from.getAttributeAsInt ("plate_coordX"));
        to.setCoordY(from.getAttributeAsInt("plate_coordY"));*/
    }

	@Override
    public void copyValues (GenotypingQCRecord from, ListGridRecord to) {
		to.setAttribute("sample_id", from.getSampleID());
		to.setAttribute ("sample_name", from.getSampleName());
		to.setAttribute ("plate_name", from.getPlateName());
		to.setAttribute("geno_run_id", from.getGenoRunID());
		to.setAttribute("geno_run", from.getRun());
		if (from.getPopulationNames()!=null){
        	StringBuilder popNames = new StringBuilder();
        	for (String popName : from.getPopulationNames()) {
        		popNames.append(popName+",");
        	}
        	String s = popNames.toString().replaceAll(",$", "");
        	to.setAttribute("population_names", s);
		}
		for (String qcName : from.getQcMap().keySet()) {
			to.setAttribute(qcName, from.getQcMap().get(qcName));
		}
    }
	
	@Override
	public List<DataSourceField> getDataSourceFields() {
		
		//final List<String> qcList = new ArrayList<String>();
		//final SamplesQCServiceAsync samplesQCService = GWT.create(SamplesQCService.class);
		final List<DataSourceField> fields = new ArrayList<DataSourceField>();
		
        DataSourceField field;
        field = new DataSourceIntegerField("sample_id", "DB ID");
        field.setRequired (true);
        field.setPrimaryKey(true);
        field.setHidden(true);
        fields.add(field);
        field = new DataSourceIntegerField("geno_run_id", "GENO RUN ID");
        field.setRequired(true);
        field.setHidden(true);
        fields.add(field);
        field = new DataSourceTextField ("sample_name", "NAME");
        field.setRequired (true);
        fields.add(field);
        field = new DataSourceTextField ("plate_name", "IN PLATE");
        field.setRequired (true);
        fields.add(field);
        field = new DataSourceTextField ("population_names", "IN POPULATIONS");
        field.setRequired (true);
        fields.add(field);
        field = new DataSourceTextField("geno_run", "RUN");
        field.setRequired(true);
        field.setHidden(true);
        fields.add(field);
        
        return fields;
    }
	
	@Override
	public GenotypingQCRecord getNewDataObjectInstance(){
		return new GenotypingQCRecord();
	}
	
	@Override
	public ListGridRecord getNewRecordInstance(){
		return new ListGridRecord();
	}
	
	@Override
	public GenotypingQCServiceAsync getServiceAsync(){
		return GWT.create(GenotypingQCService.class);
	}
}
