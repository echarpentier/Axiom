package fr.pfgen.axiom.client.ui.widgets.tabs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fr.pfgen.axiom.client.services.AnnotationFilesService;
import fr.pfgen.axiom.client.services.AnnotationFilesServiceAsync;
import fr.pfgen.axiom.client.services.PedigreeService;
import fr.pfgen.axiom.client.services.PedigreeServiceAsync;
import fr.pfgen.axiom.client.services.StudiesService;
import fr.pfgen.axiom.client.services.StudiesServiceAsync;
import fr.pfgen.axiom.client.ui.widgets.windows.ModalProgressWindow;
import fr.pfgen.axiom.shared.records.GenotypingQCRecord;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.util.StringUtil;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.RadioGroupItem;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.events.TabSelectedEvent;
import com.smartgwt.client.widgets.tab.events.TabSelectedHandler;

public class StudyPlinkTab extends Tab{

	private final StudiesServiceAsync studiesService = GWT.create(StudiesService.class);
	private final PedigreeServiceAsync pedigreeService = GWT.create(PedigreeService.class);
	private final AnnotationFilesServiceAsync annotationService = GWT.create(AnnotationFilesService.class);
	private String studyName;
	private HLayout tabPane;
	
	public StudyPlinkTab(final String studyName) {
		this.studyName = studyName;
		tabPane = new HLayout(10);
		tabPane.setDefaultLayoutAlign(Alignment.LEFT);
		
		setPrompt("Plink files");
		setTitle("&nbsp;"+Canvas.imgHTML("workflows/plink_files.png",16,16));
		
		addTabSelectedHandler(new TabSelectedHandler() {
			
			@Override
			public void onTabSelected(TabSelectedEvent event) {
				for(Canvas c : tabPane.getMembers()){
					c.destroy();
				}
				studiesService.checkPlinkFilesInStudy(studyName, new AsyncCallback<Boolean>() {

					@Override
					public void onFailure(Throwable caught) {
						SC.warn("Cannot check if Plink files exist for this study");
					}

					@Override
					public void onSuccess(Boolean result) {
						if (result!=null && result){
							tabPane.addMember(constructPlinkFilesGenerated());
						}else{
							tabPane.addMember(constructGeneratePlinkFiles());
						}
					}
				});
			}
		});
		
		setPane(tabPane);
	}

