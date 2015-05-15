package il.ac.technion.cs.sd.app.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IntegerationReceivedMailTest {

	private ServerMailApplication		server	= new ServerMailApplication("server");
	private ClientMailApplication       testClient = null;
	private List<ClientMailApplication>	clients	= new ArrayList<>();
	private Thread serverThread;
	
	private ClientMailApplication buildClient(String login) {
		ClientMailApplication $ = new ClientMailApplication(server.getAddress(), login);
		clients.add($);
		return $;
	}
	
	@Before
	public void setUp() throws Exception {
		serverThread = new Thread(() -> server.start());
		serverThread.start();
		Thread.yield(); 
		Thread.sleep(10L);
		testClient = buildClient("tester");
	}

	@SuppressWarnings("deprecation")
	@After
	public void tearDown() throws Exception {
		server.clean();
		server.stop();
		clients.forEach(c -> c.stop());
		serverThread.stop();
	}

	@Test
	public void receivedMailQueryMarksAsRead() {
		ClientMailApplication otherClient = buildClient("other");
		otherClient.sendMail("tester", "1");
		otherClient.sendMail("tester", "2");
		testClient.getIncomingMail(1);
		assertEquals("second mail should have been marked as read", 1, testClient.getNewMail().size());
	}
	
	@Test
	public void receivedMailQueryIsOrderedFromNewToOld() {
		ClientMailApplication otherClient = buildClient("other");
		
		otherClient.sendMail("tester", "oldest");
		otherClient.sendMail("tester", "middle");
		otherClient.sendMail("tester", "newest");
		
		List<Mail> incomingMail = testClient.getIncomingMail(3);
		
		assertEquals("newest should be first", "newest", incomingMail.get(0).content);
		assertEquals("middle should be second", "middle", incomingMail.get(1).content);
		assertEquals("oldest should be last", "oldest", incomingMail.get(2).content);
	}
	
	@Test
	public void receivedMailQueryContainsOnlyReceived() {
		ClientMailApplication otherClient = buildClient("other");
		
		otherClient.sendMail("tester", "ping");
		testClient.sendMail("other", "pong");
		
		List<Mail> incomingMail = testClient.getIncomingMail(2);
		assertEquals("testClient only received a single mail", 1, incomingMail.size());
		assertTrue("testClient should have received otherClient's mail", incomingMail.contains(new Mail("other", "tester", "ping")));
		assertTrue("testClient should NOT have received his own mail", !incomingMail.contains(new Mail("tester", "other", "pong")));
	}
	
	@Test
	public void receivedMailQueryReturnsNoMoreThanAskedItems() {
		ClientMailApplication otherClient = buildClient("other");
		
		otherClient.sendMail("tester", "bla bla");
		otherClient.sendMail("tester", "yada yada");
		
		assertEquals("only asked for 1 item at most", 1, testClient.getIncomingMail(1).size());
	}
	
	@Test
	public void receivedMailQueryReturnsAmountExistingLowerThanAsked() {
		ClientMailApplication otherClient = buildClient("other");
		
		otherClient.sendMail("tester", "bla bla");
		otherClient.sendMail("tester", "yada yada");
		
		assertEquals("only 2 items exist", 2, testClient.getIncomingMail(3).size());
	}
	
	@Test
	public void receivedMailQueryReturnsEmptyWhenNonReceived() {
		assertTrue("should get an empty list", testClient.getIncomingMail(1).isEmpty());
	}
	
	@Test
	public void consecutiveReceivedMailQueryResultsAreConsistent() {
		ClientMailApplication otherClient = buildClient("other");
		
		List<Mail> sentMails = Arrays.asList(
				new Mail("other", "tester", "bla bla"),
				new Mail("other", "tester", "yada yada"),
				new Mail("other", "tester", "third time's a charm"));
		
		for (Mail mail : sentMails) {
			otherClient.sendMail(mail.to, mail.content);
		}
		
		Collections.reverse(sentMails);
		
		assertEquals(sentMails, testClient.getIncomingMail(3));
		assertEquals("second call should return the exact same result", sentMails, testClient.getIncomingMail(3));
		
	}
	
}
