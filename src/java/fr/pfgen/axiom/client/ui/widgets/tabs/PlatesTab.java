package fr.pfgen.axiom.client.ui.widgets.tabs;

import fr.pfgen.axiom.client.ui.widgets.MainArea;
import fr.pfgen.axiom.client.ui.widgets.grids.listGrids.PlatesListGrid;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.VStack;

public class PlatesTab {
	
	public PlatesTab(String tabID){
		
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
		gridTitle.setContents("Plates");
		gridTitle.setStyleName("textTitle");

		final PlatesListGrid platesGrid = new PlatesListGrid();

		final IButton removePlateButton = new IButton("Remove selected", new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				final ListGridRecord selectedRecord = platesGrid.getSelectedRecord();  
				if(selectedRecord != null) {  
					SC.confirm("Removal of a plate will also remove all samples from this plate on the database.  Please note that QC for these samples will also be deleted and that if a genotyping has been performed containing the samples, another one should be done without these samples !", new BooleanCallback() {

						@Override
						public void execute(Boolean value) {
							if (value != null && value){
								platesGrid.removeData(selectedRecord);  	
							}
						}
					});
				} else { 
					SC.say("Select a record before performing this action");  
				}
			}
		});
		
		removePlateButton.setIcon("icons/Remove.png");
		removePlateButton.setOverflow(Overflow.VISIBLE);
		removePlateButton.setAutoWidth();
		removePlateButton.setLayoutAlign(Alignment.CENTER);
		
		gridLayout.addMember(gridTitle);
		gridLayout.addMember(platesGrid);
		gridLayout.addMember(removePlateButton);
		
		vStack.addMember(gridLayout);
		
		vlayout.addMember(vStack);

		/*
		 * Add layout to mainArea tab
		 */
		MainArea.addTabToTopTabset("Plates",tabID, vlayout, true);
	}
}
