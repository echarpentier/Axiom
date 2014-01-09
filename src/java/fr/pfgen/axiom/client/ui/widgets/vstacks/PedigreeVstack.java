package fr.pfgen.axiom.client.ui.widgets.vstacks;

import fr.pfgen.axiom.client.ui.widgets.grids.listGrids.PedigreeListGrid;

public class PedigreeVstack extends GenericVstack{
	
	private PedigreeListGrid grid;
		
	public PedigreeVstack(){
		super();
	}
		
	public void addGrid(){
		if (grid!=null){
			grid.destroy();
		}
		grid = new PedigreeListGrid();
		this.addMember(grid);
	}
		
	public PedigreeListGrid getGrid(){
		return grid;
	}
}
