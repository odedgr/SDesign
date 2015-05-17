package il.ac.technion.cs.sd.app.mail;

import java.io.Serializable;
import java.util.List;

/**
 * A response that can be attached to the MailRequest. 
 */
public class MailResponse implements Serializable {

	private static final long serialVersionUID = 4588924529947508861L;
	
	private final List<Mail> mailList;
	private final List<String> contactsList;
	
	/**
	 * Create a response containing a list of mail.
	 * @param mailList the mail list to add to the response.
	 * @return a response containing a list of mail.
	 */
	public static MailResponse withMailResults(List<Mail> mailList) {
		return new MailResponse(mailList, null);
	}
	
	/**
	 * Create a response containing a list of contacts.
	 * @param contactsList the contact list.
	 * @return the response with the contact list.
	 */
	public static MailResponse withContactsResults(List<String> contactsList) {
		return new MailResponse(null, contactsList);
	}
	
	private MailResponse(List<Mail> mailList, List<String> contactsList) {
		this.mailList = mailList;
		this.contactsList = contactsList;
	}
	
	/**
	 * Get the mail list in the response. An exception is thrown in case there is no mail attached. 
	 * @return The list of mails contained in this response.
	 */
	public List<Mail> getMailResults() {
		if (mailList == null) {
			throw new RuntimeException("No mail results in this response. Check the containing request type.");
		}
		return mailList;
	}
	
	/**
	 * Get the contacts list in the response. An exception is thrown in case there are no contacts attached.
	 * @return The list of contacts contained in this Envelope.
	 */
	public List<String> getContactsResults() {
		if (contactsList == null) {
			throw new RuntimeException("No contacts results in this response. Check the containing request type.");
		}
		return this.contactsList;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MailResponse other = (MailResponse) obj;
		if (mailList == null) {
			if (other.mailList != null)
				return false;
		} else if (!mailList.equals(other.mailList))
			return false;
		if (contactsList == null) {
			if (other.contactsList != null)
				return false;
		} else if (!contactsList.equals(other.contactsList))
			return false;

		return true;
	}
}
