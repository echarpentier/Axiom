package fr.pfgen.axiom.client.ui.widgets.grids.listGrids;

import fr.pfgen.axiom.client.Axiom;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.AutoFitWidthApproach;
import com.smartgwt.client.types.Autofit;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.VLayout;

public class UserListGrid extends VLayout{

	public UserListGrid(){

		/*
		 * Create listGrid into layout
		 */
		
		final Label gridTitle = new Label();
		gridTitle.setHeight(10);
		gridTitle.setContents("User");
		gridTitle.setStyleName("textTitle");
		
		this.addMember(gridTitle);
		
		final ListGrid userGrid = new ListGrid();
		userGrid.setTitle("User");
		//get user infos
		ListGridRecord[] list = new ListGridRecord[1];
		ListGridRecord userLGR = new ListGridRecord();
		userLGR.setAttribute("userID", Axiom.get().getUser().getUserID());
		userLGR.setAttribute("firstname", Axiom.get().getUser().getFirstname());
		userLGR.setAttribute("lastname", Axiom.get().getUser().getLastname());
		userLGR.setAttribute("email", Axiom.get().getUser().getEmail());
		userLGR.setAttribute("office_number", Axiom.get().getUser().getOffice_number());
		userLGR.setAttribute("team", Axiom.get().getUser().getTeam());
		userLGR.setAttribute("appID", Axiom.get().getUser().getAppID());
		userLGR.setAttribute("appPw", Axiom.get().getUser().getAppPw());
		userLGR.setAttribute("status", Axiom.get().getUser().getStatus());
		list[0] = userLGR;
		userGrid.setData(list);
		
		userGrid.setEmptyCellValue("--");
		
		ListGridField idField = new ListGridField("userID");
		idField.setAlign(Alignment.CENTER);
		idField.setType(ListGridFieldType.INTEGER);
		ListGridField firstnameField = new ListGridField("firstname");
		firstnameField.setAlign(Alignment.CENTER);
		firstnameField.setType(ListGridFieldType.TEXT);
		ListGridField lastnameField = new ListGridField("lastname");
		lastnameField.setAlign(Alignment.CENTER);
		lastnameField.setType(ListGridFieldType.TEXT);
		ListGridField emailField = new ListGridField("email");
		emailField.setType(ListGridFieldType.TEXT);
		emailField.setAlign(Alignment.CENTER);
		ListGridField officeField = new ListGridField("office_number");
		officeField.setType(ListGridFieldType.TEXT);
		officeField.setAlign(Alignment.CENTER);
		ListGridField appIDField = new ListGridField("appID");
		appIDField.setType(ListGridFieldType.TEXT);
		appIDField.setAlign(Alignment.CENTER);
		ListGridField statusField = new ListGridField("status");
		statusField.setValueMap("admin","advanced","simple");
		statusField.setAlign(Alignment.CENTER);
		
		userGrid.setFields(firstnameField,lastnameField,emailField,officeField,appIDField,statusField);
		userGrid.setAutoFitData(Autofit.BOTH);
		userGrid.setAutoHeight();
		userGrid.setAutoFitWidthApproach(AutoFitWidthApproach.BOTH);
		userGrid.setAutoFitFieldWidths(true);
		userGrid.setAutoFitFieldsFillViewport(false);
		userGrid.setOverflow(Overflow.AUTO);
		userGrid.setAutoWidth();
		userGrid.setRight(30);
		userGrid.setLeft(20);
		userGrid.setCanEdit(false);
		
		this.addMember(userGrid);
	}
}
