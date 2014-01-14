/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.pfgen.axiom.client.ui.widgets.vstacks;

import fr.pfgen.axiom.client.ui.widgets.grids.listGrids.RunningAnalysisListgrid;

/**
 *
 * @author eric
 */
public class RunningGenoVstack extends GenericVstack{
    private RunningAnalysisListgrid grid;
	
	public RunningGenoVstack(){
		super();
	}
	
	public void addGrid(){
		if (grid!=null){
			grid.destroy();
		}
		grid = new RunningAnalysisListgrid();
		this.addMember(grid);
	}
	
	public RunningAnalysisListgrid getGrid(){
		return grid;
	}
}
