package fr.pfgen.axiom.client.ui.widgets.tabs;

import java.util.List;

import fr.pfgen.axiom.client.services.AnnotationFilesService;
import fr.pfgen.axiom.client.services.AnnotationFilesServiceAsync;
import fr.pfgen.axiom.client.services.PedigreeService;
import fr.pfgen.axiom.client.services.PedigreeServiceAsync;
import fr.pfgen.axiom.client.services.SNPListsService;
import fr.pfgen.axiom.client.services.SNPListsServiceAsync;
import fr.pfgen.axiom.client.services.StudiesService;
import fr.pfgen.axiom.client.services.StudiesServiceAsync;
import fr.pfgen.axiom.client.ui.widgets.windows.ModalProgressWindow;
import fr.pfgen.axiom.client.ui.widgets.windows.SNPListsWindow;
import fr.pfgen.axiom.client.ui.widgets.windows.SubPedigreeWindow;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.util.StringUtil;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.HTMLFlow;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.VStack;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.events.TabSelectedEvent;
import com.smartgwt.client.widgets.tab.events.TabSelectedHandler;

public class StudyCGTab extends Tab{

	private final StudiesServiceAsync studiesService = GWT.create(StudiesService.class);
	private final SNPListsServiceAsync snpListsService = GWT.create(SNPListsService.class);
	private final PedigreeServiceAsync pedigreeService = GWT.create(PedigreeService.class);
	private final AnnotationFilesServiceAsync annotationService = GWT.create(AnnotationFilesService.class);
	private String studyName;
	private VLayout tabPane;
	
	public StudyCGTab(final String studyName){
		this.studyName = studyName;
		tabPane = new VLayout(10);
		tabPane.setDefaultLayoutAlign(Alignment.LEFT);
		
		setPrompt("Cluster Graphs");
		setTitle("&nbsp;"+Canvas.imgHTML("icons/Clusters.ico", 16, 16));
		
		addTabSelectedHandler(new TabSelectedHandler() {
			
			@Override
			public void onTabSelected(TabSelectedEvent event) {
				for (Canvas c : tabPane.getMembers()){
					c.destroy();
				}
				studiesService.checkGenoSamplesForStudy(studyName, new AsyncCallback<Boolean>() {

					@Override
					public void onFailure(Throwable caught) {
						SC.warn("Cannot check if samples are associated to study");
					}

					@Override
					public void onSuccess(Boolean result) {
						if (result != null){
							if (result){
								tabPane.addMember(constructClusterGraphLayout());
							}else{
								tabPane.addMember(new HTMLFlow("No&nbsp;samples&nbsp;associated&nbsp;to&nbsp;study.<br>Please&nbsp;choose&nbsp;samples&nbsp;for&nbsp;study!"));
							}
						}else{
							SC.warn("Cannot check if samples are associated to study");
						}
					}
				});
			}
		});
		
		setPane(tabPane);
	}
	
