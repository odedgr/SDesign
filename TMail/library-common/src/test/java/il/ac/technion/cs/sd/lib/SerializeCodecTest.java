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
		AuxMsg msg = AuxMsg.msg1();
		
		Codec<AuxMsg> codec = new SerializeCodec<AuxMsg>();
		byte[] b = codec.encode(msg);
		AuxMsg decodedMsg = codec.decode(b);
		assertEquals(msg, decodedMsg);
	}

}
