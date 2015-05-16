package il.ac.technion.cs.sd.app.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class IntegerationReceivedMailTest extends IntegrationTestBaseClass{
	private final String testClientName = "receivedMailTester";
	private ClientMailApplication testClient = null;
	
	private final String otherClientName = "other";
	private ClientMailApplication otherClient = null;
	
	@Override
	public void setup() throws InterruptedException {
		super.setup();
		testClient = buildClient(testClientName);
		otherClient = buildClient(otherClientName);
		
	}

	@Test
	public void receivedMailQueryMarksAsRead() {
		otherClient.sendMail(testClientName, "1");
		otherClient.sendMail(testClientName, "2");
		testClient.getIncomingMail(1);
		assertEquals("second mail should have been marked as read", 1, testClient.getNewMail().size());
	}
	
	@Test
	public void receivedMailQueryIsOrderedFromNewToOld() {
		
		otherClient.sendMail(testClientName, "oldest");
		otherClient.sendMail(testClientName, "middle");
		otherClient.sendMail(testClientName, "newest");
		
		List<Mail> incomingMail = testClient.getIncomingMail(3);
		
		assertEquals("newest should be first", "newest", incomingMail.get(0).content);
		assertEquals("middle should be second", "middle", incomingMail.get(1).content);
		assertEquals("oldest should be last", "oldest", incomingMail.get(2).content);
	}
	
	@Test
	public void receivedMailQueryContainsOnlyReceived() {
		otherClient.sendMail(testClientName, "ping");
		testClient.sendMail(otherClientName, "pong");
		
		List<Mail> incomingMail = testClient.getIncomingMail(2);
		assertEquals("testClient only received a single mail", 1, incomingMail.size());
		assertTrue("testClient should have received otherClient's mail", incomingMail.contains(new Mail(otherClientName, testClientName, "ping")));
		assertTrue("testClient should NOT have received his own mail", !incomingMail.contains(new Mail(testClientName, otherClientName, "pong")));
	}
	
	@Test
	public void receivedMailQueryReturnsNoMoreThanAskedItems() {
		otherClient.sendMail(testClientName, "bla bla");
		otherClient.sendMail(testClientName, "yada yada");
		
		assertEquals("only asked for 1 item at most", 1, testClient.getIncomingMail(1).size());
	}
	
	@Test
	public void receivedMailQueryReturnsAmountExistingLowerThanAsked() {
		otherClient.sendMail(testClientName, "bla bla");
		otherClient.sendMail(testClientName, "yada yada");
		
		assertEquals("only 2 items exist", 2, testClient.getIncomingMail(3).size());
	}
	
	@Test
	public void receivedMailQueryReturnsEmptyWhenNonReceived() {
		assertTrue("should get an empty list", testClient.getIncomingMail(1).isEmpty());
	}
	
	@Test
	public void consecutiveReceivedMailQueryResultsAreConsistent() {
		List<Mail> sentMails = Arrays.asList(
				new Mail(otherClientName, testClientName, "bla bla"),
				new Mail(otherClientName, testClientName, "yada yada"),
				new Mail(otherClientName, testClientName, "third time's a charm"));
		
		for (Mail mail : sentMails) {
			otherClient.sendMail(mail.to, mail.content);
		}
		
		Collections.reverse(sentMails);
		
		assertEquals(sentMails, testClient.getIncomingMail(3));
		assertEquals("second call should return the exact same result", sentMails, testClient.getIncomingMail(3));
		
	}
	
}
