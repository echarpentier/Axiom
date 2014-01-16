/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.pfgen.axiom.client.datasources;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.fields.DataSourceDateField;
import com.smartgwt.client.data.fields.DataSourceIntegerField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import fr.pfgen.axiom.client.services.FamiliesService;
import fr.pfgen.axiom.client.services.FamiliesServiceAsync;
import fr.pfgen.axiom.shared.records.FamilyRecord;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author eric
 */
public class FamiliesDS extends GenericGwtRpcDataSource<FamilyRecord, ListGridRecord, FamiliesServiceAsync> {
    
    @Override
    public void copyValues(ListGridRecord from, FamilyRecord to) {
        to.setId(from.getAttributeAsInt("family_id"));
        to.setName(from.getAttributeAsString("family_name"));
        to.setCreated(from.getAttributeAsDate("created"));
        to.setUser(from.getAttributeAsString("user"));
        to.setPropositus(from.getAttributeAsString("propositus"));
    }

    @Override
    public void copyValues(FamilyRecord from, ListGridRecord to) {
        to.setAttribute("family_id", from.getId());
        to.setAttribute("family_name", from.getName());
        to.setAttribute("created", from.getCreated());
        to.setAttribute("user", from.getUser());
        to.setAttribute("propositus", from.getPropositus());
    }

    @Override
    public List<DataSourceField> getDataSourceFields() {

        List<DataSourceField> fields = new ArrayList<DataSourceField>();

        //Declaring datasource fields as they appear in DB table simplifies service implementation
        DataSourceField field;
        field = new DataSourceIntegerField("family_id", "DB ID");
        //field.setRequired (true);
        field.setPrimaryKey(true);
        field.setHidden(true);
        fields.add(field);
        field = new DataSourceTextField("family_name", "NAME");
        field.setRequired(true);
        fields.add(field);
        field = new DataSourceTextField("propositus", "PROPOSITUS");
        fields.add(field);
        field = new DataSourceTextField("user", "CREATED BY");
        //field.setRequired (true);
        fields.add(field);
        field = new DataSourceDateField("created", "CREATION DATE");
        //field.setRequired (true);
        fields.add(field);

        return fields;
    }

    @Override
    public FamilyRecord getNewDataObjectInstance() {
        return new FamilyRecord();
    }

    @Override
    public ListGridRecord getNewRecordInstance() {
        return new ListGridRecord();
    }

    @Override
    public FamiliesServiceAsync getServiceAsync() {
        return GWT.create(FamiliesService.class);
    }
}
