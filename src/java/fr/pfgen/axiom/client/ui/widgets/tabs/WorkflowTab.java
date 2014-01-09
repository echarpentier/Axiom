package fr.pfgen.axiom.client.ui.widgets.tabs;

import fr.pfgen.axiom.client.ui.widgets.MainArea;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

public class WorkflowTab {

	static VLayout workflowLayout = new VLayout(10);
	
	public WorkflowTab(String tabID){
		//workflowLayout.setBorder("2px solid gray");
		
		final HLayout titleLayout = new HLayout(10);
		
		/*final VStack samplesStack = new VStack();  
		samplesStack.setShowEdges(true);
		samplesStack.setMembersMargin(5);  
		samplesStack.setLayoutMargin(10);
		
		samplesStack.setWidth("25%");
		samplesStack.setHeight100();
		
		
		final VStack StoQCStack = new VStack();  
		StoQCStack.setShowEdges(false);
		StoQCStack.setMembersMargin(5);  
		StoQCStack.setLayoutMargin(10);
		
		StoQCStack.setWidth("5%");
		StoQCStack.setHeight100();
		
		
		final VStack qcStack = new VStack();  
		qcStack.setShowEdges(true);
		qcStack.setMembersMargin(5);  
		qcStack.setLayoutMargin(10);
		
		qcStack.setWidth("25%");
		qcStack.setHeight100();
		
		
		final VStack QCtoGStack = new VStack();  
		QCtoGStack.setShowEdges(false);
		QCtoGStack.setMembersMargin(5);  
		QCtoGStack.setLayoutMargin(10);
		
		QCtoGStack.setWidth("5%");
		QCtoGStack.setHeight100();
		
		
		final VStack genotypeStack = new VStack();  
		genotypeStack.setShowEdges(true);
		genotypeStack.setMembersMargin(5);  
		genotypeStack.setLayoutMargin(10);
		
		genotypeStack.setWidth("25%");
		genotypeStack.setHeight100();
		
		
		final Img arrowRight = new Img("/axiom/images/gifs/anim-right.gif",75,75);
		arrowRight.setLayoutAlign(Alignment.CENTER);
		arrowRight.setAlign(Alignment.CENTER);
		arrowRight.setLayoutAlign(VerticalAlignment.CENTER);
		
		StoQCStack.addMember(arrowRight);
		
		//add stacks to hlayout
		hlayout.addMember(samplesStack);
		hlayout.addMember(StoQCStack);
		hlayout.addMember(qcStack);
		hlayout.addMember(QCtoGStack);
		hlayout.addMember(genotypeStack);
		*/
		
		
		workflowLayout.addMember(titleLayout);
		
		/*
		 * Add layout to mainArea tab
		 */
		MainArea.addTabToTopTabset("Workflows",tabID, workflowLayout, true);
	}
	
	public static void addNewWorkflowToLayout(String title){
		HLayout workflow = createNewWorkflow(title);
		
		workflowLayout.addMember(workflow);
	}
	
	private static HLayout createNewWorkflow(String title){
		HLayout newWorkflow = new HLayout();
		
		return newWorkflow;
	}
	
	public static VLayout getWorkflowLayout(){
		return workflowLayout;
	}
}
