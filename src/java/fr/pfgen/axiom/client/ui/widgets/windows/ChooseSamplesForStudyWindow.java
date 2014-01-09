package fr.pfgen.axiom.client.ui.widgets.windows;

import java.util.ArrayList;
import java.util.List;

import fr.pfgen.axiom.client.services.GenotypingQCService;
import fr.pfgen.axiom.client.services.GenotypingQCServiceAsync;
import fr.pfgen.axiom.client.services.StudiesService;
import fr.pfgen.axiom.client.services.StudiesServiceAsync;
import fr.pfgen.axiom.client.ui.widgets.grids.listGrids.GenoAnalysisListGrid;
import fr.pfgen.axiom.client.ui.widgets.tabs.StudySamplesTab;
import fr.pfgen.axiom.client.ui.widgets.vstacks.GenoAnalysisVstack;
import fr.pfgen.axiom.client.ui.widgets.vstacks.GenoQCVstack;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.RecordList;
import com.smartgwt.client.data.SortSpecifier;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.AutoFitWidthApproach;
import com.smartgwt.client.types.Autofit;
import com.smartgwt.client.types.DragDataAction;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.SelectionAppearance;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.DataArrivedEvent;
import com.smartgwt.client.widgets.grid.events.DataArrivedHandler;
import com.smartgwt.client.widgets.grid.events.RecordClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordClickHandler;
import com.smartgwt.client.widgets.grid.events.RecordDoubleClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordDoubleClickHandler;
import com.smartgwt.client.widgets.grid.events.RecordDropEvent;
import com.smartgwt.client.widgets.grid.events.RecordDropHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.VStack;

public class ChooseSamplesForStudyWindow extends Dialog{

	private final GenotypingQCServiceAsync genotypingQCService = GWT.create(GenotypingQCService.class);
	private final StudiesServiceAsync studiesService = GWT.create(StudiesService.class);
	private final String stackBorder = "1px solid #C0C0C0";
	private final HLayout mainHlayout;
	private final ListGrid samplesListgrid;
	final IButton proceedButton = new IButton("Proceed");
	final IButton cancelButton = new IButton("Cancel");
	final Img loadingGif = new Img("loadingStar.gif",30,30);
	
