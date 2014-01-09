package fr.pfgen.axiom.client.ui.widgets.grids.listGrids;

import com.smartgwt.client.data.SortSpecifier;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.AutoFitWidthApproach;
import com.smartgwt.client.types.Autofit;
import com.smartgwt.client.types.FetchMode;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import fr.pfgen.axiom.client.datasources.PlatesDS;

public class PlatesListGrid extends ListGrid{
	
	public PlatesListGrid(){
		
		PlatesDS datasource = new PlatesDS();
		
		//PlatesDS datasource = PlatesDS.getInstance();
		
		this.setTitle("List of plates in database.  Double clic on a plate record to edit its name.  This will have no effect on the database if the name entered is empty or identical to another record.");
		this.setDataSource(datasource);
		this.setEmptyCellValue("--");
		this.setLayoutAlign(Alignment.CENTER);
		
		ListGridField idField = new ListGridField("plate_id");
		idField.setAlign(Alignment.CENTER);
		idField.setType(ListGridFieldType.INTEGER);
		idField.setWidth(10);
		idField.setHidden(true);
		ListGridField barcodeField = new ListGridField("plate_barcode");
		barcodeField.setAlign(Alignment.CENTER);
		barcodeField.setType(ListGridFieldType.TEXT);
		barcodeField.setWidth(10);
		ListGridField nameField = new ListGridField("plate_name");
		nameField.setAlign(Alignment.CENTER);
		nameField.setType(ListGridFieldType.TEXT);
		nameField.setCanEdit(true);
		nameField.setWidth(10);
		ListGridField createdField = new ListGridField("created");
		createdField.setType(ListGridFieldType.DATE);
		createdField.setAlign(Alignment.CENTER);
		createdField.setWidth(10);
		
		this.setFields(idField,barcodeField,nameField,createdField);
		//this.setSortField("plate_id");
		
		this.setSelectionType(SelectionStyle.SINGLE);
		this.setAutoFetchData(true);
		this.setDataFetchMode(FetchMode.PAGED);
		this.setAutoFitData(Autofit.BOTH);
		this.setAutoFitMaxRecords(10);
		this.setAutoFitWidthApproach(AutoFitWidthApproach.BOTH);
		this.setAutoFitFieldWidths(true);
		this.setAutoFitFieldsFillViewport(false);
		this.setOverflow(Overflow.AUTO);
		this.setAutoWidth();
		this.setRight(30);
		this.setLeft(20);
		//this.setCanEdit(false);
		
		this.addSort(new SortSpecifier("created",SortDirection.ASCENDING));
	}
}
