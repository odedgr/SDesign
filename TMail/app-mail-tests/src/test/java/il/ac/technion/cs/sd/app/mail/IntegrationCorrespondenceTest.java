package il.ac.technion.cs.sd.app.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class IntegrationCorrespondenceTest extends IntegrationTestBaseClass {

	private final String testClientName =	"correspondenceTester";
	private ClientMailApplication       	testClient = null;
	
	private final String otherClientName = "other";
	private ClientMailApplication       	otherClient = null;
	
	@Override
	public void setup() throws InterruptedException {
		super.setup();
		testClient = buildClient(testClientName);
		otherClient = buildClient(otherClientName);
		
	}
	
	/**
	 * Sends 4 useless Mail between the testClient and "other" client, just to create a quereable correspondence.
	 * 
	 * @return List of mails sent in the correspondence, in order of sending (oldest to newest)
	 */
	private List<Mail> sendFourPingPongMails() {
		List<Mail> mails = Arrays.asList(
				new Mail(testClientName, otherClientName, "ping"),
				new Mail(otherClientName, testClientName, "pong"),
				new Mail(testClientName, otherClientName, "bang"),
				new Mail(otherClientName, testClientName, "bong"));
		
		testClient.sendMail(mails.get(0).to, mails.get(0).content);
		otherClient.sendMail(mails.get(1).to, mails.get(1).content);
		testClient.sendMail(mails.get(2).to, mails.get(2).content);
		otherClient.sendMail(mails.get(3).to, mails.get(3).content);
		
		return mails;
	}
	
	
	@Test
	public void correspondenceContainsAllMailWithOtherClient() {
		List<Mail> mails = sendFourPingPongMails();
		assertTrue(testClient.getCorrespondences(otherClientName, 4).containsAll(mails));
	}
	
	@Test
	public void correspondenceIsEmptyForNonCommunicatingClient() {
		assertTrue("hadn't communicated with no-one", testClient.getCorrespondences("anybody", 1).isEmpty());
	}
	
	@Test
	public void correspondenceQueryMarksAsRead() {
		otherClient.sendMail(testClientName, "what?");
		testClient.sendMail(otherClientName, "nothing...");
		
		testClient.getCorrespondences(otherClientName, 1);
		
		assertEquals("only the first mail should remain unread", 1, testClient.getNewMail().size());
	}
	
	@Test
	public void correspondenceQueryIsOrderedFromNewToOld() {
		List<Mail> mails = sendFourPingPongMails();
		
		Collections.reverse(mails);
		
		assertEquals(mails, testClient.getCorrespondences(otherClientName, 4));
	}
	
	@Test
	public void correspondenceQueryReturnsNoMoreThanAskedItems() {
		sendFourPingPongMails();

		assertEquals("only asked for 2 out of the 4 mails", 2, testClient.getCorrespondences(otherClientName, 2).size());
	}
	
	@Test
	public void correspondenceQueryReturnsAmountExistingLowerThanAsked() {
		List<Mail> mails = Arrays.asList(
				new Mail(testClientName, otherClientName, "oldest"),
				new Mail(otherClientName, testClientName, "older"),
				new Mail(testClientName, otherClientName, "newer"),
				new Mail(otherClientName, testClientName, "newest"));
		
		testClient.sendMail(otherClientName, mails.get(0).content);
		otherClient.sendMail(testClientName, mails.get(1).content);
		testClient.sendMail(otherClientName, mails.get(2).content);
		otherClient.sendMail(testClientName, mails.get(3).content);

		assertEquals("should only get a list the size requested", mails.size(), testClient.getCorrespondences(otherClientName, mails.size() + 1).size());
	}
	
	@Test
	public void correspondenceQueryContainsOnlyMailsWithRequestedClient() {
		ClientMailApplication alice = buildClient("alice");
		ClientMailApplication bob = buildClient("bob");
		ClientMailApplication eve = buildClient("eve");
		
		alice.sendMail("bob", "my key");
		bob.sendMail("alice", "my key too");
		eve.sendMail("alice", "tell me!");
		alice.sendMail("eve", "no");
		eve.sendMail("bob", "I want to know!");
		bob.sendMail("eve", "i think not");
		eve.sendMail("bob", "please...");
		eve.sendMail("bob", "spam");
		eve.sendMail("alice", "spam");
		alice.sendMail("bob", "BHTqo34tg!#va");   // something secret
		bob.sendMail("alice", "G@49uas3;ao5ih2"); // something top-secret
		eve.sendMail("alice", "I heard your BHTqo34tg!#va HAHA!");
		eve.sendMail("bob", "I heard your G@49uas3;ao5ih2 HAHA!");
		bob.sendMail("alice", "kill her");
		alice.sendMail("bob", "why? she doesn't know anything.");
		bob.sendMail("alice", "ok...");
		
		assertEquals(7, alice.getCorrespondences("bob", 7).size());
		assertEquals(7, bob.getCorrespondences("alice", 7).size());
		assertEquals(5, bob.getCorrespondences("eve", 5).size());
		assertEquals(4, alice.getCorrespondences("eve", 4).size());
	}

}
