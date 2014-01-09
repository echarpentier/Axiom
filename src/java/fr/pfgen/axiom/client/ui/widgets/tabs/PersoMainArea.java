package fr.pfgen.axiom.client.ui.widgets.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.BkgndRepeat;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.VStack;
import fr.pfgen.axiom.client.services.UpdateDatabaseService;
import fr.pfgen.axiom.client.services.UpdateDatabaseServiceAsync;
import fr.pfgen.axiom.client.ui.widgets.MainArea;
import fr.pfgen.axiom.client.ui.widgets.grids.listGrids.UserListGrid;

public class PersoMainArea {

	private final UpdateDatabaseServiceAsync updateDB = GWT.create(UpdateDatabaseService.class);
	
	public PersoMainArea(){
		VLayout vlayout = new VLayout(15);
		vlayout.setWidth("80%");
		vlayout.setDefaultLayoutAlign(Alignment.CENTER);
		
		vlayout.setBackgroundImage("logos/pf-genomique.png");
		vlayout.setBackgroundRepeat(BkgndRepeat.NO_REPEAT);
		vlayout.setBackgroundPosition("center");
		
		final VStack vStack = new VStack();  
		vStack.setShowEdges(true);
		vStack.setMembersMargin(5);  
		vStack.setLayoutMargin(10);
		
		vStack.addMember(new UserListGrid());
		
		vStack.setAutoWidth();
		vStack.setAutoHeight();
		
		final VStack vStack2 = new VStack();  
		vStack2.setShowEdges(true);
		vStack2.setMembersMargin(5);  
		vStack2.setLayoutMargin(10);
		vStack2.setBackgroundColor("white");
		
		vStack2.setAutoWidth();
		vStack2.setAutoHeight();
		
		
		final HLayout loadCanvas = new HLayout();
		loadCanvas.setAlign(Alignment.CENTER);
		loadCanvas.setDefaultLayoutAlign(Alignment.CENTER);
		loadCanvas.setDefaultLayoutAlign(VerticalAlignment.CENTER);
		
		final Label updateLabel = new Label("Updating database...");  
		updateLabel.setHeight(16);
		
		final Img loadingGif = new Img("loadingStar.gif",40,40);
		
		loadCanvas.addMember(updateLabel);
		loadCanvas.addMember(loadingGif);
		
		
		final VLayout updateVlayout = new VLayout(15);
		updateVlayout.setWidth(300);
		
		Label updateText = new Label();
		updateText.setContents("After loading new Axiom files on server, you have to update the database.  Please note that if you want to replace existing CEL files, overwrite previous CEL files on the server and remove plate from the database in the \"Plates\" menu before updating the database again.");
		updateText.setAutoHeight();
		updateText.setAlign(Alignment.CENTER);
		
		final IButton updateButton = new IButton();
		updateButton.setTitle("Update Database");
		updateButton.setOverflow(Overflow.VISIBLE);
		updateButton.setAutoWidth();
		updateButton.setIcon("icons/Database.png");
		updateButton.setShowDisabledIcon(false);
		updateButton.setLayoutAlign(Alignment.CENTER);
		updateButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				//disable button and start loading gif
				updateButton.disable();
				updateVlayout.addMember(loadCanvas);
				vStack2.redraw();
				//update DB on server
				updateDB.updateDatabase(new AsyncCallback<String>() {
					
					@Override
					public void onSuccess(String result) {
						updateButton.enable();
						updateVlayout.removeMember(loadCanvas);
						vStack2.redraw();
						SC.say(result);
						
						
					}
					
					@Override
					public void onFailure(Throwable caught) {
						String e = caught.toString();
						updateButton.enable();
						updateVlayout.removeMember(loadCanvas);
						vStack2.redraw();
						SC.warn(e);
					}
				});
			}
		});
		
		updateVlayout.addMember(updateText);
		updateVlayout.addMember(updateButton);
		
		vStack2.addMember(updateVlayout);
		
		/***********************************************************/

		vlayout.addMember(vStack);
		vlayout.addMember(vStack2);
			
		MainArea.addTabToTopTabset("Home","MainArea", vlayout, false);	
	}
}
