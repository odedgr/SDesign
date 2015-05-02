package il.ac.technion.cs.sd.app.mail;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The server side of the TMail application. <br>
 * This class is mainly used in our tests to start, stop, and clean the server
 */
public class ServerMailApplication {
	
	// instance variables
	private String myAddress = null;

	// loaded / stored to independent db
	private Map<String, List<Envelope>> mailBySender = new HashMap<String, List<Envelope>>();
	private Map<String, List<Envelope>> mailByReceiver = new HashMap<String, List<Envelope>>();
	

	// instance methods
	private boolean active = false;
	
	/**
	 * Starts a new web server with an arbitrary name. If you want to generate a random name, look at
	 * {@link UUID#randomUUID()}.
	 */
	public ServerMailApplication() {
//		myAddress = ....
		loadDb();
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
		
		
		this.active = true;
		throw new UnsupportedOperationException("Not implemented");
	}
	
	/**
	 * Stops the server. A stopped server can't accept mail, but doesn't delete any data.
	 * A stopped server does not use any system resources (e.g., messengers).
	 */
	public void stop() {
		
		this.active = false;
		saveDb();
		
		throw new UnsupportedOperationException("Not implemented");
	}
	
	/**
	 * Deletes <b>all</b> previously saved data. This method will be used between tests to assure that each test will
	 * run on a new, clean server. you may assume the server is stopped before this method is called.
	 */
	public void clean() {
		throw new UnsupportedOperationException("Not implemented");
	}
	
	
	////////////////
	
	private void addNewMail(Mail mail) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented");
	}
	
	private List<Mail> getMailOfSender(String sender, int howMany) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented");
//		List<Mail> list = new ArrayList<Mail>();
//		
//		return list
	}
	
	private List<Mail> getMailOfReceiver(String receiver, int howMany) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented");
//		List<Mail> list = new ArrayList<Mail>();
//		
//		return list
	}
	
	private List<Mail> getAllMailOfClient(String client, int howMany) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented");
//		List<Mail> list = new ArrayList<Mail>();
//		
//		return list
	}
	
	private List<Mail> getUnreadMailOfClient(String client, int howMany) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented");
//		List<Mail> list = new ArrayList<Mail>();
//		
//		return list
	}
	

	private void loadDb() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented");
	}
	
	private void saveDb() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented");
	}
	
	
	private class Envelope {
		private Mail msg;
		private LocalDateTime arrivalTime;
		private boolean opened = false;
		
		Envelope(Mail mail) {
			msg = mail;
			opened = false;
			arrivalTime = LocalDateTime.now();
		}
		
		public void markRead() {
			opened = true;
		}
		
		public boolean wasRead() {
			return opened;
		}
	}
}
