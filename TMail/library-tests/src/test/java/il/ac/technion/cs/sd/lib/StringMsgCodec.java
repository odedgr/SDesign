package il.ac.technion.cs.sd.lib;

/**
 * A codec for stringMsg. Just saves the inner string as is to the byte buffer. 
 */
public class StringMsgCodec implements Codec<StringMsg> {

	/**
	 * Encode the given string.
	 * @param msg the stringMsg to encode
	 * @return a byte array which holds the string.
	 */
	@Override
	public byte[] encode(StringMsg msg) {
		return msg.str.getBytes();
	}

	
	/**
	 * Decode the given byte array back to the stringMsg.
	 * @param b the byte array to decode.
	 * @return the decoded stringMsg.
	 */
	@Override
	public StringMsg decode(byte[] b) {
		return new StringMsg(new String(b));
	}

}
