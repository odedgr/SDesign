package il.ac.technion.cs.sd.app.mail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * A Container for all of a single client's mail and information.
 */
public class MailBox {
	
	Set<MailEntry> unread = new LinkedHashSet<MailEntry>(); 
	List<MailEntry> all_mail = new ArrayList<MailEntry>(); // ordered from oldest (start) to newest (end). new mails are appended at the end
	List<MailEntry> sent = new ArrayList<MailEntry>(); // ordered from oldest (start) to newest (end). new mails are appended at the end
	List<MailEntry> inbox = new ArrayList<MailEntry>(); // ordered from oldest (start) to newest (end). new mails are appended at the end
	Map<String, List<MailEntry>> correspondece = new HashMap<String, List<MailEntry>>();
	
	/**
	 * Add a new mail this client sent a new mail. Updates this client's contact list if needed.
	 * 
	 * @param entry The Mail object this client sent.
	 */
	public void addSentMail(MailEntry entry) {
		this.sent.add(entry);
		this.all_mail.add(entry);
		addToCorrespondenceWith(entry, entry.getMail().to);
	}
	
	/**
	 * Add a mail as a part of a correspondence with another client.
	 * 
	 * @param entry Mail with other client to be added.
	 * @param other Address of other client with whom the correspondence took place.
	 */
	private void addToCorrespondenceWith(MailEntry entry, String other) {
		if (!correspondece.containsKey(other)) {
			correspondece.put(other, new ArrayList<MailEntry>());
		}
		this.correspondece.get(other).add(entry);
	}

	/**
	 * Add a new mail this client received. Updates this client's contact list if needed.
	 * The received mail is marked as unread until it is returned in some query.
	 * 
	 * @param entry Mail object this client received.
	 */
	public void addReceivedMail(MailEntry entry) {
		this.inbox.add(entry);
		this.all_mail.add(entry);
		if (!entry.getIsRead()) {
			this.unread.add(entry);
		}
		addToCorrespondenceWith(entry, entry.getMail().from);
	}
	
	/**
	 * Mark a mail as read.
	 * 
	 * @param entry Mail to be marked.
	 */
	private void markAsRead(MailEntry entry) {
		entry.markAsRead();
		this.unread.remove(entry);
	}
	
	/**
	 * Return a list of all the unread mail. Marks each mail item as "read".
	 * 
	 * @return List of unread mail.
	 */
	public List<MailEntry> getUnread() {
		// Get the list to return.
		List<MailEntry> $ = new ArrayList<MailEntry>(unread);
		Collections.reverse($);
		
		// Mark all mails as read
		unread.stream().forEach(entry -> entry.markAsRead());
		unread.clear();
		
		return $;
	}
	
	/**
	 * Get a list of at most N most recently sent mail items by this client.
	 * 
	 * @param n Maximal amount of mail items to be returned.
	 * @return List of at most N most recent mail items this client sent.
	 */
	public List<MailEntry> getLastNSent(int n) {
		if (n < 0) {
			throw new IllegalArgumentException("requested amount must be non-negative");
		}
		
		return getLastMailsOrdered(this.sent, n);
	}
	
	/**
	 * Get a list of at most N most recent Mail this client received (both read and unread), and mark unread as read.
	 * 
	 * @param n - maximal amount or mail items to return.
	 * @return List of at most N most recent mails this client received.
	 */
	public List<MailEntry> getLastNReceived(int n) {
		if (n < 0) {
			throw new IllegalArgumentException("requested amount must be non-negative");
		}
		
		List<MailEntry> lastReceived = getLastMailsOrdered(this.inbox, n);
		
		for (MailEntry mail : lastReceived) {
			markAsRead(mail);
		}
		
		return lastReceived;
	}
	
	/**
	 * Get at most N most recent Mail items this client either sent or received. Mark any previously unread received items as read.
	 * 
	 * @param howMany Maximal amount of mail items to return.
	 * @return List of N most recent Mail items.
	 */
	public List<MailEntry> getLastNMails(int howMany) {
		if (howMany < 0) {
			throw new IllegalArgumentException("requested amount must be non-negative");
		}
		
		List<MailEntry> $ = getLastMailsOrdered(all_mail, howMany);
		for (MailEntry mail : $) {
			markAsRead(mail);
		}
		return $;
	}
	
	/**
	 * Get a list of this client's contacts, made up from all other clients this client ever sent/received a mail to/from.
	 * 
	 * @return List of this client's contacts.
	 */
	public List<String> getContacts() {
		return this.correspondece.keySet().stream().sorted().collect(Collectors.toList());
	}

	/**
	 * Get a list of this client's correspondence with another client.
	 * 
	 * @param otherClient Address of other client, with whom correspondence is required.
	 * @param howMany Maximal amount of returned Mail items.
	 * @return A list of Mail items sent between this and the other client, ordered by time of arrival.
	 */
	public List<MailEntry> getCorrespondeceWith(String otherClient, int howMany) {
		List<MailEntry> allMail = correspondece.get(otherClient);
		
		if (null == allMail) {
			// No correspondence with other client - return an empty list.
			return new ArrayList<MailEntry>();
		}
		
		// truncate only new items, and reverse order so newest is first
		List<MailEntry> $ = getLastMailsOrdered(allMail, howMany);
		for (MailEntry entry : $) {
			markAsRead(entry);
		}
		return $;
	}
	
	/**
	 * Get a list of at most N most recent (last) Mail items from a given list of Mail objects. Does NOT mark as read.
	 * 
	 * @param howMany - maximal amount of mail items to return.
	 * @return NEW List of at most N most recent mail entries from the list, ordered from newest to oldest.
	 */	
	private List<MailEntry> getLastMailsOrdered(List<MailEntry> list, int howMany) {
		howMany = Math.min(howMany, list.size());
		
		List<MailEntry> $ = new ArrayList<MailEntry>(list.subList(list.size() - howMany, list.size()));
		Collections.reverse($);
		return $;
	}
	
}
