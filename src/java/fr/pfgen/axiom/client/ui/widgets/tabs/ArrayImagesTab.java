package fr.pfgen.axiom.client.ui.widgets.tabs;

import fr.pfgen.axiom.client.ui.widgets.MainArea;
import fr.pfgen.axiom.client.ui.widgets.grids.tileGrids.ArrayImagesTileGrid;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.layout.VLayout;

public class ArrayImagesTab {

	public ArrayImagesTab(final String plateName,final String tabID){
		
		final VLayout vlayout = new VLayout(15);
		vlayout.setWidth("100%");
		vlayout.setDefaultLayoutAlign(Alignment.CENTER);
		
		final ArrayImagesTileGrid tileGrid = new ArrayImagesTileGrid(plateName);
		
		vlayout.addMember(tileGrid);
		
		/*
		 * Add layout to mainArea tab
		 */
		MainArea.addTabToTopTabset("Images: "+plateName,tabID, vlayout, true);
	}
}
