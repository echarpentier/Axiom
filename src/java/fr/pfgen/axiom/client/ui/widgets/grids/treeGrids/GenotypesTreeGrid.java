package fr.pfgen.axiom.client.ui.widgets.grids.treeGrids;

import java.util.List;

import fr.pfgen.axiom.client.services.GenotypingService;
import fr.pfgen.axiom.client.services.GenotypingServiceAsync;
import fr.pfgen.axiom.client.ui.ClientUtils;
import fr.pfgen.axiom.client.ui.widgets.MainArea;
import fr.pfgen.axiom.client.ui.widgets.tabs.AllGenoAnalysisTab;
import fr.pfgen.axiom.client.ui.widgets.tabs.GenotypingQCTab;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.TreeModelType;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.tree.Tree;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeNode;
import com.smartgwt.client.widgets.tree.events.FolderClickEvent;
import com.smartgwt.client.widgets.tree.events.FolderClickHandler;
import com.smartgwt.client.widgets.tree.events.FolderClosedEvent;
import com.smartgwt.client.widgets.tree.events.FolderClosedHandler;
import com.smartgwt.client.widgets.tree.events.FolderOpenedEvent;
import com.smartgwt.client.widgets.tree.events.FolderOpenedHandler;
import com.smartgwt.client.widgets.tree.events.LeafClickEvent;
import com.smartgwt.client.widgets.tree.events.LeafClickHandler;

public class GenotypesTreeGrid extends TreeGrid{
	
	private static Tree data = new Tree();
	private final GenotypingServiceAsync genotypingService = GWT.create(GenotypingService.class);
	
	public GenotypesTreeGrid(){
		setShowOpenIcons(true);
		setAnimateFolders(false);
		setCanSort(false);
		setShowHeader(false);

		buildtree();
		
		addFolderClosedHandler(new FolderClosedHandler() {
			
			@Override
			public void onFolderClosed(FolderClosedEvent event) {
				data.unloadChildren(event.getNode());
			}
		});
		
		addFolderClickHandler(new FolderClickHandler() {
			
			@Override
			public void onFolderClick(FolderClickEvent event) {
				if (event.getFolder().getName().equals("Genotyping Analysis")){
					String tabID = "allGenotypingStats";
					if (ClientUtils.tabExists(tabID)){
						MainArea.getTopTabSet().selectTab(MainArea.getTopTabSet().getTab(tabID));
					}else{
						new AllGenoAnalysisTab(tabID);
					}
				}
			}
		});
		
		addFolderOpenedHandler(new FolderOpenedHandler() {
			
			@Override
			public void onFolderOpened(FolderOpenedEvent event) {
				
				if(event.getNode().getName().equals("Genotyping Analysis")){
					genotypingService.getGenotypingNames(new AsyncCallback<List<String>>(){
						
						@Override
						public void onSuccess(List<String> result) {
							if (result != null && !result.isEmpty()){
								for (String name : result) {
									TreeNode genoName = new TreeNode(name);
									data.add(genoName, data.findById("GenoAnalysis"));
								}
								setData(data);
							}else{
								SC.warn("No genotyping analysis found !!\nPerform new analysis before using this component.");
							}
						}
						
						@Override
						public void onFailure(Throwable caught) {
							SC.warn("Can't retreive genotyping analysis names from server !");
						}
					});
				}
			}
		});
		
		addLeafClickHandler(new LeafClickHandler() {
	         
			@Override
			public void onLeafClick(LeafClickEvent event) {
				if(data.getParent(event.getLeaf()).getName().equals("Genotyping Analysis")){
					String genoName = event.getLeaf().getName();
					String tabID = genoName.replaceAll("-", "_");
					tabID = "genoQc_"+tabID;
					if (ClientUtils.tabExists(tabID)){
						MainArea.getTopTabSet().selectTab(MainArea.getTopTabSet().getTab(tabID));
					}else{
						new GenotypingQCTab(genoName,tabID);
					}
				}
			}
		});
	}
	
	private void buildtree(){
		data.setModelType(TreeModelType.CHILDREN);
		data.setRoot(new TreeNode("root"));
		
		TreeNode genoAnalysis = new TreeNode("Genotyping Analysis");
		genoAnalysis.setIsFolder(true);
		genoAnalysis.setID("GenoAnalysis");
		
		data.add(genoAnalysis, "root");
		
		setData(data);
	}
}
