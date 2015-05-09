package il.ac.technion.cs.sd.lib;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class MessageWithSenderEncoderDecoder<Message> implements
		EncoderDecoder<MessageWithSender<Message>> {

	final private EncoderDecoder<Message> messageEncoder;
	
	MessageWithSenderEncoderDecoder(EncoderDecoder<Message> med) {
		messageEncoder = med;
	}
	
	@Override
	public byte[] encode(MessageWithSender<Message> message) {
		// Encoding:
		// 1. Sender address string size
		// 2. Sender address string as raw bytes
		// 3. Message context, serialized with the given EncoderDecoder.
		
		byte[] messageBytes = messageEncoder.encode(message.content);
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
//		final int contentOffset = Integer.BYTES + senderBytes.length;
		buff.position(Integer.BYTES + senderBytes.length);
		byte[] contentBytes = new byte[buff.remaining()]; 
		buff.get(contentBytes);
		
		return new MessageWithSender<Message>(
				messageEncoder.decode(contentBytes),
				new String(senderBytes, Charset.defaultCharset()));
	}

}
