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
		Codec<Person> codec = new SerializeCodec<Person>();
		MessageWithSenderCodec<Person> msc =
				new MessageWithSenderCodec<Person>(codec);
		
		Person person = new Person();
		
		person.name = "Moshe";
		person.age = 30;
		person.height = 1.83;
		
		MessageWithSender<Person> ms = new MessageWithSender<Person>(
				person, "SarahTheSender");

		byte[] b = msc.encode(ms);
		MessageWithSender<Person> decoded = msc.decode(b);
		assertEquals(ms.sender, decoded.sender);
		
		assertEquals(ms.content.name, decoded.content.name);
		assertEquals(ms.content.age, decoded.content.age);
		assertEquals(ms.content.height, decoded.content.height, 0.0001);
	}

}

