package fr.pfgen.axiom.client.ui.widgets.grids.listGrids;

import java.util.ArrayList;
import java.util.List;

import fr.pfgen.axiom.client.datasources.GenotypingQCDS;
import fr.pfgen.axiom.client.services.GenotypingQCService;
import fr.pfgen.axiom.client.services.GenotypingQCServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.SortSpecifier;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.AutoFitWidthApproach;
import com.smartgwt.client.types.Autofit;
import com.smartgwt.client.types.FetchMode;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.events.DataArrivedEvent;
import com.smartgwt.client.widgets.grid.events.DataArrivedHandler;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.events.ClickHandler;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;

public class GenoQCListGrid extends ListGrid{

	final GenotypingQCServiceAsync genotypingQCService = GWT.create(GenotypingQCService.class);
	private List<ListGridField> totalFieldsList = new ArrayList<ListGridField>();
	
	private List<ListGridField> getFieldsList() {
		for (ListGridField field : totalFieldsList) {
			field.setAlign(Alignment.CENTER);
		}
		return totalFieldsList;
	}

	public void setCondensedTable(){
		List<ListGridField> totalList = getFieldsList();
		List<ListGridField> newList = new ArrayList<ListGridField>();
		String[] qcNames = {"sample_id","geno_run_id","sample_name","plate_name","population_names","call_rate","hom_rate","het_rate","computed_gender"};
		for (ListGridField field : totalList) {
			for (String qcName : qcNames){
				if (field.getName().equals(qcName)){
					newList.add(field);
				}
			}
		}
		this.setFields(newList.toArray(new ListGridField[newList.size()]));
	}
	
	public void setMinimumTable(){
		List<ListGridField> totalList = getFieldsList();
		List<ListGridField> newList = new ArrayList<ListGridField>();
		String[] qcNames = {"sample_id","geno_run_id","sample_name","plate_name","population_names","call_rate"};
		for (ListGridField field : totalList) {
			for (String qcName : qcNames){
				if (field.getName().equals(qcName)){
					newList.add(field);
				}
			}
		}
		this.setFields(newList.toArray(new ListGridField[newList.size()]));
	}
	
	public void setFullTable(){
		List<ListGridField> list = getFieldsList();
		this.setFields(list.toArray(new ListGridField[list.size()]));
	}
	
	public GenoQCListGrid(){
		this.setTitle("Samples QC");
		//DataSource datasource = SamplesQCDS.getInstance();
		this.setDataSource(new GenotypingQCDS());
		this.setEmptyCellValue("--");
		this.setLayoutAlign(Alignment.CENTER);
		
		final ListGridField idField = new ListGridField("sample_id");
		idField.setAlign(Alignment.CENTER);
		idField.setType(ListGridFieldType.INTEGER);
		idField.setHidden(true);
		idField.setWidth(10);
		final ListGridField genoRunIdField = new ListGridField("geno_run_id");
		genoRunIdField.setAlign(Alignment.CENTER);
		genoRunIdField.setType(ListGridFieldType.INTEGER);
		genoRunIdField.setHidden(true);
		genoRunIdField.setWidth(10);
		final ListGridField sampleField = new ListGridField("sample_name");
		sampleField.setAlign(Alignment.CENTER);
		sampleField.setType(ListGridFieldType.TEXT);
		sampleField.setWidth(10);
		final ListGridField plateField = new ListGridField("plate_name");
		plateField.setType(ListGridFieldType.TEXT);
		plateField.setAlign(Alignment.CENTER);
		plateField.setWidth(10);
		final ListGridField projectField = new ListGridField("population_names");
		projectField.setType(ListGridFieldType.TEXT);
		projectField.setAlign(Alignment.CENTER);
		projectField.setWidth(10);
		final ListGridField runField = new ListGridField("geno_run");
		runField.setType(ListGridFieldType.TEXT);
		runField.setAlign(Alignment.CENTER);
		runField.setHidden(true);
		runField.setWidth(10);
		
		totalFieldsList.add(idField);
		totalFieldsList.add(genoRunIdField);
		totalFieldsList.add(sampleField);
		totalFieldsList.add(plateField);
		totalFieldsList.add(projectField);
		totalFieldsList.add(runField);
		genotypingQCService.getGenoQcParams(new AsyncCallback<List<String>>() {
			
			@Override
			public void onSuccess(List<String> result) {
				for (String qcName : result) {
					ListGridField field = new ListGridField(qcName, qcName);
					field.setType(ListGridFieldType.TEXT);
					field.setWidth(10);
					field.setAlign(Alignment.CENTER);
					totalFieldsList.add(new ListGridField(qcName, qcName));
				}
				setMinimumTable();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				SC.warn("Can't retrieve genotyping QC params from server");
			}
		});
		
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
		
		this.addDataArrivedHandler(new DataArrivedHandler() {
			
			@Override
			public void onDataArrived(DataArrivedEvent event) {
				if (event.getEndRow()==0){
					getContextMenu().getItem(0).setEnabled(false);
				}
			}
		});
		
		this.setAutoFetchData(false);
		this.setDataFetchMode(FetchMode.BASIC);
		this.setAutoFitMaxRecords(15);
		this.setAutoFitWidthApproach(AutoFitWidthApproach.TITLE);
		this.setAutoFitFieldWidths(true);
		this.setAutoFitFieldsFillViewport(false);
		this.setOverflow(Overflow.AUTO);
		this.setAutoWidth();
		this.setAutoFitData(Autofit.BOTH);
		this.setRight(30);
		this.setLeft(20);
		this.setCanEdit(false);
		
		//this.setSelectionType(SelectionStyle.SIMPLE);  
		//this.setSelectionAppearance(SelectionAppearance.CHECKBOX);
		
		//this.addSort(new SortSpecifier("project_name",SortDirection.ASCENDING));
		this.addSort(new SortSpecifier("plate_name",SortDirection.ASCENDING));
	}
}
