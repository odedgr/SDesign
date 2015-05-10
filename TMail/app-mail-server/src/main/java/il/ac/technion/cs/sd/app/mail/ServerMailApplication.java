package il.ac.technion.cs.sd.app.mail;

import il.ac.technion.cs.sd.lib.ServerConnection;
import il.ac.technion.cs.sd.msg.MessengerException;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The server side of the TMail application. <br>
 * This class is mainly used in our tests to start, stop, and clean the server
 */
public class ServerMailApplication {
	
	// instance variables
	private String myAddress = null;
	private boolean active = false;
	private ServerConnection<Envelope> sConn = null;

	// loaded / stored to independent db
	private Map<String, MailBox> mailboxes = new HashMap<String, MailBox>();
	
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
	
	private void addNewMail(Mail mail) {
		String sender = mail.from;
		String receiver = mail.to;
		
		MailBox senderBox = mailboxes.get(sender);
		MailBox receiverBox = mailboxes.get(receiver);
		
		senderBox.sentMail(mail);
		receiverBox.receivedMail(mail);
	}
	
	private List<Mail> getCorrespondencesBetween(String requester, String otherClient, int howMany) {
		MailBox mailbox = mailboxes.get(requester);
		return mailbox.getCorrespondeceWith(otherClient, howMany);
	}
	
	private List<Mail> getSentMailOfClient(String client, int howMany) {
		MailBox mailbox = mailboxes.get(client);
		return mailbox.getLastNSent(howMany);
	}
	
	private List<Mail> getIncomingMailOfClient(String client, int howMany) {
		MailBox mailbox = mailboxes.get(client);
		return mailbox.getLastNReceived(howMany);
	}
	
	private List<Mail> getAllMailOfClient(String client, int howMany) {
		MailBox mailbox = mailboxes.get(client);
		return mailbox.getLastNMails(howMany);
	}
	
	private List<Mail> getUnreadMailOfClient(String client) {
		MailBox mailbox = mailboxes.get(client);
		return mailbox.getUnread();
	}
	

	private void loadDb() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented");
	}
	
	private void saveDb() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented");
	}
	
}
