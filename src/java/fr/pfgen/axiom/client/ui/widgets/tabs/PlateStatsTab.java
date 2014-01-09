package fr.pfgen.axiom.client.ui.widgets.tabs;

import java.util.List;
import java.util.Map;

import fr.pfgen.axiom.client.datasources.PlatesDS;
import fr.pfgen.axiom.client.services.SamplesQCService;
import fr.pfgen.axiom.client.services.SamplesQCServiceAsync;
import fr.pfgen.axiom.client.services.SamplesService;
import fr.pfgen.axiom.client.services.SamplesServiceAsync;
import fr.pfgen.axiom.client.ui.widgets.MainArea;
import fr.pfgen.axiom.client.ui.widgets.grids.listGrids.PlatesListGrid;
import fr.pfgen.axiom.client.ui.widgets.grids.listGrids.QCListGrid;
import fr.pfgen.axiom.client.ui.widgets.vstacks.GenericVstack;
import fr.pfgen.axiom.client.ui.widgets.vstacks.Upload;
import fr.pfgen.axiom.client.ui.widgets.vstacks.UploadListener;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.ComboBoxItem;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.VStack;
import com.smartgwt.client.widgets.menu.IMenuButton;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.MenuItemSeparator;
import com.smartgwt.client.widgets.menu.events.ClickHandler;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;

public class PlateStatsTab {

	private final SamplesServiceAsync samplesService = GWT.create(SamplesService.class);
	private final SamplesQCServiceAsync samplesQCService = GWT.create(SamplesQCService.class);
	
	public PlateStatsTab(final String plateName, final String tabID){
		VLayout vlayout = new VLayout(15);
		vlayout.setWidth("80%");
		vlayout.setDefaultLayoutAlign(Alignment.LEFT);
		
		final HLayout hLayout = new HLayout(10);
		hLayout.setAutoHeight();
		hLayout.setAutoWidth();
		hLayout.setDefaultLayoutAlign(Alignment.LEFT);
		
		final Menu statsMenu = new Menu();
		
		MenuItemSeparator separator = new MenuItemSeparator();
		
		MenuItem statsMenuItem = new MenuItem();
		statsMenuItem.setTitle("Statistics");
		statsMenuItem.setIcon("icons/Bar-chart.png");
		
		statsMenuItem.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(MenuItemClickEvent event) {
				if (hLayout.getMember(tabID+"_stats")!=null){
					hLayout.getMember(tabID+"_stats").destroy();
				}else{
					hLayout.addMember(constructProjectStats(plateName,tabID));
				}
			}
		});
		
		MenuItem samplesQCMenuItem = new MenuItem();
		samplesQCMenuItem.setTitle("QC grid");
		samplesQCMenuItem.setIcon("icons/32.png");
		
		samplesQCMenuItem.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(MenuItemClickEvent event) {
				if (hLayout.getMember(tabID+"_QCgrid")!=null){
					hLayout.getMember(tabID+"_QCgrid").destroy();
				}else{
					hLayout.addMember(constructQCListgrid(plateName,tabID));
				}
			}
		});
		
		MenuItem userQcMenuItem = new MenuItem();
		userQcMenuItem.setTitle("Manage QC");
		userQcMenuItem.setIcon("icons/Pinion.png");
		
		userQcMenuItem.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(MenuItemClickEvent event) {
				if (hLayout.getMember(tabID+"_manageQC")!=null){
					hLayout.getMember(tabID+"_manageQC").destroy();
				}else{
					hLayout.addMember(constructQcManagementLayout(plateName, tabID));
				}
			}
		});
		
		statsMenu.setItems(statsMenuItem,separator,userQcMenuItem,separator,samplesQCMenuItem);
		
		final IMenuButton statsButton = new IMenuButton(plateName);
		statsButton.setMenu(statsMenu);
		statsButton.setIconSpacing(20);
		statsButton.setIconAlign("left");
		statsButton.setShowDisabledIcon(false);
		statsButton.setOverflow(Overflow.VISIBLE);
		statsButton.setAutoWidth();
		
		hLayout.addMember(statsButton);
		
		vlayout.addMember(hLayout);

		/*
		 * Add layout to mainArea tab
		 */
		MainArea.addTabToTopTabset("Stats : "+plateName,tabID, vlayout, true);
	}
	
	private HLayout constructQcManagementLayout(final String plateName, String tabID) {
		final HLayout hlayout = new HLayout(10);
		hlayout.setID(tabID+"_manageQC");
		
		final GenericVstack manageVStack = new GenericVstack();
		manageVStack.setLayoutAlign(Alignment.LEFT);
		manageVStack.setDefaultLayoutAlign(Alignment.CENTER);
		manageVStack.setOverflow(Overflow.VISIBLE);
		manageVStack.setShowEdges(true);
		manageVStack.setMembersMargin(25);  
		manageVStack.setLayoutMargin(10);
		manageVStack.setAutoWidth();
		manageVStack.setAutoHeight();
		
		manageVStack.addHeaderLabel("Manage QC");
		
		IButton uploadButton = new IButton("Upload file", new com.smartgwt.client.widgets.events.ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (hlayout.getMember(1)!=null){
					hlayout.getMember(1).destroy();
				}
				hlayout.addMember(constructUploadLayout(plateName));
			}
		});
		
		uploadButton.setAutoFit(true);
		uploadButton.setIcon("icons/Upload.png");
		uploadButton.setPrompt("Upload a tab delimited file containing sample informations for this plate");
		
		IButton userParamButton = new IButton("View QC params", new com.smartgwt.client.widgets.events.ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (hlayout.getMember(1)!=null){
					hlayout.getMember(1).destroy();
				}
				hlayout.addMember(constructUserParamsLayout());
			}
		});
		
		userParamButton.setAutoFit(true);
		userParamButton.setIcon("icons/Combo-box.png");
		userParamButton.setPrompt("Visualize existing QC parameters in database");
		
		IButton downloadGraph = new IButton("Download graph", new com.smartgwt.client.widgets.events.ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (hlayout.getMember(1)!=null){
					hlayout.getMember(1).destroy();
				}
				hlayout.addMember(constructDownloadQcLayout(plateName));
			}
		});
		
		downloadGraph.setAutoFit(true);
		downloadGraph.setIcon("icons/Download.png");
		downloadGraph.setPrompt("Choose the QC parameters for the graph and download");
	
		manageVStack.addMember(uploadButton);
		manageVStack.addMember(userParamButton);
		manageVStack.addMember(downloadGraph);
		
		hlayout.addMember(manageVStack);
		
		return hlayout;
	}
	
	private VLayout constructDownloadQcLayout(final String plateName){
		final VLayout layout = new VLayout(15);
		layout.setAutoHeight();
		layout.setAutoWidth();
		layout.setBorder("2px solid grey");
		layout.setLayoutMargin(10);
		layout.setDefaultLayoutAlign(Alignment.CENTER);
		
		DynamicForm form = new DynamicForm();
		final ComboBoxItem xAxisParamSelect = new ComboBoxItem("x-axis&nbsp;param");
		
		final ComboBoxItem yAxisParamSelect = new ComboBoxItem("y-axis&nbsp;param");
		
		
		samplesQCService.getAllQcParams(new AsyncCallback<List<String>>() {

			@Override
			public void onFailure(Throwable caught) {
				SC.warn("Failed to fetch QC params");
			}

			@Override
			public void onSuccess(List<String> result) {
				if (result.isEmpty()){
					xAxisParamSelect.setEmptyDisplayValue("empty...");
					yAxisParamSelect.setEmptyDisplayValue("empty...");
				}else{
					xAxisParamSelect.setValueMap(result.toArray(new String[result.size()]));
					yAxisParamSelect.setValueMap(result.toArray(new String[result.size()]));
				}
			}
		});

		form.setFields(xAxisParamSelect,yAxisParamSelect);
		
		final Img loadingGif = new Img("loadingStar.gif",40,40);
		
		final IButton downloadGraph = new IButton("Download Graph");
		
		downloadGraph.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				downloadGraph.disable();
				layout.addMember(loadingGif);
				String xAxis = xAxisParamSelect.getValueAsString();
				if (xAxis==null || xAxis.isEmpty()){
					SC.warn("Please select a parameter for X-axis");
					return;
				}
				String yAxis = yAxisParamSelect.getValueAsString();
				if (yAxis==null || yAxis.isEmpty()){
					SC.warn("Please select a parameter for Y-axis");
					return;
				}
				samplesQCService.makeQCGraphForPlate(plateName, xAxis, yAxis, new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable caught) {
						SC.warn("Failed to make QC graph");
						layout.removeMember(loadingGif);
						downloadGraph.enable();
					}
					

					@Override
					public void onSuccess(String result) {
						layout.removeMember(loadingGif);
						downloadGraph.enable();
						if (result!=null && !result.isEmpty()){
							com.google.gwt.user.client.Window.open(GWT.getModuleBaseURL() + "fileProvider?file="+result, "_self", "");
						}else{
							SC.warn("Failed to make QC graph");
						}
					}
				});
			}
		});
		
		downloadGraph.setAutoFit(true);
		downloadGraph.setShowDisabledIcon(false);
		downloadGraph.setIcon("icons/32.png");
		
		final IButton downloadTsv = new IButton("Download Table");
		
		downloadTsv.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				downloadTsv.disable();
				layout.addMember(loadingGif);
				String xAxis = xAxisParamSelect.getValueAsString();
				if (xAxis==null || xAxis.isEmpty()){
					SC.warn("Please select a parameter for X-axis");
					return;
				}
				String yAxis = yAxisParamSelect.getValueAsString();
				if (yAxis==null || yAxis.isEmpty()){
					SC.warn("Please select a parameter for Y-axis");
					return;
				}
				samplesQCService.makeQCTsvForPlate(plateName, xAxis, yAxis, new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable caught) {
						SC.warn("Failed to download QC table");
						layout.removeMember(loadingGif);
						downloadTsv.enable();
					}

					@Override
					public void onSuccess(String result) {
						layout.removeMember(loadingGif);
						downloadTsv.enable();
						if (result!=null && !result.isEmpty()){
							com.google.gwt.user.client.Window.open(GWT.getModuleBaseURL() + "fileProvider?file="+result, "_self", "");
						}else{
							SC.warn("Failed to download QC table");
						}
					}
				});
			}
		});
		
		downloadTsv.setAutoFit(true);
		downloadTsv.setShowDisabledIcon(false);
		downloadTsv.setIcon("icons/List.png");
		
		layout.addMember(form);
		
		HLayout buttonLayout = new HLayout(10);
		buttonLayout.setAutoHeight();
		buttonLayout.setAutoWidth();
		
		buttonLayout.addMember(downloadGraph);
		buttonLayout.addMember(downloadTsv);
		
		layout.addMember(buttonLayout);
		
		return layout;
		
	}
	
	private VLayout constructUserParamsLayout(){
		final VLayout layout = new VLayout(10);
		layout.setAutoHeight();
		layout.setAutoWidth();
		layout.setBorder("2px solid grey");
		layout.setLayoutMargin(10);
		layout.setDefaultLayoutAlign(Alignment.CENTER);
		
		DynamicForm form = new DynamicForm();
		final ComboBoxItem aptParamsSelect = new ComboBoxItem("Apt&nbsp;QC&nbsp;params");
		
		samplesQCService.getQcParams(new AsyncCallback<List<String>>() {

			@Override
			public void onFailure(Throwable caught) {
				SC.warn("Failed to fetch user QC params");
			}

			@Override
			public void onSuccess(List<String> result) {
				aptParamsSelect.setValueMap(result.toArray(new String[result.size()]));
			}
		});
		
		final ComboBoxItem userParamsSelect = new ComboBoxItem("User&nbsp;QC&nbsp;params");
		
		samplesQCService.getUserParams(new AsyncCallback<List<String>>() {

			@Override
			public void onFailure(Throwable caught) {
				SC.warn("Failed to fetch user QC params");
			}

			@Override
			public void onSuccess(List<String> result) {
				if (result.isEmpty()){
					userParamsSelect.setEmptyDisplayValue("empty...");
				}else{
					userParamsSelect.setValueMap(result.toArray(new String[result.size()]));
				}
			}
		});
		
		form.setFields(aptParamsSelect,userParamsSelect);
		layout.addMember(form);
		
		return layout;
	}
	
	private VLayout constructUploadLayout(final String plateName){
		final VLayout layout = new VLayout(10);
		layout.setAutoHeight();
		layout.setAutoWidth();
		layout.setBorder("2px solid grey");
		layout.setLayoutMargin(10);
		layout.setDefaultLayoutAlign(Alignment.CENTER);
		
		final Upload uploadStack = new Upload();
		uploadStack.setHeaderLabel("Upload a file");
		uploadStack.setFileItemTitle("qcFile");
		uploadStack.setAction(GWT.getModuleBaseURL() + "fileUploader");
		
		final Img loadingGif = new Img("loadingStar.gif",40,40);
		
		uploadStack.getUploadButton().addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {
			
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
			public void uploadComplete(final String filePath) {
				if (filePath.startsWith("Error")){
					uploadStack.getUploadButton().enable();
					uploadStack.getStack().removeMember(loadingGif);
					SC.warn(filePath);
				}else{
					samplesQCService.checkUploadedQCForPlate(plateName, filePath, new AsyncCallback<Map<String, String>>() {

						@Override
						public void onFailure(Throwable caught) {
							SC.warn("Cannot check uploaded file on server");
							uploadStack.removeChild(loadingGif);
						}

						@Override
						public void onSuccess(Map<String, String> result) {
							if (result==null){
								SC.warn("Cannot check uploaded file on server");
								uploadStack.removeChild(loadingGif);
								return;
							}
							if (!result.isEmpty()){
								if (result.containsKey("No column named 'sampleName'")){
									SC.warn("No column named 'sampleName'");
									uploadStack.getUploadButton().enable();
									uploadStack.getStack().removeMember(loadingGif);
									return;
									
								}
								StringBuilder sb = new StringBuilder();
								for (String line : result.keySet()) {
									sb.append(line+" :<br>    "+result.get(line)+"<br>");
								}
								sb.append("Continue anyway?");
								SC.ask(sb.toString(), new BooleanCallback() {
									
									@Override
									public void execute(Boolean value) {
										if (value==null || !value){
											uploadStack.getUploadButton().enable();
											uploadStack.getStack().removeMember(loadingGif);
											return;
										}
									}
								});
							}
							samplesQCService.addUserQCForPlate(plateName, filePath, new AsyncCallback<String>() {

								@Override
								public void onFailure(Throwable caught) {
									SC.warn("Cannot add QC to database");
									uploadStack.removeChild(loadingGif);
								}

								@Override
								public void onSuccess(String result) {
									if (result.startsWith("Error:")){
										SC.warn(result);
									}else{
										SC.say(result);
									}
									layout.destroy();
								}
							});
						}
					});
				}
			}
		});
		
		Label explanation = new Label();
		explanation.setWidth(300);
		explanation.setContents("The&nbsp;uploaded&nbsp;file&nbsp;must&nbsp;contain&nbsp;a&nbsp;column&nbsp;named&nbsp;\"sampleName\"&nbsp;listing&nbsp;the&nbsp;names&nbsp;of&nbsp;the&nbsp;samples&nbsp;in&nbsp;the&nbsp;plate."+
								"<br>The&nbsp;other&nbsp;titles&nbsp;of&nbsp;the&nbsp;columns&nbsp;must&nbsp;be&nbsp;the&nbsp;parameter&nbsp;name&nbsp;(existing&nbsp;or&nbsp;not)."+
								"<br>Please&nbsp;note&nbsp;that&nbsp;if&nbsp;you&nbsp;enter&nbsp;a&nbsp;non-existing&nbsp;name,&nbsp;a&nbsp;QC&nbsp;parameter&nbsp;will&nbsp;be&nbsp;created&nbsp;and&nbsp;values&nbsp;of&nbsp;the&nbsp;column&nbsp;inserted&nbsp;in&nbsp;the&nbsp;database&nbsp;for&nbsp;the&nbsp;samples.");
		uploadStack.addLabel(explanation);
		
		layout.addMember(uploadStack);
		
		return layout;
	}

	private VStack constructProjectStats(final String plateName, final String tabID){
		final VStack vStack = new VStack(10);
		vStack.setID(tabID+"_stats");
		vStack.setLayoutAlign(Alignment.LEFT);
		vStack.setOverflow(Overflow.VISIBLE);
		vStack.setShowEdges(true);
		vStack.setMembersMargin(5);  
		vStack.setLayoutMargin(10);
		vStack.setAutoWidth();
		vStack.setAutoHeight();
		
		Label stackTitle = new Label();
		stackTitle.setOverflow(Overflow.VISIBLE);
		stackTitle.setAutoHeight();
		stackTitle.setAutoWidth();
		stackTitle.setContents("Statistics");
		stackTitle.setStyleName("textTitle");
		
		//Listgrid for plate details
		PlatesListGrid plateListGrid = new PlatesListGrid();
		plateListGrid.setDataSource(new PlatesDS());
		Criteria criteria = new Criteria();
		criteria.addCriteria("plate_name", plateName);
		plateListGrid.fetchData(criteria);
		
		//Label for sample number
		final Label sampleNumber = new Label();
		sampleNumber.setOverflow(Overflow.VISIBLE);
		sampleNumber.setAutoHeight();
		sampleNumber.setAutoWidth();
		samplesService.nbSamplesInPlate(plateName, new AsyncCallback<Integer>() {
		
			@Override
			public void onSuccess(Integer result) {
				sampleNumber.setContents("Number&nbsp;of&nbsp;samples&nbsp;in&nbsp;plate:&nbsp;"+result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				SC.warn("Can't retrieve number of samples in plate "+plateName);
			}
		});
		
		//label for sample number without qc
		final Label sampleWoQCNumber = new Label();
		sampleWoQCNumber.setOverflow(Overflow.VISIBLE);
		sampleWoQCNumber.setAutoHeight();
		sampleWoQCNumber.setAutoWidth();
		samplesService.nbSamplesInPlateWithoutQC(plateName, new AsyncCallback<Integer>() {
		
			@Override
			public void onSuccess(Integer result) {
				sampleWoQCNumber.setContents("Number&nbsp;of&nbsp;samples&nbsp;without&nbsp;QC&nbsp;in&nbsp;plate:&nbsp;"+result);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				SC.warn("Can't retrieve number of samples without QC in plate "+plateName);
			}
		});
		
		vStack.addMember(stackTitle);
		vStack.addMember(plateListGrid);
		vStack.addMember(sampleNumber);
		vStack.addMember(sampleWoQCNumber);
		
		return vStack;
	}
	
	private VStack constructQCListgrid(final String plateName, final String tabID){
		final VStack vStack = new VStack(10);
		vStack.setID(tabID+"_QCgrid");
		vStack.setLayoutAlign(Alignment.LEFT);
		vStack.setAlign(Alignment.LEFT);
		vStack.setOverflow(Overflow.VISIBLE);
		vStack.setShowEdges(true);
		vStack.setMembersMargin(5);  
		vStack.setLayoutMargin(10);
		vStack.setAutoWidth();
		vStack.setAutoHeight();
		
		Label gridTitle = new Label();
		gridTitle.setOverflow(Overflow.VISIBLE);
		gridTitle.setAutoHeight();
		gridTitle.setAutoWidth();
		gridTitle.setContents("Samples&nbsp;QC");
		gridTitle.setStyleName("textTitle");
		
		DynamicForm form = new DynamicForm();
		form.setAutoHeight(); 
		form.setAutoWidth();
		form.setPadding(5);
		form.setLayoutAlign(Alignment.LEFT);
		
		final SelectItem tableContents = new SelectItem();
		tableContents.setValueMap("Minimum table", "Condensed table", "User QC table", "Full table");
		tableContents.setName("Show");
		tableContents.setDefaultValue("Minimum table");
		
		form.setFields(tableContents);
		
		final QCListGrid qcListgrid = new QCListGrid();
		Criteria criteria = new Criteria();
		criteria.addCriteria("plate_name", plateName);
		qcListgrid.fetchData(criteria);
		
		tableContents.addChangedHandler(new ChangedHandler() {
			
			@Override
			public void onChanged(ChangedEvent event) {
				if (tableContents.getValueAsString().equals("Condensed table")){
					qcListgrid.setCondensedTable();
				}else if (tableContents.getValueAsString().equals("Full table")){
					qcListgrid.setFullTable();
				}else if (tableContents.getValueAsString().equals("Minimum table")){
					qcListgrid.setMinimumTable();
				}else if (tableContents.getValueAsString().equals("User QC table")){
					qcListgrid.setUserQcTable();
				}
			}
		});
		
		vStack.addMember(gridTitle);
		vStack.addMember(form);
		vStack.addMember(qcListgrid);
		
		return vStack;
	}
}