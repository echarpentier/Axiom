package fr.pfgen.axiom.server.services;

import java.io.File;
import java.util.Hashtable;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import fr.pfgen.axiom.client.services.GetFilePathService;
import fr.pfgen.axiom.server.database.ConnectionPool;
import fr.pfgen.axiom.server.database.GenoAnalysisTable;

@Deprecated
@SuppressWarnings("serial")
public class GetFilePathServiceImpl extends RemoteServiceServlet implements GetFilePathService{

	private ConnectionPool pool;
	private Hashtable<String, File> appFiles; 
	
	@Override
	@SuppressWarnings("unchecked")
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		pool = (ConnectionPool)getServletContext().getAttribute("ConnectionPool");
		appFiles = (Hashtable<String, File>)getServletContext().getAttribute("ApplicationFiles");
	}

	@Override
	public String getDQCGraphPath() {
		return appFiles.get("analysisFile").getAbsolutePath()+"/DQCGraph/DQCGraph.png";
	}

	@Override
	public String getPIGGraphPath(String genoName) {
		return GenoAnalysisTable.getGenoPathFromName(pool, genoName)+"/QCReport/PIGBoxplots.pdf";
	}
}
