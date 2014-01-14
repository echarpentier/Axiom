/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.pfgen.axiom.client.datasources;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.fields.DataSourceDateTimeField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import fr.pfgen.axiom.client.services.RunningGenoAnalysisService;
import fr.pfgen.axiom.client.services.RunningGenoAnalysisServiceAsync;
import fr.pfgen.axiom.shared.records.RunningGenotypingAnalysis;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author eric
 */
public class RunningGenoAnalysisDS extends GenericGwtRpcDataSource<RunningGenotypingAnalysis, ListGridRecord, RunningGenoAnalysisServiceAsync> {

    private static RunningGenoAnalysisDS instance;

    // forces to use the singleton through getInstance();
    private RunningGenoAnalysisDS() {
    }

    public static RunningGenoAnalysisDS getInstance() {
        if (instance == null) {
            instance = new RunningGenoAnalysisDS();
        }
        return (instance);
    }

    @Override
    public List<DataSourceField> getDataSourceFields() {
        List<DataSourceField> fields = new ArrayList<DataSourceField>();

        DataSourceField field;
        field = new DataSourceTextField("name", "NAME");
        field.setRequired(true);
        field.setPrimaryKey(true);
        fields.add(field);
        field = new DataSourceTextField("user", "USER");
        field.setRequired(true);
        fields.add(field);
        field = new DataSourceDateTimeField("start", "START");
        //field.setDateFormatter(DateDisplayFormat.TOSTRING);
        field.setRequired(true);
        fields.add(field);
        field = new DataSourceDateTimeField("end", "END");
        field.setRequired(true);
        fields.add(field);
        field = new DataSourceTextField("status", "STATUS");
        field.setRequired(false);
        fields.add(field);

        return fields;
    }

    @Override
    public void copyValues(ListGridRecord from, RunningGenotypingAnalysis to) {
        to.setName(from.getAttributeAsString("name"));
        to.setStartDate(from.getAttributeAsDate("start"));
        to.setEndDate(from.getAttributeAsDate("end"));
        to.setStatus(RunningGenotypingAnalysis.GenoAnaRunningStatus.getEnumFromLabel(from.getAttributeAsString("status")));
        to.setUser(from.getAttributeAsString("user"));
    }

    @Override
    public void copyValues(RunningGenotypingAnalysis from, ListGridRecord to) {
        to.setAttribute("name", from.getName());
        to.setAttribute("user", from.getUser());
        to.setAttribute("start", from.getStartDate());
        to.setAttribute("end", from.getEndDate());
        to.setAttribute("status", from.getStatus().getLabel());
        if (from.getEndDate() != null){
            to.setAttribute("running_icon", "staticATGC");
        }else{
            to.setAttribute("running_icon", "ATGC");
        }
    }

    @Override
    public RunningGenoAnalysisServiceAsync getServiceAsync() {
        return GWT.create(RunningGenoAnalysisService.class);
    }

    @Override
    public ListGridRecord getNewRecordInstance() {
        return new ListGridRecord();
    }

    @Override
    public RunningGenotypingAnalysis getNewDataObjectInstance() {
        return new RunningGenotypingAnalysis();
    }
}
