package il.ac.technion.cs.sd.app.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IntegrationUnreadMailTest extends IntegrationTestBaseClass{
	
	private final String testClientName = "unreadTester";
	private ClientMailApplication testClient = null;
	
	@Override
	public void setup() throws InterruptedException {
		super.setup();
		testClient = buildClient(testClientName);
	}
	
	@Test
	public void unreadContainsAllNewMail() {
		ClientMailApplication c1 = buildClient("c1");
		ClientMailApplication c2 = buildClient("c2");
		
		String[] contents = {"one", "two", "three", "four"};
		List<Mail> sentMail = new ArrayList<Mail>();
		
		for (String str : contents) {
			sentMail.add(new Mail("c1", "c2", str));
			c1.sendMail("c2", str);
		}
		
		assertTrue("Unread mail should include all mails sent.", c2.getNewMail().containsAll(sentMail));
	}
	
	@Test
	public void unreadContainsEmptyListIfNonReceived() {
		assertTrue("client's unread mail list should be empty", testClient.getNewMail().isEmpty());
	}
	
	@Test
	public void unreadQueryMarksAsRead() {
		ClientMailApplication otherClient = buildClient("other");
		otherClient.sendMail(testClientName, "once");
		otherClient.sendMail(testClientName, "twice");
		
		testClient.getNewMail();
		assertEquals("upon second call - no mail should be unread", 0, testClient.getNewMail().size());
	}
	
	@Test
	public void unreadQueryIsOrderedFromNewToOld() {
		ClientMailApplication otherClient = buildClient("other");
		
		otherClient.sendMail(testClientName, "oldest");
		otherClient.sendMail(testClientName, "middle");
		otherClient.sendMail(testClientName, "newest");
		
		List<Mail> newMail = testClient.getNewMail();
		
		assertEquals("newest should be first", "newest", newMail.get(0).content);
		assertEquals("middle should be second", "middle", newMail.get(1).content);
		assertEquals("oldest should be last", "oldest", newMail.get(2).content);
	}
	
	@Test
	public void consecutiveCallsReturnDifferentResults() {
		ClientMailApplication otherClient = buildClient("other");
		otherClient.sendMail(testClientName, "once");
		assertEquals("once", testClient.getNewMail().get(0).content);
		otherClient.sendMail(testClientName, "twice");
		assertEquals("twice", testClient.getNewMail().get(0).content);
		assertTrue(testClient.getNewMail().isEmpty());
	}

}
