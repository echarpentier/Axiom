package fr.pfgen.axiom.client.ui.widgets.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.pfgen.axiom.client.Axiom;
import fr.pfgen.axiom.client.services.PedigreeService;
import fr.pfgen.axiom.client.services.PedigreeServiceAsync;
import fr.pfgen.axiom.client.ui.widgets.grids.listGrids.PedigreeListGrid;
import fr.pfgen.axiom.client.ui.widgets.vstacks.GenericVstack;
import fr.pfgen.axiom.client.ui.widgets.vstacks.PedigreeVstack;
import fr.pfgen.axiom.client.ui.widgets.vstacks.UploadListener;
import fr.pfgen.axiom.client.ui.widgets.vstacks.Upload;
import fr.pfgen.axiom.client.ui.widgets.windows.PedigreeWindow;
import fr.pfgen.axiom.shared.records.PedigreeState;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Cursor;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.util.StringUtil;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.events.DataArrivedEvent;
import com.smartgwt.client.widgets.grid.events.DataArrivedHandler;
import com.smartgwt.client.widgets.grid.events.EditCompleteEvent;
import com.smartgwt.client.widgets.grid.events.EditCompleteHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;

public class StudyPedigreeTab extends Tab{

	private final PedigreeServiceAsync pedigreeService = GWT.create(PedigreeService.class);
	private String studyName;
	private final String studyType;
	private HLayout tabPane;
	private String borderStyle = "1px solid #C0C0C0";
	private final PedigreeWorkflow pedWorkflow;
	private final int userID = Axiom.get().getUser().getUserID();
	
	
	public StudyPedigreeTab(final String studyName, final String studyType) {
		this.studyName = studyName;
		this.studyType = studyType;
		tabPane = new HLayout(10);
		tabPane.setDefaultLayoutAlign(Alignment.LEFT);
		
		setPrompt("Pedigree");
		setTitle("&nbsp;"+Canvas.imgHTML("workflows/pedigree.png",16,16));
		
		pedWorkflow = new PedigreeWorkflow();
		tabPane.addMember(pedWorkflow,0);
		
		pedigreeService.checkPedigreeState(studyName, userID, new AsyncCallback<PedigreeState>() {

			@Override
			public void onFailure(Throwable caught) {
				SC.warn("Cannot retrieve pedigree state for study: "+studyName);
			}

			@Override
			public void onSuccess(PedigreeState pedState) {
				pedWorkflow.setAllStatusImgsNo();
				pedWorkflow.setAllGotoImgsDisabled();
				if (!pedState.isFileUploaded()){
					pedWorkflow.getUploadLine().setStatusImgNo();
					pedWorkflow.getUploadLine().getGotoImg().enable();
				}else{
					pedWorkflow.getUploadLine().setStatusImgOk();
					pedWorkflow.getUploadLine().getGotoImg().enable();
					if (!pedState.isSamplesFound()){
						pedWorkflow.getSamplesLine().setStatusImgNo();
						pedWorkflow.getSamplesLine().getGotoImg().enable();
					}else{
						pedWorkflow.getSamplesLine().setStatusImgOk();
						pedWorkflow.getSamplesLine().getGotoImg().disable();
						if (!pedState.isIndividualsDescribed()){
							pedWorkflow.getIndividualsLine().setStatusImgNo();
							pedWorkflow.getIndividualsLine().getGotoImg().enable();
						}else{
							pedWorkflow.getIndividualsLine().setStatusImgOk();
							pedWorkflow.getIndividualsLine().getGotoImg().disable();
							if (!pedState.isSexChecked()){
								pedWorkflow.getSexLine().setStatusImgNo();
								pedWorkflow.getSexLine().getGotoImg().enable();
							}else{
								pedWorkflow.getSexLine().setStatusImgOk();
								pedWorkflow.getSexLine().getGotoImg().disable();
								if (!pedState.isStatusChecked()){
									pedWorkflow.getStatusLine().setStatusImgNo();
									pedWorkflow.getStatusLine().getGotoImg().enable();
								}else{
									pedWorkflow.getStatusLine().setStatusImgOk();
									pedWorkflow.getStatusLine().getGotoImg().disable();
									if (!pedState.isAlldone()){
										pedWorkflow.getFinalLine().setStatusImgNo();
										pedWorkflow.getFinalLine().getGotoImg().enable();
									}else{
										//pedWorkflow.getFinalLine().setStatusImgOk();
										//pedWorkflow.getFinalLine().getGotoImg().disable();
										pedWorkflow.destroy();
										tabPane.addMember(showPedigreeInStudy(),0);
									}
								}
							}
						}
					}
				}
			}
		});
		
		pedWorkflow.getUploadLine().getGotoImg().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (tabPane.getMember(1)!=null){
					tabPane.getMember(1).destroy();
				}
				tabPane.addMember(constructUploadLayout(),1);
			}
		});
		pedWorkflow.getSamplesLine().getGotoImg().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (tabPane.getMember(1)!=null){
					tabPane.getMember(1).destroy();
				}
				tabPane.addMember(constructCheckSamplesLayout(),1);
			}
		});
		pedWorkflow.getIndividualsLine().getGotoImg().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (tabPane.getMember(1)!=null){
					tabPane.getMember(1).destroy();
				}
				tabPane.addMember(constructCheckIndividualsLayout(),1);
			}
		});
		pedWorkflow.getSexLine().getGotoImg().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (tabPane.getMember(1)!=null){
					tabPane.getMember(1).destroy();
				}
				tabPane.addMember(constructCheckSexLayout(),1);
			}
		});
		pedWorkflow.getStatusLine().getGotoImg().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (tabPane.getMember(1)!=null){
					tabPane.getMember(1).destroy();
				}
				tabPane.addMember(constructCheckStatusLayout(),1);
			}
		});
		pedWorkflow.getFinalLine().getGotoImg().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (tabPane.getMember(1)!=null){
					tabPane.getMember(1).destroy();
				}
				tabPane.addMember(constructFinalizeLayout(),1);
				
			}
		});
		
		setPane(tabPane);
	}
	
	private VLayout constructUploadLayout(){
		VLayout layout = new VLayout(10);
		layout.setAutoHeight();
		layout.setAutoWidth();
		layout.setBorder(borderStyle);
		layout.setLayoutMargin(10);
		layout.setDefaultLayoutAlign(Alignment.CENTER);
		
		//Map<String, String> hiddenItems = new HashMap<String, String>();
		//hiddenItems.put("studyName", studyName);
		//hiddenItems.put("user", Axiom.get().getUser().getAppID());
		//final Upload uploadStack = new Upload(hiddenItems,Mode.DEFAULT);
		final Upload uploadStack = new Upload();
		uploadStack.setHeaderLabel("Upload a pedigree file");
		uploadStack.setFileItemTitle("Pedigree");
		uploadStack.setAction(GWT.getModuleBaseURL() + "fileUploader");
		
		final Img loadingGif = new Img("loadingStar.gif",40,40);
		
		uploadStack.getUploadButton().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (uploadStack.getFileItem().getValueAsString()==null || uploadStack.getFileItem().getValueAsString().equals("")){
					SC.warn("Please select a file.");
				}else{
					uploadStack.getUploadButton().disable();
					uploadStack.getStack().addMember(loadingGif);
				}
			}
		});
		uploadStack.setUploadListener(new UploadListener() {
			
			@Override
			public void uploadComplete(String filePath) {
				if (filePath.startsWith("Error")){
					uploadStack.getUploadButton().enable();
					uploadStack.getStack().removeMember(loadingGif);
					SC.warn(filePath);
				}else{
					pedigreeService.checkUploadedPedigree(studyName, userID, filePath, new AsyncCallback<Map<String, String>>() {

						@Override
						public void onFailure(Throwable caught) {
							uploadStack.getUploadButton().enable();
							uploadStack.getStack().removeMember(loadingGif);
							SC.warn("Cannot check uploaded pedigree");
						}

						@Override
						public void onSuccess(Map<String, String> result) {
							uploadStack.getUploadButton().enable();
							uploadStack.getStack().removeMember(loadingGif);
							if (result==null){
								SC.warn("Cannot check uploaded pedigree");
							}else{
								if (result.isEmpty()){
									SC.say("Upload complete");
									pedWorkflow.setAllGotoImgsDisabled();
									pedWorkflow.setAllStatusImgsNo();
									pedWorkflow.getUploadLine().setStatusImgOk();
									pedWorkflow.getUploadLine().getGotoImg().enable();
									pedWorkflow.getSamplesLine().getGotoImg().enable();
									tabPane.getMember(1).destroy();
								}else{
									StringBuilder sb = new StringBuilder();
									for (String line : result.keySet()) {
										sb.append(result.get(line)+"<br>"+line);
									}
									SC.warn(sb.toString());
								}
							}
						}
					});
				}
			}
		});
		
		Label explanation = new Label();
		explanation.setAutoFit(true);
		explanation.setContents("Pedigree&nbsp;file&nbsp;must&nbsp;contain&nbsp;6&nbsp;columns&nbsp;tab&nbsp;delimited&nbsp;without&nbsp;header&nbsp;line:<br>familyName&nbsp;individualID&nbsp;fatherID&nbsp;motherID&nbsp;sex&nbsp;status<br>sex:&nbsp;1&nbsp;for&nbsp;'male',&nbsp;2&nbsp;for&nbsp;'female',&nbsp;0&nbsp;for&nbsp;'unknown'<br>status:&nbsp;1&nbsp;for&nbsp;'unaffected',&nbsp;2&nbsp;for&nbsp;'affected',&nbsp;0&nbsp;for&nbsp;'unknown'<br>The&nbsp;pair&nbsp;FamilyID&nbsp;-&nbsp;IndividualIDs&nbsp;must&nbsp;be&nbsp;unique");
		uploadStack.addLabel(explanation);
		
		layout.addMember(uploadStack);
		
		return layout;
	}
	
	private VLayout constructCheckSamplesLayout(){
		final VLayout layout = new VLayout(10);
		layout.setAutoHeight();
		layout.setAutoWidth();
		layout.setBorder(borderStyle);
		layout.setLayoutMargin(10);
		layout.setDefaultLayoutAlign(Alignment.CENTER);
		
		final Img loadingGif = new Img("loadingStar.gif",40,40);
		layout.addMember(loadingGif);
		
		pedigreeService.checkSamplesInPedigree(studyName, userID, new AsyncCallback<Map<String,List<String>>>() {

			@Override
			public void onFailure(Throwable caught) {
				layout.removeMember(loadingGif);
				SC.warn("Cannot compare samples in pedigree and samples in study !");
			}

			@Override
			public void onSuccess(Map<String, List<String>> result) {
				layout.removeMember(loadingGif);
				if (result.get("isNotInPed").isEmpty() && result.get("isNotInStudy").isEmpty()){
					SC.say("Samples in pedigree and in study match !");
					pedigreeService.validatePedigree(studyName, userID, new AsyncCallback<Boolean>() {

						@Override
						public void onFailure(Throwable caught) {
							SC.warn("Cannot validate samples in pedigree !");
						}

						@Override
						public void onSuccess(Boolean result) {
							if (result!=null && result){
								SC.say("Samples validated !");
								pedWorkflow.getSamplesLine().setStatusImgOk();
								pedWorkflow.getSamplesLine().getGotoImg().disable();
								pedWorkflow.getIndividualsLine().getGotoImg().enable();
								tabPane.getMember(1).destroy();
							}else{
								SC.warn("Cannot validate samples in pedigree !");
							}
						}
					});
				}else{
					Label explanationLabel = new Label();
					explanationLabel.setAutoHeight();
					explanationLabel.setAutoWidth();
					explanationLabel.setOverflow(Overflow.VISIBLE);
					explanationLabel.setContents(StringUtil.asHTML(	"- Common samples are samples found in both the uploaded pedigree file and the database samples associated to this study (Axiom.calls.txt)."+
																	"\n- Samples not found in pedigree are samples associated to the study for which a line doesn't exist in the pedigree file."+
																	"\n- Samples not found in database are samples described in pedigree and have not been associated to this study."+
																	"\n- Please note that samples in pedigree file which are not found in the \"Axiom.calls.txt\" will have their genotypes set to 0 \nand that samples associated with the study and not found in the pedigree file will simply be ignored."+
																	"\n- If you have made changes to pedigree, you can reload this page by clicking on the goto button next to \"check samples\".",true));
					
					HLayout buttonsLayout = new HLayout(20);
					buttonsLayout.setAutoHeight();
					buttonsLayout.setAutoWidth();
					
					IButton validateButton = new IButton("Validate", new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							pedigreeService.validatePedigree(studyName, userID, new AsyncCallback<Boolean>() {

								@Override
								public void onFailure(Throwable caught) {
									SC.warn("Cannot validate samples in pedigree !");
								}

								@Override
								public void onSuccess(Boolean result) {
									if (result!=null && result){
										SC.say("Samples validated !");
										pedWorkflow.getSamplesLine().setStatusImgOk();
										pedWorkflow.getSamplesLine().getGotoImg().disable();
										pedWorkflow.getIndividualsLine().getGotoImg().enable();
										tabPane.getMember(1).destroy();
									}else{
										SC.warn("Cannot validate samples in pedigree !");
									}
								}
							});
						}
					});
					validateButton.setIcon("icons/Apply.png");
					validateButton.setAutoFit(true);
					
					IButton modifyPedigreeButton = new IButton("Modify lines in pedigree", new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							new PedigreeWindow(studyName, userID);
						}
					});
					modifyPedigreeButton.setIcon("icons/Create.png");
					modifyPedigreeButton.setAutoFit(true);
					
					//not in use for the moment!!
					//addNewLineButton.setDisabled(true);
					
					IButton uploadNewFileButton = new IButton("Upload new pedigree", new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							tabPane.getMember(1).destroy();
							tabPane.addMember(constructUploadLayout(),1);
						}
					});
					uploadNewFileButton.setIcon("icons/Upload.png");
					uploadNewFileButton.setAutoFit(true);
					
					buttonsLayout.addMember(validateButton);
					buttonsLayout.addMember(modifyPedigreeButton);
					buttonsLayout.addMember(uploadNewFileButton);
					
					GenericVstack commonSamplesStack = new GenericVstack();
					GenericVstack notInPedStack = new GenericVstack();
					GenericVstack notInStudyStack = new GenericVstack();
					
					commonSamplesStack.addHeaderLabel("Common samples:");
					commonSamplesStack.addLabel(result.get("commonSamples").size()+" samples");
					for (String s : result.get("commonSamples")) {
						commonSamplesStack.addLabel(s);
					}
					
					notInPedStack.addHeaderLabel("Samples not found in pedigree:");
					notInPedStack.addLabel(result.get("isNotInPed").size()+" samples");
					for (String s : result.get("isNotInPed")) {
						notInPedStack.addLabel(s);
					}
					
					notInStudyStack.addHeaderLabel("Samples not found in database for study:");
					notInStudyStack.addLabel(result.get("isNotInStudy").size()+" samples");
					for (String s : result.get("isNotInStudy")) {
						notInStudyStack.addLabel(s);
					}
					
					HLayout hlayout = new HLayout(10);
					hlayout.setAutoHeight();
					hlayout.setAutoWidth();
					
					hlayout.addMember(commonSamplesStack);
					hlayout.addMember(notInPedStack);
					hlayout.addMember(notInStudyStack);
					
					layout.addMember(explanationLabel);
					layout.addMember(buttonsLayout);
					layout.addMember(hlayout);
				}
			}
		});
		
		return layout;
	}
	
	private VLayout constructCheckIndividualsLayout(){
		final VLayout layout = new VLayout(10);
		layout.setAutoHeight();
		layout.setAutoWidth();
		layout.setBorder(borderStyle);
		layout.setLayoutMargin(10);
		layout.setDefaultLayoutAlign(Alignment.CENTER);
		
		final Img loadingGif = new Img("loadingStar.gif",40,40);
		layout.addMember(loadingGif);
		
		pedigreeService.checkIndividualsInPedigree(studyName, userID, new AsyncCallback<Map<String,List<String>>>() {

			@Override
			public void onFailure(Throwable caught) {
				layout.removeMember(loadingGif);
				SC.warn("Cannot check individuals in pedigree !");
			}

			@Override
			public void onSuccess(Map<String,List<String>> result) {
				layout.removeMember(loadingGif);
				if (result.get("maleButMother").isEmpty() && result.get("femaleButFather").isEmpty() && result.get("indNotDescribed").isEmpty()){
					SC.say("Individuals are all correctly described !");
					pedigreeService.validatePedigree(studyName, userID, new AsyncCallback<Boolean>() {

						@Override
						public void onFailure(Throwable caught) {
							SC.warn("Cannot validate samples in pedigree !");
						}

						@Override
						public void onSuccess(Boolean result) {
							if (result!=null && result){
								SC.say("Samples validated !");
								pedWorkflow.getIndividualsLine().setStatusImgOk();
								pedWorkflow.getIndividualsLine().getGotoImg().disable();
								pedWorkflow.getSexLine().getGotoImg().enable();
								tabPane.getMember(1).destroy();
							}else{
								SC.warn("Cannot validate samples in pedigree !");
							}
						}
					});
				}else{
					Label explanationLabel = new Label();
					explanationLabel.setAutoHeight();
					explanationLabel.setAutoWidth();
					explanationLabel.setOverflow(Overflow.VISIBLE);
					explanationLabel.setContents(StringUtil.asHTML(	"- \"Male but mother\" describe individuals that are listed as men but appear as mother for another individual."+
																	"\n Either change the gender of this individual or remove them as mother for other individuals."+
																	"\n- \"Female but father\" describe individuals that are listed as female but appear as father for another individual."+
																	"\n Either change the gender of this individual or remove them as father for other individuals."+
																	"\n- Individual not described list individuals for which parents (father, mother or both) are not described as individuals."+
																	"\n Add new lines in pedigree to describe such parents."+
																	"\n- If you have made changes to pedigree, you can reload this page by clicking on the goto button next to \"check individuals\".",true));
					
					HLayout buttonsLayout = new HLayout(20);
					buttonsLayout.setAutoHeight();
					buttonsLayout.setAutoWidth();
					
					IButton modifyPedigreeButton = new IButton("Modify lines in pedigree", new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							new PedigreeWindow(studyName, userID);
						}
					});
					modifyPedigreeButton.setIcon("icons/Create.png");
					modifyPedigreeButton.setAutoFit(true);
					
					IButton uploadNewFileButton = new IButton("Upload new pedigree", new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							tabPane.getMember(1).destroy();
							tabPane.addMember(constructUploadLayout(),1);
						}
					});
					uploadNewFileButton.setIcon("icons/Upload.png");
					uploadNewFileButton.setAutoFit(true);
					
					buttonsLayout.addMember(modifyPedigreeButton);
					buttonsLayout.addMember(uploadNewFileButton);
					
					GenericVstack maleButMotherStack = new GenericVstack();
					GenericVstack femaleButFatherStack = new GenericVstack();
					GenericVstack indNotDescribedStack = new GenericVstack();
					
					maleButMotherStack.addHeaderLabel("Male but mother:");
					for (String s : result.get("maleButMother")) {
						maleButMotherStack.addLabel(s);
					}
					
					femaleButFatherStack.addHeaderLabel("Female but father:");
					for (String s : result.get("femaleButFather")) {
						femaleButFatherStack.addLabel(s);
					}
					
					indNotDescribedStack.addHeaderLabel("Individual not described:");
					for (String s : result.get("indNotDescribed")) {
						indNotDescribedStack.addLabel(s);
					}
					
					HLayout hlayout = new HLayout(10);
					hlayout.setAutoHeight();
					hlayout.setAutoWidth();
					
					hlayout.addMember(maleButMotherStack);
					hlayout.addMember(femaleButFatherStack);
					hlayout.addMember(indNotDescribedStack);
					
					layout.addMember(explanationLabel);
					layout.addMember(buttonsLayout);
					layout.addMember(hlayout);
				}
			}
		});
		
		return layout;
	}
	
	private VLayout constructCheckSexLayout(){
		final VLayout layout = new VLayout(10);
		layout.setAutoHeight();
		layout.setAutoWidth();
		layout.setBorder(borderStyle);
		layout.setLayoutMargin(10);
		layout.setDefaultLayoutAlign(Alignment.CENTER);
		
		final Img loadingGif = new Img("loadingStar.gif",40,40);
		layout.addMember(loadingGif);
		
		pedigreeService.checkGendersInPedigree(studyName, userID, new AsyncCallback<Map<String,Map<String,String>>>() {

			@Override
			public void onFailure(Throwable caught) {
				layout.removeMember(loadingGif);
				SC.warn("Cannot check genders in pedigree !");
			}

			@Override
			public void onSuccess(Map<String,Map<String,String>> result) {
				layout.removeMember(loadingGif);
				if (result.isEmpty()){
					pedigreeService.checkIndividualsInPedigree(studyName, userID, new AsyncCallback<Map<String,List<String>>>() {

						@Override
						public void onFailure(Throwable caught) {
							SC.warn("Cannot check individuals in pedigree");
						}

						@Override
						public void onSuccess(Map<String, List<String>> result) {
							if (result.get("maleButMother").isEmpty() && result.get("femaleButFather").isEmpty() && result.get("indNotDescribed").isEmpty()){
								SC.say("Genders are all correctly described !");
								pedigreeService.validatePedigree(studyName, userID, new AsyncCallback<Boolean>() {
	
									@Override
									public void onFailure(Throwable caught) {
										SC.warn("Cannot validate pedigree !");
									}
	
									@Override
									public void onSuccess(Boolean result) {
										if (result!=null && result){
											SC.say("Samples validated !");
											pedWorkflow.getSexLine().setStatusImgOk();
											pedWorkflow.getSexLine().getGotoImg().disable();
											pedWorkflow.getStatusLine().getGotoImg().enable();
											tabPane.getMember(1).destroy();
										}else{
											SC.warn("Cannot validate pedigree !");
										}
									}
								});
							}else{
								SC.warn("Changes made to pedigree show incompatibilities in step above");
								pedigreeService.invalidatePedigree(studyName, userID, new AsyncCallback<Boolean>() {

									@Override
									public void onFailure(Throwable caught) {
										SC.warn("Cannot invalidate pedigree !");
									}

									@Override
									public void onSuccess(Boolean result) {
										if (result!=null && result){
											pedWorkflow.getIndividualsLine().setStatusImgNo();
											pedWorkflow.getIndividualsLine().getGotoImg().enable();
											pedWorkflow.getSexLine().getGotoImg().disable();
											tabPane.getMember(1).destroy();
										}else{
											SC.warn("Cannot invalidate pedigree !");
										}
									}
								});
							}
						}
					});
					
				}else{
					Label explanationLabel = new Label();
					explanationLabel.setAutoHeight();
					explanationLabel.setAutoWidth();
					explanationLabel.setOverflow(Overflow.VISIBLE);
					explanationLabel.setContents(StringUtil.asHTML(	"- This section shows the differences of genders between pedigree and computed gender during genotyping"+
																	"\n- In order to have a pedigree as complete as possible, please change in pedigree file the following:"+
																	"\n  * any individual for which the gender is unknown in pedigree and known in the computed gender,"+
																	"\n  * any individual for which the gender is different (male to female or vice versa) between pedigree and computed gender",true));
					
					HLayout buttonsLayout = new HLayout(20);
					buttonsLayout.setAutoHeight();
					buttonsLayout.setAutoWidth();
					
					IButton modifyPedigreeButton = new IButton("Modify lines in pedigree", new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							new PedigreeWindow(studyName, userID);
						}
					});
					modifyPedigreeButton.setIcon("icons/Create.png");
					modifyPedigreeButton.setAutoFit(true);
					
					IButton uploadNewFileButton = new IButton("Upload new pedigree", new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							tabPane.getMember(1).destroy();
							tabPane.addMember(constructUploadLayout(),1);
						}
					});
					uploadNewFileButton.setIcon("icons/Upload.png");
					uploadNewFileButton.setAutoFit(true);
					
					//buttonsLayout.addMember(validateButton);
					buttonsLayout.addMember(modifyPedigreeButton);
					buttonsLayout.addMember(uploadNewFileButton);
					
					GenericVstack expectedGenderStack = new GenericVstack();
					
					expectedGenderStack.addHeaderLabel("Individual - Pedigree gender - Computed gender");
					for (String ind : result.keySet()) {
						expectedGenderStack.addLabel(ind+" - "+result.get(ind).get("pedigreeGender")+" - "+result.get(ind).get("computedGender"));
					}
					
					layout.addMember(explanationLabel);
					layout.addMember(buttonsLayout);
					layout.addMember(expectedGenderStack);
				}
			}
		});
		
		return layout;
	}
	
	private VLayout constructCheckStatusLayout(){
		final VLayout layout = new VLayout(10);
		layout.setAutoHeight();
		layout.setAutoWidth();
		layout.setBorder(borderStyle);
		layout.setLayoutMargin(10);
		layout.setDefaultLayoutAlign(Alignment.CENTER);
		
		/*final Img loadingGif = new Img("loadingStar.gif",40,40);
		layout.addMember(loadingGif);*/
		
		HLayout buttonsLayout = new HLayout(20);
		buttonsLayout.setAutoHeight();
		buttonsLayout.setAutoWidth();
		
		IButton validateButton = new IButton("Validate", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				pedigreeService.validatePedigree(studyName, userID, new AsyncCallback<Boolean>() {

					@Override
					public void onFailure(Throwable caught) {
						SC.warn("Cannot validate status in pedigree !");
					}

					@Override
					public void onSuccess(Boolean result) {
						if (result!=null && result){
							SC.say("Status validated !");
							pedWorkflow.getStatusLine().setStatusImgOk();
							pedWorkflow.getStatusLine().getGotoImg().disable();
							pedWorkflow.getFinalLine().getGotoImg().enable();
							tabPane.getMember(1).destroy();
						}else{
							SC.warn("Cannot validate status in pedigree !");
						}
					}
				});
			}
		});
		validateButton.setIcon("icons/Apply.png");
		validateButton.setAutoFit(true);
		
		IButton uploadNewFileButton = new IButton("Upload new pedigree", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				tabPane.getMember(1).destroy();
				tabPane.addMember(constructUploadLayout(),1);
			}
		});
		uploadNewFileButton.setIcon("icons/Upload.png");
		uploadNewFileButton.setAutoFit(true);
		
		buttonsLayout.addMember(validateButton);
		buttonsLayout.addMember(uploadNewFileButton);
		
		final HLayout hlayout = new HLayout(20);
		hlayout.setAutoHeight();
		hlayout.setAutoWidth();
		
		final PedigreeVstack affectedStack = new PedigreeVstack();
		final PedigreeVstack unaffectedStack = new PedigreeVstack();
		final PedigreeVstack unknownStack = new PedigreeVstack();
		affectedStack.addHeaderLabel("Affected individuals");
		unaffectedStack.addHeaderLabel("Unaffected individuals");
		unknownStack.addHeaderLabel("Unknown status");
		
		affectedStack.addGrid();
		final Criteria affCriteria = new Criteria();
		affCriteria.addCriteria("study_name", studyName);
		affCriteria.addCriteria("user_id", String.valueOf(userID));
		affCriteria.addCriteria("status", "affected");
		affectedStack.getGrid().fetchData(affCriteria);
		
		unaffectedStack.addGrid();
		final Criteria unaffCriteria = new Criteria();
		unaffCriteria.addCriteria("study_name", studyName);
		unaffCriteria.addCriteria("user_id", String.valueOf(userID));
		unaffCriteria.addCriteria("status", "unaffected");
		unaffectedStack.getGrid().fetchData(unaffCriteria);
		
		unknownStack.addGrid();
		final Criteria unknownCriteria = new Criteria();
		unknownCriteria.addCriteria("study_name", studyName);
		unknownCriteria.addCriteria("user_id", String.valueOf(userID));
		unknownCriteria.addCriteria("status", "unknown");
		unknownStack.getGrid().fetchData(unknownCriteria);
		
		affectedStack.getGrid().addDataArrivedHandler(new DataArrivedHandler() {
			
			@Override
			public void onDataArrived(DataArrivedEvent event) {
				if (affectedStack.getMember(2)!=null){
					affectedStack.getMember(2).destroy();
				}
				affectedStack.addLabel(affectedStack.getGrid().getTotalRows()+" individuals");
			}
		});
		
		unaffectedStack.getGrid().addDataArrivedHandler(new DataArrivedHandler() {
			
			@Override
			public void onDataArrived(DataArrivedEvent event) {
				if (unaffectedStack.getMember(2)!=null){
					unaffectedStack.getMember(2).destroy();
				}
				unaffectedStack.addLabel(unaffectedStack.getGrid().getTotalRows()+" individuals");
			}
		});
		
		unknownStack.getGrid().addDataArrivedHandler(new DataArrivedHandler() {
			
			@Override
			public void onDataArrived(DataArrivedEvent event) {
				if (unknownStack.getMember(2)!=null){
					unknownStack.getMember(2).destroy();
				}
				unknownStack.addLabel(unknownStack.getGrid().getTotalRows()+" individuals");
			}
		});
		
		List<PedigreeListGrid> pedGrids = new ArrayList<PedigreeListGrid>();
		pedGrids.add(affectedStack.getGrid());
		pedGrids.add(unaffectedStack.getGrid());
		pedGrids.add(unknownStack.getGrid());
		
		for (PedigreeListGrid grid : pedGrids) {
			grid.setAutoSaveEdits(true);
			for(ListGridField field : grid.getFields()){
				field.setCanEdit(false);
			}
			grid.getField("status").setCanEdit(true);
			grid.addEditCompleteHandler(new EditCompleteHandler() {
				
				@Override
				public void onEditComplete(EditCompleteEvent event) {
					int newStatus = Integer.parseInt(event.getNewValues().get("status").toString());
					int oldStatus = event.getOldRecord().getAttributeAsInt("status");
					
					if (oldStatus==0){
						unknownStack.getGrid().invalidateCache();
					}else if (oldStatus==1){
						unaffectedStack.getGrid().invalidateCache();
					}else if (oldStatus==2){
						affectedStack.getGrid().invalidateCache();
					}
					
					if (newStatus==0){
						unknownStack.getGrid().invalidateCache();
					}else if (newStatus==1){
						unaffectedStack.getGrid().invalidateCache();
					}else if (newStatus==2){
						affectedStack.getGrid().invalidateCache();
					}
				}
			});
		}
		
		hlayout.addMember(affectedStack);
		hlayout.addMember(unaffectedStack);
		hlayout.addMember(unknownStack);
		
		layout.addMember(buttonsLayout);
		layout.addMember(hlayout);
		
		return layout;
	}
	
	private VLayout constructFinalizeLayout(){
		final VLayout layout = new VLayout(10);
		layout.setAutoHeight();
		layout.setAutoWidth();
		layout.setBorder(borderStyle);
		layout.setLayoutMargin(10);
		layout.setDefaultLayoutAlign(Alignment.CENTER);
		
		final Img loadingGif = new Img("loadingStar.gif",40,40);
		
		HLayout buttonsLayout = new HLayout(20);
		buttonsLayout.setAutoHeight();
		buttonsLayout.setAutoWidth();
		
		IButton validateButton = new IButton("Validate", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				pedigreeService.validatePedigree(studyName, userID, new AsyncCallback<Boolean>() {

					@Override
					public void onFailure(Throwable caught) {
						SC.warn("Cannot build final pedigree !");
					}

					@Override
					public void onSuccess(Boolean result) {
						if (result!=null && result){
							SC.say("Pedigree validated !");
							//pedWorkflow.getFinalLine().setStatusImgOk();
							//pedWorkflow.getFinalLine().getGotoImg().disable();
							//pedWorkflow.destroy();
							//tabPane.getMember(1).destroy();
							//tabPane.getMember(0).destroy();
							for (Canvas c : tabPane.getMembers()) {
								c.destroy();
							}
							tabPane.addMember(showPedigreeInStudy());
						}else{
							SC.warn("Cannot build final pedigree !");
						}
					}
				});
			}
		});
		validateButton.setIcon("icons/Apply.png");
		validateButton.setAutoFit(true);
		
		IButton visualizeButton = new IButton("Visualize pedigree", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				layout.addMember(loadingGif);
				pedigreeService.visualizePedigree(studyName,userID, new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable caught) {
						SC.warn("Cannot create pedigree image !!");
					}

					@Override
					public void onSuccess(String fileName) {
						layout.removeMember(loadingGif);
						if (fileName!=null && !fileName.isEmpty()){
							com.google.gwt.user.client.Window.open(GWT.getModuleBaseURL() + "fileProvider?file="+fileName, "_self", "");	
						}else{
							SC.warn("Cannot create pedigree image !!");
						}
					}
				});
			}
		});
		visualizeButton.setIcon("workflows/pedigree.png");
		visualizeButton.setAutoFit(true);
		visualizeButton.disable();
		visualizeButton.setPrompt("You cannot visualize pedigree if this study is \"case-control\"");
		if (studyType.equals("case-control")){
			visualizeButton.disable();
		}
		
		IButton uploadNewFileButton = new IButton("Upload new pedigree", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				tabPane.getMember(1).destroy();
				tabPane.addMember(constructUploadLayout(),1);
			}
		});
		uploadNewFileButton.setIcon("icons/Upload.png");
		uploadNewFileButton.setAutoFit(true);
		
		buttonsLayout.addMember(validateButton);
		buttonsLayout.addMember(visualizeButton);
		buttonsLayout.addMember(uploadNewFileButton);
		
		layout.addMember(buttonsLayout);
		
		return layout;
	}
	
	private VLayout showPedigreeInStudy(){
		final VLayout layout = new VLayout(10);
		layout.setAutoHeight();
		layout.setAutoWidth();
		layout.setBorder(borderStyle);
		layout.setLayoutMargin(10);
		layout.setDefaultLayoutAlign(Alignment.CENTER);
		
		IButton visualizeButton = new IButton("Visualize pedigree", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				pedigreeService.visualizePedigree(studyName,userID, new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable caught) {
						SC.warn("Cannot create pedigree image !!");
					}

					@Override
					public void onSuccess(String fileName) {
						if (fileName!=null && !fileName.isEmpty()){
							com.google.gwt.user.client.Window.open(GWT.getModuleBaseURL() + "fileProvider?file="+fileName, "_self", "");	
						}else{
							SC.warn("Cannot create pedigree image !!");
						}
					}
				});
			}
		});
		visualizeButton.setIcon("workflows/pedigree.png");
		visualizeButton.setAutoFit(true);
		visualizeButton.setPrompt("You cannot visualize pedigree if this study is \"case-control\"");
		if (studyType.equals("case-control")){
			visualizeButton.disable();
		}
		
		PedigreeVstack pedStack = new PedigreeVstack();
		pedStack.addHeaderLabel("Final pedigree");
		pedStack.addGrid();
		Criteria criteria = new Criteria();
		criteria.addCriteria("study_name", studyName);
		//criteria.addCriteria("user_id", String.valueOf(userID));
		pedStack.getGrid().setCanEdit(false);
		pedStack.getGrid().fetchData(criteria);
		
		layout.addMember(visualizeButton);
		layout.addMember(pedStack);
		
		return layout;
	}
	
	private class PedigreeWorkflow extends VLayout{
		
		PedigreeWorkflowLine upload = new PedigreeWorkflowLine("Upload file", "Clic to upload a pedigree file.  Beware that uploading a new file will overwrite previous pedigree files.");
		PedigreeWorkflowLine samples = new PedigreeWorkflowLine("Check samples", "Clic to check if samples in pedigree file match samples chosen for study");
		PedigreeWorkflowLine individual = new PedigreeWorkflowLine("Check individuals", "Clic to check if all individuals in pedigree file are described");
		PedigreeWorkflowLine sex = new PedigreeWorkflowLine("Check sex", "Clic to check if sex of individuals in pedigree file match");
		PedigreeWorkflowLine status = new PedigreeWorkflowLine("Check status", "Clic to check affected status of samples in the study");
		PedigreeWorkflowLine finalPed = new PedigreeWorkflowLine("Build pedigree", "Clic to finalize the pedigree file for this study");
		
		public PedigreeWorkflow(){
			setMembersMargin(10);
			setBorder(borderStyle);
			setLayoutMargin(10);
			setAutoHeight();
			setAutoWidth();
			
			setMembers(upload,samples,individual,sex,status,finalPed);
		}
		
		public PedigreeWorkflowLine getUploadLine(){
			return upload;
		}
		
		public PedigreeWorkflowLine getSamplesLine(){
			return samples;
		}
	
		public PedigreeWorkflowLine getIndividualsLine(){
			return individual;
		}
		
		public PedigreeWorkflowLine getSexLine(){
			return sex;
		}
		
		public PedigreeWorkflowLine getStatusLine(){
			return status;
		}
		
		public PedigreeWorkflowLine getFinalLine(){
			return finalPed;
		}
		
		public void setAllStatusImgsNo(){
			upload.setStatusImgNo();
			samples.setStatusImgNo();
			individual.setStatusImgNo();
			sex.setStatusImgNo();
			status.setStatusImgNo();
			finalPed.setStatusImgNo();
		}
		
		public void setAllGotoImgsDisabled(){
			upload.getGotoImg().disable();
			samples.getGotoImg().disable();
			individual.getGotoImg().disable();
			sex.getGotoImg().disable();
			status.getGotoImg().disable();
			finalPed.getGotoImg().disable();
		}
		
		private class PedigreeWorkflowLine extends HLayout{
			
			private Label label;
			private Img gotoImg;
			private Img statusImg;
			private final String statusFalse = "icons/No.png";
			private final String statusRight = "icons/OK.png";
			private final String gotoImgGo = "icons/Go.png";
			
			public PedigreeWorkflowLine(String title, String prompt){
				setMembersMargin(10);
				setAutoHeight();
				setAutoWidth();
				setDefaultLayoutAlign(Alignment.LEFT);
				
				label = new Label();
				label.setOverflow(Overflow.VISIBLE);
				label.setAutoHeight();
				label.setWidth(130);
				label.setStyleName("textTitle");
				label.setContents(StringUtil.asHTML(title,true));
				
				gotoImg = new Img(gotoImgGo,16,16);
				gotoImg.setPrompt(prompt);
				gotoImg.setCursor(Cursor.HAND);
				gotoImg.setDisabledCursor(Cursor.NOT_ALLOWED);
				
				statusImg = new Img(statusFalse,16,16);
				statusImg.setPrompt("Status");
				
				setMembers(label,gotoImg,statusImg);
			}
			
			public Img getGotoImg(){
				return gotoImg;
			}
			
			public void setStatusImgNo(){
				statusImg.setSrc(statusFalse);
				statusImg.redraw();
			}
			
			public void setStatusImgOk(){
				statusImg.setSrc(statusRight);
				statusImg.redraw();
			}
		}
	}
}