	public ChooseSamplesForStudyWindow(final String studyName, final StudySamplesTab sp){
		setAutoSize(true);
		setIsModal(true);
		setShowModalMask(true);
		setTitle("Samples to include in study "+studyName);
		setShowMinimizeButton(false);
		setCanDragReposition(true);
		
		loadingGif.setLayoutAlign(Alignment.CENTER);
		
		addCloseClickHandler(new CloseClickHandler() {
			
			@Override
			public void onCloseClick(CloseClientEvent event) {
				destroy();
			}
		});

		mainHlayout = new HLayout(10);
		samplesListgrid = new ListGrid();
		
		proceedButton.disable();
		cancelButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				destroy();
			}
		});
		
		proceedButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				proceedButton.disable();
				addItem(loadingGif);
				studiesService.checkGenoSamplesForStudy(studyName, new AsyncCallback<Boolean>() {

					@Override
					public void onFailure(Throwable caught) {
						SC.warn("Cannot check if samples for this study have already been set");
					}

					@Override
					public void onSuccess(Boolean result) {
						if (result!=null){
							if (result){
								SC.warn("Samples for this study have alread been set");
								sp.showSamplesInStudy();
								destroy();
							}else{
								ListGridRecord[] records = samplesListgrid.getRecords();
								final List<Integer> genoRunIdList = new ArrayList<Integer>(records.length);
								for (ListGridRecord record : records) {
									genoRunIdList.add(record.getAttributeAsInt("geno_run_id"));
								}
								studiesService.addSamplesToStudy(studyName, genoRunIdList, new AsyncCallback<String>() {

									@Override
									public void onFailure(Throwable caught) {
										SC.warn(caught.toString());
										removeItem(loadingGif);
										proceedButton.enable();
									}

									@Override
									public void onSuccess(String result) {
										SC.say(result);
										sp.showSamplesInStudy();
										destroy();
									}
								});
							}
						}else{
							SC.warn("Cannot check if samples for this study have already been set");
						}
					}
				});
			}
		});
		
		final VLayout genoAnalysisVlayout = new VLayout(10);
		
		genoAnalysisVlayout.addMember(constructGenoAnalysisStack());
		genoAnalysisVlayout.addMember(constructSamplesListgrid(studyName));
		
		mainHlayout.addMember(genoAnalysisVlayout);
		
		setToolbarButtons(proceedButton,cancelButton);
		
		addItem(mainHlayout);
	}
	
	private VStack constructGenoAnalysisStack(){
		GenoAnalysisVstack genoAnalysisStack = new GenoAnalysisVstack();
		genoAnalysisStack.setShowEdges(false);
		genoAnalysisStack.setBorder(stackBorder);
		genoAnalysisStack.addHeaderLabel("Choose genotyping analysis");
		genoAnalysisStack.addGrid();
		GenoAnalysisListGrid genoAnalysisListGrid = genoAnalysisStack.getGrid();
		genoAnalysisListGrid.setAutoFitMaxRecords(8);
		genoAnalysisListGrid.setPrompt("Double clic on genotyping analysis to select samples");
		
		genoAnalysisListGrid.addRecordDoubleClickHandler(new RecordDoubleClickHandler() {
			
			@Override
			public void onRecordDoubleClick(RecordDoubleClickEvent event) {
				final String genoName = event.getRecord().getAttribute("geno_name");
				genotypingQCService.secondRunExistsInGenoAnalysis(event.getRecord().getAttribute("geno_name"), new AsyncCallback<Boolean>() {
					
					@Override
					public void onSuccess(Boolean result) {
						if (mainHlayout.getMembers().length>1){
							for (int i = 1; i < mainHlayout.getMembers().length; i++) {
								mainHlayout.getMember(i).destroy();
							}
						}
						if (result!=null){
							if (result){ //a second run exists
								VLayout genoQCVlayout = new VLayout(10);
								genoQCVlayout.addMember(constructGenoQCStack(genoName,"second"));
								genoQCVlayout.addMember(constructNoPassCallRateStack(genoName));
								mainHlayout.addMember(genoQCVlayout);
							}else{ //only one run performed
								VLayout genoQCVlayout = new VLayout(10);
								genoQCVlayout.addMember(constructGenoQCStack(genoName,"first"));
								mainHlayout.addMember(genoQCVlayout);
							}
							
						}else{
							SC.warn("Cannot check if second run exists in genotyping analysis");
						}
					}
					
					@Override
					public void onFailure(Throwable caught) {
						SC.warn("Cannot check if second run exists in genotyping analysis");
					}
				});
			}
		});
		
		return genoAnalysisStack;
	}
	
	private VStack constructNoPassCallRateStack(String genoName){
		final GenoQCVstack genoQCStack = new GenoQCVstack();
		genoQCStack.setShowEdges(false);
		genoQCStack.setBorder(stackBorder);
		Criteria criteria = new Criteria();
		criteria.addCriteria("callRateLimit", genoName);
		genoQCStack.addHeaderLabel("Samples with bad callrate on first run");
		genoQCStack.addGrid();
		genoQCStack.getGrid().fetchData(criteria);
		genoQCStack.getGrid().setAutoFitMaxRecords(8);
		genoQCStack.getGrid().setCanDragRecordsOut(true);
		genoQCStack.getGrid().setDragDataAction(DragDataAction.COPY);
		genoQCStack.getGrid().setSelectionAppearance(SelectionAppearance.CHECKBOX);
		genoQCStack.getGrid().setSelectionType(SelectionStyle.SIMPLE);
		genoQCStack.getGrid().setPrompt("Select samples with checkbox, drag and drop selected records to the \"Samples to add to study\" grid");
		genoQCStack.getGrid().addDataArrivedHandler(new DataArrivedHandler() {
			
			@Override
			public void onDataArrived(DataArrivedEvent event) {
				genoQCStack.addLabel(event.getEndRow()+" samples");
			}
		});
		
		return genoQCStack;
	}
	
	private VStack constructGenoQCStack(String genoName,String run){
		final GenoQCVstack genoQCStack = new GenoQCVstack();
		genoQCStack.setShowEdges(false);
		genoQCStack.setBorder(stackBorder);
		Criteria criteria = new Criteria();
		criteria.addCriteria("geno_name", genoName);
		criteria.addCriteria("geno_run", run);
		genoQCStack.addHeaderLabel("Genotyping QC ("+run+" run)");
		genoQCStack.addGrid();
		genoQCStack.getGrid().fetchData(criteria);
		genoQCStack.getGrid().setAutoFitMaxRecords(8);
		genoQCStack.getGrid().setCanDragRecordsOut(true);
		genoQCStack.getGrid().setDragDataAction(DragDataAction.COPY);
		genoQCStack.getGrid().setSelectionAppearance(SelectionAppearance.CHECKBOX);
		genoQCStack.getGrid().setSelectionType(SelectionStyle.SIMPLE);
		genoQCStack.getGrid().setPrompt("Select samples with checkbox, drag and drop selected records to the \"Samples to add\" grid");
		genoQCStack.getGrid().addDataArrivedHandler(new DataArrivedHandler() {
			
			@Override
			public void onDataArrived(DataArrivedEvent event) {
				genoQCStack.addLabel(event.getEndRow()+" samples");
			}
		});
		
		return genoQCStack;
	}

	private VLayout constructSamplesListgrid(String studyName){
		VLayout listgridVlayout = new VLayout(10);
		listgridVlayout.setBorder("1px solid red");
		listgridVlayout.setLayoutMargin(10);
		
		Label headerLabel = new Label();
		headerLabel.setContents("Samples&nbsp;to&nbsp;add&nbsp;to&nbsp;study");
		headerLabel.setStyleName("textTitle");
		headerLabel.setOverflow(Overflow.VISIBLE);
		headerLabel.setAutoHeight();
		headerLabel.setAutoWidth();
		
		final ListGridField idField = new ListGridField("sample_id", "ID");
		idField.setAlign(Alignment.CENTER);
		idField.setType(ListGridFieldType.INTEGER);
		idField.setHidden(true);
		idField.setWidth(10);
		final ListGridField genoRunIdField = new ListGridField("geno_run_id", "GENO RUN ID");
		genoRunIdField.setAlign(Alignment.CENTER);
		genoRunIdField.setType(ListGridFieldType.INTEGER);
		genoRunIdField.setHidden(true);
		genoRunIdField.setRequired(true);
		final ListGridField sampleField = new ListGridField("sample_name", "NAME");
		sampleField.setAlign(Alignment.CENTER);
		sampleField.setType(ListGridFieldType.TEXT);
		sampleField.setWidth(10);
		final ListGridField plateField = new ListGridField("plate_name", "PLATE");
		plateField.setType(ListGridFieldType.TEXT);
		plateField.setAlign(Alignment.CENTER);
		plateField.setWidth(10);
		final ListGridField populationField = new ListGridField("population_names", "IN POPULATIONS");
		populationField.setAlign(Alignment.CENTER);
		populationField.setType(ListGridFieldType.TEXT);
		populationField.setWidth(10);
		final ListGridField runField = new ListGridField("geno_run", "RUN");
		runField.setAlign(Alignment.CENTER);
		runField.setType(ListGridFieldType.TEXT);
		runField.setWidth(10);
		final ListGridField callRateField = new ListGridField("call_rate");
		callRateField.setAlign(Alignment.CENTER);
		callRateField.setType(ListGridFieldType.TEXT);
		callRateField.setWidth(10);
		final ListGridField removeField = new ListGridField("_remove_record", "REMOVE");
		removeField.setType(ListGridFieldType.ICON);
		removeField.setCellIcon("icons/Remove.png");
		removeField.setAlign(Alignment.CENTER);
		removeField.setWidth(20);
		removeField.setPrompt("Clic if you want to remove from genotyping");
		
		samplesListgrid.setFields(idField,genoRunIdField,sampleField,plateField,populationField,runField,callRateField,removeField);
		
		samplesListgrid.setTitle("Samples");
		samplesListgrid.setEmptyCellValue("--");
		samplesListgrid.setLayoutAlign(Alignment.CENTER);
		samplesListgrid.setAnimateRemoveRecord(true);
		samplesListgrid.setPreventDuplicates(true);
		
		samplesListgrid.setEmptyMessage("Add samples to fill the listgrid");
		samplesListgrid.setAutoFitData(Autofit.BOTH);
		samplesListgrid.setAutoFitMaxRecords(10);
		samplesListgrid.setAutoFitWidthApproach(AutoFitWidthApproach.BOTH);
		samplesListgrid.setAutoFitFieldWidths(true);
		samplesListgrid.setAutoFitFieldsFillViewport(false);
		samplesListgrid.setOverflow(Overflow.AUTO);
		samplesListgrid.setAutoWidth();
		samplesListgrid.setAutoFitData(Autofit.BOTH);
		samplesListgrid.setRight(30);
		samplesListgrid.setLeft(20);
		samplesListgrid.setCanEdit(false);
		samplesListgrid.setSelectionAppearance(SelectionAppearance.CHECKBOX);
		samplesListgrid.setSelectionType(SelectionStyle.SIMPLE);
		samplesListgrid.setCanAcceptDroppedRecords(true);
		
		samplesListgrid.addSort(new SortSpecifier("plate_name",SortDirection.ASCENDING));
		
		final Label footerLabel = new Label();
		footerLabel.setOverflow(Overflow.VISIBLE);
		footerLabel.setAutoHeight();
		footerLabel.setAutoWidth();
		
		samplesListgrid.addRecordDropHandler(new RecordDropHandler() {
			
			@Override
			public void onRecordDrop(RecordDropEvent event) {
				ListGridRecord[] dropRecords = event.getDropRecords();
				RecordList list = samplesListgrid.getDataAsRecordList();
				List<ListGridRecord> dupIDList = new ArrayList<ListGridRecord>();
				List<ListGridRecord> dupNameList = new ArrayList<ListGridRecord>();
				
				for (ListGridRecord dropRecord : dropRecords) {
					if (list.find("sample_id", dropRecord.getAttributeAsInt("sample_id"))!=null){
						event.cancel();
						dupIDList.add(dropRecord);
					}else{
						event.cancel();
						samplesListgrid.addData(dropRecord);
					}
				}
				
				for (ListGridRecord dropRecord : dupNameList) {
					if (list.find("sample_name", dropRecord.getAttributeAsString("sample_name"))!=null){
						event.cancel();
						dupNameList.add(dropRecord);
					}else{
						event.cancel();
						samplesListgrid.addData(dropRecord);
					}
				}
				
				int nbTotalSamplesInGrid = samplesListgrid.getTotalRows();
				footerLabel.setContents("Number&nbsp;of&nbsp;samples:&nbsp;"+nbTotalSamplesInGrid);
				if (nbTotalSamplesInGrid>0){
					proceedButton.enable();
				}else{
					proceedButton.disable();
				}
				StringBuilder si = new StringBuilder();
				for (ListGridRecord r : dupIDList) {
					si.append(r.getAttribute("sample_name")+", ");
				}
				StringBuilder sn = new StringBuilder();
				for (ListGridRecord r : dupNameList) {
					sn.append(r.getAttribute("sample_name")+", ");
				}
				
				StringBuilder st = new StringBuilder();
				if (!si.toString().equals("")){
					st.append("Duplicate records: "+si.toString());
				}
				if (!sn.toString().equals("")){
					st.append("<br>Duplicate sample names: "+sn.toString());
				}
				if (!st.toString().equals("")){
					SC.say(st.toString());
				}
			}
		});
		
		removeField.addRecordClickHandler(new RecordClickHandler() {
			
			@Override
			public void onRecordClick(RecordClickEvent event) {
				if(event.getField().getName().equals(removeField.getName())){
					if (samplesListgrid.getSelectedRecords().length>1){
						for (ListGridRecord record : samplesListgrid.getSelectedRecords()){
							samplesListgrid.removeData(record);
						}
					}else{
						samplesListgrid.removeData(event.getRecord());
					}
					int nbTotalSamplesInGrid = samplesListgrid.getTotalRows();
					footerLabel.setContents("Number&nbsp;of&nbsp;samples:&nbsp;"+nbTotalSamplesInGrid);
					if (nbTotalSamplesInGrid>0){
						proceedButton.enable();
					}else{
						proceedButton.disable();
					}
				}
			};
		});

		listgridVlayout.addMember(headerLabel);
		listgridVlayout.addMember(samplesListgrid);
		listgridVlayout.addMember(footerLabel);
		
		return listgridVlayout;
	}
}
