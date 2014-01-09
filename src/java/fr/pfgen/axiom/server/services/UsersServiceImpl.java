package fr.pfgen.axiom.server.services;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import fr.pfgen.axiom.client.services.UsersService;
import fr.pfgen.axiom.server.database.ConnectionPool;
import fr.pfgen.axiom.server.database.UsersTable;
import fr.pfgen.axiom.shared.GenericGwtRpcList;
import fr.pfgen.axiom.shared.records.UserRecord;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class UsersServiceImpl extends RemoteServiceServlet implements UsersService{

	private ConnectionPool pool;
	//private Hashtable<String, File> appFiles; 
	
	//@SuppressWarnings("unchecked")
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		pool = (ConnectionPool)getServletContext().getAttribute("ConnectionPool");
		//appFiles = (Hashtable<String, File>)getServletContext().getAttribute("ApplicationFiles");
	}
	
	@Override
	public List<UserRecord> fetch(Integer startRow, Integer endRow, String sortBy, Map<String, String> filterCriteria) {
		
		GenericGwtRpcList<UserRecord> outList = new GenericGwtRpcList<UserRecord>();
	    
		ConnectionPool pool = (ConnectionPool)getServletContext().getAttribute("ConnectionPool");
    	List<UserRecord> out = UsersTable.getUsers(pool, startRow,endRow,sortBy,filterCriteria);
    	
    	outList.addAll(out);
    	outList.setTotalRows(out.size());
    	
    	return outList;
	}

	@Override
	public UserRecord add(UserRecord record) {
		return UsersTable.addUser(pool, record);
	}

	@Override
	public UserRecord update(UserRecord data) {
		System.out.println(data.getUserID()+" "+data.getFirstname()+" "+data.getLastname());
		return null;
	}

	@Override
	public void remove(UserRecord data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String download(String sortBy, Map<String, String> criterias) {
		// TODO Auto-generated method stub
		return null;
	}

}
