package fr.pfgen.axiom.server.database;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import fr.pfgen.axiom.server.utils.DatabaseUtils;
import fr.pfgen.axiom.server.utils.SQLUtils;
import fr.pfgen.axiom.server.utils.ServerUtils;

import java.sql.Timestamp;
import java.text.ParseException;

public class UpdateDatabase {

	private static class CreateThumbnailsThread extends Thread{
		private String plateName;
		private File[] jpgs;
		private String arrayImagePath;
		
		public CreateThumbnailsThread(String plateName,File[] jpgs, String arrayImagePath){
			this.plateName = plateName;
			this.jpgs = jpgs;
			this.arrayImagePath = arrayImagePath;
		}
		
		@Override
		public void run() {
			File plateImageFolder = new File(arrayImagePath+"/"+plateName);
			if (plateImageFolder.exists()){ServerUtils.deleteDirectory(plateImageFolder);}
			if (plateImageFolder.mkdirs()){
				for (File jpg : jpgs){
					String jpgName = jpg.getName();
					File outputFile = new File(plateImageFolder+"/"+jpgName.replaceAll("\\.JPG$",".png"));
					if(!jpg.exists() || !jpg.isFile() || !jpg.getName().toLowerCase().endsWith(".jpg"))  {
						continue;
					}else{
						
						try{
							BufferedImage img = ImageIO.read(jpg);
							img = ServerUtils.getScaledInstance(img, 300, 300, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
							ImageIO.write(img, "png", outputFile);
						}catch (IOException ioe){
							throw new RuntimeException(ioe);
						}
					}
				}
			}
		}
	}
	
	public static String updateDatabase(ConnectionPool pool, Hashtable<String, File> appFiles){
		lookUpLibraryFiles(pool, appFiles.get("affymetrixLibraryFilesFolder"));
		lookUpAnnotationFiles(pool, appFiles.get("affymetrixAnnotationFilesFolder"));
		final String insertPlates = "INSERT IGNORE INTO plates SET plate_name=?,plate_original_name=?, plate_barcode=?,created=?";
		final String insertSamples = "INSERT IGNORE INTO samples SET sample_name=?,sample_original_name=?,plate_id=?,plate_coordX=?,plate_coordY=?,sample_path=?";
		ExecutorService threadExecutor = Executors.newFixedThreadPool(4);
		
		PreparedStatement psPlates = null;
		PreparedStatement psSamples = null;
		Connection con = null;
		
		try {
			con = pool.getConnection();
			//prepare statements
			psPlates = con.prepareStatement(insertPlates,Statement.RETURN_GENERATED_KEYS);
			psSamples = con.prepareStatement(insertSamples);
			
			//Get plate folders from the root path
			File[] plates = GetPathOfPlates(appFiles.get("platesFile").getAbsolutePath());
			for (File plate : plates) {
				String plateName = plate.getName();
				//Get plate barcode from arr files in the folder
				String plateBarcode = GetPlateBarcode(plate);
				
				if (plateBarcode == null){
					continue;
				}
				
				Date plateCreationDate = GetPlateCreationDate(plate);
				
				if (PlateAlreadyExists(plateBarcode,con)){
					//skip this plate
					continue;
				}else{
					//insert plate into db
					int plateID = InsertPlateIntoDB(psPlates,plateName,plateName,plateBarcode,plateCreationDate);
					if (plateID<0){
						continue;
					}else{
						//get files inside plate folder
						File[] cels = GetCelsInPlate(plate);
						File[] jpgs = GetJpgsInPlate(plate);
						
						for (File cel : cels) {
							String celName = cel.getName();
							String originalSampleName = celName.replaceFirst("\\.CEL$", "");
							String sampleName = GetSampleName(celName);
							String samplePath = cel.getAbsolutePath();
							
							Map<String, Integer> coords = getSampleCoordinates(plate,celName);
							
							//insert sample
							InsertSampleIntoDB(psSamples,sampleName,originalSampleName,plateID,coords.get("row"),coords.get("col"),samplePath);
						}
						
						UpdateDatabase.CreateThumbnailsThread th = new UpdateDatabase.CreateThumbnailsThread(plateName, jpgs, appFiles.get("arrayImageFile").getAbsolutePath());
						threadExecutor.execute(th);
					}
				}
			}
			psPlates.close();
			psSamples.close();
		} catch (Exception e) {
			e.printStackTrace();
			return e.toString();
		} finally {
			SQLUtils.safeClose(psPlates);
			SQLUtils.safeClose(psSamples);
			pool.close(con);
		}
		threadExecutor.shutdown();
		return "Update done !!";
	}
	
