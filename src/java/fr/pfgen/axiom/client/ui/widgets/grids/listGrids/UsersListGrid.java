package fr.pfgen.axiom.client.ui.widgets.grids.listGrids;

import fr.pfgen.axiom.client.datasources.UsersDS;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.AutoFitWidthApproach;
import com.smartgwt.client.types.Autofit;
import com.smartgwt.client.types.FetchMode;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;

public class UsersListGrid extends ListGrid{

	public UsersListGrid(){
		this.setTitle("Users");
		//this.setDataSource(UsersDS.getInstance());
		this.setEmptyCellValue("--");
		this.setLayoutAlign(Alignment.CENTER);
		
		this.setDataSource(UsersDS.getInstance());
		
		
		ListGridField idField = new ListGridField("userID", "ID");
		idField.setAlign(Alignment.CENTER);
		idField.setType(ListGridFieldType.INTEGER);
		idField.setWidth(10);
		idField.setHidden(true);
	
        ListGridField firstnameField = new ListGridField("firstname", "FIRSTNAME");
        firstnameField.setAlign(Alignment.CENTER);
        //firstnameField.setType(ListGridFieldType.TEXT);
        firstnameField.setWidth(10);
        firstnameField.setRequired (true);
        firstnameField.setCanEdit(true);
        
        ListGridField lastnameField = new ListGridField("lastname", "LASTNAME");
        lastnameField.setAlign(Alignment.CENTER);
        //lastnameField.setType(ListGridFieldType.TEXT);
        lastnameField.setWidth(10);
        lastnameField.setRequired (true);
        lastnameField.setCanEdit(true);
        
        ListGridField emailField = new ListGridField("email", "EMAIL");
        emailField.setAlign(Alignment.CENTER);
        //emailField.setType(ListGridFieldType.TEXT);
        emailField.setWidth(10);
        emailField.setRequired (true);
        emailField.setCanEdit(true);
        
        ListGridField offNumfield = new ListGridField("office_number", "OFFICE");
        offNumfield.setAlign(Alignment.CENTER);
        //offNumfield.setType(ListGridFieldType.TEXT);
        offNumfield.setWidth(10);
        offNumfield.setRequired (true);
        offNumfield.setCanEdit(true);
        
        ListGridField teamField = new ListGridField("team", "TEAM");
        teamField.setAlign(Alignment.CENTER);
        //teamField.setType(ListGridFieldType.TEXT);
        teamField.setWidth(10);
        teamField.setRequired (false);
        teamField.setCanEdit(true);
        
        ListGridField appIdField = new ListGridField("appID", "USERNAME");
        appIdField.setAlign(Alignment.CENTER);
        //appIdField.setType(ListGridFieldType.TEXT);
        appIdField.setWidth(10);
        appIdField.setRequired (true);
        appIdField.setCanEdit(true);
        
        ListGridField appPassField = new ListGridField("appPw", "PASSWORD");
        appPassField.setAlign(Alignment.CENTER);
        appPassField.setWidth(10);
        appPassField.setRequired (true);
        appPassField.setHidden(true);
        
        ListGridField statusField = new ListGridField("status", "STATUS");
        statusField.setAlign(Alignment.CENTER);
        statusField.setValueMap("admin","advanced","simple","restricted");
        statusField.setWidth(10);
        statusField.setRequired (true);
        statusField.setCanEdit(true);
		
		this.setFields(idField,firstnameField,lastnameField,emailField,offNumfield,teamField,appIdField,appPassField,statusField);
		//this.setDataPageSize(10);
		this.setAutoFetchData(true);
		this.setDataFetchMode(FetchMode.BASIC);
		this.setAutoFitData(Autofit.BOTH);
		this.setAutoFitMaxRecords(10);
		//this.setAutoFitMaxColumns(3);
		this.setAutoFitWidthApproach(AutoFitWidthApproach.BOTH);
		this.setAutoFitFieldWidths(true);
		this.setAutoFitFieldsFillViewport(false);
		this.setOverflow(Overflow.AUTO);
		this.setAutoWidth();
		//this.setWidth(600);
		this.setRight(30);
		this.setLeft(20);
		
		this.setSelectionType(SelectionStyle.SIMPLE);
	}
}
