/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.pfgen.axiom.client.ui.widgets.grids.listGrids;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.SortSpecifier;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.AutoFitWidthApproach;
import com.smartgwt.client.types.Autofit;
import com.smartgwt.client.types.DateDisplayFormat;
import com.smartgwt.client.types.FetchMode;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.RecordComponentPoolingMode;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import fr.pfgen.axiom.client.datasources.RunningGenoAnalysisDS;
import fr.pfgen.axiom.client.services.RunningGenoAnalysisService;
import fr.pfgen.axiom.client.services.RunningGenoAnalysisServiceAsync;

/**
 *
 * @author eric
 */
public class RunningAnalysisListgrid extends ListGrid {

    final RunningGenoAnalysisServiceAsync runningGenoService = GWT.create(RunningGenoAnalysisService.class);

    public RunningAnalysisListgrid() {
        RunningGenoAnalysisDS ds = RunningGenoAnalysisDS.getInstance();

        this.setTitle("Running genotyping analysis");
        this.setEmptyMessage("No genotyping analysis running");
        this.setDataSource(ds);
        this.setEmptyCellValue("--");
        this.setLayoutAlign(Alignment.CENTER);

        ListGridField nameField = new ListGridField("name");
        nameField.setAlign(Alignment.CENTER);
        nameField.setType(ListGridFieldType.TEXT);
        nameField.setWidth(10);
        ListGridField userField = new ListGridField("user");
        userField.setAlign(Alignment.CENTER);
        userField.setType(ListGridFieldType.TEXT);
        userField.setWidth(10);
        ListGridField startField = new ListGridField("start");
        startField.setAlign(Alignment.CENTER);
        startField.setType(ListGridFieldType.DATE);
        startField.setDateFormatter(DateDisplayFormat.TOEUROPEANSHORTDATETIME);
        startField.setWidth(10);
        ListGridField endField = new ListGridField("end");
        endField.setType(ListGridFieldType.DATE);
        endField.setDateFormatter(DateDisplayFormat.TOEUROPEANSHORTDATETIME);
        endField.setAlign(Alignment.CENTER);
        endField.setWidth(10);
        ListGridField statusField = new ListGridField("status");
        statusField.setType(ListGridFieldType.TEXT);
        statusField.setAlign(Alignment.CENTER);
        statusField.setWidth(10);
        ListGridField iconField = new ListGridField("running_icon", " ");
        iconField.setType(ListGridFieldType.IMAGE);
        iconField.setImageURLPrefix("icons/");
        iconField.setImageURLSuffix(".ico");
        iconField.setAutoFreeze(true);
        ListGridField buttonField = new ListGridField("buttonField", " ");
        buttonField.setAlign(Alignment.CENTER);
        buttonField.setWidth(80);

        this.setFields(iconField, nameField, userField, startField, endField, statusField, buttonField);
        //this.setSortField("plate_id");

        this.setSelectionType(SelectionStyle.NONE);
        this.setAutoFetchData(false);
        this.setDataFetchMode(FetchMode.BASIC);
        this.setAutoFitData(Autofit.BOTH);
        this.setAutoFitMaxRecords(10);
        this.setAutoFitWidthApproach(AutoFitWidthApproach.BOTH);
        this.setAutoFitFieldWidths(true);
        this.setAutoFitFieldsFillViewport(false);
        this.setOverflow(Overflow.AUTO);
        this.setAutoWidth();
        this.setRight(30);
        this.setLeft(20);
        this.setShowAllRecords(true);
        this.setShowRecordComponents(true);
        this.setShowRecordComponentsByCell(true);
        this.setRecordComponentPoolingMode(RecordComponentPoolingMode.DATA);

        //this.setCanEdit(false);

        this.addSort(new SortSpecifier("start", SortDirection.ASCENDING));
    }

    @Override
    protected Canvas createRecordComponent(final ListGridRecord record, Integer colNum) {
        String fieldName = this.getFieldName(colNum);
        if (fieldName.equals("buttonField")) {
            if (record.getAttributeAsDate("end") != null) {
                IButton button = new IButton();
                button.setHeight(18);
                button.setWidth(65);
                button.setTitle("Dismiss");
                button.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        RunningAnalysisListgrid.this.removeData(record);
                    }
                });
                return button;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