	private static void lookUpLibraryFiles(ConnectionPool pool, File libraryFilesFolder){
		if (!libraryFilesFolder.exists() || !libraryFilesFolder.canRead()){
			throw new RuntimeException("Cannot read "+libraryFilesFolder.getAbsolutePath());
		}
		
		final List<String> libraryFiles = LibraryFilesTable.getLibraryFilesNames(pool);
		
		File[] newLibraryFiles = libraryFilesFolder.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return (!libraryFiles.contains(name));
			}
		});
		
		final String psInsertLibraryFile = "INSERT IGNORE INTO library_files SET library_name=?";
		
		PreparedStatement ps = null;
		Connection con = null;
		
		try{
			con = pool.getConnection();
			ps = con.prepareStatement(psInsertLibraryFile);
			
			for (File library : newLibraryFiles) {
				File xmlFile = library.listFiles(new FilenameFilter() {
					
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith("apt-probeset-genotype.AxiomGT1.xml");
					}
				})[0];
				if (xmlFile.exists()){
					ps.setString(1, library.getName());
					ps.executeUpdate();
					ps.clearParameters();
				}
			}
			
			ps.close();
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			SQLUtils.safeClose(ps);
			pool.close(con);
		}
	}
	
	private static void lookUpAnnotationFiles(ConnectionPool pool, File annotationFilesFolder){
		if (!annotationFilesFolder.exists() || !annotationFilesFolder.canRead()){
			throw new RuntimeException("Cannot read "+annotationFilesFolder.getAbsolutePath());
		}
		
		final List<String> annotFiles = AnnotationFilesTable.getAnnotationFilesNames(pool);

		File[] newAnnotFiles = annotationFilesFolder.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return (!annotFiles.contains(name));
			}
		});
		
		final String psInsertAnnotFile = "INSERT IGNORE INTO annot_files SET annot_name=?";
		
		PreparedStatement ps = null;
		Connection con = null;
		
		try{
			con = pool.getConnection();
			ps = con.prepareStatement(psInsertAnnotFile);
			
			for (File annot : newAnnotFiles) {
				ps.setString(1, annot.getName());
				ps.executeUpdate();
				ps.clearParameters();
			}
			
			ps.close();
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			SQLUtils.safeClose(ps);
			pool.close(con);
		}
	}
	
	private static void InsertSampleIntoDB(PreparedStatement psSamples, String sampleName, String originalName, int plateID, int coordX, int coordY, String samplePath){
		try {
			psSamples.setString(1, sampleName);
			psSamples.setString(2, originalName);
			psSamples.setInt(3, plateID);
			psSamples.setInt(4, coordX);
			psSamples.setInt(5, coordY);
			psSamples.setString(6, DatabaseUtils.realDataPathToDbPath(samplePath));
			
			psSamples.executeUpdate();
			
			psSamples.clearParameters();
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
 	private static Map<String,Integer> getSampleCoordinates(File plate,String celName){
		Map<String,Integer> coords = new Hashtable<String, Integer>();
		String path = plate.getPath();
		String arrName = celName.replaceAll("\\.CEL$", ".ARR");
		
		InputStream in = null;
		XMLStreamReader parser = null;
		
		try {
			URL u = new URL("file://"+path+"/"+arrName);
			in = u.openStream();
			XMLInputFactory factory = XMLInputFactory.newInstance();
			parser = factory.createXMLStreamReader(in);
			
			for (int event = parser.next();  
		        event != XMLStreamConstants.END_DOCUMENT;
		        event = parser.next()) {
				if (parser.isStartElement() && parser.getLocalName() == "PhysicalArray"){
					for (int i = 0; i < parser.getAttributeCount(); i++) {
						if (parser.getAttributeLocalName(i) == "MediaRow"){
							coords.put("row",Integer.decode(parser.getAttributeValue(i)));
							continue;
						}else if (parser.getAttributeLocalName(i) == "MediaCol"){
							coords.put("col",Integer.decode(parser.getAttributeValue(i)));
							break;
						}
					}
					break;
				}
			}
			
			parser.close();
			in.close();

		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		} finally {
			if (parser!=null){
				try {parser.close();} catch (XMLStreamException e) {}
			}
			if (in!=null){
				try {in.close();} catch (IOException e) {}
			}
		}
		
		return coords;
	}
	
	private static String GetSampleName(String celName){
		String sampleName;
		sampleName = celName.replaceFirst("\\.CEL$", "");
		if (sampleName.matches("^.*_[A-H][0-1][0-9]$")){
			sampleName = sampleName.replaceFirst("_[A-H][0-1][0-9]$", "");
		}else if(sampleName.matches("^[A-H][0-1][0-9]_.*$")){
			sampleName = sampleName.replaceFirst("^[A-H][0-1][0-9]_", "");
		}
		
		return sampleName;
	}
	
 	private static int InsertPlateIntoDB(PreparedStatement psPlates, String plateName, String plateOriginalName, String plateBarcode, Date plateCreationDate){
		int plateID = -1;
		
		ResultSet rsk = null;
		try {
			psPlates.setString(1, plateName);
			psPlates.setString(2, plateName);
			psPlates.setString(3, plateBarcode);
			Timestamp sqlDate = DatabaseUtils.toDBDateFormat(plateCreationDate);
			psPlates.setTimestamp(4, sqlDate);
			
			psPlates.executeUpdate();
			
			rsk = psPlates.getGeneratedKeys();
			
			while (rsk.next()) {
				plateID = rsk.getInt(1);
			}
			
	        rsk.close();
			psPlates.clearParameters();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		} finally {
			SQLUtils.safeClose(rsk);
		}
		return plateID;
	}
	
	private static boolean PlateAlreadyExists(String barcode,Connection connection){
		
		String query = "SELECT * FROM plates WHERE plate_barcode=?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean exists;
		try{
			ps = connection.prepareStatement(query);
			ps.setString(1, barcode);
			rs = ps.executeQuery();
			if (rs.next()){
				exists = true;
			}else{
				exists = false;
			}
			ps.close();
			rs.close();
		} catch (SQLException e){
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			SQLUtils.safeClose(rs);
			SQLUtils.safeClose(ps);
		}
		return exists;
	}
	
	private static Date GetPlateCreationDate(File plateDir){
		Date created = null;
		File[] arrs = GetArrsInPlate(plateDir);
		for (File arr : arrs) {
			InputStream in = null;
			XMLStreamReader parser = null;
			try {
				URL u = new URL("file://"+arr.toString());
				in = u.openStream();
				XMLInputFactory factory = XMLInputFactory.newInstance();
				parser = factory.createXMLStreamReader(in);

				for (int event = parser.next();  
				event != XMLStreamConstants.END_DOCUMENT;
				event = parser.next()) {
					if (parser.isStartElement() && parser.getLocalName() == "ArraySetFile"){
						for (int i = 0; i < parser.getAttributeCount(); i++) {
							if (parser.getAttributeLocalName(i) == "CreatedDateTime"){
								created = DatabaseUtils.stringToDate(parser.getAttributeValue(i));
								break;
							}else{
								continue;
							}
						}
						break;
					}
				}
				
				in.close();
				parser.close();
			}catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (XMLStreamException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			} finally {
				if (parser!=null){
					try {parser.close();} catch (XMLStreamException e) {}
				}
				if (in!=null){
					try {in.close();} catch (IOException e) {}
				}
			}
			break;
		}
		return created;
	}
	
 	private static String GetPlateBarcode(File plateDir){
		String barcode = null;
		File[] arrs = GetArrsInPlate(plateDir);
		List<String> barcodeList = new ArrayList<String>();
		
		for (File arr : arrs) {
			InputStream in = null;
			XMLStreamReader parser = null;
			
			try {
				URL url = new URL("file://"+arr.toString());
				in = url.openStream();
				XMLInputFactory factory = XMLInputFactory.newInstance();
				parser = factory.createXMLStreamReader(in);
				
				for (int event = parser.next();  
			        event != XMLStreamConstants.END_DOCUMENT;
			        event = parser.next()) {
					if (parser.isStartElement() && parser.getLocalName() == "PhysicalArray"){
						for (int i = 0; i < parser.getAttributeCount(); i++) {
							if (parser.getAttributeLocalName(i) == "AffyBarcode"){
								barcode = parser.getAttributeValue(i);
								barcodeList.add(barcode);
								break;
							}else{
								continue;
							}
						}
						break;
					}
				}
				
				parser.close();
				in.close();

			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (XMLStreamException e) {
				throw new RuntimeException(e);
			} finally {
				if (parser!=null){
					try {parser.close();} catch (XMLStreamException e) {}
				}
				if (in!=null){
					try {in.close();} catch (IOException e) {}
				}
			}
		}
		
		int ok = 1;
		for (int i = 1; i < barcodeList.size(); i++) {
			if (barcodeList.get(i-1).compareTo(barcodeList.get(i))!=0){
				ok = 0;
				break;
			}
		}
		
		if (ok == 1){
			return barcodeList.get(0);
		}else{
			return null;
		}
	}
	
	private static File[] GetCelsInPlate(File plate){
		File[] cels = plate.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".CEL");
			}
		});
		
		Arrays.sort(cels, new Comparator<File>(){
		    @Override
			public int compare(File f1, File f2){
		        return f1.getName().compareTo(f2.getName());
		    } 
		});

		return cels;
	}
	
	private static File[] GetArrsInPlate(File plate){
		File[] arrs = plate.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".ARR");
			}
		});
		
		Arrays.sort(arrs, new Comparator<File>(){
		    @Override
			public int compare(File f1, File f2){
		        return f1.getName().compareTo(f2.getName());
		    }
		});
		
		return arrs;
	}
	
	private static File[] GetJpgsInPlate(File plate){
		return plate.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".JPG");
			}
		});
	}
	
	private static File[] GetPathOfPlates(String root){

		File platesDir = new File(root);
		File[] plates = platesDir.listFiles(new FileFilter() {
			//first check if file under root is a directory
			@Override
			public boolean accept(File dir) {
				if (dir.isDirectory()){
					String[] files = dir.list(new FilenameFilter() {	
						//check if directory contains .CEL files
						@Override
						public boolean accept(File f, String name) {
							return (name.endsWith(".CEL"));
						}
					});
					
					if(files.length>0){
						return true;
					}else{
						return false;
					}
				}else{
					return false;
				}
			}
		});
		return plates;
	}
}