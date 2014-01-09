package fr.pfgen.axiom.client.ui.widgets.stackMenus;

import java.util.ArrayList;
import java.util.List;

import fr.pfgen.axiom.client.services.CreateDQCGraphService;
import fr.pfgen.axiom.client.services.CreateDQCGraphServiceAsync;
import fr.pfgen.axiom.client.ui.widgets.grids.treeGrids.GenotypesTreeGrid;
import fr.pfgen.axiom.client.ui.widgets.vstacks.GenoAnalysisVstack;
import fr.pfgen.axiom.client.ui.widgets.windows.GenotypingWindow;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.SelectionAppearance;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.VLayout;

public class GenotypesStackMenu extends VLayout{
	
	private final CreateDQCGraphServiceAsync dqcService = GWT.create(CreateDQCGraphService.class);
	
	public GenotypesStackMenu(){
		
		this.setMembersMargin(10);
		this.setLayoutTopMargin(20);
		
		final IButton performGenotypingButton = new IButton();
		performGenotypingButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				new GenotypingWindow();
			}
		});
		
		performGenotypingButton.setPrompt("Clic on this button to perform GENOTYPING on a population.");

		performGenotypingButton.setTitle("Perform Genotyping");
		performGenotypingButton.setOverflow(Overflow.VISIBLE);
		performGenotypingButton.setAutoWidth();
		performGenotypingButton.setLayoutAlign(Alignment.CENTER);
		performGenotypingButton.setIcon("icons/staticATGC.ico");
		performGenotypingButton.setShowDisabledIcon(false);
		
		final IButton boxPlotButton = new IButton();
		boxPlotButton.setTitle("QC params boxplot");
		boxPlotButton.setAutoFit(true);
		boxPlotButton.setLayoutAlign(Alignment.CENTER);
		boxPlotButton.setIcon("icons/Graph-boxplot.ico");
		boxPlotButton.setShowDisabledIcon(false);
		
		boxPlotButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				boxPlotButton.disable();
				final Dialog chooseGenoAnalysisWin = new Dialog();
				chooseGenoAnalysisWin.setAutoCenter(true);
				chooseGenoAnalysisWin.setTitle("Choose genotyping analysis");
				chooseGenoAnalysisWin.setAutoSize(true);
				chooseGenoAnalysisWin.addCloseClickHandler(new CloseClickHandler() {
					
					@Override
					public void onCloseClick(CloseClientEvent event) {
						boxPlotButton.enable();
						chooseGenoAnalysisWin.destroy();
					}
				});
				
				final GenoAnalysisVstack genoStack = new GenoAnalysisVstack();
				genoStack.addLabel("Be careful when adding genotyping analysis.");
				genoStack.addLabel("If you add genotyping analysis which have samples in common, it might give hazardous results !");
				genoStack.addGrid();
				genoStack.getGrid().setSelectionType(SelectionStyle.SIMPLE);
				genoStack.getGrid().setSelectionAppearance(SelectionAppearance.CHECKBOX);
				
				final IButton fetchButton = new IButton("Create", new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						List<String> genoNames = new ArrayList<String>();
						ListGridRecord[] selRec = genoStack.getGrid().getSelectedRecords();
						for (ListGridRecord rec : selRec) {
							genoNames.add(rec.getAttributeAsString("geno_name"));
						}
						if (genoNames.isEmpty()){
							SC.warn("Please select at least one genotyping analysis");
						}else{
							dqcService.createParamBoxplot(genoNames, new AsyncCallback<String>() {

								@Override
								public void onFailure(Throwable caught) {
									SC.warn("Failed to create params boxplot !!");
								}

								@Override
								public void onSuccess(String result) {
									if (result!=null){
										com.google.gwt.user.client.Window.open(GWT.getModuleBaseURL() + "fileProvider?file="+result, "_self", "");
									}else{
										SC.warn("Failed to create params boxplot !!");
									}
								}
							});
						}
						boxPlotButton.enable();
					}
				});
				
				final IButton cancelButton = new IButton("Cancel", new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						boxPlotButton.enable();
						chooseGenoAnalysisWin.destroy();
					}
				});
				
				chooseGenoAnalysisWin.addItem(genoStack);
				chooseGenoAnalysisWin.setToolbarButtons(fetchButton,cancelButton);
				
				chooseGenoAnalysisWin.show();
			}
		});
		
		final GenotypesTreeGrid genoTreeGrid = new GenotypesTreeGrid();
		genoTreeGrid.setBorder("0px");
		
		this.addMember(performGenotypingButton);
		this.addMember(boxPlotButton);
		this.addMember(genoTreeGrid);
	}
}
