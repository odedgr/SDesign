package il.ac.technion.cs.sd.app.mail;

import static org.junit.Assert.*;
import il.ac.technion.cs.sd.lib.ClientConnection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ClientMailApplicationTest {
	
	ClientMailApplication client;
	ClientConnection<MailRequest> connection;
	
	static private final String clientAddress = "clientAddress";
	static private final String serverAddress = "serverAddress";

	@Before
	public void setUp() throws Exception {
		connection = Mockito.mock(ClientConnection.class);
		Mockito.when(connection.getAddress()).thenReturn(clientAddress);
		Mockito.when(connection.getServerAddress()).thenReturn(serverAddress);
		
		client = ClientMailApplication.createWithMockConnection(connection);
	}

	@After
	public void tearDown() throws Exception {
		client.stop();
	}

	@Test
	public void testSendMail() {
		String otherClient = "a friend";
		String message = "wazzap?!";
		client.sendMail(otherClient, message);
		Mockito.verify(connection).send(MailRequest.sendMail(new Mail(clientAddress, otherClient, message)));
		
	}

	@Test
	public void testGetCorrespondences() {
		fail("Not yet implemented");
	}
//
//	@Test
//	public void testGetSentMails() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetIncomingMail() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetAllMail() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetNewMail() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetContacts() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testStop() {
//		fail("Not yet implemented");
//	}

}
