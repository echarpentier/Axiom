package fr.pfgen.axiom.client.ui.widgets.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.menu.IMenuButton;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.MenuItemSeparator;
import com.smartgwt.client.widgets.menu.events.ClickHandler;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;

import fr.pfgen.axiom.client.services.GetFilePathService;
import fr.pfgen.axiom.client.services.GetFilePathServiceAsync;
import fr.pfgen.axiom.client.ui.widgets.MainArea;
import fr.pfgen.axiom.client.ui.widgets.graphs.DQCValuesPerPlate;

@Deprecated
public class AllPlatesStatsTab {
	
	private GetFilePathServiceAsync getFilePathService = GWT.create(GetFilePathService.class);

	public AllPlatesStatsTab(String tabID){
		
		VLayout vlayout = new VLayout(15);
		vlayout.setWidth("80%");
		vlayout.setDefaultLayoutAlign(Alignment.CENTER);
		
		final HLayout hLayout = new HLayout(10);
		
		final Menu statsMenu = new Menu();
		
		MenuItemSeparator separator = new MenuItemSeparator();
		
		MenuItem statsMenuItem = new MenuItem();
		statsMenuItem.setTitle("DQC per plate");
		statsMenuItem.setIcon("icons/Graph-boxplot.ico");
		statsMenuItem.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(MenuItemClickEvent event) {
				constructDQCwindow();
			}
		});
		
		
		statsMenu.setItems(statsMenuItem,separator);
		
		final IMenuButton statsButton = new IMenuButton("Stats");
		statsButton.setMenu(statsMenu);
		statsButton.setIconSpacing(20);
		statsButton.setIconAlign("left");
		statsButton.setShowDisabledIcon(false);
		statsButton.setOverflow(Overflow.VISIBLE);
		statsButton.setAutoWidth();
		
		hLayout.addMember(statsButton);
		
		vlayout.addMember(hLayout);
		
		/*
		 * Add layout to mainArea tab
		 */
		MainArea.addTabToTopTabset("All plates stats",tabID, vlayout, true);
	}
	
	private void constructDQCwindow(){
		final Dialog dqcWin = new Dialog();
		dqcWin.setAutoHeight();
		dqcWin.setAutoWidth();
		dqcWin.setAutoSize(true);
		dqcWin.setTitle("DQC boxplot");
		dqcWin.setCanDragResize(false);
		dqcWin.setCanDragReposition(true);
		dqcWin.setShowHeader(true);
		
		Menu cMenu = new Menu();
		MenuItem closeItem = new MenuItem();
		closeItem.setTitle("Close");
		closeItem.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(MenuItemClickEvent event) {
				dqcWin.destroy();	
			}
		});
		
		MenuItem saveAsItem = new MenuItem();
		saveAsItem.setTitle("Save As");
		saveAsItem.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(MenuItemClickEvent event) {
				getFilePathService.getDQCGraphPath(new AsyncCallback<String>() {
					
					@Override
					public void onSuccess(String result) {
						com.google.gwt.user.client.Window.open(GWT.getModuleBaseURL() + "fileProvider?file="+result, "_self", "");	
					}
					
					@Override
					public void onFailure(Throwable caught) {
						SC.warn("Can't download DQC Graph from server");
					}
				});
				
			}
		});
		
		cMenu.addItem(closeItem);
		cMenu.addItem(saveAsItem);
		dqcWin.setContextMenu(cMenu);
		
		VLayout vlayout = new VLayout(10);
		
		DQCValuesPerPlate DQCgraph = new DQCValuesPerPlate();
		
		vlayout.addMember(DQCgraph);
		
		dqcWin.addItem(vlayout);
		dqcWin.centerInPage();
		dqcWin.show();
	}
}
