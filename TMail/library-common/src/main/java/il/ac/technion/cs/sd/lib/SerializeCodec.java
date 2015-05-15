package il.ac.technion.cs.sd.lib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * A codec for encoding/decoding a Serializable object. This class uses the
 * default serialization methods of Java, so the coded object must implement
 * Serializable interface.
 * @param <T> The (serializable) object type to encode/decode.
 */
public class SerializeCodec<T extends Serializable> implements Codec<T> {
	/**
	 * Encodes an object to an array of bytes.
	 * @param obj the object to encode.
	 * @return an array of bytes represents the object's data.
	 */
	@Override
	public byte[] encode(T obj) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = null;
			oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			return bos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Decodes object data from an array of bytes.
	 * @param b the array of bytes to decode. 
	 * @return the decoded object.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T decode(byte[] bytes) {
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			ObjectInputStream ois;
			ois = new ObjectInputStream(bis);
			T obj = (T) ois.readObject();
			return obj;
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

}
