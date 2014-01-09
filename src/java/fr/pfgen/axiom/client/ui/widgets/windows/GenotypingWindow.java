package fr.pfgen.axiom.client.ui.widgets.windows;

import java.util.ArrayList;
import java.util.List;

import fr.pfgen.axiom.client.Axiom;
import fr.pfgen.axiom.client.datasources.SamplesDS;
import fr.pfgen.axiom.client.services.AnnotationFilesService;
import fr.pfgen.axiom.client.services.AnnotationFilesServiceAsync;
import fr.pfgen.axiom.client.services.GenotypingService;
import fr.pfgen.axiom.client.services.GenotypingServiceAsync;
import fr.pfgen.axiom.client.services.LibraryFilesService;
import fr.pfgen.axiom.client.services.LibraryFilesServiceAsync;
import fr.pfgen.axiom.client.services.PlatesService;
import fr.pfgen.axiom.client.services.PlatesServiceAsync;
import fr.pfgen.axiom.client.services.PopulationsService;
import fr.pfgen.axiom.client.services.PopulationsServiceAsync;
import fr.pfgen.axiom.client.services.SamplesService;
import fr.pfgen.axiom.client.services.SamplesServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.DSCallback;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.data.SortSpecifier;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.AutoFitWidthApproach;
import com.smartgwt.client.types.Autofit;
import com.smartgwt.client.types.Cursor;
import com.smartgwt.client.types.DragDataAction;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.SelectionAppearance;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.events.DropMoveEvent;
import com.smartgwt.client.widgets.events.DropMoveHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.SpinnerItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.form.validator.RegExpValidator;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;
import com.smartgwt.client.widgets.grid.events.RecordDropEvent;
import com.smartgwt.client.widgets.grid.events.RecordDropHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

public class GenotypingWindow extends Dialog{

	private final PopulationsServiceAsync populationsService = GWT.create(PopulationsService.class);
	private final PlatesServiceAsync platesService = GWT.create(PlatesService.class);
	private final SamplesServiceAsync samplesService = GWT.create(SamplesService.class);
	private final GenotypingServiceAsync genotypingService = GWT.create(GenotypingService.class);
	private final AnnotationFilesServiceAsync annotationFilesService = GWT.create(AnnotationFilesService.class);
	private final LibraryFilesServiceAsync libraryFilesService = GWT.create(LibraryFilesService.class);
	private int nbTotalSamplesInGrid;
	private int nbSamplesWithoutQC;
	
