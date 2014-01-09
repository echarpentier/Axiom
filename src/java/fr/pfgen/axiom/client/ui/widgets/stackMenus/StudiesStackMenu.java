package fr.pfgen.axiom.client.ui.widgets.stackMenus;

import fr.pfgen.axiom.client.Axiom;
import fr.pfgen.axiom.client.services.StudiesService;
import fr.pfgen.axiom.client.services.StudiesServiceAsync;
import fr.pfgen.axiom.client.ui.widgets.grids.treeGrids.StudiesTreeGrid;
import fr.pfgen.axiom.client.ui.widgets.tabs.StudyTab;
import fr.pfgen.axiom.shared.records.StudyRecord;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.RichTextEditor;
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
import com.smartgwt.client.widgets.form.fields.events.KeyPressEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyPressHandler;
import com.smartgwt.client.widgets.form.validator.RegExpValidator;
import com.smartgwt.client.widgets.layout.VLayout;

public class StudiesStackMenu extends VLayout {

    private final StudiesServiceAsync studiesService = GWT.create(StudiesService.class);

    public StudiesStackMenu() {

        this.setMembersMargin(10);
        this.setLayoutTopMargin(20);

        final IButton newStudyButton = new IButton();
        newStudyButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                createNewStudyWindow();
            }
        });

        newStudyButton.setPrompt("Clic on this button to create a new study.");

        newStudyButton.setTitle("New Study");
        newStudyButton.setOverflow(Overflow.VISIBLE);
        newStudyButton.setAutoWidth();
        newStudyButton.setLayoutAlign(Alignment.CENTER);

        final StudiesTreeGrid studiesTreeGrid = new StudiesTreeGrid();
        studiesTreeGrid.setBorder("0px");

        this.addMember(newStudyButton);
        this.addMember(studiesTreeGrid);
    }

    private void createNewStudyWindow() {

        final Dialog newStudyWindow = new Dialog();
        newStudyWindow.setIsModal(true);
        newStudyWindow.setAutoCenter(true);
        newStudyWindow.setAutoSize(true);
        newStudyWindow.setTitle("New Study");
        newStudyWindow.setShowMinimizeButton(false);
        newStudyWindow.setShowModalMask(true);
        newStudyWindow.addCloseClickHandler(new CloseClickHandler() {
            @Override
            public void onCloseClick(CloseClientEvent event) {
                newStudyWindow.destroy();
            }
        });

        DynamicForm form = new DynamicForm();
        form.setAutoHeight();
        form.setAutoWidth();
        form.setPadding(5);
        form.setLayoutAlign(Alignment.CENTER);
        form.setGroupTitle("Study name");

        final TextItem nameTextItem = new TextItem("studyName", "Study&nbsp;Name");
        RegExpValidator regex = new RegExpValidator("^[a-zA-Z0-9_-]{3,25}$");
        regex.setErrorMessage("Field must contain only letters, digits, underscores and hyphens. Min 3 characters, max 25.");
        nameTextItem.setValidators(regex);
        nameTextItem.setRequired(true);
        nameTextItem.setValidateOnChange(true);

        final SelectItem typeItem = new SelectItem("studyType", "Study&nbsp;Type");
        typeItem.setValueMap("case-control", "family");
        typeItem.setRequired(true);
        
        final RichTextEditor descriptionEditor = new RichTextEditor();
        descriptionEditor.setHeight(200);
        descriptionEditor.setValue("enter description here...");
        descriptionEditor.setCanDragResize(true);
        descriptionEditor.setShowEdges(true);

        form.setFields(nameTextItem, typeItem);
        form.setAutoFocus(true);

        newStudyWindow.addItem(form);
        newStudyWindow.addItem(descriptionEditor);

        final IButton addButton = new IButton("Add", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final StudyRecord newStudy = new StudyRecord();
                newStudy.setStudyName(nameTextItem.getValueAsString());
                newStudy.setStudyType(typeItem.getValueAsString());
                newStudy.setUserID(Axiom.get().getUser().getUserID());
                if (!descriptionEditor.getValue().contains("enter description here...")){
                    newStudy.setDescription(descriptionEditor.getValue());
                }
                studiesService.addNewStudy(newStudy, new AsyncCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        if (result.startsWith("Error")) {
                            SC.warn(result);
                        } else {
                            SC.say(result);
                            newStudyWindow.destroy();
                            String studyName = newStudy.getStudyName();
                            String tabID = studyName.replaceAll("-", "_");
                            tabID = "study_" + tabID;
                            new StudyTab(studyName, tabID, typeItem.getValueAsString());
                            /*if (studyType.equals("family")){
                             new FamilyStudyTab(studyName, tabID);
                             }else if (studyType.equals("case-control")){
                             new CaseControlStudyTab(studyName, tabID);
                             }*/
                        }
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        SC.warn("Failed to add new study: \n" + caught);
                    }
                });
            }
        });
        addButton.disable();

        final IButton cancelButton = new IButton("Cancel", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                newStudyWindow.destroy();
            }
        });

        nameTextItem.addChangedHandler(new ChangedHandler() {
            @Override
            public void onChanged(ChangedEvent event) {
                if (nameTextItem.validate() && nameTextItem.validate()) {
                    addButton.enable();
                } else {
                    addButton.disable();
                }
            }
        });

        nameTextItem.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {

                if (event.getKeyName().equals("Enter") && nameTextItem.validate()) {
                    final StudyRecord newStudy = new StudyRecord();
                    newStudy.setStudyName(nameTextItem.getValueAsString());
                    newStudy.setStudyType(typeItem.getValueAsString());
                    studiesService.addNewStudy(newStudy, new AsyncCallback<String>() {
                        @Override
                        public void onSuccess(String result) {
                            if (result.startsWith("Error")) {
                                SC.warn(result);
                            } else {
                                SC.say(result);
                                newStudyWindow.destroy();
                                String studyName = newStudy.getStudyName();
                                String tabID = studyName.replaceAll("-", "_");
                                tabID = "study_" + tabID;
                                new StudyTab(studyName, tabID, typeItem.getValueAsString());
                                /*if (studyType.equals("family")){
                                 new FamilyStudyTab(studyName, tabID);
                                 }else if (studyType.equals("case-control")){
                                 new CaseControlStudyTab(studyName, tabID);
                                 }*/
                            }
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            SC.warn("Failed to add new study: \n" + caught);
                        }
                    });
                }
            }
        });

        newStudyWindow.setToolbarButtons(addButton, cancelButton);

        newStudyWindow.show();
    }
}
