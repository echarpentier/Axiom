package fr.pfgen.axiom.client.datasources;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.fields.DataSourceIntegerField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.widgets.grid.ListGridRecord;

import fr.pfgen.axiom.client.services.PedigreeService;
import fr.pfgen.axiom.client.services.PedigreeServiceAsync;
import fr.pfgen.axiom.shared.records.PedigreeRecord;

public class PedigreeDS extends GenericGwtRpcDataSource<PedigreeRecord, ListGridRecord, PedigreeServiceAsync>{
	
	@Override
	public void copyValues (ListGridRecord from, PedigreeRecord to) {
		if (from.getAttributeAsInt("pedigree_id")!=null){
			to.setPedigreeID(from.getAttributeAsInt("pedigree_id"));
		}
		to.setFamilyID(from.getAttributeAsString("family_id"));
		to.setIndividualID(from.getAttributeAsString("individual_id"));
		to.setFatherID(from.getAttributeAsString("father_id"));
		to.setMotherID(from.getAttributeAsString("mother_id"));
		to.setSex(from.getAttributeAsInt("sex"));
		to.setStatus(from.getAttributeAsInt("status"));
		to.setUserID(from.getAttributeAsInt("user_id"));
		to.setStudyName(from.getAttributeAsString("study_name"));
    }

	@Override
    public void copyValues (PedigreeRecord from, ListGridRecord to) {
		to.setAttribute("pedigree_id", from.getPedigreeID());
		to.setAttribute("family_id", from.getFamilyID());
		to.setAttribute("individual_id", from.getIndividualID());
		to.setAttribute("father_id", from.getFatherID());
		to.setAttribute("mother_id", from.getMotherID());
		to.setAttribute("sex", from.getSex());
		to.setAttribute("status", from.getStatus());
		to.setAttribute("user_id", from.getUserID());
		to.setAttribute("study_name", from.getStudyName());
    }
	
	@Override
	public List<DataSourceField> getDataSourceFields() {
		
		List<DataSourceField> fields = new ArrayList<DataSourceField>();
    	
        DataSourceField field;
        field = new DataSourceIntegerField("user_id", "USER ID");
        field.setRequired(true);
        field.setHidden(true);
        field.setCanEdit(false);
        fields.add(field);
        field = new DataSourceTextField("study_name", "STUDY NAME");
        field.setRequired(true);
        field.setHidden(true);
        field.setCanEdit(false);
        fields.add(field);
        field = new DataSourceIntegerField("pedigree_id", "PEDIGREE ID");
        field.setPrimaryKey(true);
        field.setHidden(false);
        fields.add(field);
        field = new DataSourceTextField ("family_id", "FAMILY ID");
        field.setRequired (true);
        field.setCanEdit(true);
        fields.add(field);
        field = new DataSourceTextField ("individual_id", "INDIVIDUAL ID");
        field.setRequired (true);
        field.setCanEdit(true);
        fields.add(field);
        field = new DataSourceTextField ("father_id", "FATHER ID");
        field.setRequired (true);
        field.setCanEdit(true);
        fields.add(field);
        field = new DataSourceTextField("mother_id", "MOTHER ID");
        field.setRequired(true);
        field.setCanEdit(true);
        fields.add(field);
        field = new DataSourceIntegerField ("sex", "SEX");
        field.setRequired (true);
        field.setCanEdit(true);
        fields.add(field);
        field = new DataSourceIntegerField("status", "STATUS");
        field.setRequired(true);
        field.setCanEdit(true);
        fields.add(field);
        
        return fields;
    }
	
	@Override
	public PedigreeRecord getNewDataObjectInstance(){
		return new PedigreeRecord();
	}
	
	@Override
	public ListGridRecord getNewRecordInstance(){
		return new ListGridRecord();
	}
	
	@Override
	public PedigreeServiceAsync getServiceAsync(){
		return GWT.create(PedigreeService.class);
	}
}
