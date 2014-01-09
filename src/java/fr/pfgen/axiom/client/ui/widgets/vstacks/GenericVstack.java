package fr.pfgen.axiom.client.ui.widgets.vstacks;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.util.StringUtil;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.layout.VStack;

public class GenericVstack extends VStack{
	
	private Label headerLabel;

	public GenericVstack(){
		setLayoutAlign(Alignment.LEFT);
		setAlign(Alignment.LEFT);
		setDefaultLayoutAlign(Alignment.LEFT);
		setOverflow(Overflow.VISIBLE);
		setShowEdges(true); 
		setLayoutMargin(10);
		setAutoWidth();
		setAutoHeight();
		setMembersMargin(10);
	}
	
	public void addHeaderLabel(String header){
		headerLabel = new Label();
		headerLabel.setOverflow(Overflow.VISIBLE);
		headerLabel.setAutoHeight();
		headerLabel.setAutoWidth();
		headerLabel.setStyleName("textTitle");
		headerLabel.setContents(StringUtil.asHTML(header,true));
		this.addMember(headerLabel,0);
	}
	
	public void addLabel(String label){
		Label newLabel = new Label();
		newLabel.setOverflow(Overflow.VISIBLE);
		newLabel.setAutoHeight();
		newLabel.setAutoWidth();
		newLabel.setContents(StringUtil.asHTML(label,true));
		this.addMember(newLabel);
	}
	
	public void addLabel(String label, String labelID){
		Label newLabel = new Label();
		newLabel.setID(labelID);
		newLabel.setOverflow(Overflow.VISIBLE);
		newLabel.setAutoHeight();
		newLabel.setAutoWidth();
		newLabel.setContents(StringUtil.asHTML(label,true));
		this.addMember(newLabel);
	}
	
	public void removeHeader(){
		if (headerLabel!=null){
			headerLabel.destroy();
		}
	}
}
