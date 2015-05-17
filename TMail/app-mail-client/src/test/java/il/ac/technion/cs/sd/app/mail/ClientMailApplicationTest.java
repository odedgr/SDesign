package il.ac.technion.cs.sd.app.mail;

import static org.junit.Assert.*;
import il.ac.technion.cs.sd.lib.ClientConnection;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ClientMailApplicationTest {
	
	ClientMailApplication client;
	ClientConnection<MailRequest> connection;
	
	static private final String clientAddress = "clientAddress";
	static private final String serverAddress = "serverAddress";

	@SuppressWarnings("unchecked")
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
		List<Mail> expected_result = Arrays.asList(new Mail("a", "b", "c"), new Mail("e", "f", "g"));
		MailRequest expected_response = MailRequest.getCorrespondences("other", 2); 
		expected_response.attachResponse(MailResponse.withMailResults(expected_result));
		
		
		Mockito.when(connection.receiveBlocking()).thenReturn(expected_response);
		assertEquals(expected_result, client.getCorrespondences("other", 2));
		
		Mockito.verify(connection).send(MailRequest.getCorrespondences("other", 2));
	}

	@Test
	public void testGetSentMails() {
		List<Mail> expected_result = Arrays.asList(new Mail("a", "b", "c"), new Mail("e", "f", "g"));
		MailRequest expected_response = MailRequest.getMailSent(7); 
		expected_response.attachResponse(MailResponse.withMailResults(expected_result));
		
		
		Mockito.when(connection.receiveBlocking()).thenReturn(expected_response);
		assertEquals(expected_result, client.getSentMails(7));
		
		Mockito.verify(connection).send(MailRequest.getMailSent(7));
	}

	@Test
	public void testGetIncomingMail() {
		List<Mail> expected_result = Arrays.asList(new Mail("a", "b", "c"), new Mail("e", "f", "g"));
		MailRequest expected_response = MailRequest.getIncoming(7); 
		expected_response.attachResponse(MailResponse.withMailResults(expected_result));
		
		
		Mockito.when(connection.receiveBlocking()).thenReturn(expected_response);
		assertEquals(expected_result, client.getIncomingMail(7));
		
		Mockito.verify(connection).send(MailRequest.getIncoming(7));
	}

	@Test
	public void testGetAllMail() {
		List<Mail> expected_result = Arrays.asList(new Mail("a", "b", "c"), new Mail("e", "f", "g"));
		MailRequest expected_response = MailRequest.getAllMail(7); 
		expected_response.attachResponse(MailResponse.withMailResults(expected_result));
		
		
		Mockito.when(connection.receiveBlocking()).thenReturn(expected_response);
		assertEquals(expected_result, client.getAllMail(7));
		
		Mockito.verify(connection).send(MailRequest.getAllMail(7));
	}

	@Test
	public void testGetNewMail() {
		List<Mail> expected_result = Arrays.asList(new Mail("a", "b", "c"), new Mail("e", "f", "g"));
		MailRequest expected_response = MailRequest.getUnread(); 
		expected_response.attachResponse(MailResponse.withMailResults(expected_result));
		
		
		Mockito.when(connection.receiveBlocking()).thenReturn(expected_response);
		assertEquals(expected_result, client.getNewMail());
		
		Mockito.verify(connection).send(MailRequest.getUnread());
	}

	@Test
	public void testGetContacts() {
		List<String> expected_result = Arrays.asList("Aba", "Ima", "Bamba");
		MailRequest expected_response = MailRequest.getContacts(); 
		expected_response.attachResponse(MailResponse.withContactsResults(expected_result));
		
		
		Mockito.when(connection.receiveBlocking()).thenReturn(expected_response);
		assertEquals(expected_result, client.getContacts(0));
		
		Mockito.verify(connection).send(MailRequest.getContacts());
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void failWithEmptyRecipient() {
		client.sendMail("", "a");
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void failWithNullRecipient() {
		client.sendMail(null, "a");
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void failWithEmptyMessage() {
		client.sendMail("b", "");
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void failWithNullMessage() {
		client.sendMail("b", null);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void failWithEmptyOtherClient() {
		client.getCorrespondences("", 5);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void failWithNullOtherClient() {
		client.getCorrespondences(null, 5);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void failWithNegativeHowmanyInCorrespondences() {
		client.getCorrespondences(null, -5);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void failWithNegativeHowmanyInAllMail() {
		client.getAllMail(-1);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void failWithNegativeHowmanyInIncomingMail() {
		client.getIncomingMail(-6);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void failWithNegativeHowmanyInSentMail() {
		client.getSentMails(-10);
	}
}
