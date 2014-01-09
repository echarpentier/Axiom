package fr.pfgen.axiom.client.ui.widgets.grids.listGrids;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fr.pfgen.axiom.client.datasources.SamplesQCDS;
import fr.pfgen.axiom.client.services.SamplesQCService;
import fr.pfgen.axiom.client.services.SamplesQCServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.SortSpecifier;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.AutoFitWidthApproach;
import com.smartgwt.client.types.Autofit;
import com.smartgwt.client.types.ExportDisplay;
import com.smartgwt.client.types.ExportFormat;
import com.smartgwt.client.types.FetchMode;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.SelectionAppearance;
import com.smartgwt.client.types.SelectionStyle;
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

public class QCListGrid extends ListGrid{

	final SamplesQCServiceAsync samplesQCService = GWT.create(SamplesQCService.class);
	private Map<String, ListGridField> identityFields = new LinkedHashMap<String, ListGridField>();
	private Map<String, ListGridField> qcParamsFields = new HashMap<String, ListGridField>();
	private Map<String, ListGridField> userParamsFields = new HashMap<String, ListGridField>();

	public void setCondensedTable(){
		List<ListGridField> newList = new ArrayList<ListGridField>();
		for (String fieldName : identityFields.keySet()) {
			newList.add(identityFields.get(fieldName));
		}
		String[] qcNames = {"axiom_dishqc_DQC","cn-probe-chrXY-ratio_gender"};
		for (String fieldName : qcNames) {
			newList.add(qcParamsFields.get(fieldName));
		}
		
		this.setFields(newList.toArray(new ListGridField[newList.size()]));
	}
	
	public void setMinimumTable(){
		List<ListGridField> newList = new ArrayList<ListGridField>();
		for (String fieldName : identityFields.keySet()) {
			newList.add(identityFields.get(fieldName));
		}
		String[] qcNames = {"axiom_dishqc_DQC"};
		for (String fieldName : qcNames) {
			newList.add(qcParamsFields.get(fieldName));
		}
		
		this.setFields(newList.toArray(new ListGridField[newList.size()]));
	}
	
	public void setUserQcTable(){
		List<ListGridField> newList = new ArrayList<ListGridField>();
		for (String fieldName : identityFields.keySet()) {
			newList.add(identityFields.get(fieldName));
		}
		String[] qcNames = {"axiom_dishqc_DQC"};
		for (String fieldName : qcNames) {
			newList.add(qcParamsFields.get(fieldName));
		}
		for (String fieldName : userParamsFields.keySet()) {
			newList.add(userParamsFields.get(fieldName));
		}
		
		this.setFields(newList.toArray(new ListGridField[newList.size()]));
	}
	
	public void setFullTable(){
		List<ListGridField> newList = new ArrayList<ListGridField>();
		for (String fieldName : identityFields.keySet()) {
			newList.add(identityFields.get(fieldName));
		}
		for (String fieldName : qcParamsFields.keySet()) {
			newList.add(qcParamsFields.get(fieldName));
		}
		for (String fieldName : userParamsFields.keySet()) {
			newList.add(userParamsFields.get(fieldName));
		}
		
		this.setFields(newList.toArray(new ListGridField[newList.size()]));
	}
	
	public QCListGrid(){
		this.setTitle("Samples QC");
		//DataSource datasource = SamplesQCDS.getInstance();
		final SamplesQCDS datasource = SamplesQCDS.getInstance();
		this.setDataSource(datasource);
		this.setEmptyCellValue("--");
		this.setLayoutAlign(Alignment.CENTER);
		
		final ListGridField idField = new ListGridField("sample_id");
		idField.setAlign(Alignment.CENTER);
		idField.setType(ListGridFieldType.INTEGER);
		idField.setHidden(true);
		idField.setWidth(10);
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
		
		identityFields.put(idField.getName(), idField);
		identityFields.put(sampleField.getName(), sampleField);
		identityFields.put(plateField.getName(), plateField);
		identityFields.put(projectField.getName(), projectField);
		
		samplesQCService.getQcParams(new AsyncCallback<List<String>>() {
			
			@Override
			public void onSuccess(List<String> result) {
				for (String qcName : result) {
					ListGridField field = new ListGridField(qcName, qcName);
					field.setType(ListGridFieldType.TEXT);
					field.setWidth(10);
					field.setAlign(Alignment.CENTER);
					qcParamsFields.put(field.getName(), field);
				}
				setMinimumTable();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				SC.warn("Can't retrieve QC params from server");
			}
		});
		
		samplesQCService.getUserParams(new AsyncCallback<List<String>>() {

			@Override
			public void onFailure(Throwable caught) {
				SC.warn("Can't retrieve user QC params from server");
			}

			@Override
			public void onSuccess(List<String> result) {
				for (String qcName : result) {
					ListGridField field = new ListGridField(qcName, qcName);
					field.setType(ListGridFieldType.TEXT);
					field.setWidth(10);
					field.setAlign(Alignment.CENTER);
					userParamsFields.put(field.getName(), field);
				}
			}
		});
		
		Menu gridMenu = new Menu();
		
		MenuItem downloadFile = new MenuItem();
		downloadFile.setTitle("Download tsv");
		downloadFile.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(MenuItemClickEvent event) {
				DSRequest dsRequestProperties = new DSRequest();
				
				dsRequestProperties.setExportFilename("test");
				dsRequestProperties.setExportResults(true);
				dsRequestProperties.setExportAs(ExportFormat.CSV);
				dsRequestProperties.setExportDisplay(ExportDisplay.DOWNLOAD);
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
		
		this.setSelectionType(SelectionStyle.SIMPLE);  
		this.setSelectionAppearance(SelectionAppearance.CHECKBOX);
		
		this.addSort(new SortSpecifier("plate_name",SortDirection.ASCENDING));
	}
}