	private HLayout constructChooseDuplicationsLayout(final Map<String, List<GenotypingQCRecord>> dupList){
		HLayout hlayout = new HLayout(10);
		hlayout.setAutoHeight();
		hlayout.setAutoWidth();
		
		VLayout vlayout = new VLayout(10);
		vlayout.setAutoHeight();
		vlayout.setAutoWidth();
		vlayout.setBorder("2px solid grey");
		vlayout.setPadding(10);
		
		Label explanation = new Label();
		explanation.setWidth(300);
		explanation.setContents("Individual ID in pedigree matches to more than one sample in calls." +
				"  Please choose which sample in calls will be used for the individual." +
				"  Samples are described by the plate they are in and their call rate.");
		explanation.setAutoHeight();
		
		vlayout.addMember(explanation);
		
		Label label = new Label();
		label.setAutoHeight();
		label.setAutoWidth();
		label.addStyleName("textTitle");
		label.setContents(StringUtil.asHTML("Choose samples:",true));
		
		vlayout.addMember(label);
		
		DynamicForm sampleDF = new DynamicForm();
		final List<RadioGroupItem> radios = new ArrayList<RadioGroupItem>();
		for (String sampleName : dupList.keySet()) {
			RadioGroupItem radioGroupItem = new RadioGroupItem();
			radioGroupItem.setTitle(sampleName);
			
			LinkedHashMap<String, String> valueMap = new LinkedHashMap<String, String>();
			for (int i = 0; i < dupList.get(sampleName).size(); i++) {
				GenotypingQCRecord rec = dupList.get(sampleName).get(i);
				valueMap.put(String.valueOf(i), rec.getSampleName()+"&nbsp;"+rec.getPlateName()+"&nbsp;"+rec.getQcMap().get("call_rate"));
			}
			radioGroupItem.setValueMap(valueMap);
			radioGroupItem.setDefaultValue(0);
			radios.add(radioGroupItem);
		}
		
		final SelectItem annotItem = new SelectItem("annotItem", "Annotation File");
		annotItem.setWidth(220);
		
		annotationService.getAnnotationFilesNames(new AsyncCallback<List<String>>() {

			@Override
			public void onFailure(Throwable caught) {
				SC.warn("Cannot fetch availables annotation files from server");
			}

			@Override
			public void onSuccess(List<String> result) {
				annotItem.setValueMap(result.toArray(new String[result.size()]));
			}
		});
		
		List<FormItem> L=new ArrayList<FormItem>(radios);
		L.add(annotItem);
		sampleDF.setFields(L.toArray(new FormItem[radios.size()]));
	
		vlayout.addMember(sampleDF);
		
		final IButton launchButton = new IButton("Generate Plink files");
		launchButton.setAutoFit(true);
		launchButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				String annotFileName = annotItem.getValueAsString();
				if (annotFileName==null || annotFileName.isEmpty()){
					SC.warn("Please select an annotation file to use");
					return;
				}
				launchButton.disable();
				Map<String, GenotypingQCRecord> chosen = new HashMap<String, GenotypingQCRecord>();
				for (RadioGroupItem radioGroupItem : radios) {
					String name = radioGroupItem.getTitle();
					GenotypingQCRecord rec = dupList.get(name).get(Integer.parseInt(radioGroupItem.getValueAsString()));
					chosen.put(name, rec);
				}
				final ModalProgressWindow win = new ModalProgressWindow();
				win.setTitle("Generating Plink files");
				win.setLoadingBar();
				win.show();
				
				studiesService.generatePlinkFilesForStudy(studyName, annotFileName, chosen, new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable caught) {
						launchButton.enable();
						SC.warn("Failed to generated plink files");
					}

					@Override
					public void onSuccess(String result) {
						final Timer timer = new Timer() {  
							@Override
							public void run() {  
								studiesService.Affy2PlinkProgress(studyName, new AsyncCallback<String>() {
									
									@Override
									public void onSuccess(String result) {
										if (result != null){
											if (result.endsWith("DONE<br>")){
												win.destroy();
												tabPane.getMember(0).destroy();
												tabPane.addMember(constructPlinkFilesGenerated());
												cancel();
											}else{
												win.getProgressLabel().setContents(result);
											}
										}
									}
									
									@Override
									public void onFailure(Throwable caught) {
										
									}
								});
							}
						};
						
						timer.scheduleRepeating(3000);
					}
				});
			}
		});
		hlayout.addMember(vlayout);
		hlayout.addMember(launchButton);
		
		return hlayout;
	}
	
	private HLayout constructGeneratePlinkFiles(){
		final HLayout layout = new HLayout();
		layout.setAutoHeight();
		layout.setAutoWidth();
		
		final VLayout vlayout = new VLayout(10);
		vlayout.setAutoHeight();
		vlayout.setAutoWidth();
		vlayout.setBorder("2px solid grey");
		vlayout.setPadding(10);
		
		final Img loadingGif = new Img("loadingStar.gif",40,40);
		
		layout.addMember(loadingGif);
		pedigreeService.checkDuplicateSamplesInCalls(studyName, new AsyncCallback<Map<String, List<GenotypingQCRecord>>>() {

			@Override
			public void onFailure(Throwable caught) {
				layout.removeMember(loadingGif);
				SC.warn("Cannot check for duplicate samples in calls");
			}

			@Override
			public void onSuccess(Map<String, List<GenotypingQCRecord>> dupList) {
				layout.removeMember(loadingGif);
				if (dupList.isEmpty()){
					final ModalProgressWindow win = new ModalProgressWindow();
					win.setLoadingBar();
					
					DynamicForm form = new DynamicForm();
					final SelectItem annotItem = new SelectItem("annotItem", "Annotation File");
					annotItem.setWidth(220);
					
					annotationService.getAnnotationFilesNames(new AsyncCallback<List<String>>() {

						@Override
						public void onFailure(Throwable caught) {
							SC.warn("Cannot fetch availables annotation files from server");
						}

						@Override
						public void onSuccess(List<String> result) {
							annotItem.setValueMap(result.toArray(new String[result.size()]));
						}
					});
					
					form.setFields(annotItem);
					
					Label label = new Label();
					label.setAutoHeight();
					label.setAutoWidth();
					label.addStyleName("clickable");
					label.setContents(StringUtil.asHTML("Clic here to generate Plink files for this study",true));
					label.addClickHandler(new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							String annotFileName = annotItem.getValueAsString();
							if (annotFileName==null || annotFileName.isEmpty()){
								SC.warn("Please select an annotation file to use");
								return;
							}
							win.show();
							studiesService.generatePlinkFilesForStudy(studyName, annotFileName, null, new AsyncCallback<String>() {

								@Override
								public void onFailure(Throwable caught) {
									layout.removeMember(loadingGif);
									SC.warn("Failed to generated plink files");
								}

								@Override
								public void onSuccess(String result) {
									final Timer timer = new Timer() {  
										@Override
										public void run() {  
											studiesService.Affy2PlinkProgress(studyName, new AsyncCallback<String>() {
												
												@Override
												public void onSuccess(String result) {
													if (result != null){
														if (result.endsWith("DONE<br>")){
															win.destroy();
															tabPane.getMember(0).destroy();
															tabPane.addMember(constructPlinkFilesGenerated());
															cancel();
														}else{
															win.getProgressLabel().setContents(result);
														}
													}
												}
												
												@Override
												public void onFailure(Throwable caught) {
													
												}
											});
										}
									};
									
									timer.scheduleRepeating(3000);
								}
							});
						}
					});
					vlayout.addMember(form);
					vlayout.addMember(label);
					layout.addMember(vlayout);
				}else{
					layout.addMember(constructChooseDuplicationsLayout(dupList));
				}
			}
		});
		
		return layout;
	}
	
	private VLayout constructPlinkFilesGenerated(){
		VLayout layout = new VLayout();
		layout.setAutoHeight();
		layout.setAutoWidth();
		
		Label label = new Label();
		label.setAutoHeight();
		label.setAutoWidth();
		label.setStyleName("textTitle");
		label.setContents(StringUtil.asHTML("Plink files have been generated for this study",true));
		
		layout.addMember(label);
		
		return layout;
	}
}
