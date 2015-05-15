package il.ac.technion.cs.sd.lib;

import java.io.Serializable;
import java.util.Optional;

import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;
import il.ac.technion.cs.sd.msg.MessengerFactory;

// TODO: test?

/**
 * Library class for the server, which allows sending and receiving custom-type
 * messages to and from several clients.
 * 
 * @param <Message>
 *            The message type to pass between the server and clients. All
 *            clients must have the same message type.
 */
public class ServerConnection<Message> {
	
	final private Messenger messenger;
	final private Codec<Message> codec;
	
	/**
	 * Create a server connection with a given address. Using this create
	 * method, the Message type must implement Serializable, since the default
	 * codec is used.
	 * 
	 * @param address the address of the server.
	 * @return An initialized instance of ServerConnection.
	 */
	public static <Message extends Serializable> ServerConnection<Message> create(String address) {
		return create(address, new SerializeCodec<Message>());
	}
	
	/**
	 * Create a server connection with a given address and a custom codec for the message type.
	 * @param address the address of the server.
	 * @param codec a codec to encode/decode the message type.
	 * @return
	 */
	public static <Message> ServerConnection<Message> create(String address, Codec<Message> codec) {
		if (address == null || address.isEmpty()) {
			throw new IllegalArgumentException();
		}
		try {
			Messenger messenger = new MessengerFactory().start(address);
			return new ServerConnection<Message>(messenger, codec);
		} catch (MessengerException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Creates a server with a custom messenger instance. <i>intended for testing purposes.</i>
	 * @param messenger the mock-messenger to use.
	 * @return
	 */
	public static <Message extends Serializable> ServerConnection<Message> createWithMockMessenger(Messenger messenger) {
		return new ServerConnection<Message>(messenger, new SerializeCodec<Message>());
	}
	
	
	/**
	 * Creates a server with a custom messenger instance, and a custom codec for
	 * the Message type. <i>intended for testing purposes.</i>
	 * 
	 * @param messenger the mock-messenger to use.
	 * @param codec the custom codec for the Message type object.
	 * @return
	 */
	public static <Message> ServerConnection<Message> createWithMockMessenger(Messenger messenger, Codec<Message> codec) {
		return new ServerConnection<Message>(messenger, codec);
	}
	
	
	/**
	 * Constructor for the ServerConnection class.
	 * @param codec the codec to use to encode/decode the messages sent and received to and from the clients. 
	 * @param messenger the messenger to use to send and receive messages from the clients.
	 */
	private ServerConnection(Messenger messenger, Codec<Message> codec) {
		if (messenger == null || codec == null) {
			throw new IllegalArgumentException();
		}
		
		this.messenger = messenger;
		this.codec = codec;
	}
	
	/**
	 * Closes the connection. Must be called before the end of the program, or
	 * before opening another connection with the same address.
	 */
	public void kill() {
		try {
			messenger.kill();
		} catch (MessengerException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Get this ServerConnection instance address.
	 * 
	 * @return Address of this server connection.
	 */
	public String getAddress() {
		return this.messenger.getAddress();
	}
	
	/**
	 * Sends a given message to a given client.
	 * @param clientAddress the address of the client to send the message to.
	 * @param message the message to send.
	 */
	public void send(String clientAddress, Message message) {
		try {
			messenger.send(clientAddress, codec.encode(message));
		} catch (MessengerException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Returns an optional that contains a message sent to the server, with the address of the client who sent it.
	 * If no message was sent from any client, an empty optional is returned.
	 * @return an optional containing a message and sender (if exists), or an empty optional (if not).
	 */
	public Optional<MessageWithSender<Message>> receive() {
		try {
			Optional<byte[]> bytes = messenger.tryListen();
			if (!bytes.isPresent()) {
				return Optional.empty();
			}
			return Optional.of(new MessageWithSenderCodec<Message>(codec)
					.decode(bytes.get()));
			
		} catch (MessengerException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Returns a message sent to the server, with the address of the client who sent it.
	 * This call is blocking, which means that the function will wait and not return until a message is sent to the server.
	 * @return a message and sender address.
	 */
	public MessageWithSender<Message> receiveBlocking() {
		try {
			return new MessageWithSenderCodec<Message>(codec).decode(messenger.listen());
		} catch (MessengerException e) {
			throw new RuntimeException(e);
		}
	}
}


