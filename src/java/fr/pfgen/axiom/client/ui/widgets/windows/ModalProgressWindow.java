package fr.pfgen.axiom.client.ui.widgets.windows;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.Progressbar;
import com.smartgwt.client.widgets.layout.VLayout;

public class ModalProgressWindow extends Dialog {

	private final Img loadingGif = new Img("loadingStar.gif",40,40);
	private final Label progressLabel = new Label();
	private final Progressbar progressBar = new Progressbar();
	private final VLayout vlayout = new VLayout(10);
	private final Img loadingBar = new Img("gifs/LoadingBar.gif",210,19);
	
	public ModalProgressWindow(){
		setModalMaskOpacity(30);
		setIsModal(true);
		setShowModalMask(true);
		setCanDrag(false);
		setCanDragReposition(false);
		setCanDragResize(false);
		setOverflow(Overflow.VISIBLE);
		setHeight(100);
		setWidth(300);
		setAutoSize(true);
		setAutoCenter(true);
		setShowCloseButton(false);
		
		vlayout.setHeight100();
		vlayout.setWidth100();
		vlayout.setDefaultLayoutAlign(Alignment.CENTER);
		vlayout.setDefaultLayoutAlign(VerticalAlignment.CENTER);
		
		loadingGif.hide();
		vlayout.addMember(loadingGif);
		
		loadingBar.hide();
		vlayout.addMember(loadingBar);
		
		progressBar.hide();
		vlayout.addMember(progressBar);
		
		progressLabel.setHeight100();
		progressLabel.setWidth100();
		
		vlayout.addMember(progressLabel);
		
		addItem(vlayout);
	}
	
	public void setProgressBar(){
		progressBar.setHeight(20);
		progressBar.show();
	}
	
	public void setLoadingBar(){
		loadingBar.show();
	}
	
	public void setLoadingGif(){
		loadingGif.show();
	}
	
	public Progressbar getProcessBar(){
		return progressBar;
	}
	
	public Label getProgressLabel(){
		return progressLabel;
	}
}
