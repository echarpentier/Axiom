package fr.pfgen.axiom.client.datasources;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.fields.DataSourceDateField;
import com.smartgwt.client.data.fields.DataSourceIntegerField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import java.util.ArrayList;
import java.util.List;
import fr.pfgen.axiom.client.services.PopulationsService;
import fr.pfgen.axiom.client.services.PopulationsServiceAsync;
import fr.pfgen.axiom.shared.records.PopulationRecord;

public class PopulationsDS extends GenericGwtRpcDataSource<PopulationRecord,ListGridRecord,PopulationsServiceAsync> {
	
	
	@Override
	public void copyValues (ListGridRecord from, PopulationRecord to) {
    	to.setId(from.getAttributeAsInt("population_id"));
        to.setPopulationName (from.getAttributeAsString ("population_name"));
        to.setCreated (from.getAttributeAsDate ("created"));
        to.setUser (from.getAttributeAsString ("user"));
    }

	@Override
    public void copyValues (PopulationRecord from, ListGridRecord to) {
    	to.setAttribute ("population_id", from.getId());
        to.setAttribute ("population_name", from.getPopulationName());
        to.setAttribute ("created", from.getCreated());
        to.setAttribute ("user", from.getUser());
    }
	
	@Override
	public List<DataSourceField> getDataSourceFields() {
		
		List<DataSourceField> fields = new ArrayList<DataSourceField>();
		
		//Declaring datasource fields as they appear in DB table simplifies service implementation
        DataSourceField field;
        field = new DataSourceIntegerField("population_id", "DB ID");
        //field.setRequired (true);
        field.setPrimaryKey(true);
        field.setHidden(true);
        fields.add(field);
        field = new DataSourceTextField ("population_name", "NAME");
        field.setRequired (true);
        fields.add(field);
        field = new DataSourceTextField ("user", "CREATED BY");
        //field.setRequired (true);
        fields.add(field);
        field = new DataSourceDateField ("created", "CREATION DATE");
        //field.setRequired (true);
        fields.add(field);
        
        return fields;
    }

	@Override
	public PopulationRecord getNewDataObjectInstance(){
		return new PopulationRecord();
	}
	
	@Override
	public ListGridRecord getNewRecordInstance(){
		return new ListGridRecord();
	}
	
	@Override
	public PopulationsServiceAsync getServiceAsync(){
		return GWT.create(PopulationsService.class);
	}
}	
