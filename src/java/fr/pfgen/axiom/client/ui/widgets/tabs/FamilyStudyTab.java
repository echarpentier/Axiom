package fr.pfgen.axiom.client.ui.widgets.tabs;

import fr.pfgen.axiom.client.ui.widgets.MainArea;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.layout.VLayout;

@Deprecated
public class FamilyStudyTab {

	public FamilyStudyTab(String studyName, String tabID){
		VLayout vlayout = new VLayout(15);
		vlayout.setWidth("80%");
		vlayout.setDefaultLayoutAlign(Alignment.CENTER);
		
		
		
		/*
		 * Add layout to mainArea tab
		 */
		MainArea.addTabToTopTabset("Study: "+studyName,tabID, vlayout, true);
	}
}
