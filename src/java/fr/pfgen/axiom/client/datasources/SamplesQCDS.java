package fr.pfgen.axiom.client.datasources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.pfgen.axiom.client.services.SamplesQCService;
import fr.pfgen.axiom.client.services.SamplesQCServiceAsync;
import fr.pfgen.axiom.shared.records.SampleQCRecord;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.fields.DataSourceIntegerField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.widgets.grid.ListGridRecord;

public class SamplesQCDS extends GenericGwtRpcDataSource<SampleQCRecord,ListGridRecord,SamplesQCServiceAsync> {

	private static SamplesQCDS instance;
	
	// forces to use the singleton through getInstance();
	private SamplesQCDS(){
	};
	
	public static SamplesQCDS getInstance(){
		if (instance == null){
			instance = new SamplesQCDS();
		}
		return (instance);
	}
	
	@Override
	public void copyValues (ListGridRecord from, SampleQCRecord to) {
		to.setSampleID(from.getAttributeAsInt("sample_id"));
        to.setSampleName(from.getAttributeAsString ("sample_name"));
        to.setPlateName(from.getAttributeAsString ("plate_name"));
        
        if (from.getAttributeAsString("population_names")!=null && !from.getAttributeAsString("population_names").isEmpty()){
        	String[] popNames = from.getAttributeAsString("population_names").split(",");
        	List<String> popList = new ArrayList<String>(popNames.length);
        	for (String pop : popNames) {
				popList.add(pop);
			}
        	to.setPopulationNames(popList);
        }
        
        HashMap<String, String> qcMap = new HashMap<String, String>();
        String[] attributes = from.getAttributes();
        for (String att : attributes) {
			if (att.equals("sample_id") || att.equals("sample_name") || att.equals("plate_name") || att.equals("population_names")){
				continue;
			}else{
				qcMap.put(att, from.getAttributeAsString(att));
			}
		}
        to.setQcMap(qcMap);
    }

	@Override
    public void copyValues (SampleQCRecord from, ListGridRecord to) {
		to.setAttribute("sample_id", from.getSampleID());
		to.setAttribute ("sample_name", from.getSampleName());
		to.setAttribute ("plate_name", from.getPlateName());
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
		for (String userQcName : from.getUserQcMap().keySet()) {
			to.setAttribute(userQcName, from.getUserQcMap().get(userQcName));
		}
    }
	
	@Override
	public List<DataSourceField> getDataSourceFields() {
		
		final List<DataSourceField> fields = new ArrayList<DataSourceField>();
		
        DataSourceField field;
        field = new DataSourceIntegerField("sample_id", "DB ID");
        field.setRequired (true);
        field.setPrimaryKey(true);
        field.setHidden(true);
        fields.add(field);
        field = new DataSourceTextField ("sample_name", "NAME");
        field.setRequired (true);
        fields.add(field);
        field = new DataSourceTextField ("plate_name", "IN PLATE");
        field.setRequired (true);
        fields.add(field);
        field = new DataSourceTextField ("population_names", "IN POPULATION");
        field.setRequired (true);
        fields.add(field);
        
        return fields;
    }
	
	@Override
	public SampleQCRecord getNewDataObjectInstance(){
		return new SampleQCRecord();
	}
	
	@Override
	public ListGridRecord getNewRecordInstance(){
		return new ListGridRecord();
	}
	
	@Override
	public SamplesQCServiceAsync getServiceAsync(){
		return GWT.create(SamplesQCService.class);
	}
}
