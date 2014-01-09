package fr.pfgen.axiom.client.ui.widgets.grids.treeGrids;

import java.util.List;

import fr.pfgen.axiom.client.services.StudiesService;
import fr.pfgen.axiom.client.services.StudiesServiceAsync;
import fr.pfgen.axiom.client.ui.ClientUtils;
import fr.pfgen.axiom.client.ui.widgets.MainArea;
import fr.pfgen.axiom.client.ui.widgets.tabs.StudyTab;
import fr.pfgen.axiom.client.ui.widgets.tabs.AllStudiesTab;

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

public class StudiesTreeGrid extends TreeGrid{

	private static Tree data = new Tree();
	private final StudiesServiceAsync studiesService = GWT.create(StudiesService.class); 
	
	public StudiesTreeGrid(){
		
		setShowOpenIcons(true);
		setAnimateFolders(false);
		setCanSort(false);
		setShowHeader(false);

		buildtree();
		
		addFolderClosedHandler(new FolderClosedHandler() {
			
			@Override
			public void onFolderClosed(FolderClosedEvent event) {
				String nodeName = event.getNode().getName();
				if (nodeName.equals("Family-Based") || nodeName.equals("Case-Control")){
					data.unloadChildren(event.getNode());
				}
			}
		});
		
		addFolderClickHandler(new FolderClickHandler() {
			
			@Override
			public void onFolderClick(FolderClickEvent event) {
				String name = event.getFolder().getName();
				if (name.equals("Family-Based") || name.equals("Case-Control")){
					String tabID = "allStudies";
					if (ClientUtils.tabExists(tabID)){
						MainArea.getTopTabSet().selectTab(MainArea.getTopTabSet().getTab(tabID));
					}else{
						new AllStudiesTab(tabID);
					}
				}
			}
		});
		
		addFolderOpenedHandler(new FolderOpenedHandler() {
			
			@Override
			public void onFolderOpened(FolderOpenedEvent event) {
				if (event.getNode().getName().equals("Family-Based")){
					studiesService.getStudyNames("family", new AsyncCallback<List<String>>() {

						@Override
						public void onFailure(Throwable caught) {
							SC.warn("Cannot retrieve study names from server");
						}

						@Override
						public void onSuccess(List<String> result) {
							if (result != null && !result.isEmpty()){
								for (String study : result) {
									TreeNode studyName = new TreeNode(study);
									data.add(studyName, data.findById("Family"));
								}
								setData(data);
							}else{
								SC.say("No family-based study found !");
							}
						}
					});
				}else if (event.getNode().getName().equals("Case-Control")){
					studiesService.getStudyNames("case-control", new AsyncCallback<List<String>>() {

						@Override
						public void onFailure(Throwable caught) {
							SC.warn("Cannot retrieve study names from server");
						}

						@Override
						public void onSuccess(List<String> result) {
							if (result != null && !result.isEmpty()){
								for (String study : result) {
									TreeNode studyName = new TreeNode(study);
									data.add(studyName, data.findById("casecontrol"));
								}
								setData(data);
							}else{
								SC.say("No case-control study found !");
							}
						}
					});
				}
			}
		});
	
		addLeafClickHandler(new LeafClickHandler() {
			
			@Override
			public void onLeafClick(LeafClickEvent event) {
				if (data.getParent(event.getLeaf()).getName().equals("Family-Based")){
					String studyName = event.getLeaf().getName();
					String tabID = studyName.replaceAll("-", "_");
					tabID = "study_"+tabID;
					if (ClientUtils.tabExists(tabID)){
						MainArea.getTopTabSet().selectTab(MainArea.getTopTabSet().getTab(tabID));
					}else{
						new StudyTab(studyName,tabID,"family");
					}
				}else if (data.getParent(event.getLeaf()).getName().equals("Case-Control")){
					String studyName = event.getLeaf().getName();
					String tabID = studyName.replaceAll("-", "_");
					tabID = "study_"+tabID;
					if (ClientUtils.tabExists(tabID)){
						MainArea.getTopTabSet().selectTab(MainArea.getTopTabSet().getTab(tabID));
					}else{
						new StudyTab(studyName,tabID, "case-control");
					}
				}
			}
		});
	}
	
	
	private void buildtree(){
		data.setModelType(TreeModelType.CHILDREN);
		data.setRoot(new TreeNode("root"));
		
		TreeNode familyTreenode = new TreeNode("Family-Based");
		familyTreenode.setIsFolder(true);
		familyTreenode.setID("Family");
		
		TreeNode caseControlTreenode = new TreeNode("Case-Control");
		caseControlTreenode.setIsFolder(true);
		caseControlTreenode.setID("casecontrol");
		
		data.add(familyTreenode, "root");
		data.add(caseControlTreenode, "root");
		
		setData(data);
	}
}
