package fr.pfgen.axiom.client.ui.widgets.grids.tileGrids;

import fr.pfgen.axiom.client.datasources.ArrayImageDS;
import fr.pfgen.axiom.client.services.ArrayImagesService;
import fr.pfgen.axiom.client.services.ArrayImagesServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.Criteria;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.util.EventHandler;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.events.MouseWheelEvent;
import com.smartgwt.client.widgets.events.MouseWheelHandler;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.events.ClickHandler;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;
import com.smartgwt.client.widgets.tile.TileGrid;
import com.smartgwt.client.widgets.tile.TileRecord;
import com.smartgwt.client.widgets.tile.events.RecordClickEvent;
import com.smartgwt.client.widgets.tile.events.RecordClickHandler;
import com.smartgwt.client.widgets.tile.events.RecordDoubleClickEvent;
import com.smartgwt.client.widgets.tile.events.RecordDoubleClickHandler;
import com.smartgwt.client.widgets.viewer.DetailViewerField;

public class ArrayImagesTileGrid extends TileGrid{
	
	private final ArrayImagesServiceAsync arrayImageService = GWT.create(ArrayImagesService.class);

	public ArrayImagesTileGrid(final String plateName){
		 
		this.setTitle("Array Images");
		this.setTileWidth(150);  
		this.setTileHeight(180);  
		this.setHeight100();
		this.setWidth100(); 
		this.setShowAllRecords(true);
		this.setShowEdges(true);
		this.setSelectionType(SelectionStyle.SINGLE);
		
		this.setDataSource(ArrayImageDS.getInstance());
		final Criteria criteria = new Criteria();
		criteria.addCriteria("plateName", plateName);
		this.setInitialCriteria(criteria);

		DetailViewerField imageField = new DetailViewerField("image");
		imageField.setType("image");
		imageField.setImageWidth(120);
		imageField.setImageHeight(120);
		DetailViewerField nameField = new DetailViewerField("name");
		DetailViewerField coords = new DetailViewerField("coords");
		
		this.addRecordDoubleClickHandler(new RecordDoubleClickHandler() {
			
			@Override
			public void onRecordDoubleClick(RecordDoubleClickEvent event) {
			
				Window window = new Window();  
				window.setTitle(event.getRecord().getAttribute("name"));  
				window.setHeight(610);
				window.setWidth(595);
				window.setCanDragResize(true);  
				window.setShowFooter(false);  
				window.setAutoCenter(true);
				window.adjustForContent(false);
				
				Canvas canvas = new Canvas();  
				canvas.adjustForContent(false);
				canvas.setOverflow(Overflow.SCROLL);
				canvas.setAutoHeight();
				canvas.setAutoWidth();

				final Img image = new Img(GWT.getModuleBaseURL()+"imageProvider?file="+event.getRecord().getAttribute("serverPath"));

				image.setOverflow(Overflow.HIDDEN);
				image.setSize(565);
				
				final int minSize = 565;  
				final int maxSize = 10000;  
				final int zoomMultiplier = 50;
			
				canvas.addChild(image);
				window.addItem(canvas);
				
				window.draw();
				
				canvas.addMouseWheelHandler(new MouseWheelHandler() {
					
					@Override
					public void onMouseWheel(MouseWheelEvent event) {
						int wheelDelta = EventHandler.getWheelDelta();  

						int newSize = image.getWidth() - wheelDelta * zoomMultiplier;  
						if (newSize < minSize) {  
							newSize = minSize;  
						} else if (newSize > maxSize) {  
							newSize = maxSize;  
						}  
						image.setWidth(newSize);  
						image.setHeight(newSize);
					}
				});

				
				
				//canvas.addChild(window); 
			}
		});
		
		this.addRecordClickHandler(new RecordClickHandler() {
			
			@Override
			public void onRecordClick(RecordClickEvent event) {
				
				
			}
		});
		
		Menu rightClickMenu = new Menu();
		MenuItem saveAs = new MenuItem("Save image as");
		saveAs.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(MenuItemClickEvent event) {
				TileRecord record = getSelectedRecord();
				com.google.gwt.user.client.Window.open(GWT.getModuleBaseURL() + "fileProvider?file="+record.getAttribute("serverPath"), "_self", "");
			}
		});
		MenuItem tagBadQuality = new MenuItem("Tag sample as poor quality");
		tagBadQuality.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(MenuItemClickEvent event) {
				TileRecord record = getSelectedRecord();
				SC.say(record.getAttribute("sampleID"));
				
			}
		});
		
		tagBadQuality.setEnabled(false);
		
		MenuItem downloadThumbnailsPdf = new MenuItem("Download thumbnails pdf");
		downloadThumbnailsPdf.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(MenuItemClickEvent event) {
				arrayImageService.downloadThumbnailsPdf(plateName, new AsyncCallback<String>() {
					
					@Override
					public void onSuccess(String result) {
						com.google.gwt.user.client.Window.open(GWT.getModuleBaseURL() + "fileProvider?file="+result, "_self", "");
					}
					
					@Override
					public void onFailure(Throwable caught) {
						SC.warn("Cannot create thumbnails pdf");
					}
				});
			}
		});
		
		rightClickMenu.addItem(saveAs);
		rightClickMenu.addItem(tagBadQuality);
		rightClickMenu.addItem(downloadThumbnailsPdf);
		
		this.setContextMenu(rightClickMenu);
		
		this.setFields(imageField, nameField,coords);
		
		this.setTilesPerLine(12);
		this.setAutoFetchData(true);
		
		//this.setAutoFetchData(false);
	}
}
