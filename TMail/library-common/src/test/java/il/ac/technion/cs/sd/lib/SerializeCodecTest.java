package il.ac.technion.cs.sd.lib;

import static org.junit.Assert.*;

import org.junit.Test;

public class SerializeCodecTest {

	@Test
	public void CheckIntegerEncodedAndDecodedProperly() {
		Codec<Integer> codec = new SerializeCodec<Integer>();
		byte[] b = codec.encode(17);
		int decodedInt = codec.decode(b);
		assertEquals(17, decodedInt);
	}
	
	@Test
	public void CheckStringEncodedAndDecodedProperly() {
		Codec<String> codec = new SerializeCodec<String>();
		byte[] b = codec.encode("Hello World!");
		String decodedString = codec.decode(b);
		assertEquals("Hello World!", decodedString);
	}
	
	@Test
	public void CheckSerializableClassEncodedAndDecodedProperly() {
		AuxClass person = new AuxClass();
		
		person.name = "Moshe";
		person.age = 30;
		person.height = 1.83;
		
		Codec<AuxClass> codec = new SerializeCodec<AuxClass>();
		byte[] b = codec.encode(person);
		AuxClass decodedPerson = codec.decode(b);
		assertEquals(person.name, decodedPerson.name);
		assertEquals(person.age, decodedPerson.age);
		assertEquals(person.height, decodedPerson.height, 0.001);
	}

}
