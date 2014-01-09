package fr.pfgen.axiom.client.ui.widgets.stackMenus;

import fr.pfgen.axiom.client.services.CreateDQCGraphService;
import fr.pfgen.axiom.client.services.CreateDQCGraphServiceAsync;
import fr.pfgen.axiom.client.services.QCService;
import fr.pfgen.axiom.client.services.QCServiceAsync;
import fr.pfgen.axiom.client.ui.widgets.grids.treeGrids.QualityControlsTreeGrid;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.VLayout;

public class QualityControlsStackMenu extends VLayout{
	
	private final QCServiceAsync qcService = GWT.create(QCService.class);
	private final CreateDQCGraphServiceAsync dqcService = GWT.create(CreateDQCGraphService.class);

	public QualityControlsStackMenu(){

		this.setMembersMargin(10);
		this.setLayoutTopMargin(20);

		final IButton performQCButton = new IButton();
		performQCButton.setPrompt("Clic on this button to perform QC on all samples which have not yet been analysed.");
		performQCButton.setTitle("Perform QCs");
		performQCButton.setOverflow(Overflow.VISIBLE);
		performQCButton.setAutoWidth();
		performQCButton.setLayoutAlign(Alignment.CENTER);
		performQCButton.setIcon("icons/3d bar chart.png");
		performQCButton.setShowDisabledIcon(false);
		
		performQCButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				performQCButton.disable();
				qcService.NbSamplesWithoutQC(new AsyncCallback<Integer>() {

					@Override
					public void onSuccess(Integer result) {
						if (result==-1){
							SC.warn("A QC analysis is already being performed on server.\nCheck the workflow tab to see its progress.");
							performQCButton.enable();
						}else if (result==0){
							SC.say("No samples found without QC");
							performQCButton.enable();
						}else{
							SC.confirm("QC will be performed on "+result+" samples.\nConfirm?", new BooleanCallback() {

								@Override
								public void execute(Boolean value) {
									if (value != null && value){
										qcService.performQC(new AsyncCallback<String>() {

											@Override
											public void onSuccess(String result) {
												SC.say(result);
												performQCButton.enable();
											}

											@Override
											public void onFailure(Throwable caught) {
												SC.warn("Can't perform QC");
												performQCButton.enable();
											}
										});
									}else{
										performQCButton.enable();
									}
								}
							});
						}
					}

					@Override
					public void onFailure(Throwable caught) {
						SC.warn("Can't perform QC");
						performQCButton.enable();
					}
				});
			}
		});
		
		final IButton dQCGraphButton = new IButton();
		dQCGraphButton.setTitle("DQC Graph");
		dQCGraphButton.setAutoFit(true);
		dQCGraphButton.setLayoutAlign(Alignment.CENTER);
		dQCGraphButton.setIcon("icons/Graph-boxplot.ico");
		dQCGraphButton.setShowDisabledIcon(false);
		
		dQCGraphButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				dQCGraphButton.disable();
				dqcService.createDQCGraph(new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable caught) {
						dQCGraphButton.enable();
						SC.warn("Can't download DQC Graph from server");
					}

					@Override
					public void onSuccess(String result) {
						dQCGraphButton.enable();
						if (result!=null){
							com.google.gwt.user.client.Window.open(GWT.getModuleBaseURL() + "fileProvider?file="+result, "_self", "");
						}else{
							SC.warn("Can't download DQC Graph from server");
						}	
					}
				});
			}
		});

		final QualityControlsTreeGrid QCTreeGrid = new QualityControlsTreeGrid();
		QCTreeGrid.setBorder("0px");

		this.addMember(performQCButton);
		this.addMember(dQCGraphButton);
		this.addMember(QCTreeGrid);
		
	}
}
