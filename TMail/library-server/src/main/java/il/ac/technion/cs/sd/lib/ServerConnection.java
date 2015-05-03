package il.ac.technion.cs.sd.lib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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
	
	final private Messenger messenger;
	
	public static <T extends Serializable> ServerConnection<T> create(String address) throws MessengerException {
		Messenger messanger = new MessengerFactory().start(address);
		return new ServerConnection<T>(messanger);
	}
	
	// TODO maybe need to save state (active / inactive)?
	public void kill() throws MessengerException {
		messenger.kill();
	}
	
	private ServerConnection(Messenger messenger) {
		this.messenger = messenger;
	}
	
	public String address() {
		return messenger.getAddress();
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
	
	// TODO: how do we get the address of the client?
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
			return (Optional<T>) ois.readObject(); // TODO: shouldn't the cast be to T, and only then wrap with Optional?
		} catch (MessengerException | ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
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
			return (T)ois.readObject();
		} catch (MessengerException | ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
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


