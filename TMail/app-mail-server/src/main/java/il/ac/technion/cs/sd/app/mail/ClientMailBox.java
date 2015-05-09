package il.ac.technion.cs.sd.app.mail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A Container for all of a single client's mail and information.
 * 
 */
public class ClientMailBox {
	
	// TODO validate assumption - timestamps are unique
	Map<Long, MailEntry> unread = new HashMap<Long, MailEntry>(); // map (Time of arrival : MailEntry)
	List<MailEntry> sent = new ArrayList<MailEntry>(); // ordered from oldest (start) to newest (end). new mails are appended at the end
	List<MailEntry> inbox = new ArrayList<MailEntry>(); // ordered from oldest (start) to newest (end). new mails are appended at the end
	Map<String, List<MailEntry>> correspondece = new HashMap<String, List<MailEntry>>();
	
	/**
	 * Add a new mail entry, when this client sends a new mail. Updates this client's contact list if needed.
	 * 
	 * @param me MailEntry object containing the mail item this client sent.
	 */
	public void addSentMailEntry(MailEntry me) {
		this.sent.add(me);
		addToCorrespondenceWith(me, me.to());
	}
	
	private void addToCorrespondenceWith(MailEntry entry, String other) {
		List<MailEntry> list = this.correspondece.get(other);
		
		if (null == list) { // first correspondence with other client - not yet in map
			list = new ArrayList<MailEntry>();
			list.add(entry);
			this.correspondece.put(other, list);
			return;
		}
		else { // previous correspondence with other client exists - just add new one
			list.add(entry);
		}
		
	}

	/**
	 * Add a new mail entry, when this client receives a new mail.  Updates this client's contact list if needed.
	 * 
	 * @param me MailEntry object containing the mail item this client received.
	 */
	public void addReceivedMailEntry(MailEntry me) {
		this.inbox.add(me);
		this.unread.put(me.time(), me);
		addToCorrespondenceWith(me, me.from());
	}
	
	
	/**
	 * Mark a MailEntery as read.
	 * 
	 * @param me MailEntry to be marked.
	 */
	public void readMailEntry(MailEntry me) {
		this.unread.remove(me.time());
	}
	
	
	/**
	 * Return a list of all the unread mail. Marks each mail item as "read".
	 * 
	 * @return List of unread mail.
	 */
	public List<Mail> getUnread() {
		List<Mail> unreadList = new ArrayList<Mail>();
		
		for (MailEntry me : this.unread.values()) {
			unreadList.add(me.mail());
			readMailEntry(me); // also removes each entry as it is "marked"
		}

		return unreadList;
	}
	
	/**
	 * Get a list of at most N most recently sent mail items by this client.
	 * 
	 * @param n Maximal amount of mail items to be returned.
	 * @return List of at most N most recent mail items this client sent.
	 */
	public List<Mail> getLastNSent(int n) {
		return getLastEntries(this.sent, n)
				.stream()
				.map(me -> me.mail())
				.collect(Collectors.toCollection(ArrayList::new));
	}
	
	/**
	 * Get a list of at most N most recent Mail this client received (both read and unread), and mark unread as read.
	 * 
	 * @param n - maximal amount or mail items to return.
	 * @return List of at most N most recent mails this client received.
	 */
	public List<Mail> getLastNReceived(int n) {
		List<Mail> lastReceived = new ArrayList<Mail>();

		for (MailEntry me : getLastEntries(this.inbox, n)) {
			readMailEntry(me);
			lastReceived.add(me.mail());
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
		List<MailEntry> lastSent     = getLastEntries(this.sent, n),
		                lastReceived = getLastEntries(this.inbox, n);
		
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
				combinedList.add(lastReceived.get(receivedIndex++).mail());
				continue;
			}
			
			if (lastReceived.isEmpty()) {
				combinedList.add(lastSent.get(sentIndex++).mail());
				continue;
			} 
			
			if (lastReceived.get(receivedIndex).time() > lastSent.get(sentIndex).time()) {
				combinedList.add(lastReceived.get(receivedIndex++).mail());
				continue;
			}
			
			combinedList.add(lastSent.get(sentIndex++).mail());
		}
		
		return combinedList;
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
		List<MailEntry> allMail = this.correspondece.get(otherClient);
		int topLimit = allMail.size();
		
		if (howMany > topLimit) {
			howMany = topLimit;
		}
		
		this.correspondece.get(otherClient).subList(topLimit - howMany, topLimit);
		
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Get a list of at most N most recent (last) MailEntry items from a given list of MailEntrys. Does NOT mark as read.
	 * 
	 * @param n - maximal amount of mail items to return.
	 * @return List of at most N most recent mail entries from the list.
	 */	
	private List<MailEntry> getLastEntries(List<MailEntry> list, int howMany) {
		int topLimit = list.size();
		
		if (topLimit < howMany) {
			howMany = topLimit;
		}
		
		return list.subList(topLimit - howMany, topLimit);
	}
	
}

