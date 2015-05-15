package il.ac.technion.cs.sd.lib;

/**
 * A class that holds the message object sent over the messenger, and the
 * address of the client that sent this message.
 * 
 * @param <Message> The message object type.
 */
public class MessageWithSender<Message> {
	
	/**
	 * The message object instance sent over the the messenger.
	 */
	public Message content;
	/**
	 * The address of the client that sent this message.
	 */
	public String sender;
	
	/**
	 * Construct 
	 * @param content the message object instance sent over the the messenger.
	 * @param sender the address of the client that sent the message.
	 */
	public MessageWithSender(Message content, String sender) {
		this.content = content;
		this.sender = sender;
	}
}
