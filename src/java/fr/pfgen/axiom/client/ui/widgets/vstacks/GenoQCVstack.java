package fr.pfgen.axiom.client.ui.widgets.vstacks;

import fr.pfgen.axiom.client.ui.widgets.grids.listGrids.GenoQCListGrid;

public class GenoQCVstack extends GenericVstack{

	private GenoQCListGrid grid;
	
	public GenoQCVstack(){
		super();
	}
	
	public void addGrid(){
		if (grid!=null){
			grid.destroy();
		}
		grid = new GenoQCListGrid();
		this.addMember(grid);
	}
	
	public GenoQCListGrid getGrid(){
		return grid;
	}
}
