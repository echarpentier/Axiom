package fr.pfgen.axiom.client.ui.widgets.tabs;

import java.util.Date;
import fr.pfgen.axiom.client.Axiom;
import fr.pfgen.axiom.client.services.SamplesService;
import fr.pfgen.axiom.client.services.SamplesServiceAsync;
import fr.pfgen.axiom.client.ui.widgets.MainArea;
import fr.pfgen.axiom.client.ui.widgets.grids.listGrids.PopulationsListGrid;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyPressEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyPressHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.VStack;

public class PopulationsTab {

	private final SamplesServiceAsync samplesService = GWT.create(SamplesService.class);
	
	public PopulationsTab(String tabID){
		
		VLayout vlayout = new VLayout(15);
		vlayout.setWidth("80%");
		vlayout.setDefaultLayoutAlign(Alignment.CENTER);
		
		final VStack vStack = new VStack();  
		vStack.setShowEdges(true);
		vStack.setMembersMargin(5);  
		vStack.setLayoutMargin(10);
		vStack.setAutoWidth();
		vStack.setAutoHeight();
		
		VLayout gridLayout = new VLayout(10);

		final Label gridTitle = new Label();
		gridTitle.setHeight(20);
		gridTitle.setContents("Populations");
		gridTitle.setStyleName("textTitle");

		final PopulationsListGrid populationsGrid = new PopulationsListGrid();
		populationsGrid.setSelectionType(SelectionStyle.SINGLE);

		HLayout buttonLayout = new HLayout(20);
		
		IButton addButton = new IButton("Add population", new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				final Dialog newPopulationWindow = new Dialog();
				
				newPopulationWindow.setAutoSize(true); 
				newPopulationWindow.setTitle("New population");  
				newPopulationWindow.setShowMinimizeButton(false);  
				newPopulationWindow.setIsModal(true);  
				newPopulationWindow.setShowModalMask(true);  
				newPopulationWindow.addCloseClickHandler(new CloseClickHandler() {  
					@Override
					public void onCloseClick(CloseClientEvent event) {  
						newPopulationWindow.destroy();
					}  
				});  
				DynamicForm form = new DynamicForm();  
				form.setHeight100();  
				form.setWidth100();  
				form.setPadding(5);  
				form.setLayoutAlign(VerticalAlignment.BOTTOM);  
				final TextItem nameItem = new TextItem();  
				nameItem.setTitle("Population name"); 
				nameItem.setRequired(true); 
				nameItem.setKeyPressFilter("^[a-zA-Z0-9]+$");
				nameItem.setValidateOnChange(true);
				KeyPressHandler kphandler = new KeyPressHandler() {
					
					@Override
					public void onKeyPress(KeyPressEvent event) {
						if (event.getKeyName().equals("Enter")){
							if (nameItem.getValueAsString()!= null && nameItem.getValueAsString() != ""){
								ListGridRecord[] recordList = populationsGrid.getRecords();
								int ok = 1;
								for (ListGridRecord LGrecord : recordList) {
									if (LGrecord.getAttribute("population_name").equalsIgnoreCase(nameItem.getValueAsString())){
										SC.warn("Population "+nameItem.getValueAsString()+" already exists !!");
										nameItem.clearValue();
										ok = 0;
									}
								}
								if (ok == 1){
									ListGridRecord newPopulationRecord = new ListGridRecord();
									newPopulationRecord.setAttribute("population_name", nameItem.getValue());
									newPopulationRecord.setAttribute("user", Axiom.get().getUser().getFirstname()+" "+Axiom.get().getUser().getLastname());
									newPopulationRecord.setAttribute("population_id", 1);
									newPopulationRecord.setAttribute("created", new Date());
									populationsGrid.addData(newPopulationRecord);
									newPopulationWindow.destroy();
								}
							}else{
								SC.warn("Insert population name");
							}
						}
					}
				};
				nameItem.addKeyPressHandler(kphandler);
				final IButton windowAddButton = new IButton("Add");
				windowAddButton.addClickHandler(new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						if (nameItem.getValueAsString()!= null && nameItem.getValueAsString() != ""){
							ListGridRecord[] recordList = populationsGrid.getRecords();
							int ok = 1;
							for (ListGridRecord LGrecord : recordList) {
								if (LGrecord.getAttribute("population_name").equalsIgnoreCase(nameItem.getValueAsString())){
									SC.warn("Population "+nameItem.getValueAsString()+" already exists !!");
									nameItem.clearValue();
									ok = 0;
								}
							}
							if (ok == 1){
								ListGridRecord newPopulationRecord = new ListGridRecord();
								newPopulationRecord.setAttribute("population_name", nameItem.getValue());
								newPopulationRecord.setAttribute("user", Axiom.get().getUser().getFirstname()+" "+Axiom.get().getUser().getLastname());
								newPopulationRecord.setAttribute("population_id", 1);
								newPopulationRecord.setAttribute("created", new Date());
								populationsGrid.addData(newPopulationRecord);
								newPopulationWindow.destroy();
							}
						}else{
							SC.warn("Insert population name");
						}
					}
				});
				final IButton cancelButton = new IButton("Cancel");
				cancelButton.addClickHandler(new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						newPopulationWindow.destroy();
					}
				});
				newPopulationWindow.setToolbarButtons(windowAddButton,cancelButton);
				form.setFields(nameItem);
				newPopulationWindow.addItem(form);
				
				form.setAutoFocus(true);
				newPopulationWindow.show();  
			}
		});
		
		IButton removeButton = new IButton("Remove population", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				final ListGridRecord selectedRecord = populationsGrid.getSelectedRecord();
				if (selectedRecord!=null){
					final String populationName = selectedRecord.getAttribute("population_name");
					samplesService.nbSamplesInPopulation(populationName, new AsyncCallback<Integer>() {
						
						@Override
						public void onSuccess(Integer result) {
							SC.ask("Remove population", "There are "+result+" samples assigned to population "+populationName+". Are you sure you want to remove it?", new BooleanCallback() {

								@Override
								public void execute(Boolean value) {
									if (value != null && value){
										populationsGrid.removeData(selectedRecord);
									}
								}
							});	
						}
						
						@Override
						public void onFailure(Throwable caught) {
							SC.warn("Can't remove population "+populationName+" from database");
						}
					});
				}else{
					SC.say("Please select a population !!");
				}
			}
		});
		
		addButton.setIcon("icons/Create.png");
		addButton.setOverflow(Overflow.VISIBLE);
		addButton.setAutoWidth();
		
		removeButton.setIcon("icons/Remove.png");
		removeButton.setOverflow(Overflow.VISIBLE);
		removeButton.setAutoWidth();
		
		buttonLayout.addMember(addButton);
		buttonLayout.addMember(removeButton);
		
		gridLayout.addMember(gridTitle);
		gridLayout.addMember(populationsGrid);
		gridLayout.addMember(buttonLayout);
		
		vStack.addMember(gridLayout);
	
		vlayout.addMember(vStack);

		/*
		 * Add layout to mainArea tab
		 */
		MainArea.addTabToTopTabset("Populations",tabID, vlayout, true);
	}
}
