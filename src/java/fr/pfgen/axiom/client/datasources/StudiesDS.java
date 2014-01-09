package fr.pfgen.axiom.client.datasources;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.fields.DataSourceEnumField;
import com.smartgwt.client.data.fields.DataSourceIntegerField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.widgets.grid.ListGridRecord;

import fr.pfgen.axiom.client.services.StudiesService;
import fr.pfgen.axiom.client.services.StudiesServiceAsync;
import fr.pfgen.axiom.shared.records.StudyRecord;

public class StudiesDS extends GenericGwtRpcDataSource<StudyRecord, ListGridRecord, StudiesServiceAsync>{
	
	/*private static StudiesDS instance;
	
	//forces to use the singleton through getInstance();
	private StudiesDS(){
	};
	
	public static StudiesDS getInstance(){
		if (instance == null){
			instance = new StudiesDS();
		}
		return (instance);
	}*/
	
	@Override
	public List<DataSourceField> getDataSourceFields() {
		List<DataSourceField> fields = new ArrayList<DataSourceField>();
		
        DataSourceField field;
        field = new DataSourceIntegerField("study_id", "DB ID");
        field.setRequired (true);
        field.setPrimaryKey(true);
        field.setHidden(true);
        fields.add(field);
        field = new DataSourceTextField ("study_name", "NAME");
        field.setRequired (true);
        fields.add(field);
        field = new DataSourceTextField("study_path", "SERVER PATH");
        field.setRequired(true);
        field.setHidden(true);
        fields.add(field);
        field = new DataSourceEnumField("study_type", "TYPE");
        field.setValueMap("family","case-control");
        field.setRequired (true);
        fields.add(field);
        field = new DataSourceIntegerField("user_id", "USER ID");
        field.setRequired(true);
        field.setHidden(true);
        fields.add(field);
        
        return fields;
	}

	@Override
	public void copyValues(ListGridRecord from, StudyRecord to) {
		to.setStudyID(from.getAttributeAsInt("study_id"));
		to.setStudyName(from.getAttributeAsString("study_name"));
		to.setStudyPath(from.getAttributeAsString("study_path"));
		to.setStudyType(from.getAttributeAsString("study_type"));
		to.setUserID(from.getAttributeAsInt("user_id"));
	}

	@Override
	public void copyValues(StudyRecord from, ListGridRecord to) {
		to.setAttribute("study_id", from.getStudyID());
		to.setAttribute("study_name", from.getStudyName());
		to.setAttribute("study_path", from.getStudyPath());
		to.setAttribute("study_type", from.getStudyType());
		to.setAttribute("user_id", from.getUserID());
	}

	@Override
	public StudiesServiceAsync getServiceAsync() {
		return GWT.create(StudiesService.class);
	}

	@Override
	public ListGridRecord getNewRecordInstance() {
		return new ListGridRecord();
	}

	@Override
	public StudyRecord getNewDataObjectInstance() {
		return new StudyRecord();
	}
}
