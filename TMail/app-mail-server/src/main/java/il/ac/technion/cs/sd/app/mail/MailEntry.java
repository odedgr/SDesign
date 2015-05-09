package il.ac.technion.cs.sd.app.mail;

/**
 * Wrapper for Mail objects to be stored in a MailBox object, used in the Mail Server application.
 * An Entry contains a single Mail, and marks its time of arrival at the server, and whether it was read or not.
 */
public class MailEntry {
	public final Mail mail;
	public final long time;
	public boolean read;
	
	public MailEntry(Mail mail) {
		this.mail = mail;
		this.time = System.currentTimeMillis();
		this.read = false;
	}
	
	public MailEntry(Mail mail, long time, boolean read) {
		this.mail = mail;
		this.time = time;
		this.read = read;
	}
	
	/**
	 * Mark the contained Mail object as read (e.g: returned by some client's request)
	 */
	public void markAsRead() {
		this.read = true;
	}
	
	/**
	 * Check if the Mail in this entry was already read or not.
	 * 
	 * @return 'true' if the Mail was never read before, 'false' otherwise.
	 */
	public boolean isUnread() {
		return (false == this.read);
	}
}
