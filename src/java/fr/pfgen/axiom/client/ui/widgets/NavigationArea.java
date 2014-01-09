package fr.pfgen.axiom.client.ui.widgets;

import fr.pfgen.axiom.client.ui.widgets.stackMenus.AdministrationStackMenu;
import fr.pfgen.axiom.client.ui.widgets.stackMenus.StudiesStackMenu;
import fr.pfgen.axiom.client.ui.widgets.stackMenus.DataManagementStackMenu;
import fr.pfgen.axiom.client.ui.widgets.stackMenus.GenotypesStackMenu;
import fr.pfgen.axiom.client.ui.widgets.stackMenus.QualityControlsStackMenu;
import fr.pfgen.axiom.client.ui.widgets.stackMenus.WorkflowStackMenu;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.VisibilityMode;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.SectionStack;
import com.smartgwt.client.widgets.layout.SectionStackSection;

public class NavigationArea extends HLayout {

	public NavigationArea() {

		super();

		this.setMembersMargin(20);  
		this.setOverflow(Overflow.HIDDEN);
		this.setShowResizeBar(true);

		final SectionStack sectionStack = new SectionStack();
		sectionStack.setShowExpandControls(true);
		sectionStack.setAnimateSections(true);
		sectionStack.setVisibilityMode(VisibilityMode.MUTEX);
		sectionStack.setOverflow(Overflow.HIDDEN);

		/* sectionStack.addSectionHeaderClickHandler(new SectionHeaderClickHandler() {

			@Override
			public void onSectionHeaderClick(SectionHeaderClickEvent event) {
				SC.say("clic "+event.getSection().getTitle());

			}
		});*/

		/*
		 * Section for management of datas in the database
		 * -Management of projects (add,remove)
		 * -Management of samples (link to project)
		 */
		SectionStackSection section1 = new SectionStackSection("Datas");
		section1.setExpanded(true);
		section1.setItems(new DataManagementStackMenu());


		/*
		 * Section for quality controls
		 * -Performing QCs
		 * -Array images
		 * -Project Statistics
		 * -Plate Statistics
		 */
		SectionStackSection section2 = new SectionStackSection("Quality Controls");
		section2.setExpanded(false);
		section2.setItems(new QualityControlsStackMenu());


		/*
		 * Section for performing genotyping
		 * -
		 */
		SectionStackSection section3 = new SectionStackSection("Genotypes");
		section3.setExpanded(false);
		section3.setItems(new GenotypesStackMenu());


		/*
		 * Section to perform analysis
		 * -
		 */
		SectionStackSection section4 = new SectionStackSection("Studies");
		section4.setExpanded(false);
		final StudiesStackMenu studiesMenu = new StudiesStackMenu();
		section4.addItem(studiesMenu);


		/*
		 * Section to visualize workflows
		 * -
		 */
		SectionStackSection section5 = new SectionStackSection("Workflows");
		section5.setExpanded(false);
		final WorkflowStackMenu workflowMenu = new WorkflowStackMenu();
		section5.addItem(workflowMenu);


		/*
		 * Section for administration tasks
		 * -
		 */
		SectionStackSection section6 = new SectionStackSection("Administration");
		section6.setExpanded(false);
		final AdministrationStackMenu adminMenu = new AdministrationStackMenu();
		section6.addItem(adminMenu);


		sectionStack.addSection(section1);
		sectionStack.addSection(section2);
		sectionStack.addSection(section3);
		sectionStack.addSection(section4);
		sectionStack.addSection(section5);
		sectionStack.addSection(section6);


		this.addMember(sectionStack);

		/*sectionStack.addSectionHeaderClickHandler(new SectionHeaderClickHandler() {

			@Override
			public void onSectionHeaderClick(SectionHeaderClickEvent event) {
				SC.say(event.getSection().getTitle());

			}
		});*/
	} 
}
