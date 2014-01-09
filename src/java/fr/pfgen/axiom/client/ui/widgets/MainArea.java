package fr.pfgen.axiom.client.ui.widgets;

import fr.pfgen.axiom.client.ui.widgets.tabs.PersoMainArea;

import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.Side;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.events.ClickHandler;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import com.smartgwt.client.widgets.tab.events.CloseClickHandler;
import com.smartgwt.client.widgets.tab.events.TabCloseClickEvent;

public class MainArea extends VLayout {

	static TabSet topTabSet = new TabSet();
	
	public MainArea() {

		super();
		this.setOverflow(Overflow.HIDDEN);
		//this.setMemberOverlap(150);
		//this.setStackZIndex("firstOnTop");
		
		topTabSet.setTabBarPosition(Side.TOP);  
		topTabSet.setTabBarAlign(Side.LEFT);
		topTabSet.setDestroyPanes(true);
		
		this.addMember(topTabSet);
		
		new PersoMainArea();
	}

	public static void addTabToTopTabset(String title, String id, Canvas pane, boolean closable) {
		Tab tab = createTab(title,id, pane, closable);
		topTabSet.addTab(tab);
		topTabSet.selectTab(tab);
		topTabSet.addCloseClickHandler(new CloseClickHandler() {
			
			@Override
			public void onCloseClick(TabCloseClickEvent event) {
				event.getTab().getPane().destroy();	
			}
		});
	}

	private static Tab createTab(String title,String id, Canvas pane, boolean closable) {
		Tab tab = new Tab(title);
		tab.setID(id);
		tab.setCanClose(closable);
		tab.setPane(pane);
		
		//tab context menu
		Menu tabMenu = new Menu();
		MenuItem closeTabMenuItem = new MenuItem("Close tab");
		closeTabMenuItem.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(MenuItemClickEvent event) {
				if (event.getTarget().getID().equals("MainArea")){
					SC.warn("This tab cannot be closed");
				}else{
					getTopTabSet().removeTab(event.getTarget().getID());
				}
			}
		});
		MenuItem closeAllTabMenuItem = new MenuItem("Close all tabs");
		closeAllTabMenuItem.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(MenuItemClickEvent event) {
				Tab[] tabs = getTopTabSet().getTabs();
				for (Tab tab : tabs) {
					if (!tab.getID().equals("MainArea")){
						getTopTabSet().removeTab(tab.getID());
					}
				}
			}
		});
		MenuItem closeAllButThisTab = new MenuItem("Close all but this tab");
		closeAllButThisTab.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(MenuItemClickEvent event) {
				String thisTab = event.getTarget().getID();
				Tab[] tabs = getTopTabSet().getTabs();
				for (Tab tab : tabs) {
					if (!tab.getID().equals("MainArea") && !tab.getID().equals(thisTab)){
						getTopTabSet().removeTab(tab.getID());
					}
				}
			}
		});
		
		tabMenu.addItem(closeTabMenuItem);
		tabMenu.addItem(closeAllTabMenuItem);
		tabMenu.addItem(closeAllButThisTab);
		
		tab.setContextMenu(tabMenu);
		return tab;
	}

	public static TabSet getTopTabSet() {
		return topTabSet;
	}

}