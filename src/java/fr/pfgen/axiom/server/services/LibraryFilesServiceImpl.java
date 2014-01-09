package fr.pfgen.axiom.server.services;

import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import fr.pfgen.axiom.client.services.LibraryFilesService;
import fr.pfgen.axiom.server.database.ConnectionPool;
import fr.pfgen.axiom.server.database.LibraryFilesTable;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class LibraryFilesServiceImpl extends RemoteServiceServlet implements LibraryFilesService{
	
	private ConnectionPool pool;
	//private Hashtable<String, File> appFiles; 

	@Override
	//@SuppressWarnings("unchecked")
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		pool = (ConnectionPool)getServletContext().getAttribute("ConnectionPool");
		//appFiles = (Hashtable<String, File>)getServletContext().getAttribute("ApplicationFiles");
	}

	@Override
	public List<String> getLibraryFilesFolderNames() {
		return LibraryFilesTable.getLibraryFilesNames(pool);
	}
}
