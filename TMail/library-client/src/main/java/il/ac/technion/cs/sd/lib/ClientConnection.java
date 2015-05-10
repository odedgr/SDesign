package il.ac.technion.cs.sd.lib;

import java.io.Serializable;
import java.util.Optional;

import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;
import il.ac.technion.cs.sd.msg.MessengerFactory;

// TODO: pick a better name that T?
// TODO: Wrap all the serlilize and conversion in a a common class or utility func.
// TODO: javadoc.

/**
 * 
 * 
 */
public class ClientConnection<Message> {
	
	final private Messenger messenger;
	final private String serverAddress;
	final private Codec<Message> codec;
	
	public static <Message extends Serializable> ClientConnection<Message> create(String clientAddress, String serverAddress) {
		return create(clientAddress, serverAddress, new SerializeCodec<Message>());
	}

	public static <Message> ClientConnection<Message> create(String clientAddress, String serverAddress, Codec<Message> codec) {
		Messenger messenger;
		try {
			messenger = new MessengerFactory().start(clientAddress);
			return new ClientConnection<Message>(serverAddress, messenger, codec);
		} catch (MessengerException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <Message extends Serializable> ClientConnection<Message> createWithMockMessenger(String serverAddress, Messenger messenger) {
		return new ClientConnection<Message>(serverAddress, messenger, new SerializeCodec<Message>());
	}
	
	public static <Message> ClientConnection<Message> createWithMockMessenger(String serverAddress, Messenger messenger, Codec<Message> codec) {
		return new ClientConnection<Message>(serverAddress, messenger, codec);
	}
	
	private ClientConnection(String serverAddress, Messenger messenger, Codec<Message> codec) {
		if (serverAddress == null || messenger == null || codec == null) {
			throw new NullPointerException();
		}
		
		this.serverAddress = serverAddress;
		this.messenger = messenger;
		this.codec = codec;
	}
	
	public String address() {
		return messenger.getAddress();
	}
	
	public String server() {
		return serverAddress;
	}
	
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

	public Message receiveBlocking() {
		try {
			byte[] bytes = messenger.listen();
			return codec.decode(bytes);
		} catch (MessengerException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void kill() throws MessengerException {
		messenger.kill();
	}
}


