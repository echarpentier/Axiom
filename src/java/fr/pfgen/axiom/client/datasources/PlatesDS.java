package fr.pfgen.axiom.client.datasources;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.fields.DataSourceDateField;
import com.smartgwt.client.data.fields.DataSourceIntegerField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import java.util.ArrayList;
import java.util.List;
import fr.pfgen.axiom.client.services.PlatesService;
import fr.pfgen.axiom.client.services.PlatesServiceAsync;
import fr.pfgen.axiom.shared.records.PlateRecord;

public class PlatesDS extends GenericGwtRpcDataSource<PlateRecord, ListGridRecord, PlatesServiceAsync> {

    /*private static PlatesDS instance;
	
     // forces to use the singleton through getInstance();
     private PlatesDS(){
     };
	
     public static PlatesDS getInstance(){
     if (instance == null){
     instance = new PlatesDS();
     }
     return (instance);
     }*/
    @Override
    public void copyValues(ListGridRecord from, PlateRecord to) {
        to.setBarcode(from.getAttributeAsString("plate_barcode"));
        to.setName(from.getAttributeAsString("plate_name"));
        to.setCreated(from.getAttributeAsDate("created"));
        to.setId(from.getAttributeAsInt("plate_id"));
    }

    @Override
    public void copyValues(PlateRecord from, ListGridRecord to) {
        to.setAttribute("plate_barcode", from.getBarcode());
        to.setAttribute("plate_name", from.getName());
        to.setAttribute("created", from.getCreated());
        to.setAttribute("plate_id", from.getId());
    }

    @Override
    public List<DataSourceField> getDataSourceFields() {

        List<DataSourceField> fields = new ArrayList<DataSourceField>();

        //Declaring datasource fields as they appear in DB table simplifies service implementation
        DataSourceField field;
        field = new DataSourceTextField("plate_barcode", "BARCODE");
        field.setRequired(true);
        fields.add(field);
        field = new DataSourceTextField("plate_name", "NAME");
        //field.setRequired (true);
        field.setCanEdit(true);
        fields.add(field);
        field = new DataSourceDateField("created", "CREATION DATE");
        field.setRequired(true);
        fields.add(field);
        field = new DataSourceIntegerField("plate_id", "DB ID");
        field.setRequired(true);
        field.setPrimaryKey(true);
        field.setHidden(true);
        fields.add(field);

        return fields;
    }

    @Override
    public PlateRecord getNewDataObjectInstance() {
        return new PlateRecord();
    }

    @Override
    public ListGridRecord getNewRecordInstance() {
        return new ListGridRecord();
    }

    @Override
    public PlatesServiceAsync getServiceAsync() {
        return GWT.create(PlatesService.class);
    }
}