package fr.pfgen.axiom.client.ui.widgets.graphs;

import fr.pfgen.axiom.client.services.CreateDQCGraphService;
import fr.pfgen.axiom.client.services.CreateDQCGraphServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.layout.VLayout;

@Deprecated
public class DQCValuesPerPlate extends VLayout {

	final CreateDQCGraphServiceAsync dqcService = GWT.create(CreateDQCGraphService.class);
	
	public DQCValuesPerPlate(){
		
		this.setLayoutAlign(Alignment.CENTER);
		
		final VLayout loadCanvas = new VLayout();
		loadCanvas.setLayoutAlign(Alignment.CENTER);
		loadCanvas.setDefaultLayoutAlign(Alignment.CENTER);
		
		final Label loadLabel = new Label();
		loadLabel.setContents("loading&nbsp;graph");
		loadLabel.setHeight(16);
		loadLabel.setAutoWidth();
		
		final Img image = new Img("loadingStar.gif",30,30);
		
		loadCanvas.addMember(image);
		loadCanvas.addMember(loadLabel);
		
		this.addMember(loadCanvas);

		dqcService.createDQCGraph(new AsyncCallback<String>() {
			
			@Override
			public void onSuccess(String result) {
				image.setSrc(GWT.getModuleBaseURL()+"imageProvider?file="+result);
				image.setHeight(600);
				image.setWidth(600);
				loadCanvas.removeMember(loadLabel);
				image.redraw();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				SC.warn("Cannot create Dish QC graph");
			}
		});
	}
}
