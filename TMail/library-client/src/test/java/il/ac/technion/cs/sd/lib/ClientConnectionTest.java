package il.ac.technion.cs.sd.lib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.Optional;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

public class ClientConnectionTest {

	private static final String testClientAddress = "client tester address";
	private static final String testServerAddress = "client tester server address";
	private static final String testString = "test string";
	private ClientConnection<Serializable> cc = null;
	private Messenger messenger = null;
	
	/////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		this.messenger = Mockito.mock(Messenger.class);
		Mockito.when(messenger.getAddress()).thenReturn(testClientAddress);
		cc = ClientConnection.createWithMockMessenger(testServerAddress, messenger);
	}

	@After
	public void tearDown() throws Exception {
		cc.kill();
	}

	/////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	
	@Test
	public void createClientConnection() throws MessengerException {
		ClientConnection<String> local_cc = ClientConnection.create(testClientAddress, testServerAddress);
		
		assertEquals(testClientAddress, local_cc.address());
		assertEquals(testServerAddress, local_cc.server());
		
		local_cc.kill();
	}
	
	@Test (expected=NullPointerException.class)
	public void catchNullClientAddress() throws MessengerException {
		ClientConnection<String> local_cc = ClientConnection.create(null, testServerAddress);
		local_cc.kill();
	}
	
	@Test (expected=NullPointerException.class)
	public void catchNullServerAddress() throws MessengerException {
		ClientConnection<String> local_cc = ClientConnection.create(testClientAddress, null);
		local_cc.kill();
	}
	
	@Test (expected=InvalidParameterException.class)
	public void catchEmptyClientAddress() throws MessengerException {
		ClientConnection<String> local_cc = ClientConnection.create("", testServerAddress);
		local_cc.kill();
	}
	
	@Test (expected=InvalidParameterException.class)
	public void catchEmptyServerAddress() throws MessengerException {
		ClientConnection<String> local_cc = ClientConnection.create(testClientAddress, "");
		local_cc.kill();
	}
	
	@Test 
	public void blockOnBlockingReceiveWithNoMessage() throws MessengerException, Exception {
		new Thread(() -> {
			try {
				cc.receiveBlocking();
				fail("Should have blocked");
			} catch (Exception e) {
				throw new AssertionError(e);
			}
		}).start();
		Thread.sleep(100L);
	}
	
	@Test (timeout = 50L)
	public void dontBlockOnBlockingReceiveWithMessage() throws MessengerException {
		Mockito.when(messenger.listen()).thenReturn(testString.getBytes());
		
		String resultString = (String) cc.receiveBlocking();
		assertEquals("result string was created from test string - they should match", testString, resultString); 
	}
	
	@Test (timeout = 50L)
	public void dontBlockOnNonBlockingReceiveWithNoMessage() throws MessengerException {
		Optional<Serializable> resultObject = cc.receive();
		assertFalse("There should be no result, because there was no message", resultObject.isPresent());
	}
	
	@Test
	public void reconstructSimpleReceivedString() throws MessengerException {
		Mockito.when(messenger.listen()).thenReturn(testString.getBytes());
		
		Optional<Serializable> resultObject = cc.receive();
		assertTrue("Mockito should have returned an object...", resultObject.isPresent());
		
		String resultString = (String) resultObject.get();
		assertEquals("received string should match the mockito result generated from same string", testString, resultString);
	}
	
	@Test
	public void reconstructReceviedOnlyFieldsObject() throws MessengerException, IOException {
		SerializableOnlyFields sentObject = new SerializableOnlyFields(1, "one", 2.3, true, "two");
		
		Mockito.when(messenger.listen()).thenReturn(serialize(sentObject));
		
		Optional<Serializable> obj = cc.receive();
		assertTrue("Mockito should have simulated an incoming message", obj.isPresent());
		SerializableOnlyFields receivedObject = (SerializableOnlyFields) obj.get();
		
		assertEquals("Received object should match the one that was sent", sentObject, receivedObject);
	}
	
	@Test
	public void reconstructReceviedObjectWithMethods() {
		fail("Not implemented");
	}
	
	@Test
	public void reconstructReceviedCompositeObject() {
		fail("Not implemented");
	}
	
	/////////////////////////////////////////////////////////////////////////////////////
	/////////////                HELPER METHODS                 /////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	
	private <T extends Serializable> byte[] serialize(T obj) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(obj);
		byte[] bytes = bos.toByteArray(); 
		oos.close();
		
		return bytes; 
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	private <T extends Serializable> T deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bis);
		T obj = (T) ois.readObject();
		ois.close();
		
		return obj;
	}
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////
	/////////////                TESTING OBJECTS                 ////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	
	@SuppressWarnings("unused")
	private class SerializableOnlyFields implements Serializable {
		
		private static final long serialVersionUID = 1099245661835871020L;

		public int int_num;
		public String str1;
		public double double_num;
		public boolean flag;
		public String str2;
		
		public SerializableOnlyFields(int int_num, String str1,	double double_num, boolean flag, String str2) {
			this.int_num = int_num;
			this.str1 = str1;
			this.double_num = double_num;
			this.flag = flag;
			this.str2 = str2;
		}
		
		public SerializableOnlyFields() {
			this(1, "one-just-fields", 2.3, false, "two-just-fields");
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null || getClass() != obj.getClass())
				return false;
			
			SerializableOnlyFields other = (SerializableOnlyFields)obj;
			
			if (!this.str1.equals(other.str1) || !this.str2.equals(other.str2))
				return false;

			if (this.int_num != other.int_num || 0 != Double.compare(this.double_num, other.double_num) || this.flag != other.flag)
				return false;

			return true;
		}
	}

	@SuppressWarnings("unused")
	private class SerializableWithMethods extends SerializableOnlyFields {

		private static final long serialVersionUID = -76137520631303540L;

		public SerializableWithMethods(int int_num, String str1, double double_num, boolean flag, String str2) {
			super(int_num, str1, double_num, flag, str2);
		}
		
		public SerializableWithMethods() {
			this(1, "one-with-methods", 2.345, true, "two-with-methods");
		}
		
		private void doNothing() {}
		
		private void justPrint() {
			System.out.println("Just printed");
		}

		public void increase() {
			this.int_num += 1;
		}
		
		public void flipFlag() {
			this.flag = (this.flag == false);
		}
		
		public void setStr1(String newStr) {
			if (null == newStr) {
				throw new NullPointerException();
			}
			
			this.str1 = newStr;
		}
		
		public void setStr2(String newStr) {
			if (null == newStr) {
				throw new NullPointerException();
			}
			
			this.str2 = newStr;
		}
		
	}
	
}
