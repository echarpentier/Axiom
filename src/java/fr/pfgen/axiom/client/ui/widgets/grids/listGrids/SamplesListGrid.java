package fr.pfgen.axiom.client.ui.widgets.grids.listGrids;

import fr.pfgen.axiom.client.datasources.SamplesDS;

import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.SortSpecifier;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.AutoFitWidthApproach;
import com.smartgwt.client.types.Autofit;
import com.smartgwt.client.types.FetchMode;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.SelectionAppearance;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;

public class SamplesListGrid extends ListGrid{

	public SamplesListGrid(){
		
		DataSource datasource = new SamplesDS();
		
		this.setTitle("Samples");
		//this.setDataSource(SamplesDS.getInstance());
		this.setDataSource(datasource);
		this.setEmptyCellValue("--");
		this.setLayoutAlign(Alignment.CENTER);
		
		ListGridField idField = new ListGridField("sample_id");
		idField.setAlign(Alignment.CENTER);
		idField.setType(ListGridFieldType.INTEGER);
		idField.setHidden(true);
		idField.setWidth(10);
		ListGridField sampleField = new ListGridField("sample_name");
		sampleField.setAlign(Alignment.CENTER);
		sampleField.setType(ListGridFieldType.TEXT);
		sampleField.setWidth(10);
		ListGridField populationField = new ListGridField("population_names");
		populationField.setAlign(Alignment.CENTER);
		populationField.setType(ListGridFieldType.TEXT);
		populationField.setWidth(10);
		ListGridField plateField = new ListGridField("plate_name");
		plateField.setType(ListGridFieldType.TEXT);
		plateField.setAlign(Alignment.CENTER);
		plateField.setWidth(10);
		ListGridField pathField = new ListGridField("sample_path");
		pathField.setAlign(Alignment.CENTER);
		pathField.setType(ListGridFieldType.TEXT);
		pathField.setHidden(true);
		pathField.setWidth(10);
		ListGridField coordXField = new ListGridField("plate_coordX");
		coordXField.setType(ListGridFieldType.INTEGER);
		coordXField.setAlign(Alignment.CENTER);
		coordXField.setWidth(10);
		ListGridField coordYField = new ListGridField("plate_coordY");
		coordYField.setType(ListGridFieldType.INTEGER);
		coordYField.setAlign(Alignment.CENTER);
		coordYField.setWidth(10);
		
		this.setFields(idField,sampleField,populationField,plateField,pathField,coordXField,coordYField);
		//this.setDataPageSize(10);
		
		
		this.setAutoFetchData(true);
		this.setDataFetchMode(FetchMode.PAGED);
		this.setDataPageSize(96);
		this.setAutoFitData(Autofit.BOTH);
		this.setAutoFitMaxRecords(15);
		this.setAutoFitWidthApproach(AutoFitWidthApproach.BOTH);
		this.setAutoFitFieldWidths(true);
		this.setAutoFitFieldsFillViewport(false);
		this.setOverflow(Overflow.AUTO);
		this.setAutoWidth();
		//this.setAutoFitMaxWidth(500);
		this.setAutoFitData(Autofit.BOTH);
		//this.setWidth(600);
		this.setRight(30);
		this.setLeft(20);
		this.setCanEdit(false);
		
		this.setSelectionType(SelectionStyle.SIMPLE);  
		this.setSelectionAppearance(SelectionAppearance.CHECKBOX);
		
		this.addSort(new SortSpecifier("plate_name",SortDirection.ASCENDING));
		this.addSort(new SortSpecifier("plate_coordX",SortDirection.ASCENDING));
		this.addSort(new SortSpecifier("plate_coordY",SortDirection.ASCENDING));
	}
}
