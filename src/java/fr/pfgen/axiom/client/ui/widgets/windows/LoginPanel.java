package fr.pfgen.axiom.client.ui.widgets.windows;

import fr.pfgen.axiom.client.Axiom;
import fr.pfgen.axiom.client.services.LoginService;
import fr.pfgen.axiom.client.services.LoginServiceAsync;
import fr.pfgen.axiom.shared.records.UserRecord;

import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.PasswordItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.form.fields.events.KeyPressEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyPressHandler;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class LoginPanel extends Dialog {
	
	final TextItem username = new TextItem();
	final PasswordItem password = new PasswordItem();
	final IButton reset = new IButton("Reset");
	final IButton login = new IButton("Login");
	final DynamicForm form = new DynamicForm();
	private final LoginServiceAsync loginService = GWT.create(LoginService.class);
	private static UserRecord user;
	
	public LoginPanel(){
		setAutoSize(true);
		setTitle("User Authentication");
		setCanDragResize(false);
		setCanDragReposition(true);
		setAutoCenter(true);
		setIsModal(true);
		setShowModalMask(true);
		setModalMaskOpacity(50);
		setShowCloseButton(false);
		setShowMinimizeButton(false);
		setPadding(5);
		
		username.setTitle("Username");  
		username.setRequired(true); 
		username.setKeyPressFilter("^[a-zA-Z0-9_\\-]+$");
		password.setTitle("Password");
		password.setRequired(true); 
		form.setFields(new FormItem[] {username, password});
		form.setAutoFocus(true);
		form.focusInItem(username);
		form.setAutoHeight();
		form.setAutoWidth();
		form.setBrowserSpellCheck(false);
		
		reset.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				username.setValue("");
				password.setValue("");
			}
		});
		
		login.disable();
		
		KeyPressHandler kpHandler = new KeyPressHandler() {
			
			@Override
			public void onKeyPress(KeyPressEvent event) {
				if (event.getKeyName().equals("Enter") && !login.isDisabled()){
					if(validate()){
						submitLogin();
					}
				}
			}
		};

		ChangedHandler cHandler = new ChangedHandler() {
			
			@Override
			public void onChanged(ChangedEvent event) {
				if (hasValue(username) && hasValue(password)){
					login.enable();
				}else{
					login.disable();
				}
			}
		};
		
		username.addKeyPressHandler(kpHandler);
		password.addKeyPressHandler(kpHandler);
		username.addChangedHandler(cHandler);
		password.addChangedHandler(cHandler);
		
		login.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				if(validate()){
					submitLogin();
				}
			}
		});
		
		//addMember(form);
		addItem(form);
		setToolbarButtons(login,reset);
	}
	
	protected boolean hasValue(FormItem field) {
		return field.getValue() != null && field.getValue().toString().length() > 0;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void submitLogin(){
		loginService.loginServer(username.getValueAsString(),password.getValueAsString(),
				new AsyncCallback() {
					@Override
					public void onSuccess(Object result) {
						user = (UserRecord) result;
						if (user.getLoginText().startsWith("User authenticated !")) {
				            // The user is authenticated
							LoginPanel.this.hide();
							Axiom.get().setUser(user);
							Axiom.get().displayMainScreen();
				        } else {	
				        	SC.warn(user.getLoginText());
				        }
					}
					@Override
					public void onFailure(Throwable caught) {
						caught.printStackTrace();
					}			
				});
	}
	
	private boolean validate(){
		if (!hasValue(username) && !hasValue(password)){
			SC.warn("Please provide username and password");
			return false;
		}else if (!hasValue(username) && hasValue(password)) {
			SC.warn("Please provide username");
			return false;
		}else if (hasValue(username) && !hasValue(password)) {
			SC.warn("Please provide password");
			return false;	
		}else{
			return true;
		}
	}

	public TextItem getUsername() {
		return username;
	}

	public PasswordItem getPassword() {
		return password;
	}

	public IButton getLogin() {
		return login;
	}
}