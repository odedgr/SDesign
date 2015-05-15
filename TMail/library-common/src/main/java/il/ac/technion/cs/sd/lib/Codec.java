package il.ac.technion.cs.sd.lib;

/**
 * An interface for encoding and decoding object as an array of bytes, and
 * decoding an array of bytes back to an instance of the object.
 * 
 * @param <T> The object type to encode/decode.
 */
public interface Codec<T> {
	/**
	 * Encodes an object to an array of bytes.
	 * @param obj the object to encode.
	 * @return an array of bytes represents the object's data.
	 */
	public byte[] encode(T obj);
	
	/**
	 * Decodes object data from an array of bytes.
	 * @param b the array of bytes to decode. 
	 * @return the decoded object.
	 */
	public T decode(byte[] b);
}
