package fr.pfgen.axiom.server.database;

import java.sql.Connection;
//import java.sql.DatabaseMetaData;
//import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
//import java.util.Date;
import java.util.Vector;


public class ConnectionPool implements Runnable {
	private String driver, url, username, password;
	private int maxConnections;
	private boolean waitIfBusy;
	private Vector<Connection> availableConnections, busyConnections;
	private boolean connectionPending = false;

	public ConnectionPool(String driver, String url, String username, String password, int initialConnections, int maxConnections, boolean waitIfBusy) throws SQLException {
		this.driver = driver;
		this.url = url;
		this.username = username;
		this.password = password;
		this.maxConnections = maxConnections;
		this.waitIfBusy = waitIfBusy;
		if (initialConnections > maxConnections) {
			initialConnections = maxConnections;
		}
		availableConnections = new Vector<Connection>(initialConnections);
		busyConnections = new Vector<Connection>();
		for(int i=0; i<initialConnections; i++) {
			availableConnections.addElement(makeNewConnection( ));
		}
	}

	public synchronized Connection getConnection() throws SQLException {
		if (!availableConnections.isEmpty()) {
			Connection existingConnection = availableConnections.lastElement();
			int lastIndex = availableConnections.size() - 1;
			availableConnections.removeElementAt(lastIndex);
			// If connection on available list is closed (e.g.,
			// it timed out), then remove it from available list
			// and repeat the process of obtaining a connection.
			// Also wake up threads that were waiting for a
			// connection because maxConnection limit was reached.
			if (!existingConnection.isValid(5)) { //CHANGED BY ERIC replaced existingConnection.isClosed() by !existingConnection.isValid(5)
				notifyAll(); // Freed up a spot for anybody waiting
				return(getConnection());
			} else {
				busyConnections.addElement(existingConnection);
				return(existingConnection);
			}
		} else {

			// Three possible cases:
			// 1) You haven't reached maxConnections limit. So
			// establish one in the background if there isn't
			// already one pending, then wait for
			// the next available connection (whether or not
			// it was the newly established one).
			// 2) You reached maxConnections limit and waitIfBusy
			// flag is false. Throw SQLException in such a case.
			// 3) You reached maxConnections limit and waitIfBusy
			// flag is true. Then do the same thing as in second
			// part of step 1: wait for next available connection.

			if ((totalConnections() < maxConnections) &&
					!connectionPending) {
				makeBackgroundConnection();
			} else if (!waitIfBusy) {
				throw new SQLException("Connection limit reached");
			}
			// Wait for either a new connection to be established
			// (if you called makeBackgroundConnection) or for
			// an existing connection to be freed up.
			try {
				wait();
			} catch(InterruptedException ie) {}
			// Someone freed up a connection, so try again.
			return(getConnection());
		}
	}

	// You can't just make a new connection in the foreground
	// when none are available, since this can take several
	// seconds with a slow network connection. Instead,
	// start a thread that establishes a new connection,
	// then wait. You get woken up either when the new connection
	// is established or if someone finishes with an existing
	// connection.

	private void makeBackgroundConnection() {
		connectionPending = true;
		try {
			Thread connectThread = new Thread(this);
			connectThread.start();
		} catch(OutOfMemoryError oome) {
			// Give up on new connection
		}
	}

	@Override
	public void run() {
		try {
			Connection connection = makeNewConnection();
			synchronized(this) {
				availableConnections.addElement(connection);
				connectionPending = false;
				notifyAll();
			}
		} catch(Exception e) { // SQLException or OutOfMemory
			// Give up on new connection and wait for existing one
			// to free up.
		}
	}

	// This explicitly makes a new connection. Called in
	// the foreground when initializing the ConnectionPool,
	// and called in the background when running.

	private Connection makeNewConnection() throws SQLException {
		try {
			// Load database driver if not already loaded
			Class.forName(driver);
			// Establish network connection to database
			Connection connection = DriverManager.getConnection(url, username, password);
			//Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/Axiom_db", "axiom", "axiom");
			return(connection);
		} catch(ClassNotFoundException cnfe) {
			// Simplify try/catch blocks of people using this by
			// throwing only one exception type.
			throw new SQLException("Can't find class for driver: " + driver);
		}
	}

