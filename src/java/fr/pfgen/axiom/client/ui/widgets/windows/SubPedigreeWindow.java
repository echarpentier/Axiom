package fr.pfgen.axiom.client.ui.widgets.windows;

import java.util.List;
import java.util.Map;

import fr.pfgen.axiom.client.services.PedigreeService;
import fr.pfgen.axiom.client.services.PedigreeServiceAsync;
import fr.pfgen.axiom.client.ui.widgets.vstacks.Upload;
import fr.pfgen.axiom.client.ui.widgets.vstacks.UploadListener;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.form.validator.LengthRangeValidator;
import com.smartgwt.client.widgets.form.validator.RegExpValidator;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

public class SubPedigreeWindow extends Dialog {

	private final PedigreeServiceAsync pedigreeService = GWT.create(PedigreeService.class);
	private final String BORDER = "1px solid #C0C0C0";
	
	public SubPedigreeWindow(final String studyName, final SelectItem chooseSubPop){
		setAutoSize(true);
		setAutoCenter(true);
		setIsModal(true);
		setShowModalMask(true);
		setTitle("Manage subpopulations");
		
		addCloseClickHandler(new CloseClickHandler() {
			
			@Override
			public void onCloseClick(CloseClientEvent event) {
				destroy();	
			}
		});
		
		HLayout winLayout = new HLayout(10);
		winLayout.setAutoHeight();
		winLayout.setAutoWidth();
		
		final Upload uploadStack = new Upload();
		uploadStack.setFileItemTitle("file");
		uploadStack.setAction(GWT.getModuleBaseURL() + "fileUploader");
		//uploadStack.setBorder(BORDER);
		
		DynamicForm addForm = new DynamicForm();
		addForm.setPadding(5);
		addForm.setLayoutAlign(Alignment.CENTER);
		addForm.setLayoutAlign(VerticalAlignment.TOP);
		addForm.setAutoHeight();
		addForm.setAutoWidth();
		addForm.setAlign(Alignment.CENTER);
		
		final TextItem nameItem = new TextItem();
		nameItem.setTitle("name");
		nameItem.setKeyPressFilter("^[a-zA-Z0-9_-]+$");
		RegExpValidator val = new RegExpValidator("^[a-zA-Z0-9_-]+$");
		val.setErrorMessage("Only alphanumeric characters allowed");
		LengthRangeValidator val2 = new LengthRangeValidator();
		val2.setMin(3);
		val2.setMax(50);
		val2.setErrorMessage("3-50 characters");
		
		nameItem.setValidators(val, val2);
		
		addForm.setFields(nameItem);
		
		uploadStack.getStack().addMember(addForm, 0);
		uploadStack.setHeaderLabel("Upload a subpopulation file");
		
		final Img loadingGif = new Img("loadingStar.gif",40,40);
		
		uploadStack.getUploadButton().setPrompt("The uploaded file must be of pedigree type. (.fam or .txt)");
		
		uploadStack.getUploadButton().disable();
		
		nameItem.addChangedHandler(new ChangedHandler() {
			
			@Override
			public void onChanged(ChangedEvent event) {
				if (nameItem.validate()){
					uploadStack.getUploadButton().enable();
				}else{
					uploadStack.getUploadButton().disable();
				}
			}
		});
		
		uploadStack.getUploadButton().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				/*if (nameItem.getValueAsString()==null || nameItem.getValueAsString().isEmpty()){
					SC.warn("Please choose a name for subpopulation");
				}*/
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
					pedigreeService.checkUploadedSubPopulation(studyName, nameItem.getValueAsString(), filePath, new AsyncCallback<Map<String, String>>() {

						@Override
						public void onFailure(Throwable caught) {
							uploadStack.getUploadButton().enable();
							uploadStack.getStack().removeMember(loadingGif);
							SC.warn("Cannot check uploaded subpopulation");
						}

						@Override
						public void onSuccess(Map<String, String> result) {
							uploadStack.getUploadButton().enable();
							uploadStack.getStack().removeMember(loadingGif);
							if (result==null){
								SC.warn("Cannot check uploaded pedigree");
							}else{
								if (result.isEmpty()){
									SC.say("Subpopulation successfully created");
									updateSelectItem(studyName, chooseSubPop);
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
		
		VLayout uploadLayout = new VLayout();
		uploadLayout.setAutoHeight();
		uploadLayout.setAutoWidth();
		uploadLayout.setPadding(20);
		uploadLayout.setBorder(BORDER);
		
		uploadLayout.addMember(uploadStack);
		
		winLayout.addMember(uploadLayout);
		
		addItem(winLayout);
		
		show();
	}
	
	private void updateSelectItem(String studyName, final SelectItem chooseSubPop){
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
	}
}
