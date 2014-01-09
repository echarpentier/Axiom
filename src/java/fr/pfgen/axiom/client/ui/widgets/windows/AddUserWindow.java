package fr.pfgen.axiom.client.ui.widgets.windows;

import fr.pfgen.axiom.client.datasources.UsersDS;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.form.DynamicForm;

public class AddUserWindow extends Dialog{
	
	public AddUserWindow(){
		setAutoSize(true);
		setIsModal(true);
		setShowModalMask(true);
		setTitle("Add user");
		setShowMinimizeButton(false);
		setCanDragReposition(true);
		
		addCloseClickHandler(new CloseClickHandler() {
			
			@Override
			public void onCloseClick(CloseClientEvent event) {
				destroy();
			}
		});
	
		final DynamicForm form = new DynamicForm();  
		form.setAutoHeight(); 
		form.setAutoWidth();
		form.setPadding(5);
		form.setLayoutAlign(Alignment.CENTER);
		
		final UsersDS datasource = UsersDS.getInstance();
		form.setDataSource(datasource);
		form.setBrowserSpellCheck(false);
		addItem(form);
		
		final IButton addUserButton = new IButton("Add", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if (form.validate()){
					datasource.addData(form.getValuesAsRecord());
				}
			}
		});
		
		final IButton cancelButton = new IButton("Cancel", new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				destroy();
			}
		});
		
		setToolbarButtons(addUserButton,cancelButton);

		show();
	}
}
