package fr.pfgen.axiom.client.ui.widgets.tabs;

import java.util.List;

import fr.pfgen.axiom.client.services.PopulationsService;
import fr.pfgen.axiom.client.services.PopulationsServiceAsync;
import fr.pfgen.axiom.client.ui.widgets.MainArea;
import fr.pfgen.axiom.client.ui.widgets.grids.listGrids.SamplesListGrid;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.ButtonItem;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.VStack;

public class SamplesTab {

	private final PopulationsServiceAsync populationService = GWT.create(PopulationsService.class);

	public SamplesTab(String tabID){

		VLayout vlayout = new VLayout(15);
		vlayout.setWidth("80%");
		vlayout.setAutoHeight();
		vlayout.setDefaultLayoutAlign(Alignment.CENTER);
		
		HLayout hlayout = new HLayout(15);
		hlayout.setAutoWidth();
		hlayout.setAutoHeight();
		hlayout.setDefaultLayoutAlign(Alignment.CENTER);
		hlayout.setDefaultLayoutAlign(VerticalAlignment.TOP);
		
		//grid for total samples
		final VStack vStack = new VStack();  
		vStack.setShowEdges(true);
		vStack.setMembersMargin(5);  
		vStack.setLayoutMargin(10);
		vStack.setAutoWidth();
		vStack.setAutoHeight();

		VLayout gridLayout = new VLayout(10);
		
		final Label gridTitle = new Label();
		gridTitle.setHeight(20);
		gridTitle.setContents("All&nbsp;samples");
		gridTitle.setStyleName("textTitle");
		
		final SamplesListGrid samplesGrid = new SamplesListGrid();
		
		gridLayout.addMember(gridTitle);
		gridLayout.addMember(samplesGrid);

		vStack.addMember(gridLayout);
		
		//grid for samples without project assigned
		final VStack vStack2 = new VStack();  
		vStack2.setShowEdges(true);
		vStack2.setMembersMargin(5);  
		vStack2.setLayoutMargin(10);
		vStack2.setAutoWidth();
		vStack2.setAutoHeight();

		VLayout gridLayout2 = new VLayout(10);
		
		final Label gridTitle2 = new Label();
		gridTitle2.setHeight(20);
		gridTitle2.setContents("Samples&nbsp;without&nbsp;population&nbsp;assigned");
		gridTitle2.setStyleName("textTitle");
		
		final SamplesListGrid samplesWoPopulationGrid = new SamplesListGrid();
		Criteria criteria = new Criteria();
		criteria.addCriteria("noPopulation", "null");
		samplesWoPopulationGrid.setCriteria(criteria);
		samplesWoPopulationGrid.setEmptyMessage("No samples found without project assigned");
		
		gridLayout2.addMember(gridTitle2);
		gridLayout2.addMember(samplesWoPopulationGrid);

		vStack2.addMember(gridLayout2);
		
		//add the vstacks to hlayout
		hlayout.addMember(vStack);
		hlayout.addMember(vStack2);
		
		//buttons for removing from or adding samples to populations
		final DynamicForm form = new DynamicForm();  
		form.setOverflow(Overflow.VISIBLE);
		form.setAutoWidth();
		form.setWrapItemTitles(false);
		form.setLayoutAlign(Alignment.CENTER);
		form.setCellPadding(10);
		
		final SelectItem projectCB = new SelectItem();
		projectCB.setTitle("Select population");  
		
		populationService.getPopulationNames(new AsyncCallback<List<String>>() {

			@Override
			public void onSuccess(List<String> result) {
				if (result != null && !result.isEmpty()){
					projectCB.setValueMap(result.toArray(new String[result.size()]));
				}
			}	
			
			@Override
			public void onFailure(Throwable caught) {
				SC.warn("Can't retreive existing populations from server !");
			}
		});
		
		final ButtonItem removeFromPopButton = new ButtonItem();
		removeFromPopButton.setTitle("Revome selected samples from population");
		removeFromPopButton.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler() {
			
			@Override
			public void onClick(com.smartgwt.client.widgets.form.fields.events.ClickEvent event) {
				if (projectCB.getValueAsString()!=null){
					ListGridRecord[] selectedRecordsAll = samplesGrid.getSelectedRecords();
					ListGridRecord[] selectedRecordsNoPop = samplesWoPopulationGrid.getSelectedRecords();
					//List<ListGridRecord> both = new ArrayList<ListGridRecord>(selectedRecordsAll.length + selectedRecordsNoPop.length);
				    //Collections.addAll(both, selectedRecordsAll);
				    //Collections.addAll(both, selectedRecordsNoPop);
				    //ListGridRecord[] selectedRecords = both.toArray(new ListGridRecord[] {});
					if (selectedRecordsAll.length==0 && selectedRecordsNoPop.length==0){
						SC.say("Select records to assign to project");
					}else{
						if (selectedRecordsAll.length>0){
							for (ListGridRecord record: selectedRecordsAll) {
								String pop = record.getAttributeAsString("population_names");
								pop = pop.replaceAll(projectCB.getValueAsString(), "");
								record.setAttribute("population_names", pop);
								samplesGrid.updateData(record);
							}
						}
						if (selectedRecordsNoPop.length>0){
							for (ListGridRecord record: selectedRecordsNoPop) {
								String pop = record.getAttributeAsString("population_names");
								pop = pop.replaceAll(projectCB.getValueAsString(), "");
							
								record.setAttribute("population_names", pop);
								samplesWoPopulationGrid.updateData(record);
							}
						}
					}
				}else{
					SC.say("Please select population !");
				}
			}
		});
		
