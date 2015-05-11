package il.ac.technion.cs.sd.lib;

import java.io.Serializable;
import java.util.Optional;

import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;
import il.ac.technion.cs.sd.msg.MessengerFactory;

/**
 * Handle all communication for a client application that uses a matching server application for communication,
 * giving an abstraction for sending and receiving messages as Java objects. 
 */
public class ClientConnection<Message> {
	
	final private Messenger messenger;
	final private String serverAddress;
	final private Codec<Message> codec;
	
	/**
	 * Factory method for creating and starting a new client connection, enabling it to communicate with a running server.
	 * This connection is initialized for exchanging Objects of type Message, which has to be serializable.
	 * 
	 * @param clientAddress - Address of this client.
	 * @param serverAddress - Address of this client's server, through which all of its communication is done.
	 * @return A new ClientConnection instance, initialized and ready for communication.
	 */
	public static <Message extends Serializable> ClientConnection<Message> create(String serverAddress, String clientAddress) {
		return create(serverAddress, clientAddress, new SerializeCodec<Message>());
	}

	/**
	 * Factory method for creating and starting a new client connection, enabling it to communicate with a running server.
	 * This connection is initialized for exchanging Objects of any type, given a matching custom Codec object, for 
	 * Encoding / Decoding it into a byte[].
	 * 
	 * @param clientAddress - Address of this client.
	 * @param serverAddress - Address of this client's server, through which all of its communication is done.
	 * @param codec - Custom Codec used for Message conversion to/from byte[]
	 * @return A new ClientConnection instance, initialized and ready for communication.
	 */
	public static <Message> ClientConnection<Message> create(String serverAddress, String clientAddress, Codec<Message> codec) {
		Messenger messenger;
		try {
			messenger = new MessengerFactory().start(clientAddress);
			return new ClientConnection<Message>(serverAddress, messenger, codec);
		} catch (MessengerException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Factory method for a ClientConnection object, that uses a custom messenger, <i>intended for testing purposes.</i> <br>
	 * This connection is initialized for exchanging Objects of type Message, which has to be serializable. <br>
	 * <b>IMPORTANT</b>: If you use this factory method, and invoke its kill() method - the messenger will no longer be usable.  
	 * 
	 * @param serverAddress - Address of this client's server, through which all of its communication is done.
	 * @param messenger - Messenger object to be used by this connection for communication. 
	 * @return A new ClientConnection instance, ready for communication.
	 */
	public static <Message extends Serializable> ClientConnection<Message> createWithMockMessenger(String serverAddress, Messenger messenger) {
		return new ClientConnection<Message>(serverAddress, messenger, new SerializeCodec<Message>());
	}
	
	/**
	 * Factory method for a ClientConnection object, that uses a custom messenger, <i>intended for testing purposes.</i> <br>
	 * This connection is initialized for exchanging Objects of any type, given a matching custom Codec object, for 
	 * Encoding / Decoding it into a byte[]. <br>
	 * <b>IMPORTANT</b>: If you use this factory method, and invoke its kill() method - the messenger will no longer be usable.  
	 * 
	 * @param serverAddress - Address of this client's server, through which all of its communication is done.
	 * @param messenger - Messenger object to be used by this connection for communication.
	 * @param codec - Custom Codec used for Message conversion to/from byte[] 
	 * @return A new ClientConnection instance, ready for communication.
	 */
	public static <Message> ClientConnection<Message> createWithMockMessenger(String serverAddress, Messenger messenger, Codec<Message> codec) {
		return new ClientConnection<Message>(serverAddress, messenger, codec);
	}
	
	/**
	 * Constructor. Creates and initializes a ClientConnection, to work with supplied messenger for low-level
	 * communication, and a given Codec for Encoding/Decoding the type of Messages used by this connection
	 * for conversion Message <-> byte[].
	 * 
	 * @param serverAddress - Address of server with which this client will communicate.
	 * @param messenger - Messenger to be used for doing the actual low-level communication.
	 * @param codec - Encoder/Decoder of Message type used by this connection instance.
	 */
	private ClientConnection(String serverAddress, Messenger messenger, Codec<Message> codec) {
		if (serverAddress == null || messenger == null || codec == null) {
			throw new NullPointerException();
		}
		
		this.serverAddress = serverAddress;
		this.messenger = messenger;
		this.codec = codec;
	}
	
	/**
	 * Retrieve this client's address.
	 * 
	 * @return this clients address.
	 */
	public String address() {
		return messenger.getAddress();
	}
	
	/**
	 * Retrieve this address of the server used for this client's communication.
	 * 
	 * @return Address of server through which this client communicates.
	 */
	public String server() {
		return serverAddress;
	}
	
	/**
	 * Send a given Message object to the server with/through which this client communicates.
	 * 
	 * @param msg Message object to be sent.
	 */
	public void send(Message msg) {
		try {
			MessageWithSender<Message> mws = new MessageWithSender<Message>(
					msg, address());
			messenger.send(serverAddress, new MessageWithSenderCodec<Message>(
					codec).encode(mws));
		} catch (MessengerException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Try to receive an incoming message from this client's server. This is a Non-blocking method.
	 * 
	 * @return An incoming Message from this client's server - if there was one pending.
	 */
	public Optional<Message> receive() {
		try {
			Optional<byte[]> bytes = messenger.tryListen();
			if (!bytes.isPresent()) {
				return Optional.empty();
			}
			return Optional.of(codec.decode(bytes.get()));
		} catch (MessengerException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Receive as incoming message from this client's server. This is a blocking method, and will not return
	 * until a message is received from the server.
	 * 
	 * @return An incoming Message from this client's server.
	 */
	public Message receiveBlocking() {
		try {
			byte[] bytes = messenger.listen();
			return codec.decode(bytes);
		} catch (MessengerException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Kill this connection instance. After invocation, this ClientConnection instance will not be able to do
	 * any more communication, but a new instance with the same client address can be created to handle the same
	 * communication for the same client address.
	 * 
	 * @throws MessengerException
	 */
	public void kill() {
		try {
			messenger.kill();
		} catch (MessengerException e) {
			throw new RuntimeException(e);
		}
	}
}


