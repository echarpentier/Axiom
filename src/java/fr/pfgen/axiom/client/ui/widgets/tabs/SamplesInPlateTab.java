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
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.VStack;
import fr.pfgen.axiom.client.services.FamiliesService;
import fr.pfgen.axiom.client.services.FamiliesServiceAsync;

public class SamplesInPlateTab {

	private final PopulationsServiceAsync populationService = GWT.create(PopulationsService.class);
        private final FamiliesServiceAsync familyService = GWT.create(FamiliesService.class);

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
		final DynamicForm popForm = new DynamicForm(); 
                popForm.setGroupTitle("Populations");
                popForm.setIsGroup(true);
		popForm.setOverflow(Overflow.VISIBLE);
		popForm.setAutoWidth();
		popForm.setWrapItemTitles(false);
		popForm.setLayoutAlign(Alignment.CENTER);
		popForm.setCellPadding(10);
		
		final SelectItem populationCB = new SelectItem();
		populationCB.setTitle("Select population");  
		
		populationService.getPopulationNames(new AsyncCallback<List<String>>() {

			@Override
			public void onSuccess(List<String> result) {
				if (result != null && !result.isEmpty()){
					populationCB.setValueMap(result.toArray(new String[result.size()]));
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
				if (populationCB.getValueAsString()!=null){
					ListGridRecord[] selectedRecordsAll = samplesGrid.getSelectedRecords();
					
					if (selectedRecordsAll.length==0){
						SC.say("Select records to remove from population "+populationCB.getValueAsString());
					}else{
						for (ListGridRecord record: selectedRecordsAll) {
							String pop = record.getAttributeAsString("population_names");
							pop = pop.replaceAll(populationCB.getValueAsString(), "");
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
				if (populationCB.getValueAsString()!=null){
					ListGridRecord[] selectedRecordsAll = samplesGrid.getSelectedRecords();
					if (selectedRecordsAll.length==0){
						SC.say("Select records to assign to population "+populationCB.getValueAsString());
					}else{
						for (ListGridRecord record: selectedRecordsAll) {
							String addedName = new String();
							if (record.getAttributeAsString("population_names")!=null && !record.getAttributeAsString("population_names").isEmpty()){
								addedName = populationCB.getValueAsString()+","+record.getAttribute("population_names");
							}else{
								addedName = populationCB.getValueAsString();
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

		populationCB.setAlign(Alignment.CENTER);
		removeFromPopButton.setAlign(Alignment.CENTER);
		removeFromPopButton.setColSpan(2);
		removeFromPopButton.setIcon("icons/Fall.png");

		addToPopButton.setAlign(Alignment.CENTER);
		addToPopButton.setColSpan(2);
		addToPopButton.setIcon("icons/Raise.png");
		
		popForm.setFields(populationCB,addToPopButton,removeFromPopButton);
                
                //buttons for removing from or adding samples to families
		final DynamicForm famForm = new DynamicForm(); 
                famForm.setGroupTitle("Families");
                famForm.setIsGroup(true);
		famForm.setOverflow(Overflow.VISIBLE);
		famForm.setAutoWidth();
		famForm.setWrapItemTitles(false);
		famForm.setLayoutAlign(Alignment.CENTER);
		famForm.setCellPadding(10);
		
		final SelectItem familyCB = new SelectItem();
		familyCB.setTitle("Select family"); 
                
                familyService.getFamiliesNames(new AsyncCallback<List<String>>() {

			@Override
			public void onSuccess(List<String> result) {
				if (result != null && !result.isEmpty()){
					familyCB.setValueMap(result.toArray(new String[result.size()]));
				}
			}	
			
			@Override
			public void onFailure(Throwable caught) {
				SC.warn("Can't retreive existing families from server !");
			}
		});
		
		final ButtonItem removeFromFamButton = new ButtonItem();
		removeFromFamButton.setTitle("Revome selected samples from family");
		removeFromFamButton.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler() {
			
			@Override
			public void onClick(com.smartgwt.client.widgets.form.fields.events.ClickEvent event) {
				if (familyCB.getValueAsString()!=null){
					ListGridRecord[] selectedRecordsAll = samplesGrid.getSelectedRecords();
					
					if (selectedRecordsAll.length==0){
						SC.say("Select records to remove from family "+familyCB.getValueAsString());
					}else{
						for (ListGridRecord record: selectedRecordsAll) {
							String fam = record.getAttributeAsString("family_names");
							fam = fam.replaceAll(familyCB.getValueAsString(), "");
							record.setAttribute("family_names", fam);
							samplesGrid.updateData(record);
						}
					}
				}else{
					SC.say("Please select family !");
				}
			}
		});
		
		final ButtonItem addToFamButton = new ButtonItem();
		addToFamButton.setTitle("Add selected samples to family");
		addToFamButton.addClickHandler(new com.smartgwt.client.widgets.form.fields.events.ClickHandler() {
			
			@Override
			public void onClick(com.smartgwt.client.widgets.form.fields.events.ClickEvent event) {
				if (familyCB.getValueAsString()!=null){
					ListGridRecord[] selectedRecordsAll = samplesGrid.getSelectedRecords();
					if (selectedRecordsAll.length==0){
						SC.say("Select records to assign to family "+familyCB.getValueAsString());
					}else{
						for (ListGridRecord record: selectedRecordsAll) {
							String addedName = new String();
							if (record.getAttributeAsString("family_names")!=null && !record.getAttributeAsString("family_names").isEmpty()){
								addedName = familyCB.getValueAsString()+","+record.getAttribute("family_names");
							}else{
								addedName = familyCB.getValueAsString();
							}
							record.setAttribute("family_names", addedName);
							samplesGrid.updateData(record);
						}
					}
				}else{
					SC.say("Please select family !");
				}
			}
		});  

		familyCB.setAlign(Alignment.CENTER);
		removeFromFamButton.setAlign(Alignment.CENTER);
		removeFromFamButton.setColSpan(2);
		removeFromFamButton.setIcon("icons/Fall.png");

		addToFamButton.setAlign(Alignment.CENTER);
		addToFamButton.setColSpan(2);
		addToFamButton.setIcon("icons/Raise.png");
		
		famForm.setFields(familyCB,addToFamButton,removeFromFamButton);
                
                HLayout formLayout = new HLayout(20);
                formLayout.addMember(popForm);
                formLayout.addMember(famForm);
		
		gridLayout.addMember(gridTitle);
		gridLayout.addMember(samplesGrid);
		gridLayout.addMember(formLayout);
		
		vStack.addMember(gridLayout);
		
		vlayout.addMember(vStack);

		/*
		 * Add layout to mainArea tab
		 */
		
		MainArea.addTabToTopTabset("Plate: "+plateName,tabID, vlayout, true);
	}
}