		final ButtonItem addToPopButton = new ButtonItem();
		addToPopButton.setTitle("Add selected samples to population");
		addToPopButton.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler() {
			
			@Override
			public void onClick(com.smartgwt.client.widgets.form.fields.events.ClickEvent event) {
				if (projectCB.getValueAsString()!=null){
					ListGridRecord[] selectedRecordsAll = samplesGrid.getSelectedRecords();
					ListGridRecord[] selectedRecordsNoPop = samplesWoPopulationGrid.getSelectedRecords();
					if (selectedRecordsAll.length==0 && selectedRecordsNoPop.length==0){
						SC.say("Select records to assign to project");
					}else{
						if (selectedRecordsAll.length!=0){
							for (ListGridRecord record: selectedRecordsAll) {
								String addedName = new String();
								if (record.getAttributeAsString("population_names")!=null && !record.getAttributeAsString("population_names").isEmpty()){
									addedName = projectCB.getValueAsString()+","+record.getAttribute("population_names");
								}else{
									addedName = projectCB.getValueAsString();
								}
								record.setAttribute("population_names", addedName);
								samplesGrid.updateData(record);
							}
						}
						if (selectedRecordsNoPop.length!=0){
							for (ListGridRecord record: selectedRecordsNoPop) {
								String addedName = new String();
								if (record.getAttributeAsString("population_names")!=null && !record.getAttributeAsString("population_names").isEmpty()){
									addedName = projectCB.getValueAsString()+","+record.getAttribute("population_names");
								}else{
									addedName = projectCB.getValueAsString();
								}
								record.setAttribute("population_names", addedName);
								samplesWoPopulationGrid.updateData(record);
							}
						}
					}
				}else{
					SC.say("Please select population !");
				}
			}
		});
		
		final ButtonItem removeFromAllPopButton = new ButtonItem();
		removeFromAllPopButton.setTitle("Revome selected samples from all populations");
		removeFromAllPopButton.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler() {
			
			@Override
			public void onClick(com.smartgwt.client.widgets.form.fields.events.ClickEvent event) {
				ListGridRecord[] selectedRecordsAll = samplesGrid.getSelectedRecords();
				ListGridRecord[] selectedRecordsNoPop = samplesWoPopulationGrid.getSelectedRecords();
				if (selectedRecordsAll.length==0 && selectedRecordsNoPop.length==0){
					SC.say("Select records to assign to project");
				}else{
					if (selectedRecordsAll.length>0){
						for (ListGridRecord record: selectedRecordsAll) {
							record.setAttribute("population_names", "");
							samplesGrid.updateData(record);
						}
					}
					if (selectedRecordsNoPop.length>0){
						for (ListGridRecord record: selectedRecordsNoPop) {
							record.setAttribute("population_names", "");
							samplesWoPopulationGrid.updateData(record);
						}
					}
				}
			}
		});
		
		final ButtonItem refreshButton = new ButtonItem();
		refreshButton.setTitle("Refresh grids");
		refreshButton.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler() {
			
			@Override
			public void onClick(com.smartgwt.client.widgets.form.fields.events.ClickEvent event) {
				samplesGrid.invalidateCache();
				samplesWoPopulationGrid.invalidateCache();
				samplesGrid.redraw();
				samplesWoPopulationGrid.redraw();
			}
		});
		
		projectCB.setAlign(Alignment.CENTER);
		removeFromPopButton.setAlign(Alignment.CENTER);
		removeFromPopButton.setColSpan(2);
		removeFromPopButton.setIcon("icons/Fall.png");

		addToPopButton.setAlign(Alignment.CENTER);
		addToPopButton.setColSpan(2);
		addToPopButton.setIcon("icons/Raise.png");

		removeFromAllPopButton.setAlign(Alignment.CENTER);
		removeFromAllPopButton.setColSpan(2);
		removeFromAllPopButton.setIcon("icons/Fall_fall.ico");
		
		refreshButton.setAlign(Alignment.CENTER);
		refreshButton.setColSpan(2);
		refreshButton.setIcon("icons/Refresh.png");
		
		form.setFields(projectCB,addToPopButton,removeFromPopButton,removeFromAllPopButton,refreshButton);
		
		//add the hlayout to vlayout
		vlayout.addMember(hlayout);
		vlayout.addMember(form);
		//vlayout.addMember(vStack);

		/*
		 * Add layout to mainArea tab
		 */
		MainArea.addTabToTopTabset("Samples",tabID, vlayout, true);
	}
}
