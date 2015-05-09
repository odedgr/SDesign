package il.ac.technion.cs.sd.lib;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class MessageWithSenderCodec<Message> implements
		Codec<MessageWithSender<Message>> {

	final private Codec<Message> messageCodec;
	
	MessageWithSenderCodec(Codec<Message> mc) {
		messageCodec = mc;
	}
	
	@Override
	public byte[] encode(MessageWithSender<Message> message) {
		// Encoding:
		// 1. Sender address string size
		// 2. Sender address string as raw bytes
		// 3. Message context, serialized with the given codec.
		
		byte[] messageBytes = messageCodec.encode(message.content);
		byte[] senderBytes = message.sender.getBytes(Charset.defaultCharset());
		
		ByteBuffer buff = ByteBuffer.allocate(Integer.BYTES
				+ senderBytes.length + messageBytes.length);
		buff.putInt(senderBytes.length)
			.put(senderBytes)
			.put(messageBytes);
		
		return buff.array();
	}

	@Override
	public MessageWithSender<Message> decode(byte[] b) {
		ByteBuffer buff = ByteBuffer.wrap(b);
		
		// Extract sender address.
		byte[] senderBytes = new byte[buff.getInt(0)];
		buff.position(Integer.BYTES);
		buff.get(senderBytes);
		
		// Extract content.
		byte[] contentBytes = new byte[buff.remaining()]; 
		buff.position(Integer.BYTES + senderBytes.length);
		buff.get(contentBytes);
		
		return new MessageWithSender<Message>(
				messageCodec.decode(contentBytes),
				new String(senderBytes, Charset.defaultCharset()));
	}

}
