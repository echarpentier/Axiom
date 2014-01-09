package fr.pfgen.axiom.server.services;

import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import fr.pfgen.axiom.client.services.AnnotationFilesService;
import fr.pfgen.axiom.server.database.AnnotationFilesTable;
import fr.pfgen.axiom.server.database.ConnectionPool;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class AnnotationFilesServiceImpl extends RemoteServiceServlet implements AnnotationFilesService{

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
	public List<String> getAnnotationFilesNames() {
		return AnnotationFilesTable.getAnnotationFilesNames(pool);
	}
}
