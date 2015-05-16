package il.ac.technion.cs.sd.app.mail;

import java.io.Serializable;

/**
 * A request sent from the client to the server, to send a mail or to retrieve information.
 * The MailRequest contains all request data (request type and additional properties).
 * 
 * The MailRequest may contain a response; When the server gets the request, he attaches his response
 * to the request and sends the request back to the client. 
 */
public class MailRequest implements Serializable {

	private static final long serialVersionUID = 8146517154357247722L;
	
	final private RequestType type;
	final private Mail mail;  // Mail to send with SEND_MAIL request.
	final private String otherClient;  // Other client to return correspondence, with GET_CORRESPONDANCES request.
	final private int amount; // The amount of mail requested.
	// The response that will be attached to the request on its way back to the client:
	private MailResponse response;
	
	/**
	 * Enum class for representing the different types of possible requests.
	 */
	public static enum RequestType {
		SEND_MAIL,
		GET_MAIL_SENT,
		GET_INCOMING,
		GET_ALL_MAIL,
		GET_CORRESPONDANCES,
		GET_UNREAD,
		GET_CONTACTS;
	}
	
	/**
	 * Create a SEND_MAIL request.
	 * @param mail the mail to send.
	 * @return the appropriate MailRequest.
	 */
	public static MailRequest sendMail(Mail mail) {
		return new MailRequest(RequestType.SEND_MAIL, mail);
	}
	
	/**
	 * Create a GET_MAIL_SENT request.
	 * @param amount the amount of mail to retrieve
	 * @return the appropriate MailRequest.
	 */
	public static MailRequest getMailSent(int amount) {
		return new MailRequest(RequestType.GET_MAIL_SENT, amount);
	}
	
	/**
	 * Create a GET_INCOMING request.
	 * @param amount the amount of mail to retrieve
	 * @return the appropriate MailRequest.
	 */
	public static MailRequest getIncoming(int amount) {
		return new MailRequest(RequestType.GET_INCOMING, amount);
	}
	
	/**
	 * Create a GET_ALL_MAIL request.
	 * @param amount the amount of mail to retrieve
	 * @return the appropriate MailRequest.
	 */
	public static MailRequest getAllMail(int amount) {
		return new MailRequest(RequestType.GET_ALL_MAIL, amount);
	}
	
	/**
	 * Create a GET_CORRESPONDANCES request.
	 * @param otherClient the client to get the correspondance with.
	 * @param amount the amount of mail to retrieve
	 * @return the appropriate MailRequest.
	 */
	public static MailRequest getCorrespondences(String otherClient, int amount) {
		return new MailRequest(RequestType.GET_CORRESPONDANCES, amount, null, otherClient);
	}
	
	/**
	 * Create a GET_UNREAD request.
	 * @return the appropriate MailRequest.
	 */
	public static MailRequest getUnread() {
		return new MailRequest(RequestType.GET_UNREAD);
	}
	
	/**
	 * Create a GET_CONTACTS request.
	 * @return the appropriate MailRequest.
	 */
	public static MailRequest getContacts() {
		return new MailRequest(RequestType.GET_CONTACTS);
	}

	// Private constructors used by the factory methods above.
	private MailRequest(RequestType type, String otherClient) {
		this(type, -1, null, otherClient);
	}
	
	private MailRequest(RequestType type, Mail mail) {
		this(type, -1, mail, null);
	}
	
	private MailRequest(RequestType type, int amount) {
		this(type, amount, null, null);
	}
	
	private MailRequest(RequestType type) {
		this(type, -1, null, null);
	}
	
	private MailRequest(RequestType type, int amount, Mail mail, String otherClient) {
		this.type = type;
		this.amount = amount;
		this.mail = mail;
		this.otherClient = otherClient;
	}
	
	/**
	 * Attach a response to this request. A response should be attached by the
	 * server before sending the request back to the client.
	 * @param response the response to attach
	 */
	public void attachResponse(MailResponse response) {
		this.response = response;
	}
	
	/**
	 * Get the type of the request. 
	 * @return request type.
	 */
	public RequestType getType() {
		return type;
	}
	
	/**
	 * Get the amount of mails requested.
	 * @return the amount of mails requested.
	 */
	public int getAmount() {
		if (amount < 0) {
			throw new RuntimeException("No amount provided with this request. Check your request type.");
		}
		return amount;
	}

	/**
	 * Get the mail that is send in this request. Used in SEND_MAIL requests.
	 * @return the mail provided to this request
	 */
	public Mail getMail() {
		if (mail == null) {
			throw new RuntimeException("No mail provided with this request. Check your request type.");
		}
		return mail;
	}

	/**
	 * Get the other client in a GET_CORRESPONDANCES request.
	 * @return the address of the other client provided to this request.
	 */
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

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MailRequest other = (MailRequest) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (mail == null) {
			if (other.mail != null)
				return false;
		} else if (!mail.equals(other.mail))
			return false;
		if (otherClient == null) {
			if (other.otherClient != null)
				return false;
		} else if (!otherClient.equals(other.otherClient))
			return false;
		if (amount != other.amount) {
			return false;
		}
		if (response == null) {
			if (other.response != null)
				return false;
		} else if (!response.equals(other.response))
			return false;

		return true;
	}
}
