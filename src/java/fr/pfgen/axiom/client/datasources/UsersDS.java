package fr.pfgen.axiom.client.datasources;

import java.util.ArrayList;
import java.util.List;
import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.SimpleType;
import com.smartgwt.client.data.fields.DataSourceEnumField;
import com.smartgwt.client.data.fields.DataSourceIntegerField;
import com.smartgwt.client.data.fields.DataSourcePasswordField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.FieldType;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import fr.pfgen.axiom.client.services.UsersService;
import fr.pfgen.axiom.client.services.UsersServiceAsync;
import fr.pfgen.axiom.client.ui.ClientUtils;
import fr.pfgen.axiom.shared.records.UserRecord;

public class UsersDS extends GenericGwtRpcDataSource<UserRecord, ListGridRecord, UsersServiceAsync> {
	
	private static UsersDS instance;
	
	// forces to use the singleton through getInstance();
	private UsersDS(){
	}
	
	public static UsersDS getInstance(){
		if (instance==null){
			instance = new UsersDS();
		}
		return (instance);
	}
	
	@Override
	public void copyValues (ListGridRecord from, UserRecord to) {
    	//to.setUserID(from.getAttributeAsInt("userID"));
    	to.setFirstname(from.getAttributeAsString("firstname"));
    	to.setLastname(from.getAttributeAsString("lastname"));
    	to.setEmail(from.getAttributeAsString("email"));
    	if (from.getAttributeAsString("office_number")!=null){
    		to.setOffice_number(from.getAttributeAsString("office_number"));
    	}
    	if (from.getAttributeAsString("team")!=null){
    		to.setTeam(from.getAttributeAsString("team"));
    	}
    	to.setAppID(from.getAttributeAsString("appID"));
    	to.setAppPw(from.getAttributeAsString("appPw"));
    	to.setStatus(from.getAttributeAsString("status"));
    }

	@Override
    public void copyValues (UserRecord from, ListGridRecord to) {
    	to.setAttribute("userID", from.getUserID());
    	to.setAttribute("firstname", from.getFirstname());
    	to.setAttribute("lastname", from.getLastname());
    	to.setAttribute("email", from.getEmail());
    	to.setAttribute("office_number", from.getOffice_number());
    	to.setAttribute("team", from.getTeam());
    	to.setAttribute("appID", from.getAppID());
    	to.setAttribute("appPw", from.getAppPw());
    	to.setAttribute("status", from.getStatus());
    }
	
	@Override
	public List<DataSourceField> getDataSourceFields() {
		
		List<DataSourceField> fields = new ArrayList<DataSourceField>();
		
		SimpleType nameType = new ClientUtils.NameType();
		
        DataSourceField field;
        field = new DataSourceIntegerField("userID", "ID");
        field.setType(FieldType.INTEGER);
        field.setRequired (true);
        field.setPrimaryKey(true);
        field.setCanEdit(false);
        field.setHidden(true);
        fields.add(field);
        field = new DataSourceTextField ("firstname", "FIRSTNAME");
        field.setType(nameType);
        field.setRequired (true);
        fields.add(field);
        field = new DataSourceTextField ("lastname", "LASTNAME");
        field.setType(nameType);
        field.setRequired (true);
        fields.add(field);
        field = new DataSourceTextField ("email", "EMAIL");
        field.setType(new ClientUtils.EmailType());
        field.setRequired (true);
        fields.add(field);
        field = new DataSourceTextField ("office_number", "OFFICE");
        field.setType(new ClientUtils.OfficeType());
        field.setRequired (false);
        fields.add(field);
        field = new DataSourceTextField ("team", "TEAM");
        field.setRequired (false);
        fields.add(field);
        field = new DataSourceTextField ("appID", "USERNAME");
        field.setType(nameType);
        field.setRequired (true);
        fields.add(field);
        field = new DataSourcePasswordField("appPw", "PASSWORD");
        field.setRequired (true);
        field.setType(FieldType.PASSWORD);
        fields.add(field);
        field = new DataSourceEnumField("status", "STATUS");
        field.setValueMap("admin","advanced","simple","restricted");
        field.setRequired (true);
        fields.add(field);
        
        return fields;
    }

	@Override
	public UserRecord getNewDataObjectInstance(){
		return new UserRecord();
	}
	
	@Override
	public ListGridRecord getNewRecordInstance(){
		return new ListGridRecord();
	}
	
	@Override
	public UsersServiceAsync getServiceAsync(){
		return GWT.create(UsersService.class);
	}
}
