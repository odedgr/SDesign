package il.ac.technion.cs.sd.app.mail;

import java.io.Serializable;
import java.util.List;

// TODO: go over javadocs.

public class MailResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4588924529947508861L;
	
	private final List<Mail> mailList;
	private final List<String> contactsList;
	
	public static MailResponse withMailResults(List<Mail> mailList) {
		return new MailResponse(mailList, null);
	}
	
	public static MailResponse withContactsResults(List<String> contactsList) {
		return new MailResponse(null, contactsList);
	}
	
	private MailResponse(List<Mail> mailList, List<String> contactsList) {
		this.mailList = mailList;
		this.contactsList = contactsList;
	}
	
	/**
	 * Get the mail list in the envelope. Returned list will be empty if this envelope is of an incorrect type (e.g: not the
	 * response for a task that yields results as a list of mails) or if there were no matching results.
	 * 
	 * @return The list of mails contained in this Envelope.
	 */
	public List<Mail> getMailResults() {
		if (mailList == null) {
			throw new RuntimeException("No mail results in this response. Check the containing request type.");
		}
		return mailList;
	}
	
	/**
	 * Get the contacts list in the envelope. Returned list will be empty if this envelope is of an incorrect type (e.g: not the
	 * response for a contacts list request) or if there were no matching results.
	 * 
	 * @return The list of contacts contained in this Envelope.
	 */
	public List<String> getContactsResults() {
		if (contactsList == null) {
			throw new RuntimeException("No contacts results in this response. Check the containing request type.");
		}
		return this.contactsList;
	}
}
