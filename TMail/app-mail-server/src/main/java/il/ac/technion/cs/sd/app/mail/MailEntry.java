package il.ac.technion.cs.sd.app.mail;

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
	
	public void markAsRead() {
		this.read = true;
	}
	
	public boolean isUnread() {
		return (false == this.read);
	}
}
