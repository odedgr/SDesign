package il.ac.technion.cs.sd.app.mail;

import il.ac.technion.cs.sd.lib.ClientConnection;

import java.util.List;

/**
 * The client side of the TMail application.
 * Allows sending and getting mail to and from other clients using a server.
 * <br>
 * You should implement all the methods in this class 
 */
public class ClientMailApplication {
	
	private ClientConnection<MailRequest> connection;
	
	/**
	 * Creates a new application, tied to a single user
	 * @param serverAddress The address of the server to connect to for sending and requesting mail
	 * @param username The user that will be sending and accepting the mail using this object
	 */
	public ClientMailApplication(String serverAddress, String username) {
		if (serverAddress == null || serverAddress.isEmpty() || username == null || username.isEmpty()) {
			throw new IllegalArgumentException();
		}
		this.connection = ClientConnection.<MailRequest>create(serverAddress, username);
	}
	
	
	/**
	 * Creates a client mail application that uses the given connection.
	 * @param connection the connection to use.
	 */
	private ClientMailApplication(ClientConnection<MailRequest> connection) {
		if (connection == null) {
			throw new IllegalArgumentException();
		}
		this.connection = connection;
	}
	
	/**
	 * Creates a client mail application that uses the given mock connection.
	 * Used for testing purposes.
	 * @param connection the mock connection to use.
	 */
	public static ClientMailApplication createWithMockConnection(
			ClientConnection<MailRequest> connection) {
		if (connection == null) {
			throw new IllegalArgumentException();
		}
		return new ClientMailApplication(connection);
	}
	
	/**
	 * Sends a mail to another user
	 * @param whom The recipient of the mail
	 * @param what The message to send
	 */
	public void sendMail(String whom, String what) {
		if (whom == null || whom.isEmpty() || what == null || what.isEmpty()) {
			throw new IllegalArgumentException();
		}
		Mail mail = new Mail(connection.getAddress(), whom, what);
		MailRequest request = MailRequest.sendMail(mail);
		connection.send(request);
	}
	
	/**
	 * Get all mail sent from or to another client
	 * @param whom The other user that sent or received mail from the current user
	 * @param howMany how many mails to retrieve; mails are ordered by time of arrival to server
	 * @return A list, ordered of all mails matching the criteria, ordered by time of arrival, of size n <i>at most</i>  
	 */
	public List<Mail> getCorrespondences(String whom, int howMany) {
		if (whom == null || whom.isEmpty() || howMany < 0) {
			throw new IllegalArgumentException();
		}
		MailRequest request = MailRequest.getCorrespondences(whom, howMany);
		connection.send(request);
		
		MailResponse response = connection.receiveBlocking().getResponse();
		return response.getMailResults();
	}
	
	/**
	 * Get all mail sent <b>by</b> the current user
	 * @param howMany how many mails to retrieve; mails are ordered by time of arrival to server
	 * @return A list, ordered of all mails matching the criteria, ordered by time of arrival, of size n <i>at most</i>  
	 */
	public List<Mail> getSentMails(int howMany) {
		if (howMany < 0) {
			throw new IllegalArgumentException();
		}
		MailRequest request = MailRequest.getMailSent(howMany);
		connection.send(request);
		
		MailResponse response = connection.receiveBlocking().getResponse();
		return response.getMailResults();
	}
	
	/**
	 * Get all sent <b>to</b> the current user
	 * @param howMany how many mails to retrieve; mails are ordered by time of arrival to server
	 * @return A list, ordered of all mails matching the criteria, ordered by time of arrival, of size n <i>at most</i>  
	 */
	public List<Mail> getIncomingMail(int howMany) {
		if (howMany < 0) {
			throw new IllegalArgumentException();
		}
		MailRequest request = MailRequest.getIncoming(howMany);
		connection.send(request);
		
		MailResponse response = connection.receiveBlocking().getResponse();
		return response.getMailResults();
	}
	
	/**
	 * Get all sent <b>to</b> or <b>by</b> the current user
	 * @param howMany how many mails to retrieve; mails are ordered by time of arrival to server
	 * @return A list, ordered of all mails matching the criteria, ordered by time of arrival, of size n <i>at most</i>  
	 */
	public List<Mail> getAllMail(int howMany) {
		if (howMany < 0) {
			throw new IllegalArgumentException();
		}
		MailRequest request = MailRequest.getAllMail(howMany);
		connection.send(request);
		
		MailResponse response = connection.receiveBlocking().getResponse();
		return response.getMailResults();
	}
	
	/**
	 * Get all sent <b>to</b> the current user that wasn't retrieved by any method yet (including this method)
	 * @return A list, ordered of all mails matching the criteria, ordered by time of arrival  
	 */
	public List<Mail> getNewMail() {
		MailRequest request = MailRequest.getUnread();
		connection.send(request);
		
		MailResponse response = connection.receiveBlocking().getResponse();
		return response.getMailResults();
	}
	
	/**
	 * @return A list, ordered alphabetically, of all other users that sent or received mail from the current user  
	 */
	public List<String> getContacts(int howMany) {
		if (howMany < 0) {
			throw new IllegalArgumentException();
		}
		MailRequest request = MailRequest.getContacts();
		connection.send(request);
		
		MailResponse response = connection.receiveBlocking().getResponse();
		return response.getContactsResults();
	}
	
	/**
	 * A stopped client does not use any system resources (e.g., messengers).
	 * This is mainly used to clean resource use in test cleanup code.
	 */
	public void stop() {
		connection.kill();
	}
}