	public synchronized void close(Connection connection) {
		busyConnections.removeElement(connection);
		availableConnections.addElement(connection);
		// Wake up threads that are waiting for a connection
		notifyAll();
	}

	public synchronized int totalConnections() {
		return(availableConnections.size() + busyConnections.size());
	}

	/** Close all the connections. Use with caution:
	 * be sure no connections are in use before
	 * calling. Note that you are not <I>required to
	 * call this when done with a ConnectionPool, since
	 * connections are guaranteed to be closed when
	 * garbage collected. But this method gives more control
	 * regarding when the connections are closed.
	 */

	public synchronized void closeAllConnections() {
		closeConnections(availableConnections);
		availableConnections = new Vector<Connection>();
		closeConnections(busyConnections);
		busyConnections = new Vector<Connection>();
	}

	private void closeConnections(Vector<Connection> connections) {
		try {
			for(int i=0; i<connections.size(); i++) {
				Connection connection = connections.elementAt(i);
				if (!connection.isClosed()) {
					connection.close();
				}
			}
		} catch(SQLException sqle) {
			// Ignore errors; garbage collect anyhow
		}
	}

	@Override
	public synchronized String toString() {
		String info = "ConnectionPool(" + url + "," + username + ")" +
		", available=" + availableConnections.size() +
		", busy=" + busyConnections.size() +
		", max=" + maxConnections;
		return(info);
	}

	/*private String login;
	private String password;
	private String JDBCDriver;
	private String JDBCConnectionURL;
	private int ConnectionPoolSize;
	private int ConnectionPoolMax;
	//private int ConnectionUseCount;
	private Vector<ConnectionObject> pool;

	public ConnectionPool(String JDBCDriver, String JDBCConnectionURL, String login, String password, int connectionPoolSize, int connectionPoolMax){
		this.JDBCDriver = JDBCDriver;
		this.JDBCConnectionURL = JDBCConnectionURL;
		this.login = login;
		this.password = password;
		this.ConnectionPoolSize = connectionPoolSize;
		this.ConnectionPoolMax = connectionPoolMax;
	}

	// The initialize method is called in the AxiomContextListener class.
	public void initialize() {
		pool = new Vector<ConnectionObject>();
		fillPool (ConnectionPoolSize);
	}

	public Vector<ConnectionObject> getConnectionPoolObjects () {return pool;}

	 The fillPool method gets a connection and then checks to see how many connections the database can support.
	 * If it is less than ConnectionPoolSize, the pool size is adjusted.
	 * It then fills the vector with new ConnectionObjects. 
	 
	private synchronized void fillPool (int poolSize) {
		try {
			//Connection con; 
			int count = 0;
			int maxConnections = 0;
			//Class.forName (JDBCDriver);
			@SuppressWarnings("rawtypes")
			Class c = Class.forName(JDBCDriver);
			Driver driver = (Driver)c.newInstance();
			DriverManager.registerDriver(driver);

			while (count < poolSize) {
				//ConnectionObject conObject = new ConnectionObject ();
				//
				//con = DriverManager.getConnection (JDBCConnectionURL,login,password);
				//conObject.setConnection (con);
				 The first time through the loop, find out the maximum number of connections that the database can support. 
				ConnectionObject conObject = addNewConnectionObject();
				if (count == 0) {
					Connection con = conObject.getConnection ();
					DatabaseMetaData metaData = con.getMetaData ();
					maxConnections = metaData.getMaxConnections ();
					if (poolSize > maxConnections) {
						poolSize = maxConnections;
					}
					close(con);
				}
				//conObject.setInUse (false); // Mark the connection as available.
				//conObject.setUseCount (0); // Start the use count off at 0.
				//conObject.setLastAccessTime (new Date ()); // Set the first access time.
				//pool.addElement (conObject); // Add the object to the vector.
				count ++;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	private synchronized ConnectionObject addNewConnectionObject(){
		try{
			ConnectionObject conObject = new ConnectionObject ();
			//Class.forName (JDBCDriver);
			Connection con = DriverManager.getConnection (JDBCConnectionURL,login,password);
			conObject.setConnection (con);
			conObject.setInUse (false); // Mark the connection as available.
			conObject.setUseCount (0); // Start the use count off at 0.
			conObject.setLastAccessTime (new Date ()); // Set the first access time.
			pool.addElement (conObject); // Add the object to the vector.
			return conObject;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e){
			throw new RuntimeException(e);
		}
	}
	
	// The method getConnection is synchronized so that only one servlet can get a connection at a time.
	public synchronized Connection getConnection () throws SQLException {
		Connection con = null;
		ConnectionObject connectionObject = null;
		ConnectionObject conObject = null;
		int poolSize = pool.size (), count = 0;
		boolean found = false;

		if (pool == null) return null;  // Do not access an empty pool.
		// Find the first available connection in the pool.
		while ((count < poolSize) && !found) {
			conObject = (ConnectionObject) pool.elementAt (count);
			if (conObject.isAvailable()){ 
					if(!conObject.getConnection().isValid(3)){
						conObject.getConnection().close();
						Connection connection = DriverManager.getConnection (JDBCConnectionURL,login,password);
						conObject.setConnection(connection);
					}
					found = true;
			} else {
				count ++;
			}
		}
		if (found) {
			connectionObject = conObject;
		}else if (pool.size()<ConnectionPoolMax){
			connectionObject = addNewConnectionObject();
		}

		if (connectionObject == null) {
			throw new RuntimeException("All connections in use.");
		} else {
			connectionObject.setInUse (true); // Make the connection unavailable to others.
			int useCount = connectionObject.getUseCount () + 1;
			connectionObject.setUseCount (useCount); // Increment the use count.
			connectionObject.setLastAccessTime (new Date ()); // Change the access date.
			con = connectionObject.getConnection ();
		}
		//System.out.println("connection ouverte: "+con.getCatalog());
		return con;
	}

	// The close method makes the connection available again.  It does not really close the connection.
	public synchronized void close (Connection con) {
		int index = find(con);
		if (index != -1) {
			ConnectionObject conObject = (ConnectionObject) pool.elementAt(index);
			conObject.setInUse (false);
			conObject.setLastAccessTime (new Date ());
			if (pool.size()>ConnectionPoolSize){
				try{
					conObject.getConnection().close();
				}catch(SQLException e){
					throw new RuntimeException("Cannot close con:"+e);
				}
				pool.remove(index);
			}
		} else {
			throw new RuntimeException("Connection not found in pool.");
		}
	}

	 Find is a private method that searches through the vector to find a connection with the same catalog name.
	 * When one is found, its index is returned to the close method. 
	 
	private int find (Connection con) {
		int index = 0;
		boolean found = false;
		try {
			ConnectionObject conObject;
			String catalog = con.getCatalog();
			
			//System.out.println("connection a fermer: "+catalog);
			
			while ((index < pool.size ()) && !found) {
				conObject = (ConnectionObject) pool.elementAt (index);
				Connection poolCon = conObject.getConnection ();
				String name = poolCon.getCatalog ();
				if (catalog.equals(name)) {
					found = true;
				} else {
					index ++;
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Catalog Exception");
		}
		
		if (found) {
			return index;
		} else {
			return -1;
		}
	}

	 The destroy method is executed when the application is finished.
	 * It closes each connection in the pool and then sets the pool to null. 
	 
	public void destroy () {
		try {
			if (pool != null) {
				// Close each connection in the pool.
				for (int count = 0; count < pool.size(); count++) {
					ConnectionObject co = (ConnectionObject) pool.elementAt(count);
					Connection con = co.getConnection ();
					con.close(); // This really closes the connection.
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("Destroy error.");
		}
		pool = null;
	}*/
}