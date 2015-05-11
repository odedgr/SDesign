package il.ac.technion.cs.sd.app.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IntegrationUnreadMailTest {

	private ServerMailApplication		server	   = new ServerMailApplication("server");
	private ClientMailApplication       testClient = null;
	private List<ClientMailApplication>	clients	   = new ArrayList<>();
	
	private ClientMailApplication buildClient(String login) {
		ClientMailApplication $ = new ClientMailApplication(server.getAddress(), login);
		clients.add($);
		return $;
	}

	@Before
	public void setUp() throws Exception {
		server.start();
		testClient = buildClient("tester");
	}

	@After
	public void tearDown() throws Exception {
		server.stop();
		server.clean();
		clients.forEach(c -> c.stop());
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
		otherClient.sendMail("tester", "once");
		otherClient.sendMail("tester", "twice");
		
		testClient.getNewMail();
		assertEquals("upon second call - no mail should be unread", 0, testClient.getNewMail().size());
	
	}
	
	@Test
	public void unreadQueryIsOrderedFromNewToOld() {
		ClientMailApplication otherClient = buildClient("other");
		
		otherClient.sendMail("tester", "oldest");
		otherClient.sendMail("tester", "middle");
		otherClient.sendMail("tester", "newest");
		
		List<Mail> newMail = testClient.getNewMail();
		
		assertEquals("newest should be first", "newest", newMail.get(0).content);
		assertEquals("middle should be second", "middle", newMail.get(1).content);
		assertEquals("oldest should be last", "oldest", newMail.get(2).content);
	}
	
	@Test
	public void consecutiveCallsReturnDifferentResults() {
		ClientMailApplication otherClient = buildClient("other");
		otherClient.sendMail("tester", "once");
		assertEquals("once", testClient.getNewMail().get(0).content);
		otherClient.sendMail("tester", "twice");
		assertEquals("twice", testClient.getNewMail().get(0).content);
		assertTrue(testClient.getNewMail().isEmpty());
	}

}
