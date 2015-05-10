package il.ac.technion.cs.sd.lib;

import java.io.Serializable;
import java.util.Optional;

import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;
import il.ac.technion.cs.sd.msg.MessengerFactory;

// TODO: javadoc.
// TODO: test?

/**
 * 
 * 
 */
public class ServerConnection<Message> {
	
	final private Messenger messenger;
	final private Codec<Message> codec;
	final private String address;
	
	public static <Message extends Serializable> ServerConnection<Message> create(String address) {
		return create(address, new SerializeCodec<Message>());
	}
	
	public static <Message> ServerConnection<Message> create(String address, Codec<Message> codec) {
		try {
			Messenger messenger = new MessengerFactory().start(address);
			return new ServerConnection<Message>(address, codec, messenger);
		} catch (MessengerException e) {
			throw new RuntimeException(e);
		}
	}
	
	private ServerConnection(String address, Codec<Message> codec, Messenger messenger) {
		this.address = address;
		this.codec = codec;
		this.messenger = messenger;
	}
	
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
	public String address() {
		return this.address;
	}
	
	public void send(String clientAddress, Message message) {
		try {
			messenger.send(clientAddress, codec.encode(message));
		} catch (MessengerException e) {
			throw new RuntimeException(e);
		}
	}
	
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

	public MessageWithSender<Message> receiveBlocking() {
		try {
			return new MessageWithSenderCodec<Message>(codec).decode(messenger.listen());
		} catch (MessengerException e) {
			throw new RuntimeException(e);
		}
	}
}


