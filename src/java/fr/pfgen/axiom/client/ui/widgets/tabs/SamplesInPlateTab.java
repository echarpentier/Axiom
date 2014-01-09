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
import com.smartgwt.client.types.FetchMode;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.ButtonItem;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.VStack;

public class SamplesInPlateTab {

	private final PopulationsServiceAsync populationService = GWT.create(PopulationsService.class);

	public SamplesInPlateTab(String plateName,String tabID){

		final VLayout vlayout = new VLayout(15);
		vlayout.setWidth("80%");
		vlayout.setDefaultLayoutAlign(Alignment.CENTER);
		
		final VStack vStack = new VStack();  
		vStack.setShowEdges(true);
		vStack.setMembersMargin(5);  
		vStack.setLayoutMargin(10);
		vStack.setAutoWidth();
		vStack.setAutoHeight();
		
		VLayout gridLayout = new VLayout(15);

		final Label gridTitle = new Label();
		gridTitle.setWidth(400);
		gridTitle.setHeight(20);
		gridTitle.setContents("Samples&nbsp;in&nbsp;plate&nbsp;"+plateName.replaceAll("\\s", "&nbsp;"));
		gridTitle.setStyleName("textTitle");

		final SamplesListGrid samplesGrid = new SamplesListGrid();
		Criteria criteria = new Criteria();
		criteria.addCriteria("plate_name", plateName);
		samplesGrid.setCriteria(criteria);
		samplesGrid.setDataFetchMode(FetchMode.BASIC);

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
					
					if (selectedRecordsAll.length==0){
						SC.say("Select records to assign to project");
					}else{
						for (ListGridRecord record: selectedRecordsAll) {
							String pop = record.getAttributeAsString("population_names");
							pop = pop.replaceAll(projectCB.getValueAsString(), "");
							record.setAttribute("population_names", pop);
							samplesGrid.updateData(record);
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
					if (selectedRecordsAll.length==0){
						SC.say("Select records to assign to project");
					}else{
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
				}else{
					SC.say("Please select population !");
				}
			}
		});  

		projectCB.setAlign(Alignment.CENTER);
		removeFromPopButton.setAlign(Alignment.CENTER);
		removeFromPopButton.setColSpan(2);
		removeFromPopButton.setIcon("icons/Fall.png");

		addToPopButton.setAlign(Alignment.CENTER);
		addToPopButton.setColSpan(2);
		addToPopButton.setIcon("icons/Raise.png");
		
		form.setFields(projectCB,addToPopButton,removeFromPopButton);
		
		gridLayout.addMember(gridTitle);
		gridLayout.addMember(samplesGrid);
		gridLayout.addMember(form);
		
		vStack.addMember(gridLayout);
		
		vlayout.addMember(vStack);

		/*
		 * Add layout to mainArea tab
		 */
		
		MainArea.addTabToTopTabset("Plate: "+plateName,tabID, vlayout, true);
	}
}
