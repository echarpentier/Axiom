package fr.pfgen.axiom.server.services;

import java.io.File;
import java.util.Hashtable;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import fr.pfgen.axiom.client.services.UpdateDatabaseService;
import fr.pfgen.axiom.server.database.ConnectionPool;
import fr.pfgen.axiom.server.database.UpdateDatabase;

@SuppressWarnings("serial")
public class UpdateDatabaseServiceImpl extends RemoteServiceServlet implements UpdateDatabaseService {

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
	public synchronized String updateDatabase(){
		//ConnectionPool pool = (ConnectionPool)getServletContext().getAttribute("ConnectionPool");
		return UpdateDatabase.updateDatabase(pool,appFiles);
	}
}