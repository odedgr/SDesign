package il.ac.technion.cs.sd.app.mail;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Container for all of a single client's mail and information.
 */
public class MailBox {
	
	/*
	 * Wraps the mail so the equality will be based on identity and not on content equality.
	 */
	private static class MailWrapper {
	    final public Mail mail;
	    
	    public MailWrapper(Mail mail) {
			this.mail = mail;
		}
	    
	    @Override
	    public boolean equals(Object obj) {
	        MailWrapper otherWrapper = (MailWrapper)obj;
	        return (otherWrapper.mail == this.mail);
	    }
	    
	    @Override
	    public int hashCode() {
	    	return mail.hashCode();
	    }
	} 
	
	Set<MailWrapper> unread = new LinkedHashSet<MailWrapper>(); // <mail.hashCode(), mail>
	List<Mail> all_mail = new ArrayList<Mail>(); // ordered from oldest (start) to newest (end). new mails are appended at the end
	List<Mail> sent = new ArrayList<Mail>(); // ordered from oldest (start) to newest (end). new mails are appended at the end
	List<Mail> inbox = new ArrayList<Mail>(); // ordered from oldest (start) to newest (end). new mails are appended at the end
	Map<String, List<Mail>> correspondece = new HashMap<String, List<Mail>>();
	
	/**
	 * Load a complete mailbox from a previously stored database.
	 * 
	 * @param client - This clients address
	 * @param mails - List of Mail objects this clients mailbox has, by order of sending/receiving.
	 * @param read - List of boolean markers for each matching Mail index in mails, indicating if it was
	 * 		already read by the client or not.
	 */
	public void loadContentsFromDatabase(String client, List<Mail> mails, List<Boolean> read) {
		if (null == mails || null == read) {
			throw new NullPointerException("Received either a null list of mails, or list of read");
		}
		
		if (mails.size() != read.size()) {
			throw new InvalidParameterException("Lists sizes don't match");
		}
		
		Mail mail;
		for (int i = 0; i < mails.size(); ++i) {
			mail = mails.get(i);
			boolean mail_read = read.get(i);
			
			if (client == mail.from && client == mail.to) {
				this.sent.add(mail);
				this.all_mail.add(mail);
				this.inbox.add(mail);
				this.unread.add(new MailWrapper(mail));
				addToCorrespondenceWith(mail, mail.to);
				if (mail_read) {
					markAsRead(mail);
				}
				continue;
			}  
			if (client == mail.from) { // this client sent the mail
				sentMail(mail);
				continue;
			}

			receivedMail(mail);
			if (mail_read) {
				markAsRead(mail);
			}
		}
	}
	
	/**
	 * Get this mailbox contents in a fashion for string it to a database. Lists received as arguments
	 * will be filled with all this mailbox's contents.
	 * 
	 * @param mails - OUTPUT: List that will hold all of this clients of Mail objects, by order of sending/receiving.
	 * @param read - OUTPUT: List of boolean markers that will hold for each matching Mail index in mails, indicating if it was
	 * 		already read by the client or not.
	 */
	public void dumpContentsForDatabase(List<Mail> mails, List<Boolean> read) {
		if (null == mails || null == read) {
			throw new NullPointerException("Received either a null list of mails, or list of read");
		}
		
		mails.clear();
		read.clear();
		
		for (Mail mail : all_mail) {
			mails.add(mail);
			read.add(unread.contains(new MailWrapper(mail)));
		}
	}
	
	/**
	 * Add a new mail this client sent a new mail. Updates this client's contact list if needed.
	 * 
	 * @param mail The Mail object this client sent.
	 */
	public void sentMail(Mail mail) {
		this.sent.add(mail);
		this.all_mail.add(mail);
		addToCorrespondenceWith(mail, mail.to);
	}
	
	/**
	 * Add a mail as a part of a correspondence with another client.
	 * 
	 * @param mail Mail with other client to be added.
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
	 * Add a new mail this client received. Updates this client's contact list if needed.
	 * The received mail is marked as unread until it is returned in some query.
	 * 
	 * @param mail Mail object this client received.
	 */
	public void receivedMail(Mail mail) {
		this.inbox.add(mail);
		this.all_mail.add(mail);
		this.unread.add(new MailWrapper(mail));
		
		addToCorrespondenceWith(mail, mail.from);
	}
	
	/**
	 * Mark a mail as read.
	 * 
	 * @param mail Mail to be marked.
	 */
	private void markAsRead(Mail mail) {
		this.unread.remove(new MailWrapper(mail));
	}
	
	/**
	 * Return a list of all the unread mail. Marks each mail item as "read".
	 * 
	 * @return List of unread mail.
	 */
	public List<Mail> getUnread() {
		List<Mail> unreadList = new ArrayList<Mail>();
		for (MailWrapper mw : this.unread) {
			unreadList.add(mw.mail);
		}
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
	public List<Mail> getLastNMails(int howMany) {
		if (howMany < 0) {
			throw new InvalidParameterException("requested amount must be non-negative");
		}
		
		List<Mail> lastMail = getLastMailsOrdered(this.all_mail, howMany);
		
		for (Mail mail : lastMail) {
			markAsRead(mail);
		}
		
		return lastMail;
	}
	
	/**
	 * Get a list of this client's contacts, made up from all other clients this client ever sent/received a mail to/from.
	 * 
	 * @return List of this client's contacts.
	 */
	public List<String> getContacts() {
		return new ArrayList<String>(this.correspondece.keySet());
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
			markAsRead(mail);
		}
		
		return lastMailsOrdered;
	}
	
	/**
	 * Get a list of at most N most recent (last) Mail items from a given list of Mail objects. Does NOT mark as read.
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
