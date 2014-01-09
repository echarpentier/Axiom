package fr.pfgen.axiom.client.datasources;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.fields.DataSourceDateField;
import com.smartgwt.client.data.fields.DataSourceFloatField;
import com.smartgwt.client.data.fields.DataSourceIntegerField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.widgets.grid.ListGridRecord;

import fr.pfgen.axiom.client.services.GenotypingAnalysisService;
import fr.pfgen.axiom.client.services.GenotypingAnalysisServiceAsync;
import fr.pfgen.axiom.shared.records.GenotypingAnalysisRecord;

public class GenotypingAnalysisDS extends GenericGwtRpcDataSource<GenotypingAnalysisRecord, ListGridRecord, GenotypingAnalysisServiceAsync> {

	/*private static GenotypingAnalysisDS instance;
	
	private GenotypingAnalysisDS(){
	}
	
	public static GenotypingAnalysisDS getInstance(){
		if (instance == null){
			instance = new GenotypingAnalysisDS();
		}
		return (instance);
	}*/

	@Override
	public void copyValues(ListGridRecord from, GenotypingAnalysisRecord to) {
		to.setId(from.getAttributeAsInt("geno_id"));
        to.setGenoName(from.getAttributeAsString ("geno_name"));
        to.setFolderPath(from.getAttributeAsString("folder_path"));
        to.setUser (from.getAttributeAsString ("user"));
        to.setDishQCLimit(from.getAttributeAsDouble("dishQCLimit"));
        to.setCallRateLimit(from.getAttributeAsDouble("callRateLimit"));
        to.setExecuted(from.getAttributeAsDate ("executed"));
        to.setAnnotationFile(from.getAttributeAsString("annotation_file"));
        to.setLibraryFiles(from.getAttributeAsString("library_files"));
	}

	@Override
	public void copyValues(GenotypingAnalysisRecord from, ListGridRecord to) {
		to.setAttribute ("geno_id", from.getId());
        to.setAttribute ("geno_name", from.getGenoName());
        to.setAttribute("folder_path", from.getFolderPath());
        to.setAttribute ("user", from.getUser());
        to.setAttribute("dishQCLimit", from.getDishQCLimit());
        to.setAttribute("callRateLimit", from.getCallRateLimit());
        to.setAttribute ("executed", from.getExecuted());
        to.setAttribute("annotation_file", from.getAnnotationFile());
        to.setAttribute("library_files", from.getLibraryFiles());
	}
	
	@Override
	public List<DataSourceField> getDataSourceFields() {
		List<DataSourceField> fields = new ArrayList<DataSourceField>();
		
		//Declaring datasource fields as they appear in DB table simplifies service implementation
        DataSourceField field;
        field = new DataSourceIntegerField("geno_id", "DB ID");
        field.setPrimaryKey(true);
        field.setHidden(true);
        fields.add(field);
        field = new DataSourceTextField ("geno_name", "NAME");
        field.setRequired (true);
        fields.add(field);
        field = new DataSourceTextField("folder_path", "FOLDER PATH");
        field.setHidden(true);
        fields.add(field);
        field = new DataSourceTextField ("user", "CREATED BY");
        fields.add(field);
        field = new DataSourceFloatField("dishQCLimit", "DQC LIMIT");
        fields.add(field);
        field = new DataSourceFloatField("callRateLimit", "CR LIMIT");
        fields.add(field);
        field = new DataSourceDateField ("executed", "PERFORMED DATE");
        fields.add(field);
        field = new DataSourceTextField("annotation_file", "ANNOTATION FILE");
        field.setHidden(true);
        fields.add(field);
        field = new DataSourceTextField("library_files", "LIBRARY FILES");
        field.setHidden(true);
        fields.add(field);
        
        return fields;
	}

	@Override
	public GenotypingAnalysisServiceAsync getServiceAsync() {
		return GWT.create(GenotypingAnalysisService.class);
	}

	@Override
	public ListGridRecord getNewRecordInstance() {
		return new ListGridRecord();
	}

	@Override
	public GenotypingAnalysisRecord getNewDataObjectInstance() {
		return new GenotypingAnalysisRecord();
	}
}
