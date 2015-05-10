package il.ac.technion.cs.sd.lib;

public class StringMsgCodec implements Codec<StringMsg> {

	@Override
	public byte[] encode(StringMsg msg) {
		return msg.str.getBytes();
	}

	@Override
	public StringMsg decode(byte[] b) {
		return new StringMsg(new String(b));
	}

}
