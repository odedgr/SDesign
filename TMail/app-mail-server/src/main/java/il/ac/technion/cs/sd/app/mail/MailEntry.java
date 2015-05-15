package il.ac.technion.cs.sd.app.mail;

import java.io.Serializable;

/**
 * Wrapper for Mail objects to be stored in a MailBox object, used in the Mail Server application.
 * An Entry contains a single Mail, and a flag telling whether the mail has already been read.
 * Important: Two mail entries equality identity equality, i.e. two different entries containing the same mail are NOT equal.
 */
public class MailEntry implements Serializable {
	
	private static final long serialVersionUID = 2562855619274459212L;
	
	private final Mail mail;
	private boolean isRead;
	
	/**
	 * Create a new MailEntry.
	 * Mail is unread by default.
	 * 
	 * @param mail the mail item.
	 */
	public MailEntry(Mail mail) {
		this.mail = mail;
		this.isRead = false;
	}
	
	/**
	 * Create a new MailEntry, specifying whether it is read or not.
	 * @param mail
	 * @param isRead
	 */
	public MailEntry(Mail mail, boolean isRead) {
		this.mail = mail;
		this.isRead = isRead;
	}
	
	/**
	 * Get the mail held in this entry.
	 * @return the mail held in this entry. 
	 */
	public Mail getMail() {
		return mail;
	}
	
	/**
	 * Get whether the mail is read or not.
	 * @return a boolean specifying whether the mail is read or not. 
	 */
	public boolean getIsRead() {
		return isRead;
	}
	
	/**
	 * Marks the mail as read.
	 */
	public void markAsRead() {
		isRead = true;
	}
	
	/**
	 * Return whether two mail entries are identically the same.
	 */
	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}
}
