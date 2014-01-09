package fr.pfgen.axiom.client.ui.widgets.windows;

import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Window;

public class ApplicationModalWindow{
	
	private Window modalWindow;

	public ApplicationModalWindow(){
		modalWindow = new Window();
		modalWindow.setModalMaskOpacity(30);
		modalWindow.setIsModal(true);
		modalWindow.setShowModalMask(true);
		modalWindow.setCanDrag(false);
		modalWindow.setCanDragReposition(false);
		modalWindow.setCanDragResize(false);
		modalWindow.setOverflow(Overflow.VISIBLE);
		modalWindow.setAutoHeight();
		modalWindow.setAutoWidth();
		modalWindow.setAutoCenter(true);
		modalWindow.setShowHeader(false);
		modalWindow.setShowHeaderBackground(false);
		modalWindow.setShowHeaderIcon(false);
		modalWindow.setShowEdges(false);
		Img loadingGif = new Img("bigLoadingStar.gif");
		modalWindow.addChild(loadingGif);
		modalWindow.setBackgroundColor("transparent");
	}
	
	public void show(){
		modalWindow.show();
	}
	
	public void hide(){
		modalWindow.hide();
	}
}
