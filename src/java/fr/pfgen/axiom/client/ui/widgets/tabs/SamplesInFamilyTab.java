/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.pfgen.axiom.client.ui.widgets.tabs;

import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.VStack;
import fr.pfgen.axiom.client.ui.widgets.MainArea;
import fr.pfgen.axiom.client.ui.widgets.grids.listGrids.PlatesListGrid;
import fr.pfgen.axiom.client.ui.widgets.grids.listGrids.SamplesListGrid;

/**
 *
 * @author eric
 */
public class SamplesInFamilyTab {

    public SamplesInFamilyTab(final String familyName, String tabID) {

        HLayout hlayout = new HLayout(15);
        hlayout.setWidth("80%");
        hlayout.setDefaultLayoutAlign(Alignment.CENTER);
        hlayout.setAutoHeight();
        hlayout.setDefaultLayoutAlign(VerticalAlignment.TOP);

        final VStack platesVStack = new VStack();
        platesVStack.setShowEdges(true);
        platesVStack.setMembersMargin(5);
        platesVStack.setLayoutMargin(10);
        platesVStack.setAutoWidth();
        platesVStack.setAutoHeight();
        //platesVStack.setWidth("50%");

        VLayout platesGridLayout = new VLayout(10);

        final Label platesGridTitle = new Label();
        platesGridTitle.setHeight(20);
        platesGridTitle.setContents("Plates&nbsp;for&nbsp;which&nbsp;(some&nbsp;or&nbsp;all)&nbsp;samples&nbsp;are&nbsp;in&nbsp;family&nbsp;" + familyName.replaceAll("\\s", "_"));
        platesGridTitle.setStyleName("textTitle");

        PlatesListGrid platesGrid = new PlatesListGrid();
        Criteria platesCriteria = new Criteria();
        platesCriteria.addCriteria("family_name", familyName);
        platesGrid.setCriteria(platesCriteria);
        platesGrid.setEmptyMessage("No plates found for this family");

        platesGridLayout.addMember(platesGridTitle);
        platesGridLayout.addMember(platesGrid);

        platesVStack.addMember(platesGridLayout);

        final VStack samplesVStack = new VStack();
        samplesVStack.setShowEdges(true);
        samplesVStack.setMembersMargin(5);
        samplesVStack.setLayoutMargin(10);
        samplesVStack.setAutoWidth();
        samplesVStack.setAutoHeight();
        //samplesVStack.setWidth("50%");

        VLayout samplesGridLayout = new VLayout(10);

        final Label samplesGridTitle = new Label();
        samplesGridTitle.setHeight(20);
        samplesGridTitle.setContents("Samples&nbsp;in&nbsp;family&nbsp;" + familyName.replaceAll("\\s", "&nbsp;"));
        samplesGridTitle.setStyleName("textTitle");

        final SamplesListGrid samplesGrid = new SamplesListGrid();
        Criteria samplesCriteria = new Criteria();
        samplesCriteria.addCriteria("family_name", familyName);
        samplesGrid.setCriteria(samplesCriteria);
        samplesGrid.setEmptyMessage("No samples found for this family");

        final IButton removeButton = new IButton("Remove selected samples from family", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ListGridRecord[] selectedRecords = samplesGrid.getSelectedRecords();
                if (selectedRecords.length > 0) {
                    for (ListGridRecord record : selectedRecords) {
                        String fam = record.getAttributeAsString("family_names");
                        //System.out.println(pop);
                        fam = fam.replaceAll(familyName, "");

                        record.setAttribute("family_names", fam);
                        samplesGrid.updateData(record);
                    }
                    //SC.say("Selected records have been removed from population");
                } else {
                    SC.say("Please select records to update");
                }
            }
        });
        removeButton.setOverflow(Overflow.VISIBLE);
        removeButton.setAutoWidth();
        removeButton.setLayoutAlign(Alignment.CENTER);

        samplesGridLayout.addMember(samplesGridTitle);
        samplesGridLayout.addMember(samplesGrid);
        samplesGridLayout.addMember(removeButton);

        samplesVStack.addMember(samplesGridLayout);

        hlayout.addMember(platesVStack);
        hlayout.addMember(samplesVStack);

        /*
         * Add layout to mainArea tab
         */
        MainArea.addTabToTopTabset("Family: " + familyName, tabID, hlayout, true);
    }
}
