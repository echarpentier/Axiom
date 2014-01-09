package fr.pfgen.axiom.client.ui.widgets.tabs;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.util.StringUtil;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.grid.events.DataArrivedEvent;
import com.smartgwt.client.widgets.grid.events.DataArrivedHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.VStack;
import com.smartgwt.client.widgets.menu.IMenuButton;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.MenuItemSeparator;
import com.smartgwt.client.widgets.menu.events.ClickHandler;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;

import fr.pfgen.axiom.client.services.AnnotationFilesService;
import fr.pfgen.axiom.client.services.AnnotationFilesServiceAsync;
import fr.pfgen.axiom.client.services.GenotypingAnalysisService;
import fr.pfgen.axiom.client.services.GenotypingAnalysisServiceAsync;
import fr.pfgen.axiom.client.services.SNPListsService;
import fr.pfgen.axiom.client.services.SNPListsServiceAsync;
import fr.pfgen.axiom.client.ui.widgets.MainArea;
import fr.pfgen.axiom.client.ui.widgets.grids.listGrids.GenoAnalysisListGrid;
import fr.pfgen.axiom.client.ui.widgets.grids.listGrids.GenoQCListGrid;
import fr.pfgen.axiom.client.ui.widgets.grids.listGrids.QCListGrid;
import fr.pfgen.axiom.client.ui.widgets.windows.ModalProgressWindow;
import fr.pfgen.axiom.client.ui.widgets.windows.SNPListsWindow;

public class GenotypingQCTab {
	
	private final GenotypingAnalysisServiceAsync genotypingService = GWT.create(GenotypingAnalysisService.class);
	private final AnnotationFilesServiceAsync annotationService = GWT.create(AnnotationFilesService.class);
	private SNPListsServiceAsync snpListsService = GWT.create(SNPListsService.class);

	public GenotypingQCTab(final String genoName, final String tabID){
		VLayout vlayout = new VLayout(15);
		vlayout.setWidth("80%");
		vlayout.setDefaultLayoutAlign(Alignment.CENTER);
		
		final HLayout mainHlayout = new HLayout(10);
		
		final Menu statsMenu = new Menu();
		
		MenuItemSeparator separator = new MenuItemSeparator();
		
		MenuItem statsMenuItem = new MenuItem();
		statsMenuItem.setTitle("Statistics");
		statsMenuItem.setIcon("icons/Bar-chart.png");
		
		statsMenuItem.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(MenuItemClickEvent event) {
				String id = tabID+"_genoAnalysis";
				if (mainHlayout.getMember(id)!=null){
					mainHlayout.getMember(id).destroy();
				}else{
					VLayout genoAnalysisVlayout = new VLayout(10);
					genoAnalysisVlayout.setID(id);
					genoAnalysisVlayout.setAutoHeight();
					genoAnalysisVlayout.setAutoWidth();
					genoAnalysisVlayout.addMember(constructGenoAnalysisGrid(genoName));
					mainHlayout.addMember(genoAnalysisVlayout);
				}
			}
		});
		
		MenuItem samplesNoPassMenuItem = new MenuItem();
		samplesNoPassMenuItem.setTitle("Samples with bad QC");
		samplesNoPassMenuItem.setIcon("icons/Abort.png");
		
		samplesNoPassMenuItem.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(MenuItemClickEvent event) {
				String id = tabID+"_badQCgrids";
				if (mainHlayout.getMember(id)!=null){
					mainHlayout.getMember(id).destroy();
				}else{
					VLayout samplesBadQcGridsVlayout = new VLayout(10);
					samplesBadQcGridsVlayout.setID(id);
					samplesBadQcGridsVlayout.setAutoHeight();
					samplesBadQcGridsVlayout.setAutoWidth();
					samplesBadQcGridsVlayout.addMember(constructPassQcListgrid(genoName));
					samplesBadQcGridsVlayout.addMember(constructPassCallrateListgrid(genoName));
					mainHlayout.addMember(samplesBadQcGridsVlayout);
				}
			}
		});
		
		MenuItem genoQCMenuItem = new MenuItem();
		genoQCMenuItem.setTitle("Genotyping QC grids");
		genoQCMenuItem.setIcon("icons/32.png");
		
		genoQCMenuItem.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(MenuItemClickEvent event) {
				String id = tabID+"_genoQCgrids";
				if (mainHlayout.getMember(id)!=null){
					mainHlayout.getMember(id).destroy();
				}else{
					VLayout genoQcGridsVlayout = new VLayout(10);
					genoQcGridsVlayout.setID(id);
					genoQcGridsVlayout.setAutoHeight();
					genoQcGridsVlayout.setAutoWidth();
					genoQcGridsVlayout.addMember(constructGenoQCListgrid(genoName,"first"));
					genoQcGridsVlayout.addMember(constructGenoQCListgrid(genoName, "second"));
					mainHlayout.addMember(genoQcGridsVlayout);
				}
			}
		});
		
		MenuItem clusterGraphMenuItem = new MenuItem();
		clusterGraphMenuItem.setTitle("Cluster graphs");
		clusterGraphMenuItem.setIcon("icons/Clusters.ico");
		
		clusterGraphMenuItem.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(MenuItemClickEvent event) {
				String id = tabID+"_clusterGraph";
				if (mainHlayout.getMember(id)!=null){
					mainHlayout.getMember(id).destroy();
				}else{
					VLayout clusterGraphVlayout = new VLayout(10);
					clusterGraphVlayout.setID(id);
					clusterGraphVlayout.setAutoHeight();
					clusterGraphVlayout.setAutoWidth();
					clusterGraphVlayout.addMember(constructClusterGraphLayout(genoName));
					mainHlayout.addMember(clusterGraphVlayout);
				}
			}
		});
		
		statsMenu.setItems(statsMenuItem,separator,samplesNoPassMenuItem,separator,genoQCMenuItem,separator,clusterGraphMenuItem);
		
		final IMenuButton statsButton = new IMenuButton(genoName);
		statsButton.setMenu(statsMenu);
		statsButton.setIconSpacing(20);
		statsButton.setIconAlign("left");
		statsButton.setShowDisabledIcon(false);
		statsButton.setOverflow(Overflow.VISIBLE);
		statsButton.setAutoWidth();
		
		mainHlayout.addMember(statsButton);
		
		vlayout.addMember(mainHlayout);

		/*
		 * Add layout to mainArea tab
		 */
		MainArea.addTabToTopTabset("Geno QC : "+genoName,tabID, vlayout, true);
	}
	
	private VStack constructClusterGraphLayout(final String genoName){
		final VStack stack = new VStack(10);
		stack.setLayoutAlign(Alignment.LEFT);
		stack.setOverflow(Overflow.VISIBLE);
		stack.setShowEdges(true);
		stack.setMembersMargin(5);  
		stack.setLayoutMargin(10);
		stack.setAutoWidth();
		stack.setAutoHeight();
		
		Label stackTitle = new Label();
		stackTitle.setOverflow(Overflow.VISIBLE);
		stackTitle.setAutoHeight();
		stackTitle.setAutoWidth();
		stackTitle.setContents(StringUtil.asHTML("Cluster graphs",true));
		stackTitle.setStyleName("textTitle");
		
		DynamicForm form = new DynamicForm();
		final SelectItem chooseGraph = new SelectItem();
		chooseGraph.setTitle("SNP&nbsp;Lists");
		
		snpListsService.getListNames(new AsyncCallback<List<String>>() {

			@Override
			public void onFailure(Throwable caught) {
				SC.warn("Cannot fetch SNP lists from server.");
			}

			@Override
			public void onSuccess(List<String> result) {
				chooseGraph.setValueMap(result.toArray(new String[result.size()]));
			}
		});
		
		final SelectItem annotItem = new SelectItem("annotItem", "Annotation&nbsp;File");
		annotItem.setWidth(220);
		
		annotationService.getAnnotationFilesNames(new AsyncCallback<List<String>>() {

			@Override
			public void onFailure(Throwable caught) {
				SC.warn("Cannot fetch availables annotation files from server");
			}

			@Override
			public void onSuccess(List<String> result) {
				annotItem.setValueMap(result.toArray(new String[result.size()]));
			}
		});
		
		final CheckboxItem colorPlatesCheckBox = new CheckboxItem();
		colorPlatesCheckBox.setTitle("Color&nbsp;by&nbsp;plates");
		
		final CheckboxItem showPriorsCheckBox = new CheckboxItem();
		showPriorsCheckBox.setTitle("Show&nbsp;Prior&nbsp;Ellipses");
		
		final CheckboxItem showPosteriorsCheckBox = new CheckboxItem();
		showPosteriorsCheckBox.setTitle("Show&nbsp;Posterior&nbsp;Ellipses");
		
		form.setFields(chooseGraph,annotItem,colorPlatesCheckBox,showPriorsCheckBox,showPosteriorsCheckBox);
		
		HLayout buttonLayout = new HLayout(10);
		buttonLayout.setAutoHeight();
		buttonLayout.setAutoWidth();
		
		IButton addButton = new IButton("Manage lists");
		addButton.setIcon("icons/Create.png");
		addButton.setOverflow(Overflow.VISIBLE);
		addButton.setAutoWidth();
		
		buttonLayout.addMember(addButton);
		
		IButton getButton = new IButton("Get graph");
		getButton.setIcon("icons/Download.png");
		getButton.setOverflow(Overflow.VISIBLE);
		getButton.setAutoWidth();
		
		buttonLayout.addMember(getButton);
		
		addButton.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				new SNPListsWindow(chooseGraph);
			}
		});
		
		getButton.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				String listName = chooseGraph.getValueAsString();
				String annotFileName = annotItem.getValueAsString();
				boolean platesColorCheck = colorPlatesCheckBox.getValueAsBoolean();
				boolean showPriorsCheck = showPriorsCheckBox.getValueAsBoolean();
				boolean showPosteriorsCheck = showPosteriorsCheckBox.getValueAsBoolean();
				if (listName==null || listName.isEmpty()){
					SC.warn("Select a snp list above");
					return;
				}
				if (annotFileName==null || annotFileName.isEmpty()){
					SC.warn("Please select an annotation file to use");
					return;
				}
				final ModalProgressWindow win = new ModalProgressWindow();
				win.setTitle("Cluster graph");
				win.setLoadingBar();
				win.getProgressLabel().setContents(StringUtil.asHTML("Getting cluster graph...",true));
				win.show();
				snpListsService.getGraphForGenoAnalysis(genoName, listName, platesColorCheck, showPriorsCheck, showPosteriorsCheck, annotFileName, new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable caught) {
						win.destroy();
						SC.warn("Could not get cluster graph !");
					}

					@Override
					public void onSuccess(String result) {
						win.destroy();
						if (result!=null && !result.isEmpty()){
							com.google.gwt.user.client.Window.open(GWT.getModuleBaseURL() + "fileProvider?file="+result, "_self", "");
						}else{
							SC.warn("Could not get cluster graph !");
						}
					}
				});
			}
		});
		
		stack.addMember(stackTitle);
		stack.addMember(form);
		stack.addMember(buttonLayout);
		
		return stack;
	}
	
	private VStack constructGenoAnalysisGrid(final String genoName){
		
		VStack analysisStack = new VStack(10);
		analysisStack.setLayoutAlign(Alignment.LEFT);
		analysisStack.setOverflow(Overflow.VISIBLE);
		analysisStack.setShowEdges(true);
		analysisStack.setMembersMargin(5);  
		analysisStack.setLayoutMargin(10);
		analysisStack.setAutoWidth();
		analysisStack.setAutoHeight();
		
		Label analysisStackTitle = new Label();
		analysisStackTitle.setOverflow(Overflow.VISIBLE);
		analysisStackTitle.setAutoHeight();
		analysisStackTitle.setAutoWidth();
		analysisStackTitle.setContents("Genotyping&nbsp;Analysis");
		analysisStackTitle.setStyleName("textTitle");
		
		//Listgrid for analysis details
		GenoAnalysisListGrid genoAnalysisListGrid = new GenoAnalysisListGrid();
		//genoAnalysisListGrid.setDataSource(GenotypingAnalysisDS.getInstance());
		Criteria criteria = new Criteria();
		criteria.addCriteria("geno_name", genoName);
		genoAnalysisListGrid.fetchData(criteria);
		
		//Label for sample number in genotyping analysis
		final Label sampleNumber = new Label();
		sampleNumber.setOverflow(Overflow.VISIBLE);
		sampleNumber.setAutoHeight();
		sampleNumber.setAutoWidth();
		
		genotypingService.nbSamplesInGenoAnalysis(genoName, new AsyncCallback<Integer>() {
		
			@Override
			public void onSuccess(Integer result) {
				sampleNumber.setContents("Number&nbsp;of&nbsp;samples&nbsp;in&nbsp;this&nbsp;genotyping&nbsp;analysis:&nbsp;"+result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				SC.warn("Can't retrieve number of samples genotyping analysis "+genoName);
			}
		});
		
		final Label libraryLabel = new Label();
		libraryLabel.setOverflow(Overflow.VISIBLE);
		libraryLabel.setAutoHeight();
		libraryLabel.setAutoWidth();
		
		genotypingService.getLibraryNameForGeno(genoName, new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				SC.warn("Can't retrieve library files used in genotyping analysis "+genoName);
			}

			@Override
			public void onSuccess(String result) {
				libraryLabel.setContents("Library&nbsp;used:&nbsp;"+result);
			}
		});
		
		analysisStack.addMember(analysisStackTitle);
		analysisStack.addMember(genoAnalysisListGrid);
		analysisStack.addMember(sampleNumber);
		analysisStack.addMember(libraryLabel);
		
		return analysisStack;
	}	
		
	private VStack constructPassQcListgrid(String genoName){	
		VStack passQCStack = new VStack(10);
		passQCStack.setLayoutAlign(Alignment.LEFT);
		passQCStack.setOverflow(Overflow.VISIBLE);
		passQCStack.setShowEdges(true);
		passQCStack.setMembersMargin(5);  
		passQCStack.setLayoutMargin(10);
		passQCStack.setAutoWidth();
		passQCStack.setAutoHeight();
		
		Label passQCStackTitle = new Label();
		passQCStackTitle.setOverflow(Overflow.VISIBLE);
		passQCStackTitle.setAutoHeight();
		passQCStackTitle.setAutoWidth();
		passQCStackTitle.setContents("Samples&nbsp;below&nbsp;DishQC&nbsp;value");
		passQCStackTitle.setStyleName("textTitle");
		
		final QCListGrid qcListgrid = new QCListGrid();
		Criteria crit = new Criteria();
		crit.addCriteria("dishQCLimit", genoName);
		qcListgrid.fetchData(crit);
		qcListgrid.setMinimumTable();
		qcListgrid.setAutoFitMaxRecords(6);
		qcListgrid.getContextMenu().getItem(0).setEnabled(false);
		qcListgrid.setSelectionType(SelectionStyle.NONE);
		
		DynamicForm form = new DynamicForm();
		form.setAutoHeight(); 
		form.setAutoWidth();
		form.setPadding(5);
		form.setLayoutAlign(Alignment.LEFT);
		
		final SelectItem tableContents = new SelectItem();
		tableContents.setValueMap("Minimum table","Condensed table", "Full table");
		tableContents.setName("Show");
		tableContents.setDefaultValue("Minimum table");
		
		form.setFields(tableContents);
		tableContents.addChangedHandler(new ChangedHandler() {
			
			@Override
			public void onChanged(ChangedEvent event) {
				if (tableContents.getValueAsString().equals("Condensed table")){
					qcListgrid.setCondensedTable();
				}else if (tableContents.getValueAsString().equals("Full table")){
					qcListgrid.setFullTable();
				}else if (tableContents.getValueAsString().equals("Minimum table")){
					qcListgrid.setMinimumTable();
				}
			}
		});
		
		final Label sampleNumber = new Label();
		sampleNumber.setOverflow(Overflow.VISIBLE);
		sampleNumber.setAutoHeight();
		sampleNumber.setAutoWidth();
		
		qcListgrid.addDataArrivedHandler(new DataArrivedHandler() {
			
			@Override
			public void onDataArrived(DataArrivedEvent event) {
				sampleNumber.setContents("Number&nbsp;of&nbsp;samples&nbsp;below&nbsp;dish&nbsp;qc:&nbsp;"+event.getEndRow());
			}
		});
		
		passQCStack.addMember(passQCStackTitle);
		passQCStack.addMember(form);
		passQCStack.addMember(qcListgrid);
		passQCStack.addMember(sampleNumber);
		
		return passQCStack;
		
	}
	
	private VStack constructPassCallrateListgrid(String genoName){
		
		VStack passCRStack = new VStack(10);
		passCRStack.setLayoutAlign(Alignment.LEFT);
		passCRStack.setOverflow(Overflow.VISIBLE);
		passCRStack.setShowEdges(true);
		passCRStack.setMembersMargin(5);  
		passCRStack.setLayoutMargin(10);
		passCRStack.setAutoWidth();
		passCRStack.setAutoHeight();
		
		Label passCRStackTitle = new Label();
		passCRStackTitle.setOverflow(Overflow.VISIBLE);
		passCRStackTitle.setAutoHeight();
		passCRStackTitle.setAutoWidth();
		passCRStackTitle.setContents("Samples&nbsp;below&nbsp;callrate&nbsp;value");
		passCRStackTitle.setStyleName("textTitle");
		
		final GenoQCListGrid genoQcListgrid = new GenoQCListGrid();
		Criteria cri = new Criteria();
		cri.addCriteria("callRateLimit", genoName);
		genoQcListgrid.fetchData(cri);
		genoQcListgrid.setMinimumTable();
		genoQcListgrid.setAutoFitMaxRecords(6);
		genoQcListgrid.getContextMenu().getItem(0).setEnabled(false);
		genoQcListgrid.setSelectionType(SelectionStyle.NONE);
		
		DynamicForm f = new DynamicForm();
		f.setAutoHeight(); 
		f.setAutoWidth();
		f.setPadding(5);
		f.setLayoutAlign(Alignment.LEFT);
		
		final SelectItem tableContents = new SelectItem();
		tableContents.setValueMap("Minimum table","Condensed table", "Full table");
		tableContents.setName("Show");
		tableContents.setDefaultValue("Minimum table");
		
		f.setFields(tableContents);
		tableContents.addChangedHandler(new ChangedHandler() {
			
			@Override
			public void onChanged(ChangedEvent event) {
				if (tableContents.getValueAsString().equals("Condensed table")){
					genoQcListgrid.setCondensedTable();
				}else if (tableContents.getValueAsString().equals("Full table")){
					genoQcListgrid.setFullTable();
				}else if (tableContents.getValueAsString().equals("Minimum table")){
					genoQcListgrid.setMinimumTable();
				}
			}
		});
		
		final Label sampleNumber = new Label();
		sampleNumber.setOverflow(Overflow.VISIBLE);
		sampleNumber.setAutoHeight();
		sampleNumber.setAutoWidth();
		
		genoQcListgrid.addDataArrivedHandler(new DataArrivedHandler() {
			
			@Override
			public void onDataArrived(DataArrivedEvent event) {
				sampleNumber.setContents("Number&nbsp;of&nbsp;samples&nbsp;below&nbsp;call&nbsp;rate&nbsp;after&nbsp;first&nbsp;run:&nbsp;"+event.getEndRow());
			}
		});
		
		passCRStack.addMember(passCRStackTitle);
		passCRStack.addMember(f);
		passCRStack.addMember(genoQcListgrid);
		passCRStack.addMember(sampleNumber);
		
		return passCRStack;
	}
	
	private VStack constructGenoQCListgrid(final String genoName, final String run){
		final VStack vStack = new VStack(10);
		vStack.setLayoutAlign(Alignment.LEFT);
		vStack.setAlign(Alignment.LEFT);
		vStack.setOverflow(Overflow.VISIBLE);
		vStack.setShowEdges(true);
		vStack.setMembersMargin(5);  
		vStack.setLayoutMargin(10);
		vStack.setAutoWidth();
		vStack.setAutoHeight();
		
		Label gridTitle = new Label();
		gridTitle.setOverflow(Overflow.VISIBLE);
		gridTitle.setAutoHeight();
		gridTitle.setAutoWidth();
		gridTitle.setContents("Samples&nbsp;genotyping&nbsp;QC&nbsp;at&nbsp;"+run+"&nbsp;run");
		gridTitle.setStyleName("textTitle");
		
		DynamicForm form = new DynamicForm();
		form.setAutoHeight(); 
		form.setAutoWidth();
		form.setPadding(5);
		form.setLayoutAlign(Alignment.LEFT);
		
		final SelectItem tableContents = new SelectItem();
		tableContents.setValueMap("Minimum table", "Condensed table", "Full table");
		tableContents.setName("Show");
		tableContents.setDefaultValue("Minimum table");
		
		form.setFields(tableContents);
		
		//Label for sample number in genotyping run
		final Label sampleNumber = new Label();
		sampleNumber.setOverflow(Overflow.VISIBLE);
		sampleNumber.setAutoHeight();
		sampleNumber.setAutoWidth();
		
		final GenoQCListGrid genoQCListgrid = new GenoQCListGrid();
		if (run.equals("second")){
			genoQCListgrid.setEmptyMessage("No second run performed in this analysis");
		}
		genoQCListgrid.setAutoFitMaxRecords(6);
		Criteria criteria = new Criteria();
		criteria.addCriteria("geno_name", genoName);
		criteria.addCriteria("geno_run", run);
		genoQCListgrid.fetchData(criteria);
		
		tableContents.addChangedHandler(new ChangedHandler() {
			
			@Override
			public void onChanged(ChangedEvent event) {
				if (tableContents.getValueAsString().equals("Condensed table")){
					genoQCListgrid.setCondensedTable();
				}else if (tableContents.getValueAsString().equals("Full table")){
					genoQCListgrid.setFullTable();
				}else if (tableContents.getValueAsString().equals("Minimum table")){
					genoQCListgrid.setMinimumTable();
				}
			}
		});
		
		genoQCListgrid.addDataArrivedHandler(new DataArrivedHandler() {
			
			@Override
			public void onDataArrived(DataArrivedEvent event) {
				sampleNumber.setContents("Number&nbsp;of&nbsp;samples&nbsp;analysed&nbsp;in&nbsp;this&nbsp;run:&nbsp;"+event.getEndRow());
			}
		});
		
		vStack.addMember(gridTitle);
		vStack.addMember(form);
		vStack.addMember(genoQCListgrid);
		vStack.addMember(sampleNumber);
		
		return vStack;
	}
}
