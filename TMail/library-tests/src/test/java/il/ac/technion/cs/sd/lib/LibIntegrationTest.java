package il.ac.technion.cs.sd.lib;
import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;


public class LibIntegrationTest {
	
	static private final String SERVER_ADDRESS = "serv";
	
	ClientConnection<String> client1, client2;
	ServerConnection<String> server;
	
	@Rule
    public Timeout globalTimeout = Timeout.seconds(20);

	@Before
	public void setUp() throws Exception {
		server = ServerConnection.<String>create("serv");
		client1 = ClientConnection.<String>create("Haim", "serv");
		client2 = ClientConnection.<String>create("Moshe", "serv");
	}

	@After
	public void tearDown() throws Exception {
		server.kill();
		client1.kill();
		client2.kill();
	}

	@Test
	public void ServerToClientReceivedInOrder() {
		server.send(client1.address(), "one");
		server.send(client2.address(), "two");
		server.send(client1.address(), "three");
		
		assertEquals("two", client2.receiveBlocking());
		assertEquals("one", client1.receiveBlocking());
		assertEquals("three", client1.receiveBlocking());
	}
	
	@Test
	public void ClientToServerReceivedInOrder() {
		client1.send("one");
		client1.send("two");
		client2.send("three");
		client1.send("four");
		client2.send("five");
		
		{
			MessageWithSender<String> mws = server.receiveBlocking();
			assertEquals("one", mws.content);
			assertEquals(client1.address(), mws.sender);
		}
		{
			MessageWithSender<String> mws = server.receiveBlocking();
			assertEquals("two", mws.content);
			assertEquals(client1.address(), mws.sender);
		}
		{
			MessageWithSender<String> mws = server.receiveBlocking();
			assertEquals("three", mws.content);
			assertEquals(client2.address(), mws.sender);
		}
		{
			MessageWithSender<String> mws = server.receiveBlocking();
			assertEquals("four", mws.content);
			assertEquals(client1.address(), mws.sender);
		}
		{
			MessageWithSender<String> mws = server.receiveBlocking();
			assertEquals("five", mws.content);
			assertEquals(client2.address(), mws.sender);
		}
	}
	
	@Test
	public void ServerReceivesEmptyOptionalWhenNoPendingMessage() {
		client1.send("one");
		client2.send("two");
		
		{
			Optional<MessageWithSender<String>> m = server.receive();
			assertTrue(m.isPresent());
			assertEquals("one", m.get().content);
		}
		{
			Optional<MessageWithSender<String>> m = server.receive();
			assertTrue(m.isPresent());
			assertEquals("two", m.get().content);
		}
		{
			Optional<MessageWithSender<String>> m = server.receive();
			assertFalse(m.isPresent());
		}
	}
	
	@Test
	public void ClientReceivesEmptyOptionalWhenNoPendingMessage() {
		server.send(client1.address(), "one");
		{
			// Client 2 got no message, so he receives an empty optional.
			Optional<String> s = client2.receive();
			assertFalse(s.isPresent());
		}
		{
			// Client 1 gets only one message.
			Optional<String> s = client1.receive();
			assertTrue(s.isPresent());
			assertEquals("one", s.get());
		}
		{
			Optional<String> s = client1.receive();
			assertFalse(s.isPresent());
		}
	}
	
	@Test
	public void TestCustomCodec() {
		ServerConnection<StringMsg> str_server = ServerConnection.<StringMsg>create("local_server_address", new StringMsgCodec());
		ClientConnection<StringMsg> str_client = ClientConnection.<StringMsg>create("local_client_address", "local_server_address", new StringMsgCodec());
		
		str_server.send(str_client.address(), new StringMsg("Hi!"));
		assertEquals("Hi!", str_client.receiveBlocking().str);
		
		str_client.send(new StringMsg("What's up?"));
		MessageWithSender<StringMsg> ms = str_server.receiveBlocking();
		assertEquals(str_client.address(), ms.sender);
		assertEquals("What's up?", ms.content.str);
	}

}
