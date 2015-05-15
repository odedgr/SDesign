package il.ac.technion.cs.sd.lib;

import static org.junit.Assert.*;
import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.mockito.Mockito;

public class ClientConnectionTest {

	private static final String testClientAddress = "client tester address";
	private static final String testServerAddress = "client tester server address";
	private ClientConnection<String> connection = null;
	private SerializeCodec<String> codec;
	private Messenger messenger = null;
	
	@Rule
    public Timeout globalTimeout = Timeout.seconds(5);
	
	@Before
	public void setUp() throws Exception {
		messenger = Mockito.mock(Messenger.class);
		connection = ClientConnection.<String>createWithMockMessenger(testServerAddress, messenger);
		Mockito.when(messenger.getAddress()).thenReturn(testClientAddress);
		
		codec = new SerializeCodec<String>();
	}

	@After
	public void tearDown() throws Exception {
		connection.kill();
		Mockito.verify(messenger).kill();
	}

	@Test
	public void testClientAndServerAddress() throws MessengerException {
		assertEquals(testClientAddress, connection.getAddress());
		assertEquals(testClientAddress, connection.getAddress());
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void catchNullClientAddress() throws MessengerException {
		ClientConnection<Integer> local_cc = ClientConnection.<Integer>create(testServerAddress, null);
		
		// Should not get here.
		local_cc.kill();
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void catchNullServerAddress() throws MessengerException {
		ClientConnection<String> local_cc = ClientConnection.create(null, testClientAddress);
		local_cc.kill();
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void catchNullCodec() throws MessengerException {
		ClientConnection<String> local_cc = ClientConnection.create(testClientAddress, testServerAddress, null);
		local_cc.kill();
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void catchEmptyClientAddress() throws MessengerException {
		ClientConnection<String> local_cc = ClientConnection.create("", testServerAddress);
		local_cc.kill();
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void catchEmptyServerAddress() throws MessengerException {
		ClientConnection<String> local_cc = ClientConnection.create(testClientAddress, "");
		local_cc.kill();
	}
	
	@Test
	public void testSend() throws MessengerException {
		String message = "Yo dawg";
		connection.send(message);
		
		Mockito.verify(messenger).send(testServerAddress,
				new MessageWithSenderCodec<String>(codec).encode(
				new MessageWithSender<String>(message, testClientAddress)));
	}
	
	@Test 
	public void blockOnBlockingReceiveWithNoMessage() throws InterruptedException, MessengerException {
		// Wait 10 seconds to simulate a never-coming message...
		Mockito.when(messenger.listen()).thenAnswer(x -> {
			Thread.sleep(100000);
			return null;
		});
		
		new Thread(() -> {
				connection.receiveBlocking();
				fail("Should have blocked");
		}).start();
		Thread.sleep(100L);
	}
	
	@Test
	public void dontBlockOnBlockingReceiveWithMessage() throws MessengerException {
		String message = "Howdy";
		Mockito.when(messenger.listen()).thenReturn(codec.encode(message));
		assertEquals("Messages should match", message, connection.receiveBlocking()); 
	}
	
	@Test
	public void dontBlockOnNonBlockingReceiveWithNoMessage() throws MessengerException {
		Mockito.when(messenger.tryListen()).thenReturn(Optional.empty());
		
		Optional<String> message = connection.receive();
		assertFalse("There should be no result, because there was no message", message.isPresent());
	}
	
	@Test
	public void reconstructSimpleReceivedString() throws MessengerException {
		String message = "Howdy";
		Mockito.when(messenger.tryListen()).thenReturn(Optional.of(codec.encode(message)));
		
		Optional<String> resultMessage = connection.receive();
		assertTrue("A message should have been returned.", resultMessage.isPresent());
		
		assertEquals("Received message should match the one returend from the messenger",
				message, resultMessage.get());
	}
}
