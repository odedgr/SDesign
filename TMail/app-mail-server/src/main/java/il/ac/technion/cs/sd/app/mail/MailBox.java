package il.ac.technion.cs.sd.app.mail;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A Container for all of a single client's mail and information.
 * 
 */
public class MailBox {
	
	Map<Integer, Mail> unread = new LinkedHashMap<Integer, Mail>(); // <mail.hashCode(), mail>
	List<Mail> all_mail = new ArrayList<Mail>(); // ordered from oldest (start) to newest (end). new mails are appended at the end
	List<Mail> sent = new ArrayList<Mail>(); // ordered from oldest (start) to newest (end). new mails are appended at the end
	List<Mail> inbox = new ArrayList<Mail>(); // ordered from oldest (start) to newest (end). new mails are appended at the end
	Map<String, List<Mail>> correspondece = new HashMap<String, List<Mail>>();
	
	/**
	 * Add a new mail entry, when this client sends a new mail. Updates this client's contact list if needed.
	 * 
	 * @param mail MailEntry object containing the mail item this client sent.
	 */
	public void sentMail(Mail mail) {
		this.sent.add(mail);
		this.all_mail.add(mail);
		addToCorrespondenceWith(mail, mail.to);
	}
	
	/**
	 * Add a MailEntry as a part of a correspondence with another client.
	 * 
	 * @param mail MailEntry of mail with other client to be added.
	 * @param other Address of other client with whom the correspondence took place.
	 */
	private void addToCorrespondenceWith(Mail mail, String other) {
		List<Mail> list = this.correspondece.get(other);
		
		if (null == list) { // first correspondence with other client - not yet in map
			list = new ArrayList<Mail>();
			list.add(mail);
			this.correspondece.put(other, list);
			return;
		}
		else { // previous correspondence with other client exists - just add new one
			list.add(mail);
		}
		
	}

	/**
	 * Add a new mail entry, when this client receives a new mail.  Updates this client's contact list if needed.
	 * 
	 * @param mail MailEntry object containing the mail item this client received.
	 */
	public void receivedMail(Mail mail) {
		this.inbox.add(mail);
		this.all_mail.add(mail);
		this.unread.put(mail.hashCode(), mail);
		
		addToCorrespondenceWith(mail, mail.from);
	}
	
	/**
	 * Mark a MailEntery as read.
	 * 
	 * @param mail MailEntry to be marked.
	 */
	private void readMail(Mail mail) {
		this.unread.remove(mail.hashCode());
	}
	
	/**
	 * Return a list of all the unread mail. Marks each mail item as "read".
	 * 
	 * @return List of unread mail.
	 */
	public List<Mail> getUnread() {
		List<Mail> unreadList = new ArrayList<Mail>(this.unread.values());
		this.unread.clear();
		Collections.reverse(unreadList);
		return unreadList;
	}
	
	/**
	 * Get a list of at most N most recently sent mail items by this client.
	 * 
	 * @param n Maximal amount of mail items to be returned.
	 * @return List of at most N most recent mail items this client sent.
	 */
	public List<Mail> getLastNSent(int n) {
		if (n < 0) {
			throw new InvalidParameterException("requested amount must be non-negative");
		}
		
		return getLastMailsOrdered(this.sent, n);
	}
	
	/**
	 * Get a list of at most N most recent Mail this client received (both read and unread), and mark unread as read.
	 * 
	 * @param n - maximal amount or mail items to return.
	 * @return List of at most N most recent mails this client received.
	 */
	public List<Mail> getLastNReceived(int n) {
		if (n < 0) {
			throw new InvalidParameterException("requested amount must be non-negative");
		}
		
		List<Mail> lastReceived = getLastMailsOrdered(this.inbox, n);
		
		for (Mail mail : lastReceived) {
			readMail(mail);
		}
		
		return lastReceived;
	}
	
	/**
	 * Get at most N most recent Mail items this client either sent or received. Mark any previously unread received items as read.
	 * 
	 * @param howMany Maximal amount of mail items to return.
	 * @return List of N most recent Mail items.
	 */
	public List<Mail> getLastNMails(int howMany) {
		if (howMany < 0) {
			throw new InvalidParameterException("requested amount must be non-negative");
		}
		
		List<Mail> lastMail = getLastMailsOrdered(this.all_mail, howMany);
		
		for (Mail mail : lastMail) {
			readMail(mail);
		}
		
		return lastMail;
	}
	
	/**
	 * Get a list of this client's contacts, made up from all other clients this client ever sent/received a mail to/from.
	 * 
	 * @return List of this client's contacts.
	 */
	public List<String> getContacts() {
		return new ArrayList<String>(this.correspondece.keySet()); // TODO maybe conversion to list is not needed
	}

	/**
	 * Get a list of this client's correspondence with another client.
	 * 
	 * @param otherClient Address of other client, with whom correspondence is required.
	 * @param howMany Maximal amount of returned Mail items.
	 * @return A list of Mail items sent between this and the other client, ordered by time of arrival.
	 */
	public List<Mail> getCorrespondeceWith(String otherClient, int howMany) {
		List<Mail> allMail = this.correspondece.get(otherClient);
		
		if (null == allMail) { // no correspondence with other client - return empty list
			return new ArrayList<Mail>();
		}
		
		// truncate only new items, and reverse order so newest is first
		List<Mail> lastMailsOrdered = getLastMailsOrdered(allMail, howMany);
		
		for (Mail mail : lastMailsOrdered) {
			readMail(mail);
		}
		
		return lastMailsOrdered;
	}
	
	/**
	 * Get a list of at most N most recent (last) MailEntry items from a given list of MailEntrys. Does NOT mark as read.
	 * 
	 * @param howMany - maximal amount of mail items to return.
	 * @return NEW List of at most N most recent mail entries from the list, ordered from newest to oldest.
	 */	
	private List<Mail> getLastMailsOrdered(List<Mail> list, int howMany) {
		int topLimit = list.size();
		
		if (topLimit < howMany) {
			howMany = topLimit;
		}
		
		List<Mail> result = new ArrayList<Mail>(list.subList(topLimit - howMany, topLimit));
		Collections.reverse(result);
		return result;
	}
	
}

