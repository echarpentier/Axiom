package fr.pfgen.axiom.client.ui.widgets.tabs;

import fr.pfgen.axiom.client.ui.widgets.MainArea;
import com.smartgwt.client.types.Side;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.TabSet;

public class StudyTab {
	
	private String studyName;
	private final TabSet studyTabset;
	private String type;

	public StudyTab(String studyName, String tabID, String type){
		this.studyName = studyName;
		this.type = type;
		VLayout vlayout = new VLayout(15);
		vlayout.setWidth100();
		vlayout.setHeight100();
		studyTabset = new TabSet();
		studyTabset.setTabBarPosition(Side.LEFT);
		studyTabset.setWidth100();
		studyTabset.setHeight100();
		
		vlayout.addMember(constructStudyTabset());
		
		/*
		 * Add layout to mainArea tab
		 */
		MainArea.addTabToTopTabset("Study: "+studyName,tabID, vlayout, true);
	}
	
	private TabSet constructStudyTabset(){
		
		studyTabset.addTab(new StudyInfoTab(studyName));
		studyTabset.addTab(new StudySamplesTab(studyName));
		studyTabset.addTab(new StudyPedigreeTab(studyName,type));
		studyTabset.addTab(new StudyPlinkTab(studyName));
		studyTabset.addTab(new StudyCGTab(studyName));
		
		return studyTabset;
	}
}
