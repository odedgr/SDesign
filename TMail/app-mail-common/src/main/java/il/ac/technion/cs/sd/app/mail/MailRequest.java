package il.ac.technion.cs.sd.app.mail;

import java.io.Serializable;

// TODO: go over javadocs!!

/**
 * A wrapper for messages that are passed around between clients and a server.
 * An Envelope object is used for both sending a single mail from one client to another, as well as
 * sending a task request to its server and getting the appropriate results.
 * 
 * Envelopes are created using the appropriate wrap...() methods, which are built specifically to accept
 * the required parameters for each type of request. Similarly, an incoming envelope's message type is identifiable
 * via it's opcode (retrievable using the opcode() method).
 *
 */
public class MailRequest implements Serializable {

	private static final long serialVersionUID = 8146517154357247722L;
	
	public static enum RequestType {
		SEND_MAIL,
		GET_MAIL_SENT,
		GET_INCOMING,
		GET_ALL_MAIL,
		GET_CORRESPONDANCES,
		GET_UNREAD,
		GET_CONTACTS;
	}
	
	public static MailRequest sendMail(Mail mail) {
		return new MailRequest(RequestType.SEND_MAIL, mail);
	}
	public static MailRequest getMailSent(int amount) {
		return new MailRequest(RequestType.GET_MAIL_SENT, amount);
	}
	public static MailRequest getIncoming(int amount) {
		return new MailRequest(RequestType.GET_INCOMING, amount);
	}
	public static MailRequest getAllMail(int amount) {
		return new MailRequest(RequestType.GET_ALL_MAIL, amount);
	}
	public static MailRequest getCorrespondences(String otherClient) {
		return new MailRequest(RequestType.GET_CORRESPONDANCES, otherClient);
	}
	public static MailRequest getUnread() {
		return new MailRequest(RequestType.GET_UNREAD);
	}
	public static MailRequest getContacts() {
		return new MailRequest(RequestType.GET_CONTACTS);
	}
	
	final private RequestType type;
	final private Mail mail;  // Mail to send with SEND_MAIL request.
	final private String otherClient;  // Other client to return correspondence, with GET_LAST_CORRESPONDANCE_WITH_CLIENT request.
	final private int amount; // The amount of mail requested.
	
	private MailResponse response;
	

	private MailRequest(RequestType type, String otherClient) {
		this.type = type;
		this.mail = null;
		this.amount = -1;
		this.otherClient = otherClient;
	}
	
	private MailRequest(RequestType type, Mail mail) {
		this.type = type;
		this.mail = mail;
		this.amount = -1;
		this.otherClient = null;
	}
	
	private MailRequest(RequestType type, int amount) {
		this.type = type;
		this.mail = null;
		this.amount = amount;
		this.otherClient = null;
	}
	
	private MailRequest(RequestType type) {
		this.type = type;
		this.mail = null;
		this.amount = -1;
		this.otherClient = null;
	}
	
	public void attachResponse(MailResponse response) {
		this.response = response;
	}
	
	/**
	 * Get this envelope's opcode.
	 * 
	 * @return Opcode of envelope.
	 */
	public RequestType getType() {
		return type;
	}
	
	/**
	 * Get this envelope's requested amount (when sent by client) / result count (when sent from server).
	 * 
	 * @return amount requested or result count.
	 */
	public int getAmount() {
		if (amount < 0) {
			throw new RuntimeException("No amount provided with this request. Check your request type.");
		}
		return amount;
	}

	public Mail getMail() {
		if (mail == null) {
			throw new RuntimeException("No mail provided with this request. Check your request type.");
		}
		return mail;
	}

	public String getOtherClient() {
		if (otherClient == null) {
			throw new RuntimeException("No otherClient provided with this request. Check your request type.");
		}
		return otherClient;
	}
	public MailResponse getResponse() {
		if (response == null) {
			throw new RuntimeException("This request has not been responded yet.");
		}
		return response;
	}
}
