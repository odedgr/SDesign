package il.ac.technion.cs.sd.app.mail;

import java.util.UUID;

/**
 * The server side of the TMail application. <br>
 * This class is mainly used in our tests to start, stop, and clean the server
 */
public class ServerMailApplication {
	
	/**
	 * Starts a new web server with an arbitrary name. If you want to generate a random name, look at
	 * {@link UUID#randomUUID()}.
	 */
	public ServerMailApplication() {
		throw new UnsupportedOperationException("Not implemented");
	}
	
	/**
	 * @return the server's address; this address will be used by clients connecting to the server
	 */
	public String getAddress() {
		throw new UnsupportedOperationException("Not implemented");
	}
	
	/**
	 * Starts the server; any previously sent mails, data and indices are loaded
	 */
	public void start() {
		throw new UnsupportedOperationException("Not implemented");
	}
	
	/**
	 * Stops the server. A stopped server can't accept mail, but doesn't delete any data.
	 * A stopped server does not use any system resources (e.g., messengers).
	 */
	public void stop() {
		throw new UnsupportedOperationException("Not implemented");
	}
	
	/**
	 * Deletes <b>all</b> previously saved data. This method will be used between tests to assure that each test will
	 * run on a new, clean server. you may assume the server is stopped before this method is called.
	 */
	public void clean() {
		throw new UnsupportedOperationException("Not implemented");
	}
}
