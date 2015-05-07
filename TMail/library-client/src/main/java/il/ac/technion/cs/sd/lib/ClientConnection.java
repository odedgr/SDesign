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
public class ClientConnection<T extends Serializable> {
	
	final private Messenger messenger;
	final private String serverAddress;
	
	public static <T extends Serializable> ClientConnection<T> create(String clientAddress, String serverAddress) throws MessengerException {
		Messenger messanger = new MessengerFactory().start(clientAddress);
		return new ClientConnection<T>(serverAddress, messanger);
	}
	
	// TODO maybe need to save state (active / inactive)?
	public void kill() throws MessengerException {
		messenger.kill();
		
	}
	
	public ClientConnection(String serverAddress, Messenger messenger) {
		this.serverAddress = serverAddress;
		this.messenger = messenger;
	}
	
	public String address() {
		return messenger.getAddress();
	}
	
	public String server() {
		return serverAddress;
	}
	
	// TODO: handle/ignore exceptions according to staff orders
	public void send(T msg) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(bos);
			oos.writeObject(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			messenger.send(serverAddress, wrapWithMyAddress(bos.toByteArray()));
		} catch (MessengerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	// TODO method with Charset parameter might not be necessary. maybe only system default is enough.
	/**
	 * Wrap a byte[] representing an object to be sent, with this client's address byte representation, using a specific Charset.
	 * 
	 * @param objBytes The byte[] to be wrapped with this client's address.
	 * @param charset The Charset to be used for encoding the address String.
	 * @return byte[] including this client's address and the serialized (encoded) object.
	 */
	private byte[] wrapWithMyAddress(byte[] objBytes, Charset charset) {
		byte[] addressBytes = messenger.getAddress().getBytes(charset);
		int addressSizeInBytes = addressBytes.length;
		
		ByteBuffer buff = ByteBuffer.allocate(Integer.BYTES + addressSizeInBytes + objBytes.length);
		buff.putInt(addressSizeInBytes)
			.put(addressBytes)
			.put(objBytes);
		
		return buff.array();
	}
	
	/**
	 * Wrap a byte[] representing an object to be sent, with this client's address byte representation.
	 * 
	 * The clients address, represented as a String object, is encoded using the system's default Charset encoding.
	 * 
	 * @param objBytes The byte[] to be wrapped with this client's address.
	 * @return byte[] including this client's address and the serialized (encoded) object.
	 */
	private byte[] wrapWithMyAddress(byte[] objBytes) {
		return wrapWithMyAddress(objBytes, Charset.defaultCharset());
	}
	
	public Optional<T> receiveSingle() {
		Optional<byte[]> bytes;
		try {
			bytes = messenger.tryListen();
			if (!bytes.isPresent()) {
				return Optional.empty();
			}
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes.get());
			ObjectInputStream ois;
			ois = new ObjectInputStream(bis);
			
			@SuppressWarnings("unchecked")
			Optional<T> retVal = (Optional<T>) Optional.of(ois.readObject());
			return retVal;
		} catch (MessengerException | ClassNotFoundException | IOException e) {
			// TODO - What to do in case of failure?!
			e.printStackTrace();
		}
		return Optional.empty();
	}

	public T receiveSingleBlocking() {
		byte[] bytes;
		try {
			bytes = messenger.listen();
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			ObjectInputStream ois;
			ois = new ObjectInputStream(bis);
			
			@SuppressWarnings("unchecked")
			T retVal = (T)ois.readObject();
			return retVal;
		} catch (MessengerException | ClassNotFoundException | IOException e) {
			// TODO - What to do in case of failure?!
			e.printStackTrace();
		}
		System.out.println("SHOULD NEVER HAPPEN! null returned from receiveSingleBlocking");
		return null; // TODO: can not happen? if can happen, deal with it differently and NOT return null.
	}
	
	public List<T> getAllMessages(int limit) {
		return null;
	}
	
	// TODO: maybe implement a method of receiving multiple of a message.
	
	
}


