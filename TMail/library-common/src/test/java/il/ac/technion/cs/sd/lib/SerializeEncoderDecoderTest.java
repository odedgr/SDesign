package il.ac.technion.cs.sd.lib;

import static org.junit.Assert.*;

import java.io.Serializable;

import org.junit.Test;

public class SerializeEncoderDecoderTest {

	@Test
	public void CheckIntegerEncodedAndDecodedProperly() {
		EncoderDecoder<Integer> ed = new SerializeEncoderDecoder<Integer>();
		byte[] b = ed.encode(17);
		int decodedInt = ed.decode(b);
		assertEquals(17, decodedInt);
	}
	
	@Test
	public void CheckStringEncodedAndDecodedProperly() {
		EncoderDecoder<String> ed = new SerializeEncoderDecoder<String>();
		byte[] b = ed.encode("Hello World!");
		String decodedString = ed.decode(b);
		assertEquals("Hello World!", decodedString);
	}
	
	static class Person implements Serializable {
		private static final long serialVersionUID = 8494530965021917044L;
		
		public String name;
		public int age;
		public double height;
	}

	@Test
	public void CheckSerializableClassEncodedAndDecodedProperly() {
		Person person = new Person();
		
		person.name = "Moshe";
		person.age = 30;
		person.height = 1.83;
		
		EncoderDecoder<Person> ed = new SerializeEncoderDecoder<Person>();
		byte[] b = ed.encode(person);
		Person decodedPerson = ed.decode(b);
		assertEquals(person.name, decodedPerson.name);
		assertEquals(person.age, decodedPerson.age);
		assertEquals(person.height, decodedPerson.height, 0.001);
	}

}
