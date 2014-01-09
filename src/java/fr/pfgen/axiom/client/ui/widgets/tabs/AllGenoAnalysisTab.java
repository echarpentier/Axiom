package fr.pfgen.axiom.client.ui.widgets.tabs;

import java.util.List;

import fr.pfgen.axiom.client.services.GenotypingAnalysisService;
import fr.pfgen.axiom.client.services.GenotypingAnalysisServiceAsync;
import fr.pfgen.axiom.client.ui.widgets.MainArea;
import fr.pfgen.axiom.client.ui.widgets.grids.listGrids.GenoAnalysisListGrid;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
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

public class AllGenoAnalysisTab {

	private final GenotypingAnalysisServiceAsync genotypingService = GWT.create(GenotypingAnalysisService.class);
	
	public AllGenoAnalysisTab(String tabID){
		
		VLayout vlayout = new VLayout(15);
		vlayout.setWidth("80%");
		vlayout.setDefaultLayoutAlign(Alignment.CENTER);
		
		vlayout.addMember(constructGenoAnalysisGrid());
		
		/*
		 * Add layout to mainArea tab
		 */
		MainArea.addTabToTopTabset("Genotyping Analysis",tabID, vlayout, true);
	}
	
	private VStack constructGenoAnalysisGrid(){
		
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
		final GenoAnalysisListGrid genoAnalysisListGrid = new GenoAnalysisListGrid();
		//Criteria criteria = new Criteria();
		//genoAnalysisListGrid.fetchData();
		
		IButton removeButton = new IButton("Remove Analysis");
		removeButton.setIcon("icons/Remove.png");
		removeButton.setOverflow(Overflow.VISIBLE);
		removeButton.setAutoWidth();
		removeButton.setLayoutAlign(Alignment.CENTER);
		
		removeButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				final ListGridRecord selectedRecord = genoAnalysisListGrid.getSelectedRecord();
				if (selectedRecord==null){
					SC.warn("Please select a genotyping analysis");
					return;
				}
				final int genoID = selectedRecord.getAttributeAsInt("geno_id");
				genotypingService.studiesLinkedToGenoAnalysis(genoID, new AsyncCallback<List<String>>() {
					
					@Override
					public void onSuccess(List<String> studyList) {
						if (studyList==null){
							SC.warn("Can't check is studies linked to genotyping analysis");
							return;
						}
						if (studyList.size()>0){
							StringBuilder sb = new StringBuilder();
							sb.append("There are "+studyList.size()+" studies (");
							for (String studyName : studyList) {
								sb.append(studyName+",");
							}
							sb.deleteCharAt(sb.lastIndexOf(","));
							sb.append(") in which this genotyping analysis is included.<br>If you want to delete this genotyping analysis, please delete this(these) study(ies) first.");
							SC.warn(sb.toString());
							return;
						}
						if (studyList.size()==0){
							SC.ask("All data for this genotyping analysis will be permanently removed.<br>Do you want to continue?", new BooleanCallback() {
								
								@Override
								public void execute(Boolean value) {
									if (value != null && value){
										genoAnalysisListGrid.removeData(selectedRecord);
									}
								}
							});
						}
					}
					
					@Override
					public void onFailure(Throwable caught) {
						SC.warn("Can't check is studies linked to genotyping analysis");
					}
				});
			}
		});
		
		analysisStack.addMember(analysisStackTitle);
		analysisStack.addMember(genoAnalysisListGrid);
		analysisStack.addMember(removeButton);
		
		return analysisStack;
	}	
}
