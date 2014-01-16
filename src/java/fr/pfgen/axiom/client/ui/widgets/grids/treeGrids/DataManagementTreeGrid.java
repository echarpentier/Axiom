package fr.pfgen.axiom.client.ui.widgets.grids.treeGrids;

import java.util.List;

import fr.pfgen.axiom.client.services.PlatesService;
import fr.pfgen.axiom.client.services.PlatesServiceAsync;
import fr.pfgen.axiom.client.services.PopulationsService;
import fr.pfgen.axiom.client.services.PopulationsServiceAsync;
import fr.pfgen.axiom.client.ui.widgets.MainArea;
import fr.pfgen.axiom.client.ui.widgets.tabs.PlatesInPopulationTab;
import fr.pfgen.axiom.client.ui.widgets.tabs.PlatesTab;
import fr.pfgen.axiom.client.ui.widgets.tabs.PopulationsTab;
import fr.pfgen.axiom.client.ui.widgets.tabs.SamplesInPlateTab;
import fr.pfgen.axiom.client.ui.widgets.tabs.SamplesTab;
import fr.pfgen.axiom.client.ui.ClientUtils;
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
import fr.pfgen.axiom.client.services.FamiliesService;
import fr.pfgen.axiom.client.services.FamiliesServiceAsync;
import fr.pfgen.axiom.client.ui.widgets.tabs.FamiliesTab;
import fr.pfgen.axiom.client.ui.widgets.tabs.SamplesInFamilyTab;

public class DataManagementTreeGrid extends TreeGrid {

    private final PopulationsServiceAsync populationsService = GWT.create(PopulationsService.class);
    private final PlatesServiceAsync fetchPlates = GWT.create(PlatesService.class);
    private final FamiliesServiceAsync familiesService = GWT.create(FamiliesService.class);
    private static Tree data = new Tree();

