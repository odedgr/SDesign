package il.ac.technion.cs.sd.lib;

import static org.junit.Assert.*;

import org.junit.Test;

public class MessageWithSenderCodecTest {

	@Test
	public void TestIntegerMessage() {
		Codec<Integer> codec = new SerializeCodec<Integer>();
		MessageWithSenderCodec<Integer> msc =
				new MessageWithSenderCodec<Integer>(codec);
		
		MessageWithSender<Integer> ms = new MessageWithSender<Integer>(42, "MosheTheSender");
		
		byte[] b = msc.encode(ms);
		MessageWithSender<Integer> decoded = msc.decode(b);
		assertEquals(ms.content, decoded.content);
		assertEquals(ms.sender, decoded.sender);
	}
	
	@Test
	public void TestStringMessage() {
		Codec<String> codec = new SerializeCodec<String>();
		MessageWithSenderCodec<String> msc =
				new MessageWithSenderCodec<String>(codec);

		MessageWithSender<String> ms = new MessageWithSender<String>(
				"Hello there!", "SarahTheSender");

		byte[] b = msc.encode(ms);
		MessageWithSender<String> decoded = msc.decode(b);
		assertEquals(ms.content, decoded.content);
		assertEquals(ms.sender, decoded.sender);
	}
	
	@Test
	public void CheckSerializableClassEncodedAndDecodedProperly() {
		Codec<AuxMsg> codec = new SerializeCodec<AuxMsg>();
		MessageWithSenderCodec<AuxMsg> msc = new MessageWithSenderCodec<AuxMsg>(codec);
		
		MessageWithSender<AuxMsg> ms = new MessageWithSender<AuxMsg>(AuxMsg.msg2(), "SarahTheSender");
		
		byte[] b = msc.encode(ms);
		MessageWithSender<AuxMsg> decoded = msc.decode(b);
		assertEquals(ms.content, decoded.content);
		assertEquals(ms.sender, decoded.sender);
	}

}

