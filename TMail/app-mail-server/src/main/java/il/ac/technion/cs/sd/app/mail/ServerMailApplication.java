package il.ac.technion.cs.sd.app.mail;

import il.ac.technion.cs.sd.lib.ServerConnection;
import il.ac.technion.cs.sd.msg.MessengerException;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The server side of the TMail application. <br>
 * This class is mainly used in our tests to start, stop, and clean the server.
 */
public class ServerMailApplication {
	
	// instance variables
	private String myAddress = null;
	private boolean active = false;
	private ServerConnection<Envelope> sConn = null;

	// loaded / stored to independent db
	private Map<String, MailBox> mailboxes = new HashMap<String, MailBox>(); // <client address : client mailbox>
	
	/**
	 * Starts a new mail server. Servers with the same name retain all their information until
	 * {@link ServerMailApplication#clean()} is called.
	 * 
	 * @param name The name of the server by which it is known.
	 */
	public ServerMailApplication(String name) {
		if (null == name || name.equals("")) {
			throw new InvalidParameterException("Server name cannot be null or empty");
		}
		
		sConn = ServerConnection.create(name);
		myAddress = name;
	
		throw new UnsupportedOperationException("Not implemented");
	}
	
	/**
	 * @return the server's address; this address will be used by clients connecting to the server
	 */
	public String getAddress() {
		return sConn.address();
	}
	
	/**
	 * Starts the server; any previously sent mails, data and indices under this server name are loaded. It is possible
	 * to start a new server instance in same, or another process. You may assume that two server instances with the
	 * same name won't be in parallel. Similarly, {@link ServerMailApplication#stop()} will be called before subsequent
	 * calls to {@link ServerMailApplication#start()}.
	 */
	public void start() {
		loadDb();
		sConn.start();
		this.active = true;
		
		// TODO start listening for incoming requests, using the sConn receive methods
	}
	
	/**
	 * Stops the server. A stopped server can't accept mail, but doesn't delete any data. A stopped server does not use
	 * any system resources (e.g., messengers).
	 */
	public void stop() {
		saveDb();
		this.active = false;
		sConn.kill();
	}
	
	/**
	 * Deletes <b>all</b> previously saved data. This method will be used between tests to assure that each test will
	 * run on a new, clean server. you may assume the server is stopped before this method is called.
	 */
	public void clean() {
		mailboxes = new HashMap<String, MailBox>();
		// TODO should sConn also be reset?
		// TODO should the state change?
	}
	
		////////////////
	
	/**
	 * Add a new mail item, sent from one client to another, via this server.
	 * Updates both clients' mailboxes.
	 * 
	 * @param mail Mail item that was sent.
	 */
	private void addNewMail(Mail mail) {
		String sender = mail.from;
		String receiver = mail.to;
		
		MailBox senderBox = mailboxes.get(sender);
		MailBox receiverBox = mailboxes.get(receiver);
		
		senderBox.sentMail(mail);
		receiverBox.receivedMail(mail);
	}
	
	/**
	 * Retrieve at most howMany most recent mails from the complete correspondence between two clients.
	 * 
	 * @param requester - Address of the client requesting the correspondence.
	 * @param otherClient - Address of client with whom correspondence was asked for by the requester.
	 * @param howMany - Maximal amount of mail items to be returned.
	 * @return List of at most howMany, most recent mail items, in the correspondence between the two clients, ordered from newest to oldest.
	 */
	private List<Mail> getCorrespondencesBetween(String requester, String otherClient, int howMany) {
		MailBox mailbox = mailboxes.get(requester);
		return mailbox.getCorrespondeceWith(otherClient, howMany);
	}
	
	/**
	 * Retrieve at most howMany of the most recent mail items a given client has sent.
	 * 
	 * @param client - Address of sending client
	 * @param howMany - Maximal amount of mail items to return.
	 * @return List of at most howMany, most recent mail items, the client has sent, ordered from newest to oldest.
	 */
	private List<Mail> getSentMailOfClient(String client, int howMany) {
		MailBox mailbox = mailboxes.get(client);
		return mailbox.getLastNSent(howMany);
	}
	
	/**
	 * Retrieve at most howMany of the most recent mail items a given client has received.
	 * 
	 * @param client - Address of the receiving client.
	 * @param howMany - Maximal amount of mail items to return.
	 * @return List of at most howMany, most recent mail items, the client has received, ordered from newest to oldest.
	 */
	private List<Mail> getIncomingMailOfClient(String client, int howMany) {
		MailBox mailbox = mailboxes.get(client);
		return mailbox.getLastNReceived(howMany);
	}
	
	/**
	 * Retrieve at most howMany of the most recent mail items a given client has either sent or received.
	 * 
	 * @param client - Address of client whose mails are queried.
	 * @param howMany - Maximal amount of mail items to return.
	 * @return List of at most howMany, most recent mail items, the client has either sent or received, ordered from newest to oldest.
	 */
	private List<Mail> getAllMailOfClient(String client, int howMany) {
		MailBox mailbox = mailboxes.get(client);
		return mailbox.getLastNMails(howMany);
	}
	
	/**
	 * Retrieve all unread mail items of a given client.
	 * 
	 * @param client - Address of client of which to get the unread mail.
	 * @return List of all the unread mail items of the given client.
	 */
	private List<Mail> getUnreadMailOfClient(String client) {
		MailBox mailbox = mailboxes.get(client);
		return mailbox.getUnread();
	}
	
	/**
	 * Load a previously stored database of mailboxes and their contents into the active server.
	 */
	private void loadDb() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented");
	}
	
	/**
	 * Store all of this server's current data (mailboxes and their contents) into a file. 
	 */
	private void saveDb() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented");
	}
	
}
