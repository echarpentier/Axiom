package fr.pfgen.axiom.server.servlets;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import fr.pfgen.axiom.server.database.ConnectionPool;
import fr.pfgen.axiom.server.utils.DatabaseUtils;
import fr.pfgen.axiom.server.utils.GlobalDefs;
import fr.pfgen.axiom.server.utils.ServerUtils;

public class AxiomContextListener implements ServletContextListener {
	
	@Override
	public void contextDestroyed(ServletContextEvent ctx) {
		ConnectionPool pool = (ConnectionPool)ctx.getServletContext().getAttribute("ConnectionPool");
		if (pool != null){
			pool.closeAllConnections();
			pool = null;
			ctx.getServletContext().removeAttribute("ConnectionPool");
		}
		Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {DriverManager.deregisterDriver(driver);} catch (SQLException e) {}
        }

	
		@SuppressWarnings("unchecked")
		Map<String, File> m = (Map<String, File>)ctx.getServletContext().getAttribute("ApplicationFiles");
		if(m!=null){	
			File tmpFolder = m.get("temporaryFolder");
			if(tmpFolder!=null) ServerUtils.deleteDirectory(tmpFolder);
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent ctx){
		/*File testFile = new File("/commun/data/users/echarpentier/server4/Axiom");
	
		try {
			IOUtils.chown(testFile, "echarpentier", true);
			IOUtils.chgrp(testFile, "avenir", true);
			IOUtils.chmod(testFile, "755", true);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		System.setProperty("java.awt.headless", "true");
		String cfgFile=ctx.getServletContext().getInitParameter("configurationFile");
		if(cfgFile==null) throw new RuntimeException("No path for configuration file found at application start up !");		
		Properties props = new Properties();
		InputStream xmlStream = null;
		
		try {
			xmlStream=ctx.getServletContext().getResourceAsStream("/WEB-INF/config/"+cfgFile);
			if(xmlStream==null) throw new FileNotFoundException(
					"Cannot get \"/WEB-INF/config/"+cfgFile+"\"."
					+ ctx.getServletContext().getRealPath("/WEB-INF/config/"+cfgFile)+" "+
					ctx.getServletContext().getContextPath()
					);
			props.loadFromXML(xmlStream);
			xmlStream.close();
			ctx.getServletContext().log(props.toString());
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		String driver = props.getProperty("JDBCDriver");
		String uri = props.getProperty("JDBCConnectionURL");
		String login = props.getProperty("DBlogin");
		String password = props.getProperty("DBpassword");
		int connectionPoolSize = java.lang.Integer.parseInt(props.getProperty("ConnectionPoolSize"));
		int connectionPoolMax = java.lang.Integer.parseInt(props.getProperty("ConnectionPoolMax"));
		ConnectionPool connectionPool;
		try {
			connectionPool = new ConnectionPool(driver, uri, login, password, connectionPoolSize, connectionPoolMax, true);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		ctx.getServletContext().setAttribute("ConnectionPool", connectionPool);
		
		//Set application Files on server
		final File axiomFolder = new File(props.getProperty("AxiomPath"));
		if (!axiomFolder.isDirectory()){
			if (!axiomFolder.mkdirs()){
				throw new RuntimeException("Can't create directory "+axiomFolder.getAbsolutePath());
			}
		}else{
			if (!axiomFolder.canWrite()){
				throw new RuntimeException("Can't write in "+axiomFolder.getAbsolutePath());
			}
		}
		
		final File dataFolder = new File(props.getProperty("DataPath"));
		if (!dataFolder.isDirectory()){
			if (!dataFolder.mkdirs()){
				throw new RuntimeException("Can't create directory "+dataFolder.getAbsolutePath());
			}
		}else{
			if (!dataFolder.canWrite()){
				throw new RuntimeException("Can't write in "+dataFolder.getAbsolutePath());
			}
		}
		
		String axiomPathReplacementInDB = props.getProperty("AxiomPathReplacementInDB");
		if (axiomPathReplacementInDB==null || axiomPathReplacementInDB.isEmpty()) throw new IllegalStateException("No value for axiom path replacement string in database !!");
		
		String dataPathReplacementInDB = props.getProperty("DataPathReplacementInDB");
		if (dataPathReplacementInDB==null || dataPathReplacementInDB.isEmpty()) throw new IllegalStateException("No value for data path replacement string in database !!");
		
		final File arrayImageFile = new File(axiomFolder, props.getProperty("AxiomPath.ArrayImagePath"));
		if (!arrayImageFile.isDirectory()){
			if (!arrayImageFile.mkdirs()){
				throw new RuntimeException("Can't create directory "+arrayImageFile.getAbsolutePath());
			}
		}else{
			if (!arrayImageFile.canWrite()){
				throw new RuntimeException("Can't write in "+arrayImageFile.getAbsolutePath());
			}
		}
		
		final File platesFile = new File(dataFolder, "Data");
		if (!platesFile.exists()){
			throw new RuntimeException(platesFile.getAbsolutePath()+" doesn't exist");
		}else if (!platesFile.canRead()){
			throw new RuntimeException("Can't read "+platesFile.getAbsolutePath());
		}
		
		final File analysisFile = new File(axiomFolder, props.getProperty("AxiomPath.AnalysisPath"));
		if (!analysisFile.isDirectory()){
			if (!analysisFile.mkdirs()){
				throw new RuntimeException("Can't create directory "+analysisFile.getAbsolutePath());
			}
		}else{
			if (!analysisFile.canWrite()){
				throw new RuntimeException("Can't write in "+analysisFile.getAbsolutePath());
			}
		}
		
		final File affymetrixLibraryFilesFolder = new File(axiomFolder, props.getProperty("AxiomPath.AffxLibraryFilesFolder"));
		if (!affymetrixLibraryFilesFolder.exists()){
			throw new RuntimeException(affymetrixLibraryFilesFolder.getAbsolutePath()+" doesn't exist");
		}else if (!affymetrixLibraryFilesFolder.canRead()){
			throw new RuntimeException("Can't read "+affymetrixLibraryFilesFolder.getAbsolutePath());
		}
		
		final File affymetrixAnnotationFilesFolder = new File(axiomFolder, props.getProperty("AxiomPath.AffxAnnotationFilesFolder"));
		if (!affymetrixAnnotationFilesFolder.exists()){
			throw new RuntimeException(affymetrixAnnotationFilesFolder.getAbsolutePath()+" doesn't exist");
		}else if (!affymetrixAnnotationFilesFolder.canRead()){
			throw new RuntimeException("Can't read "+affymetrixAnnotationFilesFolder.getAbsolutePath());
		}
		
		final File aptBinFolder = new File(props.getProperty("APTbinPath"));
		if (!aptBinFolder.exists()){
			throw new RuntimeException(aptBinFolder.getAbsolutePath()+" doesn't exist");
		}else if (!aptBinFolder.canExecute()){
				throw new RuntimeException("Can't execute APT bin, check for permissions in folder: "+aptBinFolder.getAbsolutePath());
		}
		
		final File RScriptBin = new File(props.getProperty("RscriptBinPath"));
		if (!RScriptBin.exists()){
			throw new RuntimeException(RScriptBin.getAbsolutePath()+" doesn't exist");
		}else if (!RScriptBin.canExecute()){
			throw new RuntimeException("Can't execute "+RScriptBin.getAbsolutePath());
		}
		
		final File rScriptsFolder = new File(axiomFolder, props.getProperty("AxiomPath.Rscripts"));
		if (!rScriptsFolder.exists()){
			throw new RuntimeException(rScriptsFolder.getAbsolutePath()+" doesn't exist");
		}else if (!rScriptsFolder.canRead() || !rScriptsFolder.canExecute()){
			throw new RuntimeException("Can't read or execute: "+rScriptsFolder.getAbsolutePath());
		}
		
		final File tmpFolder = new File(axiomFolder, props.getProperty("AxiomPath.temporaryFolder"));
		if (!tmpFolder.isDirectory()){
			if (!tmpFolder.mkdirs()){
				throw new RuntimeException("Can't create directory "+tmpFolder.getAbsolutePath());
			}
		}else{
			ServerUtils.deleteDirectory(tmpFolder);
			if (!tmpFolder.mkdirs()){
				throw new RuntimeException("Can't create directory "+tmpFolder.getAbsolutePath());
			}
			if (!tmpFolder.canWrite()){
				throw new RuntimeException("Can't write in "+tmpFolder.getAbsolutePath());
			}
		}
		
		final File imageNotFoundFile = new File(axiomFolder, props.getProperty("AxiomPath.ImageNotFoundPath"));
		
		ctx.getServletContext().setAttribute("MaxGenoAnalysis", Integer.parseInt(props.getProperty("MaxGenoAnalysis")));
		ctx.getServletContext().setAttribute("MaxQCAnalysis", Integer.parseInt(props.getProperty("MaxQCAnalysis")));
		
		Map<String, File> appFiles = new Hashtable<String, File>();
		appFiles.put("mainFile", axiomFolder);
		GlobalDefs.getInstance().setAxiomPath(axiomFolder.getAbsolutePath());
		GlobalDefs.getInstance().setAxiomPathReplacementInDB(axiomPathReplacementInDB);
		GlobalDefs.getInstance().setDataPath(dataFolder.getAbsolutePath());
		GlobalDefs.getInstance().setDataPathReplacementInDB(dataPathReplacementInDB);
		appFiles.put("arrayImageFile", arrayImageFile);
		appFiles.put("platesFile", platesFile);
		appFiles.put("analysisFile", analysisFile);
		appFiles.put("affymetrixLibraryFilesFolder", affymetrixLibraryFilesFolder);
		appFiles.put("affymetrixAnnotationFilesFolder", affymetrixAnnotationFilesFolder);
		appFiles.put("APTbin", aptBinFolder);
		appFiles.put("RScriptBin", RScriptBin);
		appFiles.put("RScripts", rScriptsFolder);
		appFiles.put("temporaryFolder", tmpFolder);
		appFiles.put("imageNotFound", imageNotFoundFile);
		ctx.getServletContext().setAttribute("ApplicationFiles", appFiles);
		
		/*****  Database creation
		 * Queries in axiom.sql are all executed.
		 * Tables will be created if they do not exist ("CREATE TABLE IF NOT EXISTS", "INSERT IGNORE", etc...)
		 * The instructions to init the database are in axiom.sql located under folder specified in servlet context
		 */
		File sqlFile = new File(axiomFolder, props.getProperty("AxiomPath.DatabaseCreationFile"));
		DatabaseUtils.initDatabase(connectionPool,sqlFile);
	}
}
