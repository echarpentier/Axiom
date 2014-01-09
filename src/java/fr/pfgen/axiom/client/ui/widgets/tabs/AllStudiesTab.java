package fr.pfgen.axiom.client.ui.widgets.tabs;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.VStack;

import fr.pfgen.axiom.client.ui.widgets.MainArea;
import fr.pfgen.axiom.client.ui.widgets.grids.listGrids.StudiesListGrid;

public class AllStudiesTab {
	
	//private SNPListsServiceAsync snpListsService = GWT.create(SNPListsService.class);
	final StudiesListGrid studiesListGrid = new StudiesListGrid();

	public AllStudiesTab(String tabID){
		VLayout vlayout = new VLayout(15);
		vlayout.setWidth("80%");
		vlayout.setDefaultLayoutAlign(Alignment.CENTER);
		
		HLayout hlayout = new HLayout(10);
		hlayout.setAutoHeight();
		hlayout.setAutoWidth();
		
		hlayout.addMember(constructStudiesGrid());
		//hlayout.addMember(constructClusterGraphLayout());
		
		vlayout.addMember(hlayout);
		
		MainArea.addTabToTopTabset("All studies",tabID, vlayout, true);
	}
	
	/*private VStack constructClusterGraphLayout(){
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
		
		final CheckboxItem colorPlatesCheckBox = new CheckboxItem();
		colorPlatesCheckBox.setTitle("Color&nbsp;by&nbsp;plates");
		
		final CheckboxItem showEllipseCheckBox = new CheckboxItem();
		showEllipseCheckBox.setTitle("Show&nbsp;Prior&nbsp;Ellipses");
		
		form.setFields(chooseGraph,colorPlatesCheckBox,showEllipseCheckBox);
		
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
				if (studiesListGrid.getSelectedRecord()==null){
					SC.warn("Select a study in the grid");
					return;
				}
				
				String studyName = studiesListGrid.getSelectedRecord().getAttributeAsString("study_name");
				String listName = chooseGraph.getValueAsString();
				boolean platesColorCheck = colorPlatesCheckBox.getValueAsBoolean();
				boolean showEllipseCheck = showEllipseCheckBox.getValueAsBoolean();
				if (listName==null || listName.isEmpty()){
					SC.warn("Select a snp list above");
					return;
				}
				final ModalProgressWindow win = new ModalProgressWindow();
				win.setTitle("Cluster graph");
				win.setLoadingBar();
				win.getProgressLabel().setContents(StringUtil.asHTML("Getting cluster graph...",true));
				win.show();
				snpListsService.getGraphForStudy(studyName, listName, platesColorCheck, showEllipseCheck, new AsyncCallback<String>() {

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
	}*/
	
	private VStack constructStudiesGrid(){
	
		VStack studyStack = new VStack(10);
		studyStack.setLayoutAlign(Alignment.CENTER);
		studyStack.setOverflow(Overflow.VISIBLE);
		studyStack.setShowEdges(true);
		studyStack.setMembersMargin(5);  
		studyStack.setLayoutMargin(10);
		studyStack.setAutoWidth();
		studyStack.setAutoHeight();
		
		Label studyStackTitle = new Label();
		studyStackTitle.setOverflow(Overflow.VISIBLE);
		studyStackTitle.setAutoHeight();
		studyStackTitle.setAutoWidth();
		studyStackTitle.setContents("Studies");
		studyStackTitle.setStyleName("textTitle");
		
		IButton removeButton = new IButton("Remove Study");
		removeButton.setIcon("icons/Remove.png");
		removeButton.setOverflow(Overflow.VISIBLE);
		removeButton.setAutoWidth();
		removeButton.setLayoutAlign(Alignment.CENTER);
		
		removeButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				final ListGridRecord selectedRecord = studiesListGrid.getSelectedRecord();
				if (selectedRecord==null){
					SC.warn("Please select a study");
					return;
				}
				SC.ask("All data for this study will be permanently removed.<br>Do you want to continue?", new BooleanCallback() {
					
					@Override
					public void execute(Boolean value) {
						if (value != null && value){
							studiesListGrid.removeData(selectedRecord);
						}
					}
				});
			}
		});
		
		studyStack.addMember(studyStackTitle);
		studyStack.addMember(studiesListGrid);
		studyStack.addMember(removeButton);
		
		return studyStack;
	}
}
