package fr.pfgen.axiom.client.ui.widgets.stackMenus;

import fr.pfgen.axiom.client.ui.widgets.grids.treeGrids.DataManagementTreeGrid;
import com.smartgwt.client.widgets.layout.VLayout;

public class DataManagementStackMenu extends VLayout{
	
	public DataManagementStackMenu(){
		
		this.setMembersMargin(10);
		this.setLayoutTopMargin(20);
	
		final DataManagementTreeGrid dataTreeGrid = new DataManagementTreeGrid();
		dataTreeGrid.setBorder("0px");
		
		this.addMember(dataTreeGrid);
	}
}
