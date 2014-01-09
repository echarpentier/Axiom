package fr.pfgen.axiom.client.datasources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import fr.pfgen.axiom.client.services.ArrayImagesService;
import fr.pfgen.axiom.client.services.ArrayImagesServiceAsync;
import fr.pfgen.axiom.shared.GenericGwtRpcList;
import fr.pfgen.axiom.shared.records.ArrayImageRecord;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.data.DSRequest;
import com.smartgwt.client.data.DSResponse;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.DataSourceField;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.data.fields.DataSourceIntegerField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.rpc.RPCResponse;
import com.smartgwt.client.types.DSDataFormat;
import com.smartgwt.client.types.DSProtocol;
import com.smartgwt.client.types.FieldType;
import com.smartgwt.client.widgets.tile.TileRecord;

public class ArrayImageDS extends DataSource  {
	
	private final String lettersCoords = "ABCDEFGH";
	private static ArrayImageDS instance;
	private ArrayImagesServiceAsync serviceAsync;
	
	// forces to use the singleton through getInstance();
	private ArrayImageDS(){
		setDataProtocol (DSProtocol.CLIENTCUSTOM);
        setDataFormat (DSDataFormat.CUSTOM);
        setClientOnly (false);
        setDataSourceFields();
		serviceAsync = getServiceAsync();
	};
	
	public static ArrayImageDS getInstance(){
		if (instance == null){
			instance = new ArrayImageDS();
		}
		return (instance);
	}
	
	@Override
    protected Object transformRequest (DSRequest request) {
        String requestId = request.getRequestId ();
        DSResponse response = new DSResponse ();
        response.setAttribute ("clientContext", request.getAttributeAsObject ("clientContext"));
        // Asume success
        response.setStatus (0);
        switch (request.getOperationType ()) {
            case FETCH:
                executeFetch (requestId, request, response);
                break;
            default:
                // Operation not implemented.
                break;
        }
        return request.getData ();
    }

	@SuppressWarnings("unchecked")
    protected void executeFetch (final String requestId,final DSRequest request,final DSResponse response) {
    	final Integer startRow = request.getStartRow();
		final Integer endRow = request.getEndRow();
		Criteria criteria = request.getCriteria();
		Map<String, String> criterias = new HashMap<String, String>();
		if (criteria != null) {			
			criterias = criteria.getValues();
		}
		serviceAsync.fetch(startRow, 
				endRow, 
				// we can't use request.getSortBy() here because it throws a ClassCastException (known bug).
				//TODO: replace with request.getSortBy() as soon as the bug is fixed.
				request.getAttribute("sortBy"), 
				criterias,
				new AsyncCallback<List<ArrayImageRecord>>() {
			@Override
			public void onFailure(Throwable caught) {
				response.setStatus(RPCResponse.STATUS_FAILURE);
				processResponse(requestId, response);
			}

			@Override
			public void onSuccess(List<ArrayImageRecord> result) {
				List<TileRecord> records = new ArrayList<TileRecord>();
				for (ArrayImageRecord data : result) {
					TileRecord newRec = getNewRecordInstance();
					copyValues(data, newRec);
					records.add(newRec);
				}
				// if those are set, the client wants paging. you have to use GenericGwtRpcList
				if (startRow != null && endRow != null && result instanceof GenericGwtRpcList<?>) {					
					Integer totalRows = result.size();
					response.setStartRow(startRow);
					if (totalRows == null) {
						throw new NullPointerException("totalRows cannot be null when using GenericGwtRpcList");
					}
					// endRow can't be higher than totalRows
					response.setEndRow(endRow.intValue() < totalRows.intValue() ? endRow : totalRows);
					response.setTotalRows(totalRows);
				}
				
				response.setData(records.toArray(new Record[result.size()]));
				processResponse(requestId, response);
			}
		});
	}

    public void copyValues (ArrayImageRecord from, TileRecord to) {
    	to.setAttribute("sampleID", from.getSampleID());
        to.setAttribute("name", from.getName());
        to.setAttribute("image", GWT.getModuleBaseURL()+from.getImage());
        to.setAttribute("coordX", from.getCoordX());
        to.setAttribute("coordY", from.getCoordY());
        to.setAttribute("serverPath", from.getServerPath());
        to.setAttribute("coords", lettersCoords.substring(from.getCoordX(), from.getCoordX()+1)+"-"+(from.getCoordY()+1));
    }
    
    private void setDataSourceFields() {
		List<DataSourceField> fields = getDataSourceFields();
		if (fields != null) {
			for (DataSourceField field : fields) {
				addField(field);
			}
		}
	}
	
	private List<DataSourceField> getDataSourceFields() {
		
		List<DataSourceField> fields = new ArrayList<DataSourceField>();
    	
		//Declaring datasource fields as they appear in DB table simplifies service implementation
        DataSourceField field;
        field = new DataSourceIntegerField("sampleID");
        field.setPrimaryKey(true);
        fields.add(field);
        field = new DataSourceField("image", FieldType.IMAGE);
        //field.setShowFileInline(false);
        field.setRequired (true);
        fields.add(field);
        field = new DataSourceTextField ("name");
        //field.setPrimaryKey(true);
        field.setRequired (true);
        fields.add(field);
        field = new DataSourceIntegerField("coordX");
        field.setRequired (true);
        field.setHidden(true);
        fields.add(field);
        field = new DataSourceIntegerField("coordY");
        field.setRequired (true);
        field.setHidden(true);
        fields.add(field);
        field = new DataSourceTextField ("serverPath");
        field.setRequired (true);
        field.setHidden(true);
        fields.add(field);
        field = new DataSourceTextField ("coords");
        field.setRequired (true);
        fields.add(field);
        
        return fields;
    }

	public ArrayImageRecord getNewDataObjectInstance(){
		return new ArrayImageRecord();
	}

	public TileRecord getNewRecordInstance(){
		return new TileRecord();
	}

	public ArrayImagesServiceAsync getServiceAsync(){
		return GWT.create(ArrayImagesService.class);
	}
}
