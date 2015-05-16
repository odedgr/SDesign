package il.ac.technion.cs.sd.app.mail;

import il.ac.technion.cs.sd.lib.MessageWithSender;
import il.ac.technion.cs.sd.lib.ServerConnection;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The server side of the TMail application. <br>
 * This class is mainly used in our tests to start, stop, and clean the server.
 */
public class ServerMailApplication {
	
	// instance variables
	private final String address;
	private ServerConnection<MailRequest> connection;
	
	private List<MailEntry> history;
	private DataSaver<List<MailEntry>> dataSaver;  

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
		
		address = name;
		
		history = new ArrayList<MailEntry>();
		dataSaver = new FileDataSaver<List<MailEntry>>("app-mail-data-" + address);
	}
	
	/**
	 * @return the server's address; this address will be used by clients connecting to the server
	 */
	public String getAddress() {
		return address;
	}
	
	/**
	 * Starts the server; any previously sent mails, data and indices under this server name are loaded. It is possible
	 * to start a new server instance in same, or another process. You may assume that two server instances with the
	 * same name won't be in parallel. Similarly, {@link ServerMailApplication#stop()} will be called before subsequent
	 * calls to {@link ServerMailApplication#start()}.
	 */
	public void start() {
		loadData();
		connection = ServerConnection.<MailRequest>create(address);
		
		while (true) {
			MessageWithSender<MailRequest> signed_request = connection.receiveBlocking();
			String client = signed_request.sender;
			MailRequest request = signed_request.content;
			Optional<MailResponse> response = handleRequest(client, request);
			if (response.isPresent()) {
				request.attachResponse(response.get());
				// Send the request with the attached response back to the client.
				connection.send(client, request);
			}
		}
	}
	
	/**
	 * Stops the server. A stopped server can't accept mail, but doesn't delete any data. A stopped server does not use
	 * any system resources (e.g., messengers).
	 */
	public void stop() {
		saveData();
		if (null != connection) {
			connection.kill();
		}
	}
	
	/**
	 * Deletes <b>all</b> previously saved data. This method will be used between tests to assure that each test will
	 * run on a new, clean server. you may assume the server is stopped before this method is called.
	 */
	public void clean() {
		mailboxes = new HashMap<String, MailBox>();
		history = new ArrayList<MailEntry>();
		dataSaver.clean();
	}
	
	// TODO: is optional the best choice?
	private Optional<MailResponse> handleRequest(String client, MailRequest request) {
		MailResponse response = null;
		switch (request.getType()) {
		case GET_ALL_MAIL:
			response = MailResponse.withMailResults(getAllMailOfClient(client, request.getAmount()));
			break;
		case GET_CONTACTS:
			response = MailResponse.withContactsResults(getContacts(client));
			break;
		case GET_CORRESPONDANCES:
			response = MailResponse.withMailResults(getCorrespondencesBetween(client,request.getOtherClient(), request.getAmount()));
			break;
		case GET_INCOMING:
			response = MailResponse.withMailResults(getIncomingMailOfClient(client, request.getAmount()));
			break;
		case GET_MAIL_SENT:
			response = MailResponse.withMailResults(getSentMailOfClient(client, request.getAmount()));
			break;
		case GET_UNREAD:
			response = MailResponse.withMailResults(getUnreadMailOfClient(client));
			break;
		case SEND_MAIL:
			addNewMail(request.getMail());
			break;
		default:
			break;
		}
		return response != null ? Optional.of(response) : Optional.empty();
	}

	
	/**
	 * Add a new mail item, sent from one client to another, via this server.
	 * Updates both clients' mailboxes.
	 * 
	 * @param mail Mail item that was sent.
	 */
	private void addNewMail(Mail mail) {
		addNewMailEntry(new MailEntry(mail));

	}
	
	/**
	 * Add a new mail entry, sent from a client or loaded from history.
	 * Updates both clients' mailboxes.
	 * 
	 * @param entry the mail entry to add.
	 */
	private void addNewMailEntry(MailEntry entry) {
		getMailBoxOfClient(entry.getMail().from).addSentMail(entry);
		getMailBoxOfClient(entry.getMail().to).addReceivedMail(entry);
		history.add(entry);
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
		MailBox mailbox = getMailBoxOfClient(requester);
		return mailbox.getCorrespondeceWith(otherClient, howMany).stream()
				.map(entry -> entry.getMail()).collect(Collectors.toList());
	}
	
	/**
	 * Retrieve at most howMany of the most recent mail items a given client has sent.
	 * 
	 * @param client - Address of sending client
	 * @param howMany - Maximal amount of mail items to return.
	 * @return List of at most howMany, most recent mail items, the client has sent, ordered from newest to oldest.
	 */
	private List<Mail> getSentMailOfClient(String client, int howMany) {
		MailBox mailbox = getMailBoxOfClient(client);
		return mailbox.getLastNSent(howMany).stream()
				.map(entry -> entry.getMail()).collect(Collectors.toList());
	}
	
	/**
	 * Retrieve at most howMany of the most recent mail items a given client has received.
	 * 
	 * @param client - Address of the receiving client.
	 * @param howMany - Maximal amount of mail items to return.
	 * @return List of at most howMany, most recent mail items, the client has received, ordered from newest to oldest.
	 */
	private List<Mail> getIncomingMailOfClient(String client, int howMany) {
		MailBox mailbox = getMailBoxOfClient(client);
		return mailbox.getLastNReceived(howMany).stream()
				.map(entry -> entry.getMail()).collect(Collectors.toList());
	}
	
	private List<String> getContacts(String client) {
		MailBox mailbox = getMailBoxOfClient(client);
		return mailbox.getContacts();
	}
	
	/**
	 * Retrieve at most howMany of the most recent mail items a given client has either sent or received.
	 * 
	 * @param client - Address of client whose mails are queried.
	 * @param howMany - Maximal amount of mail items to return.
	 * @return List of at most howMany, most recent mail items, the client has either sent or received, ordered from newest to oldest.
	 */
	private List<Mail> getAllMailOfClient(String client, int howMany) {
		MailBox mailbox = getMailBoxOfClient(client);
		return mailbox.getLastNMails(howMany).stream()
				.map(entry -> entry.getMail()).collect(Collectors.toList());
	}
	
	/**
	 * Retrieve all unread mail items of a given client.
	 * 
	 * @param client - Address of client of which to get the unread mail.
	 * @return List of all the unread mail items of the given client.
	 */
	private List<Mail> getUnreadMailOfClient(String client) {
		MailBox mailbox = getMailBoxOfClient(client); 
		return mailbox.getUnread().stream()
				.map(entry -> entry.getMail()).collect(Collectors.toList());
	}
	
	/**
	 * Store all of this server's current data (mailboxes and their contents) into a file. 
	 */
	private void saveData() {
		dataSaver.save(history);
	}
	
	/**
	 * Load a previously stored database of mailboxes and their contents into the active server.
	 */
	private void loadData() {
		history = new ArrayList<MailEntry>();
		mailboxes = new HashMap<String, MailBox>(); // TODO: move out to function 'reset mailboxes'
		
		Optional<List<MailEntry>> loaded_history = dataSaver.load();
		if (!loaded_history.isPresent()) {
			return;
		}
		
		for (MailEntry entry : loaded_history.get()) {
			addNewMailEntry(entry);
		}
	}
	
	/**
	 * Safely get a mailbox of a given client. If non exists, a new mailbox will be created for the client.
	 * 
	 * @param client Address of client for whom the mailbox is requested.
	 * @return MailBox of the given client.
	 */
	private MailBox getMailBoxOfClient(String client) {
		MailBox mb = mailboxes.get(client);
		
		if (null == mb) {
			mb = new MailBox();
			mailboxes.put(client, mb);
		}
		
		return mb;
	}
}
