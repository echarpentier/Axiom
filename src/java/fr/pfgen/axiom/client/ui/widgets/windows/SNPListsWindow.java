package fr.pfgen.axiom.client.ui.widgets.windows;

import java.util.ArrayList;
import java.util.List;

import fr.pfgen.axiom.client.services.SNPListsService;
import fr.pfgen.axiom.client.services.SNPListsServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.SelectItem;
import com.smartgwt.client.widgets.form.fields.TextAreaItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.layout.HLayout;

public class SNPListsWindow extends Dialog {
	
	private SNPListsServiceAsync snpListsService = GWT.create(SNPListsService.class);
	private final String BORDER = "1px solid #C0C0C0";

	public SNPListsWindow(final SelectItem item){
		setAutoSize(true);
		setAutoCenter(true);
		setIsModal(true);
		setShowModalMask(true);
		setTitle("Manage SNP lists");
		
		addCloseClickHandler(new CloseClickHandler() {
			
			@Override
			public void onCloseClick(CloseClientEvent event) {
				destroy();	
			}
		});
		
		final DynamicForm addform = new DynamicForm();
		addform.setPadding(5);
		addform.setLayoutAlign(Alignment.CENTER);
		addform.setLayoutAlign(VerticalAlignment.TOP);
		addform.setIsGroup(true);
		addform.setBorder(BORDER);
		addform.setGroupTitle("Add list");
		
		final TextItem nameItem = new TextItem();
		nameItem.setTitle("name");
		nameItem.setKeyPressFilter("^[a-zA-Z0-9_-]+$");
		final TextAreaItem listItem = new TextAreaItem();
		listItem.setTitle("list");
		
		addform.setFields(nameItem,listItem);
		
		final DynamicForm removeform = new DynamicForm();
		removeform.setPadding(5);
		removeform.setLayoutAlign(Alignment.CENTER);
		removeform.setLayoutAlign(VerticalAlignment.TOP);
		removeform.setIsGroup(true);
		removeform.setBorder(BORDER);
		removeform.setGroupTitle("Remove list");
		
		final SelectItem chooseListForRemoval = new SelectItem();
		chooseListForRemoval.setName("choose&nbsp;list");
		
		removeform.setFields(chooseListForRemoval);
		
		final DynamicForm visualizeForm = new DynamicForm();
		visualizeForm.setPadding(5);
		visualizeForm.setLayoutAlign(Alignment.CENTER);
		visualizeForm.setLayoutAlign(VerticalAlignment.TOP);
		visualizeForm.setIsGroup(true);
		visualizeForm.setBorder(BORDER);
		visualizeForm.setGroupTitle("Show list");
		
		final SelectItem chooseListForVisualization = new SelectItem();
		chooseListForVisualization.setName("choose&nbsp;list");
		
		final TextAreaItem snpListVisu = new TextAreaItem();
		snpListVisu.setTitle("list");

		visualizeForm.setFields(chooseListForVisualization, snpListVisu);
		
		updateSelectItems(chooseListForRemoval, chooseListForVisualization);
		
		HLayout formsLayout = new HLayout(10);
		formsLayout.setAutoHeight();
		formsLayout.setAutoWidth();
		
		formsLayout.addMember(addform);
		formsLayout.addMember(removeform);
		formsLayout.addMember(visualizeForm);

		addItem(formsLayout);
		
		chooseListForVisualization.addChangedHandler(new ChangedHandler() {
			
			@Override
			public void onChanged(ChangedEvent event) {
				snpListsService.getSNPinList(chooseListForVisualization.getValueAsString(), new AsyncCallback<List<String>>() {

					@Override
					public void onFailure(Throwable caught) {
						SC.warn("Failed to fetch SNPs in list on server !!");
					}

					@Override
					public void onSuccess(List<String> result) {
						if (result==null || result.isEmpty()){
							SC.warn("Failed to fetch SNPs in list on server !!");
							return;
						}
						StringBuilder sb = new StringBuilder();
						for (String snp : result) {
							sb.append(snp+"\n");
						}
						snpListVisu.setValue(sb.toString());
					}
				});
			}
		});
		
		final IButton addListButton = new IButton("Create list", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (listItem.getValueAsString()==null){
					SC.warn("Please insert SNPs in the list !");
					return;
				}
				String[] snps = listItem.getValueAsString().split("\\n");
				List<String> snpList = new ArrayList<String>();
				for (String snp : snps) {
					if (!snp.isEmpty()){
						snpList.add(snp);
					}
				}
				snpListsService.createNewList(nameItem.getValueAsString(), snpList, new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable caught) {
						SC.warn("Cannot create snp list");
					}

					@Override
					public void onSuccess(String result) {
						if (result!=null){
							if (result.startsWith("Error:")){
								SC.warn(result);
							}else{
								SC.say(result);
								destroy();
								snpListsService.getListNames(new AsyncCallback<List<String>>() {

									@Override
									public void onFailure(Throwable caught) {
										SC.warn("Cannot fetch SNP lists from server.");
									}

									@Override
									public void onSuccess(List<String> result) {
										item.setValueMap(result.toArray(new String[result.size()]));
										updateSelectItems(chooseListForRemoval,chooseListForVisualization);
									}
								});
							}
						}else{
							SC.warn("Cannot create snp list");
						}
					}
				});
			}
		});
		
		addListButton.setIcon("icons/Create.png");
		
		final IButton removeButton = new IButton("Remove list", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				final String listName = chooseListForRemoval.getValueAsString();
				if (listName==null){
					SC.warn("Please select a SNP list to remove");
					return;
				}
				SC.ask("All cluster graphs for list \""+listName+"\" will be permanently removed.<br>Continue?", new BooleanCallback() {
					
					@Override
					public void execute(Boolean value) {
						if (value!=null && value){
							snpListsService.deleteSNPList(listName, new AsyncCallback<Boolean>() {

								@Override
								public void onFailure(Throwable caught) {
									SC.warn("Could not removed SNP list on server");
								}

								@Override
								public void onSuccess(Boolean result) {
									if (result!=null && result){
										SC.say(listName+" was successfully removed");
										updateSelectItems(chooseListForRemoval, chooseListForVisualization);
									}else{
										SC.warn("Could not remove SNP list on server");
									}
								}
							});
						}
					}
				});
			}
		});
		
		removeButton.setIcon("icons/Remove.png");
		
		final IButton cancelButton = new IButton("Cancel", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				destroy();
			}
		});
		
		cancelButton.setIcon("icons/Cancel.png");
		
		setToolbarButtons(addListButton,removeButton,cancelButton);
	
		show();
	}
	
	private void updateSelectItems(final SelectItem rem, final SelectItem vis){
		snpListsService.getListNames(new AsyncCallback<List<String>>() {

			@Override
			public void onFailure(Throwable caught) {
				
			}

			@Override
			public void onSuccess(List<String> result) {
				rem.setValueMap(result.toArray(new String[result.size()]));
				vis.setValueMap(result.toArray(new String[result.size()]));
			}
		});
	}
}
