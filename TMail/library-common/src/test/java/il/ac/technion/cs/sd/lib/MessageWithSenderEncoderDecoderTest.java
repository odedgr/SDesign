package il.ac.technion.cs.sd.lib;

import static org.junit.Assert.*;

import org.junit.Test;

public class MessageWithSenderEncoderDecoderTest {

	@Test
	public void TestIntegerMessage() {
		EncoderDecoder<Integer> ed = new SerializeEncoderDecoder<Integer>();
		MessageWithSenderEncoderDecoder<Integer> msed =
				new MessageWithSenderEncoderDecoder<Integer>(ed);
		
		MessageWithSender<Integer> ms = new MessageWithSender<Integer>(42, "MosheTheSender");
		
		byte[] b = msed.encode(ms);
		MessageWithSender<Integer> decoded = msed.decode(b);
		assertEquals(ms.content, decoded.content);
		assertEquals(ms.sender, decoded.sender);
	}
	
	@Test
	public void TestStringMessage() {
		EncoderDecoder<String> ed = new SerializeEncoderDecoder<String>();
		MessageWithSenderEncoderDecoder<String> msed =
				new MessageWithSenderEncoderDecoder<String>(ed);

		MessageWithSender<String> ms = new MessageWithSender<String>(
				"Hello there!", "SarahTheSender");

		byte[] b = msed.encode(ms);
		MessageWithSender<String> decoded = msed.decode(b);
		assertEquals(ms.content, decoded.content);
		assertEquals(ms.sender, decoded.sender);
	}
	
	@Test
	public void CheckSerializableClassEncodedAndDecodedProperly() {
		EncoderDecoder<Person> ed = new SerializeEncoderDecoder<Person>();
		MessageWithSenderEncoderDecoder<Person> msed =
				new MessageWithSenderEncoderDecoder<Person>(ed);
		
		Person person = new Person();
		
		person.name = "Moshe";
		person.age = 30;
		person.height = 1.83;
		
		MessageWithSender<Person> ms = new MessageWithSender<Person>(
				person, "SarahTheSender");

		byte[] b = msed.encode(ms);
		MessageWithSender<Person> decoded = msed.decode(b);
		assertEquals(ms.sender, decoded.sender);
		
		assertEquals(ms.content.name, decoded.content.name);
		assertEquals(ms.content.age, decoded.content.age);
		assertEquals(ms.content.height, decoded.content.height, 0.0001);
	}

}

