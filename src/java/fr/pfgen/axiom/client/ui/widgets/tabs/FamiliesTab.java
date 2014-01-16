/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.pfgen.axiom.client.ui.widgets.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.KeyPressEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyPressHandler;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.VStack;
import fr.pfgen.axiom.client.Axiom;
import fr.pfgen.axiom.client.services.SamplesService;
import fr.pfgen.axiom.client.services.SamplesServiceAsync;
import fr.pfgen.axiom.client.ui.widgets.MainArea;
import fr.pfgen.axiom.client.ui.widgets.grids.listGrids.FamiliesListGrid;
import java.util.Date;

/**
 *
 * @author eric
 */
public class FamiliesTab {

    private final SamplesServiceAsync samplesService = GWT.create(SamplesService.class);

    public FamiliesTab(String tabID) {

        VLayout vlayout = new VLayout(15);
        vlayout.setWidth("80%");
        vlayout.setDefaultLayoutAlign(Alignment.CENTER);

        final VStack vStack = new VStack();
        vStack.setShowEdges(true);
        vStack.setMembersMargin(5);
        vStack.setLayoutMargin(10);
        vStack.setAutoWidth();
        vStack.setAutoHeight();

        VLayout gridLayout = new VLayout(10);

        final Label gridTitle = new Label();
        gridTitle.setHeight(20);
        gridTitle.setContents("Families");
        gridTitle.setStyleName("textTitle");

        final FamiliesListGrid familiesGrid = new FamiliesListGrid();
        familiesGrid.setSelectionType(SelectionStyle.SINGLE);

        HLayout buttonLayout = new HLayout(20);

        IButton addButton = new IButton("Add family", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final Dialog newFamilyWindow = new Dialog();

                newFamilyWindow.setAutoSize(true);
                newFamilyWindow.setTitle("New family");
                newFamilyWindow.setShowMinimizeButton(false);
                newFamilyWindow.setIsModal(true);
                newFamilyWindow.setShowModalMask(true);
                newFamilyWindow.addCloseClickHandler(new CloseClickHandler() {
                    @Override
                    public void onCloseClick(CloseClientEvent event) {
                        newFamilyWindow.destroy();
                    }
                });
                DynamicForm form = new DynamicForm();
                form.setHeight100();
                form.setWidth100();
                form.setPadding(5);
                form.setLayoutAlign(VerticalAlignment.BOTTOM);
                final TextItem nameItem = new TextItem();
                nameItem.setTitle("Family name");
                nameItem.setRequired(true);
                nameItem.setKeyPressFilter("^[a-zA-Z0-9]+$");
                nameItem.setValidateOnChange(true);
                
                final TextItem propositusItem = new TextItem();
                propositusItem.setTitle("Propositus");
                propositusItem.setRequired(false);
                
                KeyPressHandler kphandler = new KeyPressHandler() {
                    @Override
                    public void onKeyPress(KeyPressEvent event) {
                        if (event.getKeyName().equals("Enter")) {
                            if (nameItem.getValueAsString() != null && !nameItem.getValueAsString().isEmpty()) {
                                ListGridRecord[] recordList = familiesGrid.getRecords();
                                int ok = 1;
                                for (ListGridRecord LGrecord : recordList) {
                                    if (LGrecord.getAttribute("family_name").equalsIgnoreCase(nameItem.getValueAsString())) {
                                        SC.warn("Family " + nameItem.getValueAsString() + " already exists !!");
                                        nameItem.clearValue();
                                        ok = 0;
                                    }
                                }
                                if (ok == 1) {
                                    ListGridRecord newFamilyRecord = new ListGridRecord();
                                    newFamilyRecord.setAttribute("family_name", nameItem.getValue());
                                    newFamilyRecord.setAttribute("user", Axiom.get().getUser().getFirstname() + " " + Axiom.get().getUser().getLastname());
                                    newFamilyRecord.setAttribute("family_id", 1);
                                    newFamilyRecord.setAttribute("created", new Date());
                                    newFamilyRecord.setAttribute("propositus", propositusItem.getValueAsString());
                                    familiesGrid.addData(newFamilyRecord);
                                    newFamilyWindow.destroy();
                                }
                            } else {
                                SC.warn("Insert family name");
                            }
                        }
                    }
                };
                nameItem.addKeyPressHandler(kphandler);
                final IButton windowAddButton = new IButton("Add");
                windowAddButton.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        if (nameItem.getValueAsString() != null && !nameItem.getValueAsString().isEmpty()) {
                                ListGridRecord[] recordList = familiesGrid.getRecords();
                                int ok = 1;
                                for (ListGridRecord LGrecord : recordList) {
                                    if (LGrecord.getAttribute("family_name").equalsIgnoreCase(nameItem.getValueAsString())) {
                                        SC.warn("Family " + nameItem.getValueAsString() + " already exists !!");
                                        nameItem.clearValue();
                                        ok = 0;
                                    }
                                }
                                if (ok == 1) {
                                    ListGridRecord newFamilyRecord = new ListGridRecord();
                                    newFamilyRecord.setAttribute("family_name", nameItem.getValue());
                                    newFamilyRecord.setAttribute("user", Axiom.get().getUser().getFirstname() + " " + Axiom.get().getUser().getLastname());
                                    newFamilyRecord.setAttribute("family_id", 1);
                                    newFamilyRecord.setAttribute("created", new Date());
                                    newFamilyRecord.setAttribute("propositus", propositusItem.getValueAsString());
                                    familiesGrid.addData(newFamilyRecord);
                                    newFamilyWindow.destroy();
                                }
                            } else {
                                SC.warn("Insert family name");
                            }
                    }
                });
                final IButton cancelButton = new IButton("Cancel");
                cancelButton.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        newFamilyWindow.destroy();
                    }
                });
                newFamilyWindow.setToolbarButtons(windowAddButton, cancelButton);
                
                form.setFields(nameItem,propositusItem);
                newFamilyWindow.addItem(form);

                form.setAutoFocus(true);
                newFamilyWindow.show();
            }
        });

        IButton removeButton = new IButton("Remove family", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                final ListGridRecord selectedRecord = familiesGrid.getSelectedRecord();
                if (selectedRecord != null) {
                    final String familyName = selectedRecord.getAttribute("family_name");
                    samplesService.nbSamplesInFamily(familyName, new AsyncCallback<Integer>() {
                        @Override
                        public void onSuccess(Integer result) {
                            SC.ask("Remove family", "There are " + result + " samples in family " + familyName + ". Are you sure you want to remove it?", new BooleanCallback() {
                                @Override
                                public void execute(Boolean value) {
                                    if (value != null && value) {
                                        familiesGrid.removeData(selectedRecord);
                                    }
                                }
                            });
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            SC.warn("Can't remove family " + familyName + " from database");
                        }
                    });
                } else {
                    SC.say("Please select a family !!");
                }
            }
        });

        addButton.setIcon("icons/Create.png");
        addButton.setOverflow(Overflow.VISIBLE);
        addButton.setAutoWidth();

        removeButton.setIcon("icons/Remove.png");
        removeButton.setOverflow(Overflow.VISIBLE);
        removeButton.setAutoWidth();

        buttonLayout.addMember(addButton);
        buttonLayout.addMember(removeButton);

        gridLayout.addMember(gridTitle);
        gridLayout.addMember(familiesGrid);
        gridLayout.addMember(buttonLayout);

        vStack.addMember(gridLayout);

        vlayout.addMember(vStack);

        /*
         * Add layout to mainArea tab
         */
        MainArea.addTabToTopTabset("Families", tabID, vlayout, true);
    }
}
