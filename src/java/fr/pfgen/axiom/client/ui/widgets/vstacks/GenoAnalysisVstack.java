package fr.pfgen.axiom.client.ui.widgets.vstacks;

import fr.pfgen.axiom.client.ui.widgets.grids.listGrids.GenoAnalysisListGrid;

public class GenoAnalysisVstack extends GenericVstack{

	private GenoAnalysisListGrid grid;
	
	public GenoAnalysisVstack(){
		super();
	}
	
	public void addGrid(){
		if (grid!=null){
			grid.destroy();
		}
		grid = new GenoAnalysisListGrid();
		this.addMember(grid);
	}
	
	public GenoAnalysisListGrid getGrid(){
		return grid;
	}
}