    public DataManagementTreeGrid() {

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
                //projects clic
                if (event.getFolder().getName().equals("Populations")) {
                    String tabID = "Populations";
                    if (ClientUtils.tabExists(tabID)) {
                        MainArea.getTopTabSet().selectTab(MainArea.getTopTabSet().getTab(tabID));
                    } else {
                        new PopulationsTab(tabID);
                    }
                } else if (event.getFolder().getName().equals("Families")){
                    String tabID = "Families";
                    if (ClientUtils.tabExists(tabID)){
                        MainArea.getTopTabSet().selectTab(MainArea.getTopTabSet().getTab(tabID));
                    }else{
                        new FamiliesTab(tabID);
                    }
                //plates clic
                } else if (event.getFolder().getName().equals("Plates")) {
                    String tabID = "Plates";
                    if (ClientUtils.tabExists(tabID)) {
                        MainArea.getTopTabSet().selectTab(MainArea.getTopTabSet().getTab(tabID));
                    } else {
                        new PlatesTab(tabID);
                    }
                //samples clic
                } else if (event.getFolder().getName().equals("Samples")) {
                    String tabID = "Samples";
                    if (ClientUtils.tabExists(tabID)) {
                        MainArea.getTopTabSet().selectTab(MainArea.getTopTabSet().getTab(tabID));
                    } else {
                        new SamplesTab(tabID);
                    }
                }
            }
        });

        addFolderOpenedHandler(new FolderOpenedHandler() {
            @Override
            public void onFolderOpened(FolderOpenedEvent event) {
                //opening Projects
                if (event.getNode().getName().equals("Populations")) {
                    populationsService.getPopulationNames(new AsyncCallback<List<String>>() {
                        @Override
                        public void onSuccess(List<String> result) {
                            if (result != null && !result.isEmpty()) {
                                //String projects[] = result.split("/");
                                for (String population : result) {
                                    TreeNode populationName = new TreeNode(population);
                                    populationName.setIsFolder(false);
                                    data.add(populationName, "Populations");
                                }
                                setData(data);
                            } else {
                                SC.warn("No populations found !!\nAdd populations by clicking on \"Population\" folder");
                            }
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            SC.warn("Can't retreive existing populations from server !");
                        }
                    });
                //Opening families    
                } else if (event.getNode().getName().equals("Families")) {
                    familiesService.getFamiliesNames(new AsyncCallback<List<String>>() {
                        @Override
                        public void onSuccess(List<String> result) {
                            if (result != null && !result.isEmpty()) {
                                for (String family : result) {
                                    TreeNode familyName = new TreeNode(family);
                                    data.add(familyName, "Families");
                                }
                                setData(data);
                            } else {
                                SC.warn("No families found !!\nAdd familes by clicking on \"Families\" folder");
                            }
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            SC.warn("Can't retreive existing families from server !");
                        }
                    });
                //opening Plates
                } else if (event.getNode().getName().equals("Plates")) {
                    fetchPlates.getPlateNames(new AsyncCallback<List<String>>() {
                        @Override
                        public void onSuccess(List<String> result) {
                            if (result != null && !result.isEmpty()) {
                                for (String plate : result) {
                                    TreeNode plateName = new TreeNode(plate);
                                    data.add(plateName, "Plates");
                                }
                                setData(data);
                            } else {
                                SC.warn("No plates found !!\nAdd plate folder on server under the appropriate location and update the database.");
                            }
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            SC.warn("Can't retreive existing plates from server !");
                        }
                    });
                } else if (event.getNode().getName().equals("Samples")) {
                    //void
                }
            }
        });

        addLeafClickHandler(new LeafClickHandler() {
            @Override
            public void onLeafClick(LeafClickEvent event) {
                //Projects
                if (data.getParent(event.getLeaf()).getName().equals("Populations")) {
                    String populationName = event.getLeaf().getName();
                    String tabID = populationName.replaceAll("-", "_");
                    tabID = "population_" + tabID;
                    if (ClientUtils.tabExists(tabID)) {
                        MainArea.getTopTabSet().selectTab(MainArea.getTopTabSet().getTab(tabID));
                    } else {
                        new PlatesInPopulationTab(populationName, tabID);
                    }
                //Families    
                } else if (data.getParent(event.getLeaf()).getName().equals("Families")) {
                    String familyName = event.getLeaf().getName();
                    String tabID = familyName.replaceAll("-", "_");
                    tabID = "family_" + tabID;
                    if (ClientUtils.tabExists(tabID)) {
                        MainArea.getTopTabSet().selectTab(MainArea.getTopTabSet().getTab(tabID));
                    } else {
                        new SamplesInFamilyTab(familyName, tabID);
                    }
                //Plates
                } else if (data.getParent(event.getLeaf()).getName().equals("Plates")) {
                    String plateName = event.getLeaf().getName();
                    String tabID = plateName.replaceAll("-", "_");
                    tabID = "plate_" + tabID;
                    if (ClientUtils.tabExists(tabID)) {
                        MainArea.getTopTabSet().selectTab(MainArea.getTopTabSet().getTab(tabID));
                    } else {
                        new SamplesInPlateTab(plateName, tabID);
                    }
                    //Samples
                } else if (data.getParent(event.getLeaf()).getName().equals("Samples")) {
                }
            }
        });
    }

    private void buildtree() {
        data.setModelType(TreeModelType.CHILDREN);
        data.setRoot(new TreeNode("root"));
        TreeNode populationsTreenode = new TreeNode("Populations");
        populationsTreenode.setIsFolder(true);
        populationsTreenode.setID("PopulationsTreenode");
        TreeNode familiesTreenode = new TreeNode("Families");
        familiesTreenode.setIsFolder(true);
        familiesTreenode.setID("FamiliesTreenode");
        TreeNode platesTreenode = new TreeNode("Plates");
        platesTreenode.setIsFolder(true);
        platesTreenode.setID("PlatesTreenode");
        TreeNode samplesTreeNode = new TreeNode("Samples");
        samplesTreeNode.setIsFolder(true);
        samplesTreeNode.setID("SamplesTreenode");

        data.add(populationsTreenode, "root");
        data.add(familiesTreenode, "root");
        data.add(platesTreenode, "root");
        data.add(samplesTreeNode, "root");

        setData(data);
    }
}
