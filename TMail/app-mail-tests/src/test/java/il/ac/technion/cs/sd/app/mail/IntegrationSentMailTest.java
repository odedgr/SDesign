package il.ac.technion.cs.sd.app.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class IntegrationSentMailTest extends IntegrationTestBaseClass {
	private final String testClientName = "sentTester";
	private ClientMailApplication testClient = null;
	
	@Override
	public void setup() throws InterruptedException {
		super.setup();
		testClient = buildClient(testClientName);
	}

	@Test
	public void sentMailQueryIsOrderedFromNewToOld() {
		List<Mail> mails = Arrays.asList(
				new Mail(testClientName, "other", "oldest"),
				new Mail(testClientName, "other", "slightly used"),
				new Mail(testClientName, "other", "brand new"));
		
		for (Mail mail : mails) {
			testClient.sendMail(mail.to, mail.content);
		}
		
		Collections.reverse(mails);
		
		assertEquals(mails, testClient.getSentMail(3));
	}
	
	@Test
	public void sendMailQueryContainsOnlySent() {
		testClient.sendMail("other", "testing, testing...");
		buildClient("other").sendMail(testClientName, "heard you just fine");
		testClient.sendMail("other", "what's up?");
		
		List<Mail> sent = testClient.getSentMail(2);
		
		assertEquals("testClient sent out exactly 2 mails", 2, sent.size());
		assertTrue(sent.contains(new Mail(testClientName, "other", "testing, testing...")));
		assertTrue(sent.contains(new Mail(testClientName, "other", "what's up?")));
	}
	
	@Test
	public void sentMailQueryReturnsNoMoreThanAskedItems() {
		testClient.sendMail("nobody", "one");
		testClient.sendMail("nobody", "two");
		
		assertEquals("only asked for 1 mail", 1, testClient.getSentMail(1).size());
	}
	
	@Test
	public void sentMailQueryReturnsAmountExistingLowerThanAsked() {
		testClient.sendMail("nobody", "one");
		testClient.sendMail("nobody", "two");
		
		assertEquals("only sent out 2 mails", 2, testClient.getSentMail(5).size());
	}
	
	@Test
	public void sentMailQueryReturnsEmptyWhenNonSent() {
		assertTrue("should get an empty list", testClient.getSentMail(1).isEmpty());
		buildClient("other").sendMail(testClientName, "hello");
		assertTrue("should get an empty list", testClient.getSentMail(1).isEmpty());
	}
	
}
