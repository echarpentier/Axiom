package fr.pfgen.axiom.server.services;

import fr.pfgen.axiom.client.services.LoginService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import fr.pfgen.axiom.server.database.ConnectionPool;
import fr.pfgen.axiom.server.database.UsersTable;
import fr.pfgen.axiom.shared.records.UserRecord;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class LoginServiceImpl extends RemoteServiceServlet implements
		LoginService {
	
	//private static final String USER_SESSION = "GWTAppUser";
	private ConnectionPool pool;
	//private Hashtable<String, File> appFiles; 
	
	//@SuppressWarnings("unchecked")
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		pool = (ConnectionPool)getServletContext().getAttribute("ConnectionPool");
		//appFiles = (Hashtable<String, File>)getServletContext().getAttribute("ApplicationFiles");
	}
	
	//private static final long serialVersionUID = 12247875631L;
	 
	private void setUserInSession(UserRecord user) {
	    HttpSession session = getThreadLocalRequest().getSession();
	    session.setMaxInactiveInterval(20);
	    //session.setAttribute("user_id", user.getUserID());
	    session.setAttribute("AxiomUser", user);
	}
	
	private UserRecord getUserFromSession() {
	    HttpSession session = getThreadLocalRequest().getSession();
	    if (session.isNew()){
	    	return null;
	    }
	  	return (UserRecord) session.getAttribute("AxiomUser");
	}
	
	@Override
	public UserRecord loginServer(String userName, String password){
		
		UserRecord user = UsersTable.getUser(pool, userName, password);
		if (user.getLoginText().startsWith("User authenticated !")){
			setUserInSession(user);
		}
		return user;
	}
	
	@Override
	public UserRecord checkUserLogin(){
		return getUserFromSession();
	}
}