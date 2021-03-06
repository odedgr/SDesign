package il.ac.technion.cs.sd.app.mail;

import java.util.ArrayList;

import il.ac.technion.cs.sd.lib.MessageWithSender;
import il.ac.technion.cs.sd.lib.ServerConnection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ServerMailApplicationTest {
	
	ServerMailApplication server;
	ServerConnection<MailRequest> connection;
	private Thread serverThread;
	static private final String serverAddress = "serverAddress";
	static private final String clientAddress = "clientAddress";
	
	
	private void startServer() throws InterruptedException {
		serverThread = new Thread(() -> server.start());
		serverThread.start();
		Thread.yield(); // STRONG hints to the OS to start the server thread, though nothing can be *truly* deterministic
		Thread.sleep(500);
	}	
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		connection = Mockito.mock(ServerConnection.class);
		Mockito.when(connection.getAddress()).thenReturn(serverAddress);
		server = new ServerMailApplication(serverAddress);
		server.injectMockConnection(connection);
	}

	@SuppressWarnings("deprecation")
	@After
	public void tearDown() throws Exception {
		server.stop();
		server.clean();
		serverThread.stop();
	}
	
	private MessageWithSender<MailRequest> newRequest(MailRequest request) {
		return new MessageWithSender<MailRequest>(request, clientAddress);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void verifyMailResponseReturnedAllMail() throws InterruptedException {
		MailRequest request = MailRequest.getAllMail(7);
		// Throwing an IllegalMonitorStateException simulates server shut down.
		Mockito.when(connection.receiveBlocking()).thenReturn(newRequest(request)).thenThrow(IllegalMonitorStateException.class);
		startServer();
		
		MailRequest reponse = MailRequest.getAllMail(7);
		reponse.attachResponse(MailResponse.withMailResults(new ArrayList<Mail>()));
		Mockito.verify(connection).send(clientAddress, reponse);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void verifyMailResponseReturnedUnread() throws InterruptedException {
		MailRequest request = MailRequest.getUnread();
		// Throwing an IllegalMonitorStateException simulates server shut down.
		Mockito.when(connection.receiveBlocking()).thenReturn(newRequest(request)).thenThrow(IllegalMonitorStateException.class);
		startServer();
		
		MailRequest reponse = MailRequest.getUnread();
		reponse.attachResponse(MailResponse.withMailResults(new ArrayList<Mail>()));
		Mockito.verify(connection).send(clientAddress, reponse);
		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void verifyContactsResponseReturned() throws InterruptedException {
		MailRequest request = MailRequest.getContacts();
		// Throwing an IllegalMonitorStateException simulates server shut down.
		Mockito.when(connection.receiveBlocking()).thenReturn(newRequest(request)).thenThrow(IllegalMonitorStateException.class);
		startServer();
		request.attachResponse(MailResponse.withContactsResults(new ArrayList<String>()));
		
		MailRequest reponse = MailRequest.getContacts();
		reponse.attachResponse(MailResponse.withContactsResults(new ArrayList<String>()));
		Mockito.verify(connection).send(clientAddress, reponse);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void verifyMailResponseReturnedIncoming() throws InterruptedException {
		MailRequest request = MailRequest.getIncoming(12);
		// Throwing an IllegalMonitorStateException simulates server shut down.
		Mockito.when(connection.receiveBlocking()).thenReturn(newRequest(request)).thenThrow(IllegalMonitorStateException.class);
		startServer();
		request.attachResponse(MailResponse.withContactsResults(new ArrayList<String>()));
		
		MailRequest reponse = MailRequest.getIncoming(12);
		reponse.attachResponse(MailResponse.withContactsResults(new ArrayList<String>()));
		Mockito.verify(connection).send(clientAddress, reponse);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void verifyMailResponseReturnedMailSent() throws InterruptedException {
		MailRequest request = MailRequest.getMailSent(25);
		// Throwing an IllegalMonitorStateException simulates server shut down.
		Mockito.when(connection.receiveBlocking()).thenReturn(newRequest(request)).thenThrow(IllegalMonitorStateException.class);
		startServer();
		request.attachResponse(MailResponse.withContactsResults(new ArrayList<String>()));
		
		MailRequest reponse = MailRequest.getMailSent(25);
		reponse.attachResponse(MailResponse.withContactsResults(new ArrayList<String>()));
		Mockito.verify(connection).send(clientAddress, reponse);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void verifyMailResponseReturnedCorrespondences() throws InterruptedException {
		MailRequest request = MailRequest.getCorrespondences("Judy", 30);
		// Throwing an IllegalMonitorStateException simulates server shut down.
		Mockito.when(connection.receiveBlocking()).thenReturn(newRequest(request)).thenThrow(IllegalMonitorStateException.class);
		startServer();
		request.attachResponse(MailResponse.withContactsResults(new ArrayList<String>()));
		
		MailRequest reponse = MailRequest.getCorrespondences("Judy", 30);
		reponse.attachResponse(MailResponse.withContactsResults(new ArrayList<String>()));
		Mockito.verify(connection).send(clientAddress, reponse);
	}
}