	private VStack constructClusterGraphLayout(){
		final VStack stack = new VStack(10);
		stack.setLayoutAlign(Alignment.LEFT);
		stack.setOverflow(Overflow.VISIBLE);
		stack.setShowEdges(true);
		stack.setMembersMargin(5);  
		stack.setLayoutMargin(10);
		stack.setAutoWidth();
		stack.setAutoHeight();
		
		Label stackTitle = new Label();
		stackTitle.setOverflow(Overflow.VISIBLE);
		stackTitle.setAutoHeight();
		stackTitle.setAutoWidth();
		stackTitle.setContents(StringUtil.asHTML("Cluster graphs",true));
		stackTitle.setStyleName("textTitle");
		
		DynamicForm form = new DynamicForm();
		final SelectItem chooseSNPList = new SelectItem();
		chooseSNPList.setTitle("SNP&nbsp;Lists");
		final SelectItem chooseSubPop = new SelectItem();
		chooseSubPop.setTitle("Subpopulation");
		final SelectItem annotItem = new SelectItem("annotItem", "Annotation File");
		annotItem.setWidth(220);
		
		snpListsService.getListNames(new AsyncCallback<List<String>>() {

			@Override
			public void onFailure(Throwable caught) {
				SC.warn("Cannot fetch SNP lists from server.");
			}

			@Override
			public void onSuccess(List<String> result) {
				chooseSNPList.setValueMap(result.toArray(new String[result.size()]));
			}
		});
		
		pedigreeService.getSubPopNames(studyName, new AsyncCallback<List<String>>() {

			@Override
			public void onFailure(Throwable caught) {
				SC.warn("Cannot fetch subpopulations for this study on server !!");
			}

			@Override
			public void onSuccess(List<String> result) {
				chooseSubPop.setValueMap(result.toArray(new String[result.size()]));
			}
		});
		
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
		
		final CheckboxItem colorPlatesCheckBox = new CheckboxItem();
		colorPlatesCheckBox.setTitle("Color&nbsp;by&nbsp;plates");
		
		final CheckboxItem showEllipseCheckBox = new CheckboxItem();
		showEllipseCheckBox.setTitle("Show&nbsp;Prior&nbsp;Ellipses");
		
		form.setFields(chooseSNPList,chooseSubPop,annotItem,colorPlatesCheckBox,showEllipseCheckBox);
		
		VLayout buttonLayout = new VLayout(8);
		buttonLayout.setAutoHeight();
		buttonLayout.setAutoWidth();
		buttonLayout.setDefaultLayoutAlign(Alignment.CENTER);
		buttonLayout.setLayoutAlign(Alignment.CENTER);
		
		IButton getButton = new IButton("Get graph");
		getButton.setIcon("icons/Download.png");
		getButton.setAutoFit(true);
		
		getButton.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				String listName = chooseSNPList.getValueAsString();
				String subPopName = chooseSubPop.getValueAsString();
				String annotFileName = annotItem.getValueAsString();
				boolean platesColorCheck = colorPlatesCheckBox.getValueAsBoolean();
				boolean showEllipseCheck = showEllipseCheckBox.getValueAsBoolean();
				if (listName==null || listName.isEmpty()){
					SC.warn("Select a snp list above");
					return;
				}
				if (annotFileName==null || annotFileName.isEmpty()){
					SC.warn("Please select an annotation file to use");
					return;
				}
				final ModalProgressWindow win = new ModalProgressWindow();
				win.setTitle("Cluster graph");
				win.setLoadingBar();
				win.getProgressLabel().setContents(StringUtil.asHTML("Getting cluster graph...",true));
				win.show();
				snpListsService.getGraphForStudy(studyName, listName, platesColorCheck, showEllipseCheck, subPopName, annotFileName, new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable caught) {
						win.destroy();
						SC.warn("Could not get cluster graph !");
					}

					@Override
					public void onSuccess(String result) {
						win.destroy();
						if (result!=null && !result.isEmpty()){
							com.google.gwt.user.client.Window.open(GWT.getModuleBaseURL() + "fileProvider?file="+result, "_self", "");
						}else{
							SC.warn("Could not get cluster graph !");
						}
					}
				});
			}
		});
		
		IButton manageListButton = new IButton("Manage lists");
		manageListButton.setIcon("icons/Create.png");
		manageListButton.setAutoFit(true);
		
		manageListButton.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				new SNPListsWindow(chooseSNPList);
			}
		});
		
		IButton subPopButton = new IButton("Subpopulations");
		subPopButton.setIcon("workflows/User_Group-64x64.png");
		subPopButton.setIconSize(16);
		subPopButton.setAutoFit(true);
		
		subPopButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				new SubPedigreeWindow(studyName, chooseSubPop);
			}
		});
		
		buttonLayout.addMember(getButton);
		buttonLayout.addMember(manageListButton);
		buttonLayout.addMember(subPopButton);
		
		stack.addMember(stackTitle);
		stack.addMember(form);
		stack.addMember(buttonLayout);
		
		return stack;
	}
}
