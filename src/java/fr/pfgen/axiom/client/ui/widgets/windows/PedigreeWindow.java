package fr.pfgen.axiom.client.ui.widgets.windows;

import fr.pfgen.axiom.client.ui.widgets.grids.listGrids.PedigreeListGrid;

import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.util.StringUtil;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.RowEditorExitEvent;
import com.smartgwt.client.widgets.grid.events.RowEditorExitHandler;

public class PedigreeWindow extends Dialog{
	
	//private final StudiesServiceAsync studiesService = GWT.create(StudiesService.class);

	public PedigreeWindow(final String studyName, final int userID){
		setAutoSize(true);
		setTitle("Modify pedigree for study "+studyName);
		setShowMinimizeButton(false);
		setCanDragReposition(true);
		
		addCloseClickHandler(new CloseClickHandler() {
			
			@Override
			public void onCloseClick(CloseClientEvent event) {
				destroy();	
			}
		});
		
		Label explanationLabel = new Label();
		explanationLabel.setAutoHeight();
		explanationLabel.setAutoWidth();
		explanationLabel.setOverflow(Overflow.VISIBLE);
		explanationLabel.setContents(StringUtil.asHTML(	"sex: 1-male, 2-female, 0-unknown"+
														"\nstatus: 1-unaffected, 2-affected, 0-unknown"+
														"\nInserting a new line or modifying individual ID to"+
														"\n an existing ID will have no effect",true));

		explanationLabel.setMargin(10);
		addItem(explanationLabel);
		
		/*
		final DynamicForm form = new DynamicForm();
		
		final StaticTextItem headerLabel = new StaticTextItem();
		headerLabel.setTitle("Header");
		headerLabel.setDefaultValue("familyID&nbsp;&nbsp;individualID&nbsp;&nbsp;fatherID&nbsp;&nbsp;motherID&nbsp;&nbsp;sex&nbsp;&nbsp;status");
		
		final TextAreaItem textAreaItem = new TextAreaItem();
		textAreaItem.setWidth(450);
		textAreaItem.setHeight(200);
		textAreaItem.setTitle("Pedigree");
		textAreaItem.setPrompt("Please be sure to insert correct number of columns and only accepted characters when inserting a new line.  You can use <b>\"space\"</b> instead of <b>\"tab\"</b> since no spaces are allowed for the different fields.");
		textAreaItem.setHoverWidth(300);
		studiesService.getPedigreeContent(studyName, user, new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				SC.warn("Cannot get pedigree content");
			}

			@Override
			public void onSuccess(String result) {
				textAreaItem.setValue(result);
			}
		});
		
		form.setItems(headerLabel,textAreaItem);
		
		addItem(form);
		
		final Img loadingGif = new Img("loadingStar.gif",40,40);
		
		final IButton modifyButton = new IButton("Modify");
		modifyButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				modifyButton.disable();
				addItem(loadingGif);
				studiesService.applyChangesToPedigree(studyName,user, new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable caught) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onSuccess(String result) {
						// TODO Auto-generated method stub
						
					}
				});
			}
		});
		
		final IButton resetButton = new IButton("Reset", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				studiesService.getPedigreeContent(studyName, user, new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable caught) {
						SC.warn("Cannot get pedigree content");
					}

					@Override
					public void onSuccess(String result) {
						textAreaItem.setValue(result);		
					}
				});
			}
		});
		resetButton.setPrompt("Reset button will undo all changes made after last \"modify\" and accepted clic");
		
		final IButton cancelButton = new IButton("Cancel", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				destroy();
			}
		});
		
		setToolbarButtons(modifyButton,resetButton,cancelButton);*/
		
		final PedigreeListGrid pedGrid = new PedigreeListGrid();
		final Criteria criteria = new Criteria();
		criteria.addCriteria("study_name", studyName);
		criteria.addCriteria("user_id", String.valueOf(userID));
		pedGrid.fetchData(criteria);
		
		addItem(pedGrid);

		IButton editButton = new IButton("Edit New");
		editButton.setPrompt("Insert a new line in pedigree");
		editButton.addClickHandler(new ClickHandler() {  
			@Override
			public void onClick(ClickEvent event) {  
				pedGrid.startEditingNew();
			}  
		});
		
		/*IButton saveButton = new IButton("Save");
		saveButton.setPrompt("Save all edits to pedigree file");
		saveButton.addClickHandler(new ClickHandler() {  
			public void onClick(ClickEvent event) {
				int[] editedRowNumbers = pedGrid.getAllEditRows();
				for (int i : editedRowNumbers) {
					//Record rec = pedGrid.getEditedRecord(i);
					System.out.println(pedGrid.getEditedRecord(i).getAttribute("individual_id"));
					pedGrid.getEditedRecord(i).setAttribute("user_id", userID);
					pedGrid.getEditedRecord(i).setAttribute("study_name", studyName);
					//rec.setAttribute("user_id", userID);
					//rec.setAttribute("study_name", studyName);
					//if (rec.getAttributeAsInt("pedigree_id")!=null){
						//pedGrid.updateData(rec);
					//}else{
						//pedGrid.addData(rec);
					//}
					if (pedGrid.getRecordList().contains(rec)){
						pedGrid.updateData(rec);
					}else{
						pedGrid.addData(rec);
					}
					//pedGrid.updateData(rec);
				}
				pedGrid.saveAllEdits(new Function() {
					
					@Override
					public void execute() {
						pedGrid.invalidateCache();
						pedGrid.fetchData(criteria);
					}
				});
				//pedGrid.invalidateCache();
				//pedGrid.fetchData(criteria);
			}  
		}); */ 
		
		pedGrid.addRowEditorExitHandler(new RowEditorExitHandler() {

			@Override
			public void onRowEditorExit(final RowEditorExitEvent event) {
				Record gridRecord;
				Boolean ok = true;
				if (event.getRecord() != null) {
					gridRecord = event.getRecord();
					//This will be an update operations
					if (event.getNewValues().get("individual_id")!=null){
						for(ListGridRecord rec : pedGrid.getRecords()){
							if (rec.getAttributeAsInt("pedigree_id").equals(gridRecord.getAttributeAsInt("pedigree_id"))){ 
								continue;
							}
							if (event.getNewValues().get("individual_id").equals(rec.getAttributeAsString("individual_id"))){
								SC.warn("Individual ID \""+event.getNewValues().get("individual_id")+"\" already exists !");
								ok=false;
								return;
							}
						}
					}
				} else {
					gridRecord = new Record();
					//This will be a new record creation
					gridRecord.setAttribute("user_id", userID);
					gridRecord.setAttribute("study_name", studyName);
				}
				if (pedGrid.validateRow(event.getRowNum())) {
					for (Object attribute : event.getNewValues().keySet()) {
						//Here you will be able to see all the newly edited values
						gridRecord.setAttribute(String.valueOf(attribute),  event.getNewValues().get(attribute));
					}
					//Finally you will have a record with all unsaved values.Send it to server
					if (event.getRecord() != null){
						pedGrid.updateData(gridRecord);
					}else{
						ok = true;
						for(ListGridRecord rec : pedGrid.getRecords()){
							if (gridRecord.getAttributeAsString("individual_id").equals(rec.getAttributeAsString("individual_id"))){
								SC.warn("Individual ID \""+rec.getAttributeAsString("individual_id")+"\" already exists !");
								ok=false;
							}
						}
						if (ok){
							pedGrid.addData(gridRecord);
						}
					}
				}
			}
		});

		
		IButton removeButton = new IButton("Remove");
		removeButton.setPrompt("Remove the selected line from pedigree");
		removeButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				pedGrid.removeSelectedData();
			}
		});

		IButton discardButton = new IButton("Discard");  
		discardButton.setPrompt("Discard all edits (blue)");
		discardButton.addClickHandler(new ClickHandler() {  
			@Override
			public void onClick(ClickEvent event) {  
				pedGrid.discardAllEdits();  
			}  
		});  
		
		setToolbarButtons(editButton,removeButton,discardButton);
		show();
	}
}
