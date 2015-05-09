package il.ac.technion.cs.sd.app.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MailBoxTest {
	
	MailBox mailbox = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		mailbox = new MailBox();
	}

	@After
	public void tearDown() throws Exception {
		mailbox = null;
	}

//////////////////////////////////////////////////
	
	@Test
	public void sentMailReceiverAddedToContacts() {
		mailbox.sentMail(new Mail("from", "to", "bla bla"));
		assertTrue("contacts should include the sender of an incomming mail.", mailbox.getContacts().contains("to"));
	}
	
	@Test
	public void receivedMailSenderAddedToContacts() {
		mailbox.receivedMail(new Mail("from", "to", "bla bla"));
		assertTrue("contacts should include the receiver of an outgoing mail.", mailbox.getContacts().contains("from"));
	}
	
	@Test
	public void correspondenceIncludesSentMail() {
		Mail mail = new Mail("from", "to", "bla bla");
		mailbox.sentMail(mail);
		assertTrue("correspondence should include sent mail", mailbox.getCorrespondeceWith("to", 1).contains(mail));
	}
	
	@Test
	public void correspondenceIncludesReceivedMail() {
		Mail mail = new Mail("from", "to", "bla bla");
		mailbox.receivedMail(mail);
		assertTrue("correspondence should include received mail", mailbox.getCorrespondeceWith("from", 1).contains(mail));
	}
	
	@Test
	public void correspondenceIncludesBothSentAndReceived() {
		Mail in_mail = new Mail("other", "myself", "bla bla");
		mailbox.receivedMail(in_mail);
		
		Mail out_mail = new Mail("myself", "other", "yada yada");
		mailbox.sentMail(out_mail);
		
		assertTrue("correspondence should include received mail", 
				mailbox.getCorrespondeceWith("other", 2).containsAll(Arrays.asList(in_mail, out_mail)));
	}
	
	@Test
	public void sentMailsAreOrderedFromNewestToOldest() {
		// check order is preserved after queries
		final int sentCount = 3;
		sendMailsFromMe(sentCount);
		
		List<Mail> mails = mailbox.getLastNSent(sentCount);
		
		assertMailOrder(mails);
	}
	
	private void sendMailsFromMe(int howMany) {
		for (int i = 0; i < howMany; ++i) {
			mailbox.sentMail(new Mail("me", "other-" + i, String.valueOf(i)));
		}
	}
	
	private void receiveMails(int howMany) {
		for (int i = 0; i < howMany; ++i) {
			mailbox.receivedMail(new Mail("other-" + i, "me", String.valueOf(i)));
		}
	}

	@Test
	public void unreadMailsAreOrderedFromNewestToOldest() {
		final int receiveCount = 5;
		receiveMails(receiveCount);
		
		List<Mail> mails = mailbox.getUnread();
		assertEquals("should have " + receiveCount + " unread mails", receiveCount, mails.size());
		
		assertMailOrder(mails);
	}
	
	@Test
	public void incomingMailsAreOrderedFromNewestToOldest() {
		final int receiveCount = 5;
		receiveMails(receiveCount);
		
		List<Mail> mails = mailbox.getLastNReceived(receiveCount);
		assertEquals("should have received" + receiveCount + " mails", receiveCount, mails.size());
		
		assertMailOrder(mails);
	}
	
	@Test
	public void allMailIncludesBothSentAndReceived() {
		Mail outgoing1 = new Mail("me", "other", "outgoing1");
		Mail outgoing2 = new Mail("me", "other", "outgoing2");
		Mail incoming = new Mail("other", "me", "incoming");
		
		mailbox.sentMail(outgoing1);
		mailbox.sentMail(outgoing2);
		mailbox.receivedMail(incoming);
		
		List<Mail> mails = mailbox.getLastNMails(3);
		assertEquals("all mail from mailbox should include 3 messages (2 out, 1 in)", 3, mails.size());
		
		assertTrue("all mail should include both sent+received messages", mails.containsAll(Arrays.asList(outgoing1, outgoing2, incoming)));
	}
	
	@Test
	public void allMailResultsAreOrderedFromNewestToOldest() throws InterruptedException {
		final int totalMailAmount = 10;
		final int requestedMailAmount = 10;
		
		for (int i = 0; i < totalMailAmount; i += 2) {
			mailbox.sentMail(new Mail("me", "other", String.valueOf(i)));
			Thread.sleep(2);
			mailbox.receivedMail(new Mail("other", "me", String.valueOf(i + 1)));
		}
		
		List<Mail> mails = mailbox.getLastNMails(requestedMailAmount);
		
		assertMailOrder(mails);
	}

	private void assertMailOrder(List<Mail> mails) {
		int currentMsgIndex, prevMsgIndex = Integer.parseInt(mails.get(0).content);
		
		for (Mail mail : mails) {
			// extracted index from content is a number, they should go down
			currentMsgIndex = Integer.parseInt(mail.content);
			assertTrue("mail not ordered in descending order of receiving", currentMsgIndex <= prevMsgIndex);
			prevMsgIndex = currentMsgIndex;
		}
	}
	
	@Test
	public void getAllResultsWhenAskingMoreThanExists() {
		// ask for more entries than amount in existence, make sure to get all that exist
		final int largestSizeTested = 5;

		for (int i = 0; i < largestSizeTested - 1; ++i) {
			assertEquals("sent only " + i + " mails - should return " + i, i, mailbox.getLastNMails(largestSizeTested).size());
			mailbox.sentMail(new Mail("me", "other", ""));
		}
	}
	
	@Test
	public void noMoreThanMaximalAmountOfResults() {
		// ask for less entries than amount in existence
		final int mailCount = 5;
		final int requestAmount = mailCount - 1;

		for (int i = 0; i < mailCount; ++i) {
			mailbox.sentMail(new Mail("me", "other", ""));
		}
		
		assertEquals("should only return at most requested amount, no more", requestAmount, mailbox.getLastNMails(requestAmount).size());
	}
	
	@Test
	public void returnedReceivedEntriesAreNewestAndMarkedAsRead() {
		// fetch entries with different queries and see if they are returned as unread
		Mail older = new Mail("you", "me", "ping");
		Mail newer = new Mail("you", "me", "pong");
		
		mailbox.receivedMail(older);
		mailbox.receivedMail(newer);
		
		List<Mail> onlyLastMail = mailbox.getLastNReceived(1); 
		
		assertTrue("returned mail list should contain the newer mail", onlyLastMail.contains(newer));
		assertTrue("returned mail list should NOT contain the older mail", !onlyLastMail.contains(older));
		
		assertEquals("only one mail should have remained unread", 1, mailbox.getUnread().size());
		assertEquals("no mail should have remained unread", 0, mailbox.getUnread().size());
	}
	
	@Test
	public void returnedEntriesAreNewestAndMarkedAsRead() throws InterruptedException {
		// fetch entries with different queries and see if they are returned as unread
		Mail oldest = new Mail("you", "me", "oldest");
		Mail older  = new Mail("me", "you", "older");
		Mail newer  = new Mail("you", "me", "newer");
		Mail newest = new Mail("me", "you", "newest");
		
		mailbox.receivedMail(oldest);
		mailbox.sentMail(older);
		mailbox.receivedMail(newer);
		mailbox.sentMail(newest);
		
		List<Mail> onlyLastMail = mailbox.getLastNMails(2); 
		
		assertTrue("returned mail list should contain the newest mail", onlyLastMail.contains(newest));
		assertTrue("returned mail list should contain the newer mail", onlyLastMail.contains(newer));
		assertTrue("returned mail list should NOT contain the older mail", !onlyLastMail.contains(older));
		assertTrue("returned mail list should NOT contain the oldest mail", !onlyLastMail.contains(oldest));
		
		assertEquals("only one mail should have remained unread", 1, mailbox.getUnread().size());
	}
	
	@Test
	public void onlyReturnedEntriesAreMarkedAsUnread() {
		// ask for less than the unread amount, and make sure there still are more unread
		final int totalAmount = 10;
		receiveMails(totalAmount);
		
		mailbox.getLastNReceived(totalAmount - 2);
		Assert.assertNotEquals(0, mailbox.getUnread().size());
		
		receiveMails(totalAmount);
		mailbox.getLastNReceived(totalAmount - 2);
		mailbox.getLastNReceived(totalAmount - 1);
		Assert.assertNotEquals(0, mailbox.getUnread().size());
		
	}

}
