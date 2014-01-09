package fr.pfgen.axiom.client.ui.widgets.grids.listGrids;

import fr.pfgen.axiom.client.datasources.StudiesDS;

import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.AutoFitWidthApproach;
import com.smartgwt.client.types.Autofit;
import com.smartgwt.client.types.FetchMode;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;

public class StudiesListGrid extends ListGrid{

	public StudiesListGrid() {
		
		DataSource datasource = new StudiesDS();
		
		this.setTitle("Studies");
		this.setDataSource(datasource);
		this.setEmptyCellValue("--");
		this.setLayoutAlign(Alignment.CENTER);
		
		ListGridField idField = new ListGridField("study_id");
		idField.setAlign(Alignment.CENTER);
		idField.setType(ListGridFieldType.INTEGER);
		idField.setHidden(true);
		idField.setWidth(10);

		ListGridField nameField = new ListGridField("study_name");
		nameField.setAlign(Alignment.CENTER);
		nameField.setType(ListGridFieldType.TEXT);
		nameField.setWidth(10);

		ListGridField pathField = new ListGridField("study_path");
		pathField.setAlign(Alignment.CENTER);
		pathField.setType(ListGridFieldType.TEXT);
		pathField.setHidden(true);
		pathField.setWidth(10);
		
		ListGridField typeField = new ListGridField("study_type");
		typeField.setAlign(Alignment.CENTER);
		typeField.setType(ListGridFieldType.TEXT);
		typeField.setWidth(10);
		
		ListGridField userIDfield = new ListGridField("user_id");
		userIDfield.setAlign(Alignment.CENTER);
		userIDfield.setType(ListGridFieldType.INTEGER);
		userIDfield.setHidden(true);
		userIDfield.setWidth(10);
		
		
		this.setFields(idField,nameField,pathField,typeField,userIDfield);
	
		this.setSelectionType(SelectionStyle.SINGLE);
		this.setAutoFetchData(true);
		this.setDataFetchMode(FetchMode.BASIC);
		this.setAutoFitData(Autofit.BOTH);
		this.setAutoFitMaxRecords(10);
		this.setAutoFitWidthApproach(AutoFitWidthApproach.BOTH);
		this.setAutoFitFieldWidths(true);
		this.setAutoFitFieldsFillViewport(false);
		this.setOverflow(Overflow.AUTO);
		this.setAutoWidth();
		this.setRight(30);
		this.setLeft(20);
		this.setCanEdit(false);
	}
}
