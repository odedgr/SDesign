package il.ac.technion.cs.sd.app.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class IntegrationAllMailTest extends IntegrationTestBaseClass{
	private final String testClientName = "allMailTester";
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
	public void allMailQueryMarksAsRead() {
		otherClient.sendMail(testClientName, "1");
		otherClient.sendMail(testClientName, "2");
		testClient.sendMail(otherClientName, "back at'ya");
		testClient.getAllMail(2);
		
		assertEquals("first mail should have been marked as read", 1, testClient.getNewMail().size());
	}
	
	@Test
	public void allMailQueryIsOrderedFromNewToOld() {
		List<Mail> mails = Arrays.asList(
				new Mail(otherClientName, testClientName, "oldest"),
				new Mail(otherClientName, testClientName, "slightly used"),
				new Mail(otherClientName, testClientName, "brand new"));
		
		for (Mail mail : mails) {
			otherClient.sendMail(mail.to, mail.content);
		}
		
		Collections.reverse(mails);
		
		assertEquals(mails, testClient.getAllMail(3));
	}
	
	@Test
	public void allMailQueryContainsBothSendAndReceive() {
		List<Mail> mails = Arrays.asList(
				new Mail(otherClientName, testClientName, "bla bla"),
				new Mail(testClientName, otherClientName, "yada yada"));
		
		otherClient.sendMail(mails.get(0).to, mails.get(0).content);
		testClient.sendMail(mails.get(1).to, mails.get(1).content);
		
		Collections.reverse(mails);
		
		assertEquals(mails, testClient.getAllMail(2));
	}
	
	@Test
	public void allMailQueryReturnsNoMoreThanAskedItems() {
		final int smallPositiveAmount = 7;
		for (int i = 0; i < smallPositiveAmount; ++i) {
			otherClient.sendMail(testClientName, "Boo!");
		}
		
		int smallerAmount = smallPositiveAmount - 1;
		assertEquals("should get no more results than amount asked for", smallerAmount, testClient.getAllMail(smallerAmount).size());
	}
	
	@Test
	public void allMailQueryReturnsAmountExistingLowerThanAsked() {
		otherClient.sendMail(testClientName, "bla bla");
		otherClient.sendMail(testClientName, "yada yada");
		
		assertEquals("only 2 items exist", 2, testClient.getAllMail(3).size());
	}
	
	@Test
	public void allMailQueryReturnsEmptyWhenNoActivity() {
		assertTrue("should get an empty list", testClient.getAllMail(1).isEmpty());
	}
	
}
