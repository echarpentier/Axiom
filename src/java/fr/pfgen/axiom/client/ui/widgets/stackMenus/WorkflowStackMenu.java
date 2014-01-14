package fr.pfgen.axiom.client.ui.widgets.stackMenus;

import fr.pfgen.axiom.client.ui.ClientUtils;
import fr.pfgen.axiom.client.ui.widgets.MainArea;
import fr.pfgen.axiom.client.ui.widgets.tabs.QCWorkflowTab;
import fr.pfgen.axiom.client.ui.widgets.tabs.WorkflowTab;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.VLayout;
import fr.pfgen.axiom.client.ui.widgets.tabs.GenotypingWorkflowTab;

public class WorkflowStackMenu extends VLayout{

	public WorkflowStackMenu(){
		setWidth100();
		setWidth100();
		setMembersMargin(20);
		setLayoutTopMargin(20);
		//setAlign(Alignment.CENTER);
		//setAlign(VerticalAlignment.CENTER);
	
		//setBorder("2px solid blue");
		
		IButton showWorkflowsButton = new IButton("Workflows", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				String tabID = "Workflows";
				if (ClientUtils.tabExists(tabID)){
					MainArea.getTopTabSet().selectTab(MainArea.getTopTabSet().getTab(tabID));
				}else{
					new WorkflowTab(tabID);
				}
			}
		});
		showWorkflowsButton.setOverflow(Overflow.VISIBLE);
		showWorkflowsButton.setLayoutAlign(Alignment.CENTER);
		showWorkflowsButton.setIcon("icons/Application.png");
		
		IButton showQCAnalysis = new IButton("QC", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				String tabID = "QCTab";
				if (ClientUtils.tabExists(tabID)){
					MainArea.getTopTabSet().selectTab(MainArea.getTopTabSet().getTab(tabID));
				}else{
					new QCWorkflowTab(tabID);
				}
			}
		});
		showQCAnalysis.setOverflow(Overflow.VISIBLE);
		showQCAnalysis.setLayoutAlign(Alignment.CENTER);
		showQCAnalysis.setIcon("icons/qc.ico");
		
		IButton showGenotypeAnalysis = new IButton("GENOTYPING", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				String tabID = "GenoQCTab";
				if (ClientUtils.tabExists(tabID)){
					MainArea.getTopTabSet().selectTab(MainArea.getTopTabSet().getTab(tabID));
				}else{
					new GenotypingWorkflowTab(tabID);
				}
			}
		});
		showGenotypeAnalysis.setOverflow(Overflow.VISIBLE);
		showGenotypeAnalysis.setLayoutAlign(Alignment.CENTER);
		showGenotypeAnalysis.setIcon("icons/ATGC.ico");
		
		showQCAnalysis.setWidth(showGenotypeAnalysis.getWidth());
		showWorkflowsButton.setWidth(showGenotypeAnalysis.getWidth());
		
		addMember(showWorkflowsButton);
		addMember(showQCAnalysis);
		addMember(showGenotypeAnalysis);
	}
}
