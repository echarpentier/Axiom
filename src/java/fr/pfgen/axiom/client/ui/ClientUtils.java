package fr.pfgen.axiom.client.ui;

import com.smartgwt.client.data.SimpleType;
import com.smartgwt.client.types.FieldType;
import com.smartgwt.client.widgets.form.validator.RegExpValidator;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import fr.pfgen.axiom.client.ui.widgets.MainArea;

public class ClientUtils {
	
	public static class NameType extends SimpleType {
		public NameType() {
			super("nameType", FieldType.TEXT);
			RegExpValidator validator = new RegExpValidator("^\\w{3,19}$");
			validator.setErrorMessage("Input must contain at least 3 characters and at most 19. Only word characters allowed (a-z,A-Z,_,0-9)");
			setValidators(validator);
		}
	}
	
	public static class OfficeType extends SimpleType {
		public OfficeType() {
			super("officeType", FieldType.TEXT);
			RegExpValidator validator = new RegExpValidator("^\\w{2,5}$");
			validator.setErrorMessage("Input must contain at least 2 characters and at most 5. Only word characters allowed (a-z,A-Z,_,0-9)");
			setValidators(validator);
		}
	}
	
	public static class EmailType extends SimpleType {
		public EmailType() {
			super("emailType", FieldType.TEXT);
			RegExpValidator validatorFormat = new RegExpValidator("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$");
			RegExpValidator validatorLength = new RegExpValidator("^.{6,59}$");
			validatorFormat.setErrorMessage("Invalid email adress");
			setValidators(validatorFormat,validatorLength);
		}
	}

	public static boolean tabExists(final String tabID){
		if (MainArea.getTopTabSet().getTab(tabID)!=null){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * Export data from a listrgrid
	 * @param listGrid the {@link ListGrid}
	 * @return a {@link StringBuilder} containing data in CSV format
	 */
	public static StringBuilder exportCSV(ListGrid listGrid) {
		StringBuilder stringBuilder = new StringBuilder(); // csv data in here
		
		// column names
		ListGridField[] fields = listGrid.getFields();
		for (int i = 0; i < fields.length; i++) {
			ListGridField listGridField = fields[i];
			stringBuilder.append("\"");
			stringBuilder.append(listGridField.getName());
			stringBuilder.append("\",");
		}
		stringBuilder.deleteCharAt(stringBuilder.length() - 1); // remove last ","
		stringBuilder.append("\n");
		
		// column data
		ListGridRecord[] records = listGrid.getRecords();
		for (int i = 0; i < records.length; i++) {
			ListGridRecord listGridRecord = records[i];
			ListGridField[] listGridFields = listGrid.getFields();
			for (int j = 0; j < listGridFields.length; j++) {
				ListGridField listGridField = listGridFields[j];
				stringBuilder.append("\"");
				stringBuilder.append(listGridRecord.getAttribute(listGridField.getName()));
				stringBuilder.append("\",");
			}
			stringBuilder.deleteCharAt(stringBuilder.length() - 1); // remove last ","
			stringBuilder.append("\n");
		}
		return stringBuilder;
	}
}
