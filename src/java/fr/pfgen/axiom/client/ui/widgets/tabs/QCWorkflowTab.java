package fr.pfgen.axiom.client.ui.widgets.tabs;

import fr.pfgen.axiom.client.services.QCService;
import fr.pfgen.axiom.client.services.QCServiceAsync;
import fr.pfgen.axiom.client.ui.widgets.MainArea;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.Progressbar;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.layout.VStack;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.events.TabDeselectedEvent;
import com.smartgwt.client.widgets.tab.events.TabDeselectedHandler;
import com.smartgwt.client.widgets.tab.events.TabSelectedEvent;
import com.smartgwt.client.widgets.tab.events.TabSelectedHandler;

public class QCWorkflowTab extends Tab{

	private final QCServiceAsync qcService = GWT.create(QCService.class);
	
	public QCWorkflowTab(String tabID){
		VLayout vlayout = new VLayout(15);
		vlayout.setWidth("80%");
		vlayout.setDefaultLayoutAlign(Alignment.CENTER);
		
		final VStack vStack = new VStack();  
		vStack.setShowEdges(true);
		vStack.setMembersMargin(5);
		vStack.setLayoutMargin(10);
		vStack.setWidth(300);
		vStack.setAutoHeight();
		
		/************************************************/ 
		final Label progressLabel = new Label("No QC analysis running");
		progressLabel.setHeight(16);
		vStack.addMember(progressLabel);

		final Progressbar progressBar = new Progressbar();
		progressBar.setHeight(20);
		progressBar.setVertical(false);
		vStack.addMember(progressBar); 

		final Timer timer = new Timer() {  
			@Override
			public void run() {  
				qcService.QCProgress(new AsyncCallback<String>() {
					
					@Override
					public void onSuccess(String result) {
						if (result == null || result.contains("No analysis running")){
							//cancel();
							progressLabel.setContents("No QC analysis running");
							progressBar.setPercentDone(0);
							
						}else if (result.startsWith("Processing")){
							String[] line = result.split("\\s");
							progressLabel.setContents(result);
							//System.out.println(java.lang.Double.parseDouble(line[1])+" "+java.lang.Double.parseDouble(line[3])+" "+java.lang.Double.parseDouble(line[1])/java.lang.Double.parseDouble(line[3])*100);
							progressBar.setPercentDone((int)(java.lang.Double.parseDouble(line[1])/java.lang.Double.parseDouble(line[3])*100));
						}
					}
					
					@Override
					public void onFailure(Throwable caught) {
						
					}
				});
			}
		};
		
		timer.scheduleRepeating(3000);
		
		this.addTabSelectedHandler(new TabSelectedHandler() {
			
			@Override
			public void onTabSelected(TabSelectedEvent event) {
				timer.scheduleRepeating(3000);
			}
		});
		
		this.addTabDeselectedHandler(new TabDeselectedHandler() {
			
			@Override
			public void onTabDeselected(TabDeselectedEvent event) {
				timer.cancel();
			}
		});
		/************************************************/
		vlayout.addMember(vStack);

		/*
		 * Add layout to mainArea tab
		 */
		MainArea.addTabToTopTabset("QC", tabID, vlayout, true);
	}
}
