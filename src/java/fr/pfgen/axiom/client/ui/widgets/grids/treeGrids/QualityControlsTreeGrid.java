package fr.pfgen.axiom.client.ui.widgets.grids.treeGrids;

import java.util.List;

import fr.pfgen.axiom.client.services.PlatesService;
import fr.pfgen.axiom.client.services.PlatesServiceAsync;
import fr.pfgen.axiom.client.services.PopulationsService;
import fr.pfgen.axiom.client.services.PopulationsServiceAsync;
import fr.pfgen.axiom.client.ui.ClientUtils;
import fr.pfgen.axiom.client.ui.widgets.MainArea;
import fr.pfgen.axiom.client.ui.widgets.tabs.ArrayImagesTab;
import fr.pfgen.axiom.client.ui.widgets.tabs.PlateStatsTab;
import fr.pfgen.axiom.client.ui.widgets.tabs.PopulationStatsTab;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.TreeModelType;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.tree.Tree;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeNode;
import com.smartgwt.client.widgets.tree.events.FolderClosedEvent;
import com.smartgwt.client.widgets.tree.events.FolderClosedHandler;
import com.smartgwt.client.widgets.tree.events.FolderOpenedEvent;
import com.smartgwt.client.widgets.tree.events.FolderOpenedHandler;
import com.smartgwt.client.widgets.tree.events.LeafClickEvent;
import com.smartgwt.client.widgets.tree.events.LeafClickHandler;

public class QualityControlsTreeGrid extends TreeGrid{

	private static Tree data = new Tree();
	private final PlatesServiceAsync platesService = GWT.create(PlatesService.class);
	private final PopulationsServiceAsync populationsService = GWT.create(PopulationsService.class);
	
	public QualityControlsTreeGrid(){
		
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
		
		/*addFolderClickHandler(new FolderClickHandler() {
			
			@Override
			public void onFolderClick(FolderClickEvent event) {
				if (event.getFolder().getName().equals("Plate Statistics")){
					String tabID = "allPlatesStats";
					if (ClientUtils.tabExists(tabID)){
						MainArea.getTopTabSet().selectTab(MainArea.getTopTabSet().getTab(tabID));
					}else{
						new AllPlatesStatsTab(tabID);
					}
				}
			}
		});*/
		
		addFolderOpenedHandler(new FolderOpenedHandler() {
			
			@Override
			public void onFolderOpened(FolderOpenedEvent event) {
				if (event.getNode().getName().equals("Array Images")){
					platesService.getPlateNames(new AsyncCallback<List<String>>() {

						@Override
						public void onSuccess(List<String> result) {
							if (result != null && !result.isEmpty()){
								for (String plate : result) {
									TreeNode plateName = new TreeNode(plate);
									data.add(plateName, data.findById("ArrayImages"));
								}
								setData(data);
							}else{
								SC.warn("No plates found !!\nAdd plate folder on server under the appropriate location and update the database.");
							}
						}

						@Override
						public void onFailure(Throwable caught) {
							SC.warn("Can't retreive existing plates from server !");
						}
					});
				}else if (event.getNode().getName().equals("Population Statistics")){
					populationsService.getPopulationNames(new AsyncCallback<List<String>>() {

						@Override
						public void onSuccess(List<String> result) {
							if (result != null && !result.isEmpty()){
								for (String project : result) {
									TreeNode projectName = new TreeNode(project);
									projectName.setIsFolder(false);
									data.add(projectName, data.findById("PopulationStats"));
								}
								setData(data);
							}else{
								SC.warn("No populations found !!\nAdd populations by clicking on \"Populations\" folder in Data section");
							}
						}

						@Override
						public void onFailure(Throwable caught) {
							SC.warn("Can't retreive existing projects from server !");
						}
					});
				}else if(event.getNode().getName().equals("Plate Statistics")){
					platesService.getPlateNames(new AsyncCallback<List<String>>() {

						@Override
						public void onSuccess(List<String> result) {
							if (result != null && !result.isEmpty()){
								for (String plate : result) {
									TreeNode plateName = new TreeNode(plate);
									data.add(plateName, data.findById("PlateStats"));
								}
								setData(data);
							}else{
								SC.warn("No plates found !!\nAdd plate folder on server under the appropriate location and update the database.");
							}
						}

						@Override
						public void onFailure(Throwable caught) {
							SC.warn("Can't retreive existing plates from server !");
						}
					});
				}
			}
		});

		addLeafClickHandler(new LeafClickHandler() {
			         
			@Override
			public void onLeafClick(LeafClickEvent event) {
				if (data.getParent(event.getLeaf()).getName().equals("Array Images")){
					String plateName = event.getLeaf().getName();
					String tabID = plateName.replaceAll("-", "_");
					tabID = "images_"+tabID;
					if (ClientUtils.tabExists(tabID)){
						MainArea.getTopTabSet().selectTab(MainArea.getTopTabSet().getTab(tabID));
					}else{
						new ArrayImagesTab(plateName,tabID);
					}
				}else if (data.getParent(event.getLeaf()).getName().equals("Population Statistics")){
					String populationName = event.getLeaf().getName();
					String tabID = "stats_"+populationName;
					if (ClientUtils.tabExists(tabID)){
						MainArea.getTopTabSet().selectTab(MainArea.getTopTabSet().getTab(tabID));
					}else{
						new PopulationStatsTab(populationName,tabID);
					}
				}else if (data.getParent(event.getLeaf()).getName().equals("Plate Statistics")){
					String plateName = event.getLeaf().getName();
					String tabID = plateName.replaceAll("-", "_");
					tabID = "stats_"+tabID;
					if (ClientUtils.tabExists(tabID)){
						MainArea.getTopTabSet().selectTab(MainArea.getTopTabSet().getTab(tabID));
					}else{
						new PlateStatsTab(plateName,tabID);
					}
				}
			}
		});
	}
	
	private void buildtree(){
		data.setModelType(TreeModelType.CHILDREN);
		data.setRoot(new TreeNode("root"));
		
		TreeNode imagesTreenode = new TreeNode("Array Images");
		imagesTreenode.setIsFolder(true);
		imagesTreenode.setID("ArrayImages");
		
		TreeNode populationsStatsTreenode = new TreeNode("Population Statistics");
		populationsStatsTreenode.setIsFolder(true);
		populationsStatsTreenode.setID("PopulationStats");
		
		TreeNode platesStatsTreeNode = new TreeNode("Plate Statistics");
		platesStatsTreeNode.setIsFolder(true);
		platesStatsTreeNode.setID("PlateStats");
		
		data.add(imagesTreenode, "root");
		data.add(populationsStatsTreenode, "root");
		data.add(platesStatsTreeNode, "root");
		
		setData(data);
	}
}
