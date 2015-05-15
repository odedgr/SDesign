package il.ac.technion.cs.sd.lib;

import static org.junit.Assert.*;

import java.util.Optional;

import il.ac.technion.cs.sd.msg.Messenger;
import il.ac.technion.cs.sd.msg.MessengerException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ServerConnectionTest {
	
	private ServerConnection<String> connection;
	private Messenger messenger;

	@Before
	public void setUp() throws Exception {
		messenger = Mockito.mock(Messenger.class);
		connection = ServerConnection.<String>createWithMockMessenger(messenger); 
	}

	@After
	public void tearDown() throws Exception {
		connection.kill();
		Mockito.verify(messenger).kill();
	}

	@Test
	public void VerifySendUsesMessenger() throws MessengerException {
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
		byte[] encodedMessage = new MessageWithSenderCodec<String>(
				new SerializeCodec<String>())
				.encode(new MessageWithSender<String>(message, clientAddress));

		Mockito.when(messenger.tryListen()).thenReturn(
				Optional.of(encodedMessage));
		
		Optional<MessageWithSender<String>> mws = connection.receive();
		assertTrue(mws.isPresent());
		assertEquals(message, mws.get().content);
		assertEquals(clientAddress, mws.get().sender);
	}
	
	@Test
	public void VerifyReturnsEmptyOptionalWhenNoMessegePending() throws MessengerException {
		Mockito.when(messenger.tryListen()).thenReturn(Optional.empty());
		
		Optional<MessageWithSender<String>> mws = connection.receive();
		assertFalse(mws.isPresent());
	}
	
	@Test
	public void testReceiveBlocking() throws MessengerException {
		String message = "HI!";
		String clientAddress = "claddr";
		byte[] encodedMessage = new MessageWithSenderCodec<String>(
				new SerializeCodec<String>())
				.encode(new MessageWithSender<String>(message, clientAddress));

		Mockito.when(messenger.listen()).thenReturn(encodedMessage);
		
		MessageWithSender<String> mws = connection.receiveBlocking();
		assertEquals(message, mws.content);
		assertEquals(clientAddress, mws.sender);
	}
	
	@Test(timeout = 1000L)
	public void testRecieveBlockedUntilMessageArrives() throws MessengerException, InterruptedException {
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
