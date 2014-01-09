package fr.pfgen.axiom.client.ui.widgets.tabs;

import fr.pfgen.axiom.client.ui.widgets.MainArea;
import fr.pfgen.axiom.client.ui.widgets.grids.listGrids.UsersListGrid;
import fr.pfgen.axiom.client.ui.widgets.windows.AddUserWindow;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.VStack;

public class UsersTab {

	public UsersTab(String tabID){
		VLayout vlayout = new VLayout(15);
		vlayout.setWidth("80%");
		vlayout.setDefaultLayoutAlign(Alignment.CENTER);
		
		final VStack vStack = new VStack();  
		vStack.setShowEdges(true);
		vStack.setMembersMargin(5);  
		vStack.setLayoutMargin(10);
		vStack.setAutoWidth();
		vStack.setAutoHeight();
		
		VLayout gridLayout = new VLayout(10);

		final Label gridTitle = new Label();
		gridTitle.setHeight(20);
		gridTitle.setContents("Users");
		gridTitle.setStyleName("textTitle");

		final UsersListGrid usersGrid = new UsersListGrid();

		final IButton addUserButton = new IButton("Add user", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				new AddUserWindow();
			}
		});
		
		addUserButton.setIcon("icons/Create.png");
		addUserButton.setOverflow(Overflow.VISIBLE);
		addUserButton.setAutoWidth();
		addUserButton.setLayoutAlign(Alignment.CENTER);
		
		HLayout buttonLayout = new HLayout(20);
		
		buttonLayout.addMember(addUserButton);
		
		gridLayout.addMember(gridTitle);
		gridLayout.addMember(usersGrid);
		gridLayout.addMember(buttonLayout);
		
		vStack.addMember(gridLayout);
		
		vlayout.addMember(vStack);

		/*
		 * Add layout to mainArea tab
		 */
		MainArea.addTabToTopTabset("Users",tabID, vlayout, true);
	}
}
