package il.ac.technion.cs.sd.lib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
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
	
	public static <Message extends Serializable> ClientConnection<Message> create(
			String clientAddress, String serverAddress) {
		Messenger messanger;
		try {
			messanger = new MessengerFactory().start(clientAddress);
			return new ClientConnection<Message>(serverAddress, messanger,
					new SerializeCodec<Message>());
		} catch (MessengerException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <Message> ClientConnection<Message> Message(
			String clientAddress, String serverAddress, Codec<Message> codec) {
		Messenger messenger;
		try {
			messenger = new MessengerFactory().start(clientAddress);
			return new ClientConnection<Message>(serverAddress, messenger, codec);
		} catch (MessengerException e) {
			throw new RuntimeException(e);
		}
	}
	
	public ClientConnection(String serverAddress, Messenger messenger, Codec<Message> codec) {
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
	
	public Optional<Message> receiveSingle() {
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

	public Message receiveSingleBlocking() {
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


