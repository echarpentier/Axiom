package fr.pfgen.axiom.client.ui.widgets.vstacks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Encoding;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.util.StringUtil;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.ValuesManager;
import com.smartgwt.client.widgets.form.fields.CheckboxItem;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.HiddenItem;
import com.smartgwt.client.widgets.form.fields.UploadItem;
import com.smartgwt.client.widgets.layout.VStack;

/**
 * 
 *
 * @author Pete Boysen (pboysen@iastate.edu)
 * @since May 9, 2009
 * @version May 9, 2009
 */
public class Upload extends VStack {
	public static enum Mode {DEFAULT, CONVERSIONS};
	public static final String TARGET="uploadTarget";
	
	private DynamicForm uploadForm;
	private UploadItem fileItem;
	private UploadListener listener;
	private List<HiddenItem> hiddenItems;
	private VStack stack = new VStack();
	private IButton uploadButton = new IButton("Upload");
	
	/**
	 * 
	 */
	 public Upload() {
		 this(null,Mode.DEFAULT);
	 }

	 /**
	  * @param args
	  */
	 public Upload(Map<String,String> args,Mode mode) {
		 initComplete(this);
		 List<FormItem> items = new ArrayList<FormItem>();
		 if (args != null) {
			 hiddenItems = new ArrayList<HiddenItem>();
			 for(String key: args.keySet()) {
				 HiddenItem item = new HiddenItem(key);
				 item.setValue(args.get(key));
				 items.add(item);
				 hiddenItems.add(item);
			 };
		 }
		 ValuesManager vm = new ValuesManager();
		 uploadForm = new DynamicForm();
		 uploadForm.setValuesManager(vm);
		 uploadForm.setEncoding(Encoding.MULTIPART);
		 uploadForm.setTarget(TARGET);

		 fileItem = new UploadItem("file");
		 fileItem.setTitle("File");
		 fileItem.setWidth(300);
		 items.add(fileItem);
		 
		 //IButton uploadButton = new IButton("Upload");
		 uploadButton.setIcon("icons/Upload.png");
		 //Button uploadButton = new Button("Upload");
		 uploadButton.addClickHandler(new ClickHandler(){
			 @Override
			public void onClick(ClickEvent e) {
				 String obj = fileItem.getValueAsString();
				 if (obj != null && !obj.equals("")) {
					 uploadForm.submitForm();
				 } /*else {
					 SC.warn("Please select a file.");
				 }*/
			 }
		 });
		 
		 stack.setAutoHeight();
		 stack.setAutoWidth();
		 stack.setMembersMargin(10);
		 stack.setDefaultLayoutAlign(Alignment.CENTER);

		 /*NamedFrame frame = new NamedFrame(TARGET);
                frame.setWidth("1");
                frame.setHeight("1");
                frame.setVisible(false);*/

		 /*VStack mainLayout = new VStack();
                //mainLayout.setWidth(300);
                //mainLayout.setHeight(200);
                mainLayout.setAutoHeight();
                mainLayout.setAutoWidth();*/

		 if (mode == Mode.CONVERSIONS) {         
			 CheckboxItem unzip = new CheckboxItem("unzip");
			 unzip.setDefaultValue(true);
			 unzip.setTitle("Unzip .zip file");
			 items.add(unzip);
			 CheckboxItem overwrite = new CheckboxItem("overwrite");
			 overwrite.setDefaultValue(false);
			 overwrite.setTitle("Overwrite existing file"); 
			 items.add(overwrite);
			 CheckboxItem convertpdf = new CheckboxItem("convertpdf");
			 convertpdf.setDefaultValue(true);
			 convertpdf.setTitle("Convert Word document to PDF"); 
			 items.add(convertpdf);
			 CheckboxItem streaming = new CheckboxItem("streaming");
			 streaming.setDefaultValue(true);
			 streaming.setTitle("Convert video file to streaming format(flv)"); 
			 items.add(streaming);
			 CheckboxItem thumbnail = new CheckboxItem("thumbnail");
			 thumbnail.setDefaultValue(true);
			 thumbnail.setTitle("Make thumbnail(48x48) from image");
			 items.add(thumbnail);
		 }
		 FormItem[] fitems = new FormItem[items.size()];
		 items.toArray(fitems);
		 uploadForm.setItems(fitems);
		 stack.addMember(uploadForm);
		 stack.addMember(uploadButton);
		 //mainLayout.addMember(stack);
		 //mainLayout.addMember(frame);
		 addChild(stack);
	 }

	 public String getFile() {
		 Object obj = fileItem.getValue();
		 if (obj == null)
			 return null;
		 else
			 return obj.toString();
	 }

	 public void setHiddenItem(String name, String value) {
		 for (HiddenItem item: hiddenItems)
			 if (item.getName().equals(name)) {
				 item.setValue(value);
				 return;
			 }
	 }

	 public void setAction(String url) {
		 uploadForm.setAction(url);
	 }

	 public void setUploadListener(UploadListener listener) {
		 this.listener = listener;
	 }

	 public void uploadComplete(String fileName) {
		 if (listener != null)
			 listener.uploadComplete(fileName);
	 }


	 private native void initComplete(Upload upload) /*-{
           $wnd.uploadComplete = function (fileName) {
               upload.@fr.pfgen.axiom.client.ui.widgets.vstacks.Upload::uploadComplete(Ljava/lang/String;)(fileName);
           };
        }-*/;

     public void setHeaderLabel(String header){
    	 Label headerLabel = new Label();
    	 headerLabel.setOverflow(Overflow.VISIBLE);
		 headerLabel.setAutoHeight();
		 headerLabel.setAutoWidth();
		 headerLabel.setStyleName("textTitle");
		 headerLabel.setContents(StringUtil.asHTML(header,true));
		 stack.addMember(headerLabel, 0);
     }
     
     public void setFileItemTitle(String title){
    	 fileItem.setTitle(title);
     }
     
     public UploadItem getFileItem(){
    	 return fileItem;
     }
     
     public VStack getStack(){
    	 return stack;
     }
     
     public IButton getUploadButton(){
    	 return uploadButton;
     }
     
     public void addLabel(Label label){
    	 stack.addMember(label);
     }
}