	public GenotypingWindow(){
		setAutoSize(true);
		setIsModal(true);
		setShowModalMask(true);
		setTitle("New Genotyping");
		setShowMinimizeButton(false);
		setCanDragReposition(true);
		
		addCloseClickHandler(new CloseClickHandler() {
			
			@Override
			public void onCloseClick(CloseClientEvent event) {
				destroy();
			}
		});
		
		//hlayout for different samples to add (population, plates, samples)
		final HLayout mainHlayout = new HLayout(20);
		
		final VLayout populationVlayout = new VLayout(10);
		populationVlayout.setBorder("1px solid #C0C0C0");
		populationVlayout.setLayoutMargin(5);
		final VLayout plateVlayout = new VLayout(10);
		plateVlayout.setBorder("1px solid #C0C0C0");
		plateVlayout.setLayoutMargin(5);
		final VLayout sampleVlayout = new VLayout(10);
		sampleVlayout.setBorder("1px solid #C0C0C0");
		sampleVlayout.setLayoutMargin(5);
		final VLayout sampleListVlayout = new VLayout(10);
		sampleListVlayout.setBorder("1px solid #C0C0C0");
		sampleListVlayout.setLayoutMargin(5);
		
		final Label populationTitle = new Label();
		populationTitle.setHeight(20);
		populationTitle.setContents("Populations");
		populationTitle.setStyleName("textTitle");
		
		final Label plateTitle = new Label();
		plateTitle.setHeight(20);
		plateTitle.setContents("Plates");
		plateTitle.setStyleName("textTitle");
		
		final Label sampleTitle = new Label();
		sampleTitle.setHeight(20);
		sampleTitle.setContents("Samples");
		sampleTitle.setStyleName("textTitle");
		
		final Label totalSamplesTitle = new Label();
		totalSamplesTitle.setHeight(20);
		totalSamplesTitle.setContents("Total&nbsp;samples&nbsp;included");
		totalSamplesTitle.setStyleName("textTitle");
		
		final Label numberSamplesInGrid = new Label();
		numberSamplesInGrid.setHeight(10);
		final Label numberSamplesWoQcInGrid = new Label();
		numberSamplesWoQcInGrid.setHeight(10);
		
		final IButton proceedButton = new IButton("Proceed");
		proceedButton.disable();
		final IButton cancelButton = new IButton("Cancel", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				destroy();
			}
		});
		
		final ListGrid samplesListGrid = new ListGrid();
		
		final ListGridField idField = new ListGridField("sample_id", "ID");
		idField.setAlign(Alignment.CENTER);
		idField.setType(ListGridFieldType.INTEGER);
		idField.setHidden(true);
		idField.setWidth(10);
		final ListGridField sampleField = new ListGridField("sample_name", "NAME");
		sampleField.setAlign(Alignment.CENTER);
		sampleField.setType(ListGridFieldType.TEXT);
		sampleField.setWidth(10);
		final ListGridField populationField = new ListGridField("population_names", "IN POPULATIONS");
		populationField.setAlign(Alignment.CENTER);
		populationField.setType(ListGridFieldType.TEXT);
		populationField.setWidth(10);
		final ListGridField plateField = new ListGridField("plate_name", "PLATE");
		plateField.setType(ListGridFieldType.TEXT);
		plateField.setAlign(Alignment.CENTER);
		plateField.setWidth(10);
		final ListGridField pathField = new ListGridField("sample_path", "PATH");
		pathField.setAlign(Alignment.CENTER);
		pathField.setType(ListGridFieldType.TEXT);
		pathField.setHidden(true);
		pathField.setWidth(10);
		final ListGridField coordXField = new ListGridField("plate_coordX", "X COORD");
		coordXField.setType(ListGridFieldType.INTEGER);
		coordXField.setAlign(Alignment.CENTER);
		coordXField.setWidth(10);
		final ListGridField coordYField = new ListGridField("plate_coordY", "Y COORD");
		coordYField.setType(ListGridFieldType.INTEGER);
		coordYField.setAlign(Alignment.CENTER);
		coordYField.setWidth(10);
		final ListGridField removeField = new ListGridField("_remove_record", "REMOVE");
		removeField.setType(ListGridFieldType.ICON);
		removeField.setCellIcon("icons/Remove.png");
		removeField.setAlign(Alignment.CENTER);
		removeField.setWidth(20);
		removeField.setPrompt("Clic if you want to remove from genotyping");
		
		final IButton addPopulationButton = new IButton("Add population", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				final Window populationWindow = new Window();
				populationWindow.setTitle("Choose a population");
				populationWindow.setAutoSize(true);
				populationWindow.setIsModal(true);
				populationWindow.setShowModalMask(true);
				populationWindow.setShowMinimizeButton(false);
				populationWindow.setCanDragReposition(true);
				populationWindow.setAutoCenter(true);
				final DynamicForm populationForm = new DynamicForm();
				populationForm.setAutoHeight();
				populationForm.setAutoWidth();
				populationForm.setPadding(5);
				populationForm.setLayoutAlign(Alignment.CENTER);

				final SelectItem selectPopulation = new SelectItem();
				selectPopulation.setTitle("Population");
				
				populationsService.getPopulationNames(new AsyncCallback<List<String>>() {

					@Override
					public void onSuccess(List<String> result) {
						if (result != null && !result.isEmpty()){
							selectPopulation.setValueMap(result.toArray(new String[result.size()]));
						}
					}
					
					@Override
					public void onFailure(Throwable caught) {
						SC.warn("Can't retreive existing projects from server !");
					}
				});
				
				selectPopulation.addChangedHandler(new ChangedHandler() {
					
					@Override
					public void onChanged(ChangedEvent event) {
						//SamplesDS samplesDS = SamplesDS.getInstance();
						SamplesDS samplesDS = new SamplesDS();
						Criteria criteria = new Criteria();
						criteria.addCriteria("population_name", selectPopulation.getValueAsString());
						samplesDS.fetchData(criteria, new DSCallback() {
							
							@Override
							public void execute(DSResponse response, Object rawData, DSRequest request) {
								if (response.getData().length!=0){

									RecordList list = samplesListGrid.getDataAsRecordList();
									for (Record record : response.getData()) {
										if (list.find("sample_id", record.getAttributeAsInt("sample_id"))==null){
											list.add(record);
										}
									}
									samplesListGrid.setData(list);
									nbTotalSamplesInGrid = samplesListGrid.getTotalRows();
									numberSamplesInGrid.setContents("Number&nbsp;of&nbsp;samples:&nbsp;"+nbTotalSamplesInGrid);

									Record[] sampleList = list.duplicate();
									List<Integer> sampleIDList = new ArrayList<Integer>(sampleList.length);
									for (Record record : sampleList){
										sampleIDList.add(record.getAttributeAsInt("sample_id"));
									}
									samplesService.nbSamplesWithoutQC(sampleIDList, new AsyncCallback<Integer>() {

										@Override
										public void onFailure(Throwable caught) {
											SC.warn("Cannot retrieve the number of samples without QC on server.");
										}

										@Override
										public void onSuccess(Integer result) {
											nbSamplesWithoutQC = result;
											if (nbSamplesWithoutQC>0){
												numberSamplesWoQcInGrid.setContents("<font color=\"red\">Number&nbsp;of&nbsp;samples&nbsp;without&nbsp;QC:&nbsp;"+nbSamplesWithoutQC+".\nPerform&nbsp;QC&nbsp;analysis&nbsp;before&nbsp;running&nbsp;genotyping&nbsp;on&nbsp;these&nbsp;samples.</font>");
												proceedButton.disable();
											}else{
												proceedButton.enable();
											}
										}
									});
								}
							}
						});
						populationWindow.destroy();
					}
				});
				populationWindow.addCloseClickHandler(new CloseClickHandler() {
					
					@Override
					public void onCloseClick(CloseClientEvent event) {
						populationWindow.destroy();
					}
				});
				populationForm.setFields(selectPopulation);
				populationWindow.addItem(populationForm);
				populationWindow.show();
			}
		});
		
		addPopulationButton.setPrompt("Adding a population will add all the samples from this population in the listgrid.");
		addPopulationButton.setIcon("icons/Create.png");
		addPopulationButton.setOverflow(Overflow.VISIBLE);
		addPopulationButton.setAutoWidth();
		
		final IButton removePopulationButton = new IButton("Remove population", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				final Window populationWindow = new Window();
				populationWindow.setTitle("Choose a population");
				populationWindow.setAutoSize(true);
				populationWindow.setIsModal(true);
				populationWindow.setShowModalMask(true);
				populationWindow.setShowMinimizeButton(false);
				populationWindow.setCanDragReposition(true);
				populationWindow.setAutoCenter(true);
				final DynamicForm populationForm = new DynamicForm();
				populationForm.setAutoHeight();
				populationForm.setAutoWidth();
				populationForm.setPadding(5);
				populationForm.setLayoutAlign(Alignment.CENTER);

				final SelectItem selectPopulation = new SelectItem();
				selectPopulation.setTitle("Population");
				
				populationsService.getPopulationNames(new AsyncCallback<List<String>>() {

					@Override
					public void onSuccess(List<String> result) {
						if (result != null && !result.isEmpty()){
							selectPopulation.setValueMap(result.toArray(new String[result.size()]));
						}
					}
					
					@Override
					public void onFailure(Throwable caught) {
						SC.warn("Can't retreive existing projects from server !");
					}
				});
				
				selectPopulation.addChangedHandler(new ChangedHandler() {
					
					@Override
					public void onChanged(ChangedEvent event) {
						ListGridRecord[] records = samplesListGrid.getRecords();
						for (ListGridRecord record : records) {
							if (record.getAttributeAsString("population_names")!=null){
								//&&(record.getAttributeAsString("population_names")).equals((selectPopulation.getValueAsString()))){
								String[] popNames = record.getAttributeAsString("population_names").split(",");
								for (String popName : popNames) {
									if (selectPopulation.getValueAsString().equals(popName)){
										samplesListGrid.removeData(record);
									}
								}
							}
						}
						nbTotalSamplesInGrid = samplesListGrid.getTotalRows();
						numberSamplesInGrid.setContents("Number&nbsp;of&nbsp;samples:&nbsp;"+nbTotalSamplesInGrid);
						
						RecordList list = samplesListGrid.getDataAsRecordList();
						Record[] sampleList = list.duplicate();
						if (sampleList.length>0){
							List<Integer> sampleIDList = new ArrayList<Integer>(sampleList.length);
							for (Record record : sampleList){
								sampleIDList.add(record.getAttributeAsInt("sample_id"));
							}
							samplesService.nbSamplesWithoutQC(sampleIDList, new AsyncCallback<Integer>() {

								@Override
								public void onFailure(Throwable caught) {
									SC.warn("Cannot retrieve the number of samples without QC on server.");
								}

								@Override
								public void onSuccess(Integer result) {
									nbSamplesWithoutQC = result;
									if (nbSamplesWithoutQC>0){
										numberSamplesWoQcInGrid.setContents("<font color=\"red\">Number&nbsp;of&nbsp;samples&nbsp;without&nbsp;QC:&nbsp;"+nbSamplesWithoutQC+".\nPerform&nbsp;QC&nbsp;analysis&nbsp;before&nbsp;running&nbsp;genotyping&nbsp;on&nbsp;these&nbsp;samples.</font>");
									}else{
										numberSamplesWoQcInGrid.setContents("");
										proceedButton.enable();
									}
								}
							});
						}else{
							numberSamplesWoQcInGrid.setContents("");
							proceedButton.disable();
						}
						populationWindow.destroy();
					}
				});
				populationWindow.addCloseClickHandler(new CloseClickHandler() {
					
					@Override
					public void onCloseClick(CloseClientEvent event) {
						populationWindow.destroy();
					}
				});
				populationForm.setFields(selectPopulation);
				populationWindow.addItem(populationForm);
				populationWindow.show();
			}
		});
		
		removePopulationButton.setPrompt("Removing a population will remove all the samples from this population in the listgrid.");
		removePopulationButton.setIcon("icons/Remove.png");
		removePopulationButton.setOverflow(Overflow.VISIBLE);
		removePopulationButton.setAutoWidth();
		
		final IButton addPlateButton = new IButton("Add plate", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				final Window plateWindow = new Window();
				plateWindow.setTitle("Choose a plate");
				plateWindow.setAutoSize(true);
				plateWindow.setIsModal(true);
				plateWindow.setShowModalMask(true);
				plateWindow.setShowMinimizeButton(false);
				plateWindow.setCanDragReposition(true);
				plateWindow.setAutoCenter(true);
				final DynamicForm plateForm = new DynamicForm();  
				plateForm.setAutoHeight(); 
				plateForm.setAutoWidth();
				plateForm.setPadding(5);
				plateForm.setLayoutAlign(Alignment.CENTER);

				final SelectItem selectPlate = new SelectItem();
				selectPlate.setTitle("Plate");
				
				platesService.getPlateNames(new AsyncCallback<List<String>>() {

					@Override
					public void onSuccess(List<String> result) {
						if (result != null && !result.isEmpty()){
							selectPlate.setValueMap(result.toArray(new String[result.size()]));
						}
					}	
					
					@Override
					public void onFailure(Throwable caught) {
						SC.warn("Can't retreive existing plates from server !");
					}
				});
				
				selectPlate.addChangedHandler(new ChangedHandler() {
					
					@Override
					public void onChanged(ChangedEvent event) {
						//SamplesDS samplesDS = SamplesDS.getInstance();
						SamplesDS samplesDS = new SamplesDS();
						Criteria criteria = new Criteria();
						criteria.addCriteria("plate_name", selectPlate.getValueAsString());
						samplesDS.fetchData(criteria, new DSCallback() {
							
							@Override
							public void execute(DSResponse response, Object rawData, DSRequest request) {
								RecordList list = samplesListGrid.getDataAsRecordList();
								for (Record record : response.getData()) {
									if (list.find("sample_id", record.getAttributeAsInt("sample_id"))==null){
										list.add(record);
									}
								}
								samplesListGrid.setData(list);
								nbTotalSamplesInGrid = samplesListGrid.getTotalRows();
								numberSamplesInGrid.setContents("Number&nbsp;of&nbsp;samples:&nbsp;"+nbTotalSamplesInGrid);
								
								Record[] sampleList = list.duplicate();
								List<Integer> sampleIDList = new ArrayList<Integer>(sampleList.length);
								for (Record record : sampleList){
									sampleIDList.add(record.getAttributeAsInt("sample_id"));
								}
								samplesService.nbSamplesWithoutQC(sampleIDList, new AsyncCallback<Integer>() {

									@Override
									public void onFailure(Throwable caught) {
										SC.warn("Cannot retrieve the number of samples without QC on server.");
									}

									@Override
									public void onSuccess(Integer result) {
										nbSamplesWithoutQC = result;
										if (nbSamplesWithoutQC>0){
											numberSamplesWoQcInGrid.setContents("<font color=\"red\">Number&nbsp;of&nbsp;samples&nbsp;without&nbsp;QC:&nbsp;"+nbSamplesWithoutQC+".\nPerform&nbsp;QC&nbsp;analysis&nbsp;before&nbsp;running&nbsp;genotyping&nbsp;on&nbsp;these&nbsp;samples.</font>");
											proceedButton.disable();
										}else{
											proceedButton.enable();
										}
									}
								});
							}
						});
						plateWindow.destroy();
					}
				});
				plateWindow.addCloseClickHandler(new CloseClickHandler() {
					
					@Override
					public void onCloseClick(CloseClientEvent event) {
						plateWindow.destroy();
					}
				});
				plateForm.setFields(selectPlate);
				plateWindow.addItem(plateForm);
				plateWindow.show();
			}
		});
		
		addPlateButton.setPrompt("Adding a plate will add all the samples from this plate in the listgrid");
		addPlateButton.setIcon("icons/Create.png");
		addPlateButton.setOverflow(Overflow.VISIBLE);
		addPlateButton.setAutoWidth();

		final IButton removePlateButton = new IButton("Remove plate", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				final Window plateWindow = new Window();
				plateWindow.setTitle("Choose a plate");
				plateWindow.setAutoSize(true);
				plateWindow.setIsModal(true);
				plateWindow.setShowModalMask(true);
				plateWindow.setShowMinimizeButton(false);
				plateWindow.setCanDragReposition(true);
				plateWindow.setAutoCenter(true);
				final DynamicForm plateForm = new DynamicForm();
				plateForm.setAutoHeight();
				plateForm.setAutoWidth();
				plateForm.setPadding(5);
				plateForm.setLayoutAlign(Alignment.CENTER);

				final SelectItem selectPlate = new SelectItem();
				selectPlate.setTitle("Plate");
				
				platesService.getPlateNames(new AsyncCallback<List<String>>() {

					@Override
					public void onSuccess(List<String> result) {
						if (result != null && !result.isEmpty()){
							selectPlate.setValueMap(result.toArray(new String[result.size()]));
						}
					}	
					
					@Override
					public void onFailure(Throwable caught) {
						SC.warn("Can't retreive existing plates from server !");
					}
				});
				
				selectPlate.addChangedHandler(new ChangedHandler() {
					
					@Override
					public void onChanged(ChangedEvent event) {
						ListGridRecord[] records = samplesListGrid.getRecords();
						for (ListGridRecord record : records) {
							if (record.getAttributeAsString("plate_name")!=null&&(record.getAttributeAsString("plate_name")).equals((selectPlate.getValueAsString()))){
								samplesListGrid.removeData(record);
							}
						}
						nbTotalSamplesInGrid = samplesListGrid.getTotalRows();
						numberSamplesInGrid.setContents("Number&nbsp;of&nbsp;samples:&nbsp;"+nbTotalSamplesInGrid);
						
						RecordList list = samplesListGrid.getDataAsRecordList();
						Record[] sampleList = list.duplicate();
						if (sampleList.length>0){
							List<Integer> sampleIDList = new ArrayList<Integer>(sampleList.length);
							for (Record record : sampleList){
								sampleIDList.add(record.getAttributeAsInt("sample_id"));
							}
							samplesService.nbSamplesWithoutQC(sampleIDList, new AsyncCallback<Integer>() {

								@Override
								public void onFailure(Throwable caught) {
									SC.warn("Cannot retrieve the number of samples without QC on server.");
								}

								@Override
								public void onSuccess(Integer result) {
									nbSamplesWithoutQC = result;
									if (nbSamplesWithoutQC>0){
										numberSamplesWoQcInGrid.setContents("<font color=\"red\">Number&nbsp;of&nbsp;samples&nbsp;without&nbsp;QC:&nbsp;"+nbSamplesWithoutQC+".\nPerform&nbsp;QC&nbsp;analysis&nbsp;before&nbsp;running&nbsp;genotyping&nbsp;on&nbsp;these&nbsp;samples.</font>");
									}else{
										numberSamplesWoQcInGrid.setContents("");
										proceedButton.enable();
									}
								}
							});
						}else{
							numberSamplesWoQcInGrid.setContents("");
							proceedButton.disable();
						}
						plateWindow.destroy();
					}
				});
				plateWindow.addCloseClickHandler(new CloseClickHandler() {
					
					@Override
					public void onCloseClick(CloseClientEvent event) {
						plateWindow.destroy();
					}
				});
				plateForm.setFields(selectPlate);
				plateWindow.addItem(plateForm);
				plateWindow.show();
			}
		});
		
		removePlateButton.setPrompt("Removing a plate will remove all the samples from this plate in the listgrid.");
		removePlateButton.setIcon("icons/Remove.png");
		removePlateButton.setOverflow(Overflow.VISIBLE);
		removePlateButton.setAutoWidth();		
		
		final IButton addSampleButton = new IButton("Add sample", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (sampleVlayout.getMember("geno_listgrid_sample")!=null){
					sampleVlayout.getMember("geno_listgrid_sample").destroy();
				}
				if (sampleVlayout.getMember("geno_plate_select")!=null){
					sampleVlayout.getMember("geno_plate_select").destroy();
				}
				final DynamicForm plateForm = new DynamicForm();
				plateForm.setID("geno_plate_select");
				plateForm.setAutoHeight(); 
				plateForm.setAutoWidth();
				plateForm.setPadding(5);
				plateForm.setLayoutAlign(Alignment.CENTER);

				final SelectItem selectPlate = new SelectItem();
				selectPlate.setTitle("Plate");
				
				platesService.getPlateNames(new AsyncCallback<List<String>>() {

					@Override
					public void onSuccess(List<String> result) {
						if (result != null && !result.isEmpty()){
							selectPlate.setValueMap(result.toArray(new String[result.size()]));
						}
					}	
					
					@Override
					public void onFailure(Throwable caught) {
						SC.warn("Can't retreive existing plates from server !");
					}
				});
				
				selectPlate.addChangedHandler(new ChangedHandler() {
					
					@Override
					public void onChanged(ChangedEvent event) {
						if (sampleVlayout.getMember("geno_listgrid_sample")!=null){
							sampleVlayout.getMember("geno_listgrid_sample").destroy();
						}
						final ListGrid samplesToAddListGrid = new ListGrid();
						samplesToAddListGrid.setID("geno_listgrid_sample");
						samplesToAddListGrid.setTitle("Samples to add. Drag and drop records.");
						samplesToAddListGrid.setEmptyCellValue("--");
						samplesToAddListGrid.setLayoutAlign(Alignment.CENTER);
						
						ListGridField[] defFields = {idField,sampleField,populationField,plateField,pathField,coordXField,coordYField};
						samplesToAddListGrid.setDefaultFields(defFields);
						samplesToAddListGrid.setAutoFitData(Autofit.BOTH);
						samplesToAddListGrid.setAutoFitMaxRecords(10);
						samplesToAddListGrid.setAutoFitWidthApproach(AutoFitWidthApproach.BOTH);
						samplesToAddListGrid.setAutoFitFieldWidths(true);
						samplesToAddListGrid.setAutoFitFieldsFillViewport(false);
						samplesToAddListGrid.setOverflow(Overflow.AUTO);
						samplesToAddListGrid.setAutoWidth();
						samplesToAddListGrid.setAutoFitData(Autofit.BOTH);
						samplesToAddListGrid.setRight(30);
						samplesToAddListGrid.setLeft(20);
						samplesToAddListGrid.setCanEdit(false);
						samplesToAddListGrid.setSelectionType(SelectionStyle.SINGLE);
						samplesToAddListGrid.setCanDragRecordsOut(true);
						samplesToAddListGrid.setDragDataAction(DragDataAction.COPY);
						
						samplesToAddListGrid.addSort(new SortSpecifier("plate_name",SortDirection.ASCENDING));
						samplesToAddListGrid.addSort(new SortSpecifier("plate_coordX",SortDirection.ASCENDING));
						samplesToAddListGrid.addSort(new SortSpecifier("plate_coordY",SortDirection.ASCENDING));	
						
						//SamplesDS samplesDS = SamplesDS.getInstance();
						SamplesDS samplesDS = new SamplesDS();
						Criteria criteria = new Criteria();
						criteria.addCriteria("plate_name", selectPlate.getValueAsString());
						samplesDS.fetchData(criteria, new DSCallback() {
							
							@Override
							public void execute(DSResponse response, Object rawData, DSRequest request) {
								samplesToAddListGrid.setData(response.getDataAsRecordList());
							}
						});
						sampleVlayout.addMember(samplesToAddListGrid);
					}
				});
				plateForm.setFields(selectPlate);
				sampleVlayout.addMember(plateForm);
			}
		});
		
		samplesListGrid.setTitle("Samples");
		samplesListGrid.setEmptyCellValue("--");
		samplesListGrid.setLayoutAlign(Alignment.CENTER);
		samplesListGrid.setAnimateRemoveRecord(true);
		samplesListGrid.setPreventDuplicates(true);
		
		removeField.addRecordClickHandler(new RecordClickHandler() {
			
			@Override
			public void onRecordClick(RecordClickEvent event) {
				if(event.getField().getName().equals(removeField.getName())){
					if (samplesListGrid.getSelectedRecords().length>1){
						for (ListGridRecord record : samplesListGrid.getSelectedRecords()){
							samplesListGrid.removeData(record);
						}
					}else{
						samplesListGrid.removeData(event.getRecord());
					}
					nbTotalSamplesInGrid = samplesListGrid.getTotalRows();
					numberSamplesInGrid.setContents("Number&nbsp;of&nbsp;samples:&nbsp;"+nbTotalSamplesInGrid);
					
					RecordList list = samplesListGrid.getDataAsRecordList();
					Record[] sampleList = list.duplicate();
					if (sampleList.length>0){
						List<Integer> sampleIDList = new ArrayList<Integer>(sampleList.length);
						for (Record record : sampleList){
							sampleIDList.add(record.getAttributeAsInt("sample_id"));
						}
						samplesService.nbSamplesWithoutQC(sampleIDList, new AsyncCallback<Integer>() {

							@Override
							public void onFailure(Throwable caught) {
								SC.warn("Cannot retrieve the number of samples without QC on server.");
							}

							@Override
							public void onSuccess(Integer result) {
								nbSamplesWithoutQC = result;
								if (nbSamplesWithoutQC>0){
									numberSamplesWoQcInGrid.setContents("<font color=\"red\">Number&nbsp;of&nbsp;samples&nbsp;without&nbsp;QC:&nbsp;"+nbSamplesWithoutQC+".\nPerform&nbsp;QC&nbsp;analysis&nbsp;before&nbsp;running&nbsp;genotyping&nbsp;on&nbsp;these&nbsp;samples.</font>");
								}else{
									numberSamplesWoQcInGrid.setContents("");
									proceedButton.enable();
								}
							}
						});
					}else{
						numberSamplesWoQcInGrid.setContents("");
						proceedButton.disable();
					}
				}
				
			}
		});
		
		samplesListGrid.addDropMoveHandler(new DropMoveHandler() {
			
			@Override
			public void onDropMove(DropMoveEvent event) {
				samplesListGrid.setCursor(Cursor.CROSSHAIR);
			}
		});
		
		samplesListGrid.addRecordDropHandler(new RecordDropHandler() {
			
			@Override
			public void onRecordDrop(RecordDropEvent event) {
				ListGridRecord[] dropRecords = event.getDropRecords();
				RecordList list = samplesListGrid.getDataAsRecordList();
				for (ListGridRecord dropRecord : dropRecords) {
					if (list.find("sample_id", dropRecord.getAttributeAsInt("sample_id"))!=null){
						event.cancel();
					}else{
						event.cancel();
						samplesListGrid.addData(dropRecord);
					}
				}
				
				nbTotalSamplesInGrid = samplesListGrid.getTotalRows();
				numberSamplesInGrid.setContents("Number&nbsp;of&nbsp;samples:&nbsp;"+nbTotalSamplesInGrid);
				
				list = samplesListGrid.getDataAsRecordList();
				Record[] sampleList = list.duplicate();
				if (sampleList.length>0){
					List<Integer> sampleIDList = new ArrayList<Integer>(sampleList.length);
					for (Record record : sampleList){
						sampleIDList.add(record.getAttributeAsInt("sample_id"));
					}
					samplesService.nbSamplesWithoutQC(sampleIDList, new AsyncCallback<Integer>() {

						@Override
						public void onFailure(Throwable caught) {
							SC.warn("Cannot retrieve the number of samples without QC on server.");
						}

						@Override
						public void onSuccess(Integer result) {
							nbSamplesWithoutQC = result;
							if (nbSamplesWithoutQC>0){
								numberSamplesWoQcInGrid.setContents("<font color=\"red\">Number&nbsp;of&nbsp;samples&nbsp;without&nbsp;QC:&nbsp;"+nbSamplesWithoutQC+".\nPerform&nbsp;QC&nbsp;analysis&nbsp;before&nbsp;running&nbsp;genotyping&nbsp;on&nbsp;these&nbsp;samples.</font>");
							}else{
								numberSamplesWoQcInGrid.setContents("");
								proceedButton.enable();
							}
						}
					});
				}else{
					numberSamplesWoQcInGrid.setContents("");
					proceedButton.disable();
				}
			}
		});
		
		samplesListGrid.setFields(idField,sampleField,populationField,plateField,pathField,coordXField,coordYField,removeField);
		samplesListGrid.setEmptyMessage("Add populations, plates or samples to fill the listgrid");
		samplesListGrid.setAutoFitData(Autofit.BOTH);
		samplesListGrid.setAutoFitMaxRecords(15);
		samplesListGrid.setAutoFitWidthApproach(AutoFitWidthApproach.BOTH);
		samplesListGrid.setAutoFitFieldWidths(true);
		samplesListGrid.setAutoFitFieldsFillViewport(false);
		samplesListGrid.setOverflow(Overflow.AUTO);
		samplesListGrid.setAutoWidth();
		samplesListGrid.setAutoFitData(Autofit.BOTH);
		samplesListGrid.setRight(30);
		samplesListGrid.setLeft(20);
		samplesListGrid.setCanEdit(false);  
		samplesListGrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
		samplesListGrid.setSelectionType(SelectionStyle.SIMPLE);
		samplesListGrid.setCanAcceptDroppedRecords(true);
		
		samplesListGrid.addSort(new SortSpecifier("plate_name",SortDirection.ASCENDING));
		samplesListGrid.addSort(new SortSpecifier("plate_coordX",SortDirection.ASCENDING));
		samplesListGrid.addSort(new SortSpecifier("plate_coordY",SortDirection.ASCENDING));
	
		addSampleButton.setIcon("icons/Create.png");
		addSampleButton.setOverflow(Overflow.VISIBLE);
		addSampleButton.setAutoWidth();
		
		populationVlayout.addMember(populationTitle);
		populationVlayout.addMember(addPopulationButton);
		populationVlayout.addMember(removePopulationButton);
		
		plateVlayout.addMember(plateTitle);
		plateVlayout.addMember(addPlateButton);
		plateVlayout.addMember(removePlateButton);
		
		sampleVlayout.addMember(sampleTitle);
		sampleVlayout.addMember(addSampleButton);
		
		sampleListVlayout.addMember(totalSamplesTitle);
		sampleListVlayout.addMember(samplesListGrid);
		sampleListVlayout.addMember(numberSamplesInGrid);
		sampleListVlayout.addMember(numberSamplesWoQcInGrid);
		
		mainHlayout.addMember(populationVlayout);
		mainHlayout.addMember(plateVlayout);
		mainHlayout.addMember(sampleVlayout);
		mainHlayout.addMember(sampleListVlayout);
		
		final HLayout formsHlayout = new HLayout(20);
		formsHlayout.setTop(20);
		formsHlayout.setLayoutAlign(Alignment.CENTER);
		
		final DynamicForm nameForm = new DynamicForm();
		nameForm.setAutoHeight(); 
		nameForm.setAutoWidth();
		nameForm.setPadding(5);
		nameForm.setLayoutAlign(Alignment.CENTER);
		nameForm.setIsGroup(true);
		nameForm.setBorder("1px solid #C0C0C0");
		nameForm.setGroupTitle("Genotyping Options");

		final TextItem nameTextItem = new TextItem("genoName", "Genotyping Name");
		RegExpValidator regex = new RegExpValidator("^[a-zA-Z0-9_-]{3,25}$");
		regex.setErrorMessage("Field must contain only letters, digits, underscores and hyphens. Min 3 characters, max 25.");
		nameTextItem.setValidators(regex);
		nameTextItem.setRequired(true);
		
		final CheckboxItem filterByQC = new CheckboxItem("filterQC", "DishQC Filter");
		filterByQC.setPrompt("if checked, samples with dishQC < specified value will be removed from genotyping.(RECOMMENDED 0.82)");
		filterByQC.setHoverWidth(250);
		filterByQC.setDefaultValue(true);

		final SpinnerItem dishQCSpinnerItem = new SpinnerItem("dishQC", "DishQC Value");
		dishQCSpinnerItem.setRequired(true);
		dishQCSpinnerItem.setDefaultValue(0.82d);  
		dishQCSpinnerItem.setMin(0.70);  
		dishQCSpinnerItem.setMax(0.99);  
		dishQCSpinnerItem.setStep(0.01d);
		dishQCSpinnerItem.setWidth(60);
		
		final CheckboxItem multipleGeno = new CheckboxItem("multiple", "Multiple Genotyping");
		multipleGeno.setPrompt("If checked, all samples with call rate < specified value after a genotyping run will be removed before performing a new run.  Please note that this option can considerably increase the amount of time needed for the run.(RECOMMENDED 97.0)");
		multipleGeno.setHoverWidth(500);
		multipleGeno.setDefaultValue(true);
		
		final SpinnerItem callRateSpinnerItem = new SpinnerItem("callRate", "Call Rate Value");
		callRateSpinnerItem.setRequired(true);
		callRateSpinnerItem.setDefaultValue(97.0d);  
		callRateSpinnerItem.setMin(90.0);  
		callRateSpinnerItem.setMax(99.9);  
		callRateSpinnerItem.setStep(0.1d);
		callRateSpinnerItem.setWidth(60);
		
		final SelectItem annotItem = new SelectItem("annotFile", "Annotation File");
		annotItem.setRequired(true);
		annotItem.setWidth(250);
		annotItem.setPrompt("The annotation file will not be used for the genotyping runs.  It is only used to lookup the SNP frequencies in the file \"metrics.txt\"");
		annotItem.setHoverWidth(500);
		
		annotationFilesService.getAnnotationFilesNames(new AsyncCallback<List<String>>() {

			@Override
			public void onSuccess(List<String> result) {
				if (result != null && !result.isEmpty()){
					annotItem.setValueMap(result.toArray(new String[result.size()]));
				}
			}	
			
			@Override
			public void onFailure(Throwable caught) {
				SC.warn("Can't retreive annotation files from server !");
			}
		});
		
		final SelectItem libraryItem = new SelectItem("libraryFiles", "Library Files");
		libraryItem.setRequired(true);
		libraryItem.setWidth(250);
		libraryItem.setPrompt("The library files determine which SNPs will be called during the genotyping runs. This parameter must be set carefully. Please use the old library (\"r2\") for genotyping of the old chemistry chips and the new one (\"r4\") for the new chemistry.");
		libraryItem.setHoverWidth(500);
		
		libraryFilesService.getLibraryFilesFolderNames(new AsyncCallback<List<String>>() {

			@Override
			public void onSuccess(List<String> result) {
				if (result != null && !result.isEmpty()){
					libraryItem.setValueMap(result.toArray(new String[result.size()]));
				}
			}	
			
			@Override
			public void onFailure(Throwable caught) {
				SC.warn("Can't retreive library files from server !");
			}
		});
		
		filterByQC.addChangedHandler(new ChangedHandler() {
			
			@Override
			public void onChanged(ChangedEvent event) {
				if ((Boolean) event.getValue()){
					dishQCSpinnerItem.enable();
				}else{
					dishQCSpinnerItem.disable();
				}
			}
		});
		
		multipleGeno.addChangedHandler(new ChangedHandler() {
			
			@Override
			public void onChanged(ChangedEvent event) {
				if ((Boolean) event.getValue()){
					callRateSpinnerItem.enable();
				}else{
					callRateSpinnerItem.disable();
				}
			}
		});
		
		nameForm.setFields(nameTextItem,filterByQC,dishQCSpinnerItem,multipleGeno,callRateSpinnerItem,annotItem,libraryItem);
		
		formsHlayout.addMember(nameForm);
			
		addItem(mainHlayout);
		addItem(formsHlayout);
		
		final Img loadingGif = new Img("loadingStar.gif",30,30);
		loadingGif.setLayoutAlign(Alignment.CENTER);
		
		proceedButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (nameForm.validate()){
					proceedButton.disable();
					addItem(loadingGif);
					ListGridRecord[] records = samplesListGrid.getRecords();
					final List<Integer> sampleIdList = new ArrayList<Integer>(records.length);
					for (ListGridRecord record : records) {
						sampleIdList.add(record.getAttributeAsInt("sample_id"));
					}
					final double dishQCValue,callRateValue;
					if (dishQCSpinnerItem.isDisabled()){
						dishQCValue = 0;
					}else{
						dishQCValue = Double.parseDouble(dishQCSpinnerItem.getValueAsString());
					}
					if (callRateSpinnerItem.isDisabled()){
						callRateValue = 0;
					}else{
						callRateValue = Double.parseDouble(callRateSpinnerItem.getValueAsString());
					}
					
					SC.confirm("Confirm new genotyping", "A new genotyping calling will be launched on "+sampleIdList.size()+" samples.  Please confirm !", new BooleanCallback() {
						
						@Override
						public void execute(Boolean value) {
							if (value != null && value){
								genotypingService.performGenotyping(sampleIdList, nameTextItem.getValueAsString(), dishQCValue, callRateValue, Axiom.get().getUser().getUserID(), libraryItem.getValueAsString(), annotItem.getValueAsString(), new AsyncCallback<String>() {
									
									@Override
									public void onFailure(Throwable caught) {
										SC.warn("Can't perform genotyping on server");
										destroy();
									}

									@Override
									public void onSuccess(String result) {
										if (result.startsWith("This name has already been used") || result.startsWith("Maximum simultaneous genotyping analysis authorized is reached")){
											removeItem(loadingGif);
											proceedButton.enable();
											SC.say(result);
										}else{
											SC.say(result);
											destroy();
										}
									}
								});
							}else{
								removeItem(loadingGif);
								proceedButton.enable();
							}
						}
					});
				}else{
					SC.warn("Please make appropriate corrections on form before submitting");
				}
			}
		});
		
		setToolbarButtons(proceedButton,cancelButton);
		show();
	}
}
