package il.ac.technion.cs.sd.lib;

// TODO add javasoc
public interface EncoderDecoder<T> {
	public byte[] encode(T obj);
	public T decode(byte[] b);
}
