package il.ac.technion.cs.sd.lib;

import static org.junit.Assert.*;

import java.util.Optional;

import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.mockito.Mockito;

public class ServerConnectionTest {

	private static final String testServerAddress = "servAddrTest";
	
	private ServerConnection<String> connection;
	private Messenger messenger;
	
	private MessageWithSenderCodec<String> codec;
	
	@Rule
    public Timeout globalTimeout = Timeout.seconds(5);

	@Before
	public void setUp() throws Exception {
		messenger = Mockito.mock(Messenger.class);
		Mockito.when(messenger.getAddress()).thenReturn(testServerAddress);
		connection = ServerConnection.<String>createWithMockMessenger(messenger);
		
		codec = new MessageWithSenderCodec<String>(new SerializeCodec<String>());
	}

	@After
	public void tearDown() throws Exception {
		connection.kill();
		Mockito.verify(messenger).kill();
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void exceptionThrownWhenNullAddressPassed() {
		ServerConnection<Integer> con = ServerConnection.<Integer>create(null);

		// Should not get here.
		con.kill();
		fail("An exception should have been thrown");
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void exceptionThrownWhenEmptyAddressPassed() {
		ServerConnection<Integer> con = ServerConnection.<Integer>create("");

		// Should not get here.
		con.kill();
		fail("An exception should have been thrown");
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void exceptionThrownWhenNullCodecPassed() {
		ServerConnection<Integer> con = ServerConnection.<Integer>create("addr", null);
		
		// Should not get here.
		con.kill();
		fail("An exception should have been thrown");
	}
	
	@Test
	public void testGetAddress() {
		assertEquals(testServerAddress, connection.getAddress());
	}

	@Test
	public void verifySendUsesMessenger() throws MessengerException {
		String message = "HI!";
		byte[] encodedMessage = new SerializeCodec<String>().encode(message);
		String clientAddress = "claddr";
		connection.send(clientAddress, message);
		Mockito.verify(messenger).send(clientAddress, encodedMessage);
	}
	
	@Test
	public void testReceive() throws MessengerException {
		String message = "HI!";
		String clientAddress = "claddr";
		byte[] encodedMessage = codec.encode(new MessageWithSender<String>(message, clientAddress));

		Mockito.when(messenger.tryListen()).thenReturn(Optional.of(encodedMessage));
		
		Optional<MessageWithSender<String>> mws = connection.receive();
		assertTrue(mws.isPresent());
		assertEquals(message, mws.get().content);
		assertEquals(clientAddress, mws.get().sender);
	}
	
	@Test
	public void verifyReturnsEmptyOptionalWhenNoMessegePending() throws MessengerException {
		Mockito.when(messenger.tryListen()).thenReturn(Optional.empty());
		
		Optional<MessageWithSender<String>> mws = connection.receive();
		assertFalse(mws.isPresent());
	}
	
	@Test
	public void testReceiveBlocking() throws MessengerException {
		String message = "HI!";
		String clientAddress = "claddr";
		byte[] encodedMessage = codec.encode(new MessageWithSender<String>(message, clientAddress));

		Mockito.when(messenger.listen()).thenReturn(encodedMessage);
		
		MessageWithSender<String> mws = connection.receiveBlocking();
		assertEquals(message, mws.content);
		assertEquals(clientAddress, mws.sender);
	}
	
	@Test(timeout = 1000L)
	public void testRecieveBlocked() throws MessengerException, InterruptedException {
		// Wait 10 seconds to simulate a never-coming message...
		Mockito.when(messenger.listen()).thenAnswer(x -> {
			Thread.sleep(100000);
			return null;
		});
		
		new Thread(() -> {
			connection.receiveBlocking();
			fail("Should have been blocked");
		}).start();
		Thread.sleep(100);
	} 

}
