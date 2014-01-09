package fr.pfgen.axiom.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import fr.pfgen.axiom.client.services.LoginService;
import fr.pfgen.axiom.client.services.LoginServiceAsync;
import fr.pfgen.axiom.client.ui.widgets.ApplicationMenu;
import fr.pfgen.axiom.client.ui.widgets.HeaderArea;
import fr.pfgen.axiom.client.ui.widgets.MainArea;
import fr.pfgen.axiom.client.ui.widgets.NavigationArea;
import fr.pfgen.axiom.client.ui.widgets.windows.LoginPanel;
import fr.pfgen.axiom.shared.records.UserRecord;

import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

public class Axiom implements EntryPoint {
	
	private static Axiom singleton;
	private final LoginServiceAsync loginService = GWT.create(LoginService.class);
	public UserRecord user;

    public static Axiom get() {
    	return singleton;
    }
    
    private static final int HEADER_HEIGHT = 85;
    
    private VLayout mainLayout;
    private HLayout northLayout;
    private HLayout southLayout;
    private VLayout eastLayout;
    private HLayout westLayout;
    
    @Override
	public void onModuleLoad() {
    	singleton=this;
    	
    	startSession();
    }
    
    public VLayout getMainLayout(){
    	return mainLayout;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void startSession(){
    	loginService.checkUserLogin(
				new AsyncCallback() {
					@Override
					public void onSuccess(Object result) {
						UserRecord user = (UserRecord) result;
						if (user == null) {
							LoginPanel l = new LoginPanel();
					        l.draw();
				        } else {
				        	Axiom.get().setUser(user);
				        	Axiom.get().displayMainScreen();
				        }
					}
					@Override
					public void onFailure(Throwable caught) {
						caught.printStackTrace();
					}			
				});
    }
    
    public void setUser(UserRecord user){
    	this.user = user;
    }
    
    public UserRecord getUser(){
    	return user;
    }
    
    public void displayMainScreen(){
    	Window.enableScrolling(false);
        Window.setMargin("0px");
        
        // main layout occupies the whole area
        mainLayout = new VLayout();
        mainLayout.setWidth100();
        mainLayout.setHeight100();

        northLayout = new HLayout();
        northLayout.setHeight(HEADER_HEIGHT);

        VLayout vLayout = new VLayout();
        vLayout.addMember(new HeaderArea());
        vLayout.addMember(new ApplicationMenu());
        northLayout.addMember(vLayout);

        westLayout = new NavigationArea();
        westLayout.setWidth("12%");
        
        eastLayout = new MainArea();
        eastLayout.setWidth("88%");
        
        southLayout = new HLayout();
        southLayout.setMembers(westLayout, eastLayout);

        mainLayout.addMember(northLayout);
        mainLayout.addMember(southLayout);

        // add the main layout container to GWT's root panel
        RootLayoutPanel.get().clear();
        RootLayoutPanel.get().add(mainLayout);
    }
}
