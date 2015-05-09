package il.ac.technion.cs.sd.app.mail;

/**
 * Wrapper for Mail objects to be stored in a MailBox object, used in the Mail Server application.
 * An Entry contains a single Mail, and marks its time of arrival at the server, and whether it was read or not.
 */
public class MailEntry {
	private final Mail mail;
	private final long time;
	
	/**
	 * Create a new MailEntry for use upon receiving a new mail from some client to another.
	 * 
	 * @param mail Received Mail item.
	 */
	public MailEntry(Mail mail) {
		this.mail = mail;
		this.time = System.currentTimeMillis();
	}
	
	public MailEntry(Mail mail, long time) {
		this.mail = mail;
		this.time = time;
	}
	
	public long time() {
		return this.time;
	}
	
	public String to() {
		return this.mail.to;
	}
	
	public String from() {
		return this.mail.from;
	}
	
	public Mail mail() {
		return this.mail;
	}
}
