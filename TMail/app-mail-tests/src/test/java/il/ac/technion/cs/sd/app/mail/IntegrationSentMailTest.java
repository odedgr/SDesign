package il.ac.technion.cs.sd.app.mail;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IntegrationSentMailTest {

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
	public void sentMailQueryIsOrderedFromNewToOld() {
		List<Mail> mails = Arrays.asList(
				new Mail("tester", "other", "oldest"),
				new Mail("tester", "other", "slightly used"),
				new Mail("tester", "other", "brand new"));
		
		for (Mail mail : mails) {
			testClient.sendMail(mail.to, mail.content);
		}
		
		Collections.reverse(mails);
		
		assertEquals(mails, testClient.getSentMails(3));
	}
	
	@Test
	public void sendMailQueryContainsOnlySent() {
		testClient.sendMail("other", "testing, testing...");
		buildClient("other").sendMail("tester", "heard you just fine");
		testClient.sendMail("other", "what's up?");
		
		List<Mail> sent = testClient.getSentMails(2);
		
		assertEquals("testClient sent out exactly 2 mails", 2, sent.size());
		assertTrue(sent.contains(new Mail("tester", "other", "testing, testing...")));
		assertTrue(sent.contains(new Mail("tester", "other", "what's up?")));
	}
	
	@Test
	public void sentMailQueryReturnsNoMoreThanAskedItems() {
		testClient.sendMail("nobody", "one");
		testClient.sendMail("nobody", "two");
		
		assertEquals("only asked for 1 mail", 1, testClient.getSentMails(1).size());
	}
	
	@Test
	public void sentMailQueryReturnsAmountExistingLowerThanAsked() {
		testClient.sendMail("nobody", "one");
		testClient.sendMail("nobody", "two");
		
		assertEquals("only sent out 2 mails", 2, testClient.getSentMails(5).size());
	}
	
	@Test
	public void sentMailQueryReturnsEmptyWhenNonSent() {
		assertTrue("should get an empty list", testClient.getSentMails(1).isEmpty());
		buildClient("other").sendMail("tester", "hello");
		assertTrue("should get an empty list", testClient.getSentMails(1).isEmpty());
	}
	
}
