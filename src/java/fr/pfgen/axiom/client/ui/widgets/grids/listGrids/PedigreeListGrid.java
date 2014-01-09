package fr.pfgen.axiom.client.ui.widgets.grids.listGrids;

import fr.pfgen.axiom.client.datasources.PedigreeDS;

import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.SortSpecifier;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.AutoFitWidthApproach;
import com.smartgwt.client.types.Autofit;
import com.smartgwt.client.types.FetchMode;
import com.smartgwt.client.types.ListGridEditEvent;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.RowEndEditAction;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.widgets.form.validator.LengthRangeValidator;
import com.smartgwt.client.widgets.form.validator.RegExpValidator;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.events.ClickHandler;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;

public class PedigreeListGrid extends ListGrid{

	ListGrid singleton;
	
	public PedigreeListGrid(){
	
		singleton = this;
		PedigreeDS datasource = new PedigreeDS();
		
		setTitle("List of individuals in Pedigree.");
		setDataSource(datasource);
		setLayoutAlign(Alignment.CENTER);
		
		RegExpValidator wordRegexVal = new RegExpValidator("\\w+");
		wordRegexVal.setErrorMessage("Only word characters allowed: a-zA-Z_0-9");
		wordRegexVal.setClientOnly(true);
		LengthRangeValidator lengthVal = new LengthRangeValidator();
		lengthVal.setClientOnly(true);
		lengthVal.setMin(1);
		lengthVal.setMax(15);
		lengthVal.setErrorMessage("Length must be at minimum 1 and maximum 15 characters");
		RegExpValidator digitRegexVal = new RegExpValidator("[0-3]"); 
		digitRegexVal.setClientOnly(true);
		digitRegexVal.setErrorMessage("A single digit allowed: 0,1 or 2");
		
		ListGridField idField = new ListGridField("pedigree_id");
		idField.setType(ListGridFieldType.INTEGER);
		idField.setAlign(Alignment.CENTER);
		idField.setWidth(10);
		idField.setHidden(true);
		
		ListGridField familyField = new ListGridField("family_id");
		familyField.setType(ListGridFieldType.TEXT);
		familyField.setAlign(Alignment.CENTER);
		familyField.setWidth(10);
		familyField.setValidators(wordRegexVal, lengthVal);
		
		ListGridField individualField = new ListGridField("individual_id");
		individualField.setType(ListGridFieldType.TEXT);
		individualField.setAlign(Alignment.CENTER);
		individualField.setWidth(10);
		individualField.setValidators(wordRegexVal, lengthVal);
		
		ListGridField fatherField = new ListGridField("father_id");
		fatherField.setType(ListGridFieldType.TEXT);
		fatherField.setAlign(Alignment.CENTER);
		fatherField.setWidth(10);
		fatherField.setValidators(wordRegexVal, lengthVal);
		
		ListGridField motherField = new ListGridField("mother_id");
		motherField.setType(ListGridFieldType.TEXT);
		motherField.setAlign(Alignment.CENTER);
		motherField.setWidth(10);
		motherField.setValidators(wordRegexVal, lengthVal);
		
		ListGridField sexField = new ListGridField("sex");
		sexField.setType(ListGridFieldType.INTEGER);
		sexField.setAlign(Alignment.CENTER);
		sexField.setWidth(10);
		sexField.setValidators(digitRegexVal);
		
		ListGridField statusField = new ListGridField("status");
		statusField.setType(ListGridFieldType.INTEGER);
		statusField.setAlign(Alignment.CENTER);
		statusField.setWidth(10);
		statusField.setValidators(digitRegexVal);
		
		ListGridField userField = new ListGridField("user_id");
		userField.setType(ListGridFieldType.INTEGER);
		userField.setAlign(Alignment.CENTER);
		userField.setWidth(10);
		userField.setHidden(true);
		
		ListGridField studyField = new ListGridField("study_name");
		studyField.setType(ListGridFieldType.TEXT);
		studyField.setAlign(Alignment.CENTER);
		studyField.setWidth(10);
		studyField.setHidden(true);
		
		setFields(idField,familyField,individualField,fatherField,motherField,sexField,statusField,userField,studyField);
		
		Menu gridMenu = new Menu();
		
		MenuItem downloadFile = new MenuItem();
		downloadFile.setTitle("Download tsv");
		downloadFile.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(MenuItemClickEvent event) {
				DSRequest dsRequestProperties = new DSRequest();
				
				//dsRequestProperties.setExportFilename("test");
				dsRequestProperties.setExportResults(true);
				//dsRequestProperties.setExportAs(ExportFormat.CSV);
				//dsRequestProperties.setExportDisplay(ExportDisplay.DOWNLOAD);
				exportData(dsRequestProperties);
			}
		});
		
		
		gridMenu.addItem(downloadFile);
		this.setContextMenu(gridMenu);
		
		
		setAutoFetchData(false);  
		setCanEdit(true);  
		setModalEditing(true);
		setRowEndEditAction(RowEndEditAction.DONE);
		setEditEvent(ListGridEditEvent.DOUBLECLICK);
		setSaveByCell(false);
		setAutoSaveEdits(false);
		setAutoWidth();
		setSelectionType(SelectionStyle.SINGLE);
		setValidateByCell(true);
		setDataFetchMode(FetchMode.BASIC);
		setAutoFitData(Autofit.BOTH);
		setAutoFitMaxRecords(10);
		setAutoFitWidthApproach(AutoFitWidthApproach.BOTH);
		setAutoFitFieldWidths(true);
		setAutoFitFieldsFillViewport(false);
		setOverflow(Overflow.AUTO);
		setRight(30);
		setLeft(20);
		
		addSort(new SortSpecifier("individual_id",SortDirection.ASCENDING));
	}
}
