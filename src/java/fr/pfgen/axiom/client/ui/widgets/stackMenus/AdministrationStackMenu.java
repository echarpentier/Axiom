package fr.pfgen.axiom.client.ui.widgets.stackMenus;

import fr.pfgen.axiom.client.Axiom;
import fr.pfgen.axiom.client.ui.ClientUtils;
import fr.pfgen.axiom.client.ui.widgets.MainArea;
import fr.pfgen.axiom.client.ui.widgets.tabs.UsersTab;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.VLayout;

public class AdministrationStackMenu extends VLayout{

	public AdministrationStackMenu(){
		
		this.setMembersMargin(10);
		this.setLayoutTopMargin(20);
		
		final IButton usersButton = new IButton("Users", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				String tabID = "UsersTab";
				if (ClientUtils.tabExists(tabID)){
					MainArea.getTopTabSet().selectTab(MainArea.getTopTabSet().getTab(tabID));
				}else{
					new UsersTab(tabID);
				}
			}
		});
		
		usersButton.setOverflow(Overflow.VISIBLE);
		usersButton.setAutoWidth();
		usersButton.setLayoutAlign(Alignment.CENTER);
		usersButton.setIcon("icons/People.png");
		usersButton.setShowDisabledIcon(false);
		
		this.disable();
		
		if (Axiom.get().getUser().getStatus().equals("admin")){
			this.enable();
		}
		
		this.addMember(usersButton);
	}
}
