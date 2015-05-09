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
// TODO: test?

/**
 * 
 * 
 */
public class ServerConnection<T extends Serializable> {
	
	private Messenger messenger; // cannot be final, because uopn stop/start new ones are created, and can't be revived
	final private String myAddress;
	
	public static <T extends Serializable> ServerConnection<T> create(String address) throws MessengerException {
		return new ServerConnection<T>(address);
	}
	
	public void stop() {
		if (null == this.messenger) {  // already stopped
			return;
		}
		
		try {
			messenger.kill();
		} catch (MessengerException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			messenger = null;
		}
		
	}
	
	public void start() {
		if (null != this.messenger) { // already started - no need to start again
			return;
		}
		
		try {
			this.messenger = new MessengerFactory().start(myAddress);
		} catch (MessengerException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
	
	private ServerConnection(String address) {
		this.myAddress = address;
	}
	
	/**
	 * Get this ServerConnection instance address.
	 * 
	 * @return Address of this server connection.
	 */
	public String address() {
		return this.myAddress;
	}
	
	// TODO: handle/ignore exceptions according to staff orders
	public void send(String clientAddress, T msg) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(bos);
			oos.writeObject(msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			messenger.send(clientAddress, bos.toByteArray());
		} catch (MessengerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Optional<T> receiveSingle() {
		// TODO change return type, so both the object and the clients source address ('from') is returned.
		Optional<byte[]> bytes;
		try {
			bytes = messenger.tryListen();
			if (!bytes.isPresent()) {
				return Optional.empty();
			}

			ByteBuffer buff = ByteBuffer.wrap(bytes.get());
			
			String from = extractClientAddress(buff);
			byte[] msgBytes = extractPayload(buff, from.getBytes().length);
			
			ByteArrayInputStream bis = new ByteArrayInputStream(msgBytes);
			ObjectInputStream ois = new ObjectInputStream(bis);
			
			return (Optional<T>) ois.readObject(); // TODO: shouldn't the cast be to T, and only then wrap with Optional?
		} catch (MessengerException | ClassNotFoundException | IOException e) {
			// TODO - What to do in case of failure?!
			e.printStackTrace();
		}
		return Optional.empty();
	}

	/**
	 * Extract raw bytes (byte[]) of serialized object from raw received message.
	 * 
	 * @param buff ByteBuffer wrapping the raw byte[] as received from messenger.
	 * @param offset Start point in the byte[] in the ByteBuffer, from which to extract the object's byte[]
	 * @return byte[] containing only the original serialized (encoded) Object.
	 */
	private byte[] extractPayload(ByteBuffer buff, int offset) {
		
		int metadataSize = offset + Integer.BYTES;
		
		byte[] bytes = new byte[buff.capacity() - metadataSize];
		buff.get(bytes, metadataSize, bytes.length);
		
		return buff.array();
	}

	/**
	 * Re-build the client's address as a String object from raw message, using the system's default Charset.
	 * 
	 * @param buff ByteBuffer object wrapped around the raw byte[] of received message.
	 * @return String object representing the sending client's address.
	 */
	private String extractClientAddress(ByteBuffer buff) {
		return extractClientAddress(buff, Charset.defaultCharset());
	}
	
	// TODO method with specific charset might not be necessary, maybe only system's default encoding is needed
	/**
	 * Re-build the client's address as a String object from raw message, using a specific Charset.
	 * 
	 * @param buff ByteBuffer object wrapped around the raw byte[] of received message.
	 * @param charset Charset to be used for decoding bytes into a String object.
	 * @return String object representing the sending client's address.
	 */
	private String extractClientAddress(ByteBuffer buff, Charset charset) {
		int addressSizeInBytes = buff.getInt(0);
		byte[] addressBytes = new byte[addressSizeInBytes]; 
		
		buff.get(/* output destination */    addressBytes, 
				 /* offset in byte buffer */ Integer.BYTES, 
				 /* amount of bytes */       addressSizeInBytes);
		
		return new String(addressBytes, charset);
	}

	public T receiveSingleBlocking() {
		// TODO change return type so it contains both the object and the source client's address ('from')
		byte[] bytes;
		try {
			bytes = messenger.listen();
			
			ByteBuffer buff = ByteBuffer.wrap(bytes);
			
			String from = extractClientAddress(buff);
			byte[] msgBytes = extractPayload(buff, from.getBytes().length);

			ByteArrayInputStream bis = new ByteArrayInputStream(msgBytes);
			ObjectInputStream ois;
			ois = new ObjectInputStream(bis);
			return (T)ois.readObject();
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


