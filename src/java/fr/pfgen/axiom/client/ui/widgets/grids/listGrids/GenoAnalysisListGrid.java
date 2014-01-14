package fr.pfgen.axiom.client.ui.widgets.grids.listGrids;

import fr.pfgen.axiom.client.datasources.GenotypingAnalysisDS;
import com.smartgwt.client.data.SortSpecifier;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.AutoFitWidthApproach;
import com.smartgwt.client.types.Autofit;
import com.smartgwt.client.types.FetchMode;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;

public class GenoAnalysisListGrid extends ListGrid {

    public GenoAnalysisListGrid() {

        //GenotypingAnalysisDS datasource = GenotypingAnalysisDS.getInstance();
        GenotypingAnalysisDS datasource = new GenotypingAnalysisDS();

        this.setTitle("List of genotyping analysis.");
        this.setDataSource(datasource);
        this.setEmptyCellValue("--");
        this.setLayoutAlign(Alignment.CENTER);

        ListGridField idField = new ListGridField("geno_id");
        idField.setAlign(Alignment.CENTER);
        idField.setType(ListGridFieldType.INTEGER);
        idField.setWidth(10);
        idField.setHidden(true);

        ListGridField nameField = new ListGridField("geno_name");
        nameField.setAlign(Alignment.CENTER);
        nameField.setType(ListGridFieldType.TEXT);
        nameField.setWidth(10);

        ListGridField pathField = new ListGridField("folder_path");
        pathField.setAlign(Alignment.CENTER);
        pathField.setType(ListGridFieldType.TEXT);
        pathField.setHidden(true);
        pathField.setWidth(10);

        ListGridField userField = new ListGridField("user");
        userField.setAlign(Alignment.CENTER);
        userField.setType(ListGridFieldType.TEXT);
        userField.setWidth(10);

        ListGridField dishQCField = new ListGridField("dishQCLimit");
        dishQCField.setAlign(Alignment.CENTER);
        dishQCField.setType(ListGridFieldType.FLOAT);
        dishQCField.setWidth(10);

        ListGridField callRateField = new ListGridField("callRateLimit");
        callRateField.setAlign(Alignment.CENTER);
        callRateField.setType(ListGridFieldType.FLOAT);
        callRateField.setWidth(10);

        ListGridField annotField = new ListGridField("annotation_file");
        annotField.setAlign(Alignment.CENTER);
        annotField.setType(ListGridFieldType.TEXT);
        annotField.setHidden(true);
        annotField.setWidth(10);

        ListGridField libraryField = new ListGridField("library_files");
        libraryField.setAlign(Alignment.CENTER);
        libraryField.setType(ListGridFieldType.TEXT);
        libraryField.setHidden(true);
        libraryField.setWidth(10);

        ListGridField executedField = new ListGridField("executed");
        executedField.setType(ListGridFieldType.DATE);
        executedField.setAlign(Alignment.CENTER);
        executedField.setWidth(10);

        this.setFields(idField, nameField, pathField, userField, dishQCField, callRateField, annotField, libraryField, executedField);

        this.setSelectionType(SelectionStyle.SINGLE);
        this.setAutoFetchData(true);
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
        this.setCanEdit(false);

        this.addSort(new SortSpecifier("executed", SortDirection.DESCENDING));
    }
}
