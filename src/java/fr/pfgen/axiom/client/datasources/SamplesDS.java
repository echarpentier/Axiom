package fr.pfgen.axiom.client.datasources;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.fields.DataSourceIntegerField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import java.util.ArrayList;
import java.util.List;
import fr.pfgen.axiom.client.services.SamplesService;
import fr.pfgen.axiom.client.services.SamplesServiceAsync;
import fr.pfgen.axiom.shared.records.SampleRecord;

public class SamplesDS extends GenericGwtRpcDataSource<SampleRecord, ListGridRecord, SamplesServiceAsync> {

    //private static SamplesDS instance;
    // forces to use the singleton through getInstance();
	/*private SamplesDS(){
     };
	
     public static SamplesDS getInstance(){
     if (instance == null){
     instance = new SamplesDS();
     }
     return (instance);
     }*/
    @Override
    public void copyValues(ListGridRecord from, SampleRecord to) {
        to.setSampleID(from.getAttributeAsInt("sample_id"));
        to.setSampleName(from.getAttributeAsString("sample_name"));
        List<String> l = new ArrayList<String>();
        for (String popName : from.getAttributeAsString("population_names").split(",")) {
            if (popName != null && !popName.isEmpty()) {
                l.add(popName);
            }
        }
        to.setPopulationNames(l);
        to.setPlateName(from.getAttributeAsString("plate_name"));
        to.setSamplePath(from.getAttribute("sample_path"));
        to.setCoordX(from.getAttributeAsInt("plate_coordX"));
        to.setCoordY(from.getAttributeAsInt("plate_coordY"));
    }

    @Override
    public void copyValues(SampleRecord from, ListGridRecord to) {
        to.setAttribute("sample_id", from.getSampleID());
        to.setAttribute("sample_name", from.getSampleName());
        if (from.getPopulationNames() != null) {
            StringBuilder popNames = new StringBuilder();
            for (String popName : from.getPopulationNames()) {
                popNames.append(popName + ",");
            }
            String s = popNames.toString().replaceAll(",$", "");
            to.setAttribute("population_names", s);
        }
        to.setAttribute("plate_name", from.getPlateName());
        to.setAttribute("sample_path", from.getSamplePath());
        to.setAttribute("plate_coordX", from.getCoordX());
        to.setAttribute("plate_coordY", from.getCoordY());
    }

    @Override
    public List<DataSourceField> getDataSourceFields() {

        List<DataSourceField> fields = new ArrayList<DataSourceField>();

        DataSourceField field;
        field = new DataSourceIntegerField("sample_id", "DB ID");
        field.setRequired(true);
        field.setPrimaryKey(true);
        field.setHidden(true);
        fields.add(field);
        field = new DataSourceTextField("sample_name", "NAME");
        field.setRequired(true);
        fields.add(field);
        field = new DataSourceTextField("population_names", "IN POPULATIONS");
        field.setRequired(false);
        fields.add(field);
        field = new DataSourceTextField("plate_name", "IN PLATE");
        field.setRequired(true);
        fields.add(field);
        field = new DataSourceTextField("sample_path", "SERVER PATH");
        field.setRequired(true);
        field.setHidden(true);
        fields.add(field);
        field = new DataSourceIntegerField("plate_coordX", "X COORD");
        field.setRequired(true);
        fields.add(field);
        field = new DataSourceIntegerField("plate_coordY", "Y COORD");
        field.setRequired(true);
        fields.add(field);

        return fields;
    }

    @Override
    public SampleRecord getNewDataObjectInstance() {
        return new SampleRecord();
    }

    @Override
    public ListGridRecord getNewRecordInstance() {
        return new ListGridRecord();
    }

    @Override
    public SamplesServiceAsync getServiceAsync() {
        return GWT.create(SamplesService.class);
    }
}