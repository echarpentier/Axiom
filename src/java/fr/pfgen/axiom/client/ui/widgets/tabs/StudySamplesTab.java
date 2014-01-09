package fr.pfgen.axiom.client.ui.widgets.tabs;

import fr.pfgen.axiom.client.services.StudiesService;
import fr.pfgen.axiom.client.services.StudiesServiceAsync;
import fr.pfgen.axiom.client.ui.widgets.grids.listGrids.GenoAnalysisListGrid;
import fr.pfgen.axiom.client.ui.widgets.vstacks.GenoAnalysisVstack;
import fr.pfgen.axiom.client.ui.widgets.vstacks.GenoQCVstack;
import fr.pfgen.axiom.client.ui.widgets.windows.ChooseSamplesForStudyWindow;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.DragDataAction;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.util.StringUtil;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.events.DataArrivedEvent;
import com.smartgwt.client.widgets.grid.events.DataArrivedHandler;
import com.smartgwt.client.widgets.grid.events.RecordDoubleClickEvent;
import com.smartgwt.client.widgets.grid.events.RecordDoubleClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.VStack;
import com.smartgwt.client.widgets.tab.Tab;

public class StudySamplesTab extends Tab{

	private final StudiesServiceAsync studiesService = GWT.create(StudiesService.class);
	private String studyName;
	private VLayout tabPane;

	public StudySamplesTab(String name){
		this.studyName = name;
		tabPane = new VLayout(10);
		tabPane.setDefaultLayoutAlign(Alignment.LEFT);
		
		setPrompt("Samples");
		setTitle("&nbsp;"+Canvas.imgHTML("workflows/User_Group-64x64.png",16,16));
		
		studiesService.checkGenoSamplesForStudy(studyName, new AsyncCallback<Boolean>() {

			@Override
			public void onFailure(Throwable caught) {
				SC.warn("Cannot retrieve samples associated with study");
			}

			@Override
			public void onSuccess(Boolean result) {
				if (result != null && result){
					showSamplesInStudy();
				}else{
					chooseSamplesForStudy();
				}
			}
		});

		setPane(tabPane);
	}

	public void showSamplesInStudy(){
		if (tabPane.getMember(0)!=null){
			tabPane.getMember(0).destroy();
		}
		final HLayout layout = new HLayout(10);
		layout.setAutoWidth();
		layout.setAutoHeight();

		GenoAnalysisVstack genoAnalysisStack = new GenoAnalysisVstack();
		genoAnalysisStack.addHeaderLabel("Genotyping analysis in study");
		genoAnalysisStack.addGrid();

		Criteria criteria = new Criteria();
		criteria.addCriteria("study_name", studyName);
		GenoAnalysisListGrid grid = genoAnalysisStack.getGrid();
		grid.fetchData(criteria);
		grid.setPrompt("Double clic on genotyping analysis to see samples in study");
		grid.addRecordDoubleClickHandler(new RecordDoubleClickHandler() {
			
			@Override
			public void onRecordDoubleClick(RecordDoubleClickEvent event) {
				final String genoName = event.getRecord().getAttribute("geno_name");
				
				if (layout.getMembers().length>1){
					for (int i = 1; i < layout.getMembers().length; i++) {
							layout.getMember(i).destroy();
					}
				}
				
				VLayout genoQCVlayout = new VLayout(10);
				genoQCVlayout.addMember(constructGenoQCStack(genoName,"first"));
				genoQCVlayout.addMember(constructGenoQCStack(genoName,"second"));
				layout.addMember(genoQCVlayout);
			}
		});


		layout.addMember(genoAnalysisStack);

		tabPane.addMember(layout,0);
	}

	private void chooseSamplesForStudy(){
		final ChooseSamplesForStudyWindow win = new ChooseSamplesForStudyWindow(studyName,this);
		HLayout layout = new HLayout();
		Label label = new Label();
		label.setAutoHeight();
		label.setAutoWidth();
		label.addStyleName("clickable");
		label.setContents(StringUtil.asHTML("Clic here to choose samples for this study",true));
		label.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				studiesService.checkGenoSamplesForStudy(studyName, new AsyncCallback<Boolean>() {

					@Override
					public void onFailure(Throwable caught) {
						SC.warn("Cannot retrieve samples associated with study");
					}

					@Override
					public void onSuccess(Boolean result) {
						if (result){
							win.destroy();
							showSamplesInStudy();
						}else{
							win.show();
						}
					}
				});
			}
		});
		
		layout.addMember(label);
		
		tabPane.addMember(layout);
	}
	
	private VStack constructGenoQCStack(String genoName,String run){
		final GenoQCVstack genoQCStack = new GenoQCVstack();
		genoQCStack.setShowEdges(true);
		Criteria criteria = new Criteria();
		criteria.addCriteria("geno_name", genoName);
		criteria.addCriteria("geno_run", run);
		criteria.addCriteria("study_name", studyName);
		genoQCStack.addHeaderLabel("Genotyping QC ("+run+" run)");
		genoQCStack.addGrid();
		genoQCStack.getGrid().fetchData(criteria);
		genoQCStack.getGrid().setAutoFitMaxRecords(8);
		genoQCStack.getGrid().setCanDragRecordsOut(true);
		genoQCStack.getGrid().setDragDataAction(DragDataAction.COPY);
		genoQCStack.getGrid().addDataArrivedHandler(new DataArrivedHandler() {
			
			@Override
			public void onDataArrived(DataArrivedEvent event) {
				genoQCStack.addLabel(event.getEndRow()+" samples");
			}
		});
		
		return genoQCStack;
	}
}