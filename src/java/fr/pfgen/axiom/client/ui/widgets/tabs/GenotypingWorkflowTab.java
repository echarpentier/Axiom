/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.pfgen.axiom.client.ui.widgets.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.events.TabDeselectedEvent;
import com.smartgwt.client.widgets.tab.events.TabDeselectedHandler;
import com.smartgwt.client.widgets.tab.events.TabSelectedEvent;
import com.smartgwt.client.widgets.tab.events.TabSelectedHandler;
import fr.pfgen.axiom.client.services.GenotypingService;
import fr.pfgen.axiom.client.services.GenotypingServiceAsync;
import fr.pfgen.axiom.client.ui.widgets.MainArea;
import fr.pfgen.axiom.client.ui.widgets.vstacks.RunningGenoVstack;

/**
 *
 * @author eric
 */
public class GenotypingWorkflowTab extends Tab {

    //private final GenotypingServiceAsync genoService = GWT.create(GenotypingService.class);

    public GenotypingWorkflowTab(String tabID) {
        VLayout vlayout = new VLayout(15);
        vlayout.setWidth("80%");
        vlayout.setDefaultLayoutAlign(Alignment.CENTER);

        final RunningGenoVstack stack = new RunningGenoVstack();
        stack.setShowEdges(true);
        stack.addHeaderLabel("Running genotyping analysis");
        stack.addGrid();
        stack.getGrid().fetchData();
        stack.getGrid().setAutoFitMaxRecords(12);

        final Timer timer = new Timer() {
            @Override
            public void run() {
                stack.getGrid().invalidateCache();
                stack.getGrid().invalidateRecordComponents();
            }
        };

        timer.scheduleRepeating(15000);

        this.addTabSelectedHandler(new TabSelectedHandler() {
            @Override
            public void onTabSelected(TabSelectedEvent event) {
                timer.scheduleRepeating(15000);
            }
        });

        this.addTabDeselectedHandler(new TabDeselectedHandler() {
            @Override
            public void onTabDeselected(TabDeselectedEvent event) {
                timer.cancel();
            }
        });
        /**
         * *********************************************
         */
        vlayout.addMember(stack);

        /*
         * Add layout to mainArea tab
         */
        MainArea.addTabToTopTabset("Genotyping", tabID, vlayout, true);
    }
}
