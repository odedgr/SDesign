package il.ac.technion.cs.sd.app.mail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A Container for all of a single client's mail and information.
 * 
 */
public class ClientMailBox {
	
	Set<String> contacts = new HashSet<String>(); // no need for duplicates, need insertion to be O(1)
	// TODO validate assumption - timestamps are unique
	Map<Long, MailEntry> unread = new HashMap<Long, MailEntry>(); // map (Time of arrival : MailEntry)
	List<MailEntry> sent = new ArrayList<MailEntry>(); // ordered from oldest (start) to newest (end). new mails are appended at the end
	List<MailEntry> inbox = new ArrayList<MailEntry>(); // ordered from oldest (start) to newest (end). new mails are appended at the end
	
	/**
	 * Add a new mail entry, when this client sends a new mail. Updates this client's contact list if needed.
	 * 
	 * @param me MailEntry object containing the mail item this client sent.
	 */
	public void addSentMailEntry(MailEntry me) {
		this.sent.add(me);
		this.contacts.add(me.mail.to); // "this" client sent the mail
	}
	
	/**
	 * Add a new mail entry, when this client receives a new mail.  Updates this client's contact list if needed.
	 * 
	 * @param me MailEntry object containing the mail item this client received.
	 */
	public void addReceivedMailEntry(MailEntry me) {
		this.inbox.add(me);
		this.unread.put(me.time, me);
		this.contacts.add(me.mail.from); // "this" client received the mail
	}
	
	
	/**
	 * Mark a MailEntery as read.
	 * 
	 * @param me MailEntry to be marked.
	 */
	public void readMailEntry(MailEntry me) {
		me.markAsRead();
		this.unread.remove(me.time);
	}
	
	
	/**
	 * Return a list of all the unread mail. Marks each mail item as "read".
	 * 
	 * @return List of unread mail.
	 */
	public List<Mail> getUnread() {
		List<Mail> unreadList = new ArrayList<Mail>();
		
		for (MailEntry me : this.unread.values()) {
			unreadList.add(me.mail);
			readMailEntry(me); // also removes each entry as it is "marked"
		}

		return unreadList;
	}
	
	/**
	 * Get a list of at most N most recent MailEntry items this client sent.
	 * 
	 * @param n - maximal amount of MailEntry items to return.
	 * @return List of at most N most recent MailEntry items this client sent.
	 */
	private List<MailEntry> getLastNSentEntries(int n) {
		int topLimit = this.sent.size();
		
		if (topLimit < n) {
			n = topLimit;
		}
		
		return this.sent.subList(topLimit - n, topLimit);
	}
	
	
	/**
	 * Get a list of at most N most recently sent mail items by this client.
	 * 
	 * @param n Maximal amount of mail items to be returned.
	 * @return List of at most N most recent mail items this client sent.
	 */
	public List<Mail> getLastNSent(int n) {
		return getLastNSentEntries(n)
				.stream()
				.map(me -> me.mail)
				.collect(Collectors.toCollection(ArrayList::new));
	}

	/**
	 * Get a list of at most N most recent MailEntry items this client received (both read and unread). Does NOT mark as read.
	 * 
	 * @param n - maximal amount of mail items to return.
	 * @return List of at most N most recent mails this client received.
	 */
	private List<MailEntry> getLastNReceivedEntries(int n) {
		int topLimit = this.inbox.size();
		
		if (topLimit < n) {
			n = topLimit;
		}
		
		return this.inbox.subList(topLimit - n, topLimit); // start inclusive, end exclusive
	}
	
	/**
	 * Get a list of at most N most recent Mail this client received (both read and unread), and mark unread as read.
	 * 
	 * @param n - maximal amount or mail items to return.
	 * @return List of at most N most recent mails this client received.
	 */
	public List<Mail> getLastNReceived(int n) {
		List<Mail> lastReceived = new ArrayList<Mail>();

		for (MailEntry me : getLastNReceivedEntries(n)) {
			readMailEntry(me);
			lastReceived.add(me.mail);
		}
		
		return lastReceived;
	}
	
	/**
	 * Get at most N most recent Mail items this client either sent or received. Mark any previously unread received items as read.
	 * 
	 * @param n Maximal amount of mail items to return.
	 * @return List of N most recent Mail items.
	 */
	public List<Mail> getLastNMails(int n) {
		List<MailEntry> lastSent     = getLastNSentEntries(n),
		                lastReceived = getLastNReceivedEntries(n);
		
		List<Mail> combinedList = new ArrayList<Mail>();
		
		int combinedIndex = 0, sentIndex = 0, receivedIndex = 0;
		
		// truncate if there are less than requested items
		if (n > lastSent.size() + lastReceived.size()) {
			n = lastSent.size() + lastReceived.size();
		}
		
		// do a linear-timed merge of at most n Mails from n most recent sent, and n most recent received.
		while (combinedIndex < n && (!lastSent.isEmpty() || !lastReceived.isEmpty())) {
			++combinedIndex;
			if (lastSent.isEmpty()) {
				combinedList.add(lastReceived.get(receivedIndex++).mail);
				continue;
			}
			
			if (lastReceived.isEmpty()) {
				combinedList.add(lastSent.get(sentIndex++).mail);
				continue;
			} 
			
			if (lastReceived.get(receivedIndex).time > lastSent.get(sentIndex).time) {
				combinedList.add(lastReceived.get(receivedIndex++).mail);
				continue;
			}
			
			combinedList.add(lastSent.get(sentIndex++).mail);
		}
		
		return combinedList;
	}
	
	/**
	 * Get a list of this client's contacts, made up from all other clients this client ever sent/received a mail to/from.
	 * 
	 * @return List of this client's contacts.
	 */
	public List<String> getContacts() {
		return new ArrayList<String>(this.contacts); // TODO maybe conversion to list is not needed
	}
}
