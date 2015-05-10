package il.ac.technion.cs.sd.lib;

public class MessageWithSender<Message> {
	public Message content;
	public String sender;
	
	public MessageWithSender(Message content, String sender) {
		this.content = content;
		this.sender = sender;
	}
}
