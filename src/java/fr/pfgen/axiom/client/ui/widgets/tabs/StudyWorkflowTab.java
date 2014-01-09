package fr.pfgen.axiom.client.ui.widgets.tabs;

import fr.pfgen.axiom.client.services.StudiesService;
import fr.pfgen.axiom.client.services.StudiesServiceAsync;
import fr.pfgen.axiom.shared.records.StudyWorkflowState;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Cursor;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.events.TabSelectedEvent;
import com.smartgwt.client.widgets.tab.events.TabSelectedHandler;

public class StudyWorkflowTab extends Tab{
	
	private final StudiesServiceAsync studiesService = GWT.create(StudiesService.class);
	private String studyName;
	private VLayout tabPane;
	private Img samplesImg = new Img("workflows/User_Group-64x64.png");
	private Img pedigreeImg = new Img("workflows/pedigree.png");
	private Img pfilesImg = new Img("workflows/plink_files.png");
	private Img qcImg = new Img("workflows/chart.png");
	private final String downLeftArrow = "workflows/arrow-left-down.jpg";
	private final String downRightArrow = "workflows/arrow-right-down.jpg";
	private final String rightArrow = "workflows/arrow-right.jpg";
	private final String leftArrow = "workflows/arrow-left.jpg";
	private final String downArrow = "workflows/arrow-down.jpg";
	private HLayout samplesLayout;
	private HLayout qcLayout;
	
	public StudyWorkflowTab(String studyName){
		this.studyName = studyName;
		tabPane = new VLayout(10);
		//tabPane.setDefaultLayoutAlign(Alignment.CENTER);
		tabPane.setAutoHeight();
		tabPane.setAutoWidth();
		
		tabPane.addMember(samplesHlayout());
		tabPane.addMember(downArrowImg());
		tabPane.addMember(qcHlayout());
		tabPane.addMember(downArrowImg());
		
		setPrompt("Workflow");
		setTitle("&nbsp;"+Canvas.imgHTML("icons/Diagram.png"));
		addTabSelectedHandler(new TabSelectedHandler() {
			
			@Override
			public void onTabSelected(TabSelectedEvent event) {
				updateWorkflowImages();
			}
		});
		
		setPane(tabPane);
		updateWorkflowImages();
	}
	
	public void disableSamplesImg(){
		this.samplesImg.setPrompt("Samples for this study have already been selected");
		this.samplesImg.disable();
	}
	
	public void enableSamplesImg(){
		this.samplesImg.enable();
		this.samplesImg.setPrompt("Clic to choose samples for this study");
	}
	
	public void disablePedigreeImg(){
		this.pedigreeImg.setPrompt("Pedigree for this study has already been set");
		this.pedigreeImg.disable();
	}
	
	public void enablePedigreeImg(){
		this.pedigreeImg.enable();
		this.pedigreeImg.setPrompt("Clic to set the pedigree file for this study");
	}
	
	public void disablePlinkImg(){
		this.pfilesImg.setPrompt("Plink files for this study have already been created");
		this.pfilesImg.disable();
	}
	
	public void enablePlinkImg(){
		this.pfilesImg.enable();
		this.pfilesImg.setPrompt("Clic to generate plink files for this study");
	}
	
	public void disableQcImg(){
		this.qcImg.disable();
		this.qcImg.setPrompt("Individual and SNP QC have already been performed for this study");
	}
	
	public void enableQcImg(){
		this.qcImg.enable();
		this.qcImg.setPrompt("Perform individual and snp QC for this study");
	}
	
	public void disableAllImg(){
		this.disableSamplesImg();
		this.disablePedigreeImg();
		this.disablePlinkImg();
		this.disableQcImg();
	}
	
	public void enableAllImg(){
		this.enableSamplesImg();
		this.enablePedigreeImg();
		this.enablePlinkImg();
		this.enableQcImg();
	}
	
	public void updateWorkflowImages(){
		tabPane.disable();
		studiesService.checkWorkflowState(studyName, new AsyncCallback<StudyWorkflowState>() {

			@Override
			public void onFailure(Throwable caught) {
				SC.warn("Cannot retrieve workflow state from server");
			}

			@Override
			public void onSuccess(StudyWorkflowState states) {
				if (states.isSamplesDone()){
					disableSamplesImg();
				}else{
					enableSamplesImg();
				}
				if (states.isPedigreeDone()){
					disablePedigreeImg();
				}else{
					enablePedigreeImg();
				}
				if (states.isPlinkFilesDone()){
					disablePlinkImg();
				}else{
					enablePlinkImg();
				}
				tabPane.enable();
			}
		});
	}
	
	private Img downArrowImg(){
		Img img = new Img(downArrow);
		img.setSize(64);
		img.setLayoutAlign(Alignment.RIGHT);
		
		return img;
	}
	
	private Img rightArrowImg(){
		Img img = new Img(rightArrow);
		img.setSize(64);
		
		return img;
	}
	
	private Img leftArrowImg(){
		Img img = new Img(leftArrow);
		img.setSize(64);
		
		return img;
	}
	
	private Img downLeftArrowImg(){
		Img img = new Img(downLeftArrow);
		img.setSize(64);
		
		return img;
	}
	
	private Img downRightArrowImg(){
		Img img = new Img(downRightArrow);
		img.setSize(64);
		
		return img;
	}
	
	private HLayout samplesHlayout(){
		samplesLayout = new HLayout(10);
		samplesLayout.setAutoWidth();
		samplesLayout.setAutoHeight();
		samplesLayout.setDefaultLayoutAlign(Alignment.CENTER);
		samplesLayout.setBorder("2px solid grey");
		
		samplesImg.setSize(64);
		samplesImg.setCursor(Cursor.HAND);
		samplesImg.setDisabledCursor(Cursor.NOT_ALLOWED);
		samplesImg.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				getTabSet().selectTab(1);
			}
		});
		
		pedigreeImg.setSize(64);
		pedigreeImg.setCursor(Cursor.HAND);
		pedigreeImg.setDisabledCursor(Cursor.NOT_ALLOWED);
		pedigreeImg.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				getTabSet().selectTab(2);	
			}
		});
		
		pfilesImg.setSize(64);
		pfilesImg.setCursor(Cursor.HAND);
		pfilesImg.setDisabledCursor(Cursor.NOT_ALLOWED);
		pfilesImg.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				getTabSet().selectTab(3);
			}
		});
		
		samplesLayout.addMember(samplesImg);
		samplesLayout.addMember(rightArrowImg());
		samplesLayout.addMember(pedigreeImg);
		samplesLayout.addMember(rightArrowImg());
		samplesLayout.addMember(pfilesImg);
		
		return samplesLayout;
	}
	
	private HLayout qcHlayout(){
		qcLayout = new HLayout(10);
		qcLayout.setAutoWidth();
		qcLayout.setAutoHeight();
		qcLayout.setDefaultLayoutAlign(Alignment.CENTER);
		qcLayout.setBorder("2px solid grey");
		qcLayout.setLayoutAlign(Alignment.RIGHT);
		
		qcImg.setSize(64);
		qcImg.setCursor(Cursor.HAND);
		qcImg.setDisabledCursor(Cursor.NOT_ALLOWED);
		qcImg.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				getTabSet().selectTab(4);	
			}
		});
		
		qcLayout.addMember(qcImg);
		return qcLayout;
	}
}