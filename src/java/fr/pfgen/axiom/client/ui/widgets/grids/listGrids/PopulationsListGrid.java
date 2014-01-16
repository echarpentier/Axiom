package fr.pfgen.axiom.client.ui.widgets.grids.listGrids;

import com.smartgwt.client.data.SortSpecifier;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.AutoFitWidthApproach;
import com.smartgwt.client.types.Autofit;
import com.smartgwt.client.types.FetchMode;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import fr.pfgen.axiom.client.datasources.PopulationsDS;

public class PopulationsListGrid extends ListGrid {

    public PopulationsListGrid() {

        //DataSource datasource = new ProjectsDS();

        this.setTitle("Populations");
        this.setDataSource(new PopulationsDS());
        this.setEmptyCellValue("--");
        this.setLayoutAlign(Alignment.CENTER);

        ListGridField idField = new ListGridField("population_id");
        idField.setAlign(Alignment.CENTER);
        idField.setType(ListGridFieldType.INTEGER);
        idField.setWidth(10);
        idField.setHidden(true);
        ListGridField nameField = new ListGridField("population_name");
        nameField.setAlign(Alignment.CENTER);
        nameField.setType(ListGridFieldType.TEXT);
        nameField.setWidth(10);
        ListGridField userField = new ListGridField("user");
        userField.setType(ListGridFieldType.TEXT);
        userField.setAlign(Alignment.CENTER);
        userField.setWidth(10);
        ListGridField createdField = new ListGridField("created");
        createdField.setType(ListGridFieldType.DATE);
        createdField.setAlign(Alignment.CENTER);
        createdField.setWidth(10);

        this.setFields(idField, nameField, userField, createdField);
        //this.setDataPageSize(10);
        this.setAutoFetchData(true);
        this.setDataFetchMode(FetchMode.PAGED);
        this.setAutoFitData(Autofit.BOTH);
        this.setAutoFitMaxRecords(10);
        //this.setAutoFitMaxColumns(3);
        this.setAutoFitWidthApproach(AutoFitWidthApproach.BOTH);
        this.setAutoFitFieldWidths(true);
        this.setAutoFitFieldsFillViewport(false);
        this.setOverflow(Overflow.AUTO);
        this.setAutoWidth();
        //this.setWidth(600);
        this.setRight(30);
        this.setLeft(20);
        this.setCanEdit(false);

        this.addSort(new SortSpecifier("created", SortDirection.ASCENDING));
        this.addSort(new SortSpecifier("population_id", SortDirection.DESCENDING));

        //Criteria criteria = new Criteria();
        //criteria.addCriteria("id", "2");
        //this.fetchData(criteria);
    }
}
