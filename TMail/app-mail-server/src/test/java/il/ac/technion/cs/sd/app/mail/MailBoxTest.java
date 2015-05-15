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
	
	MailBox mailbox;
	
	@Before
	public void setUp() throws Exception {
		mailbox = new MailBox();
	}

	private MailEntry newEntry(String from, String to, String content) {
		return new MailEntry(new Mail(from, to, content));
	}
	
	@Test
	public void sentMailReceiverAddedToContacts() {
		MailEntry entry = newEntry("from", "to", "bla bla");
		mailbox.addSentMail(entry);
		assertTrue("contacts should include the sender of an incomming mail.", mailbox.getContacts().contains("to"));
	}
	
	@Test
	public void receivedMailSenderAddedToContacts() {
		MailEntry entry = newEntry("from", "to", "bla bla");
		mailbox.addReceivedMail(entry);
		assertTrue("contacts should include the receiver of an outgoing mail.", mailbox.getContacts().contains("from"));
	}
	
	@Test
	public void correspondenceIncludesSentMail() {
		MailEntry entry = newEntry("from", "to", "bla bla");
		mailbox.addSentMail(entry);
		assertTrue("correspondence should include sent mail", mailbox.getCorrespondeceWith("to", 1).contains(entry));
	}
	
	@Test
	public void correspondenceIncludesReceivedMail() {
		MailEntry entry = newEntry("from", "to", "bla bla");
		mailbox.addReceivedMail(entry);
		assertTrue("correspondence should include received mail", mailbox.getCorrespondeceWith("from", 1).contains(entry));
	}
	
	@Test
	public void correspondenceIncludesBothSentAndReceived() {
		MailEntry incoming = newEntry("other", "myself", "bla bla");
		mailbox.addReceivedMail(incoming);
		
		MailEntry outgoing = newEntry("myself", "other", "yada yada");
		mailbox.addSentMail(outgoing);
		
		assertTrue("correspondence should include received mail", 
				mailbox.getCorrespondeceWith("other", 2).containsAll(Arrays.asList(incoming, outgoing)));
	}
	
	@Test
	public void sentMailsAreOrderedFromNewestToOldest() {
		// check order is preserved after queries
		final int sentCount = 3;
		sendMailsFromMe(sentCount);
		
		List<MailEntry> mails = mailbox.getLastNSent(sentCount);
		
		assertMailOrder(mails);
	}
	
	private void sendMailsFromMe(int howMany) {
		for (int i = 0; i < howMany; ++i) {
			mailbox.addSentMail(newEntry("me", "other-" + i, String.valueOf(i)));
		}
	}
	
	private void receiveMails(int howMany) {
		for (int i = 0; i < howMany; ++i) {
			mailbox.addReceivedMail(newEntry("other-" + i, "me", String.valueOf(i)));
		}
	}

	@Test
	public void unreadMailsAreOrderedFromNewestToOldest() {
		final int receiveCount = 5;
		receiveMails(receiveCount);
		
		List<MailEntry> mails = mailbox.getUnread();
		assertEquals("should have " + receiveCount + " unread mails", receiveCount, mails.size());
		
		assertMailOrder(mails);
	}
	
	@Test
	public void incomingMailsAreOrderedFromNewestToOldest() {
		final int receiveCount = 5;
		receiveMails(receiveCount);
		
		List<MailEntry> mails = mailbox.getLastNReceived(receiveCount);
		assertEquals("should have received" + receiveCount + " mails", receiveCount, mails.size());
		
		assertMailOrder(mails);
	}
	
	@Test
	public void allMailIncludesBothSentAndReceived() {
		MailEntry outgoing1 = newEntry("me", "other", "outgoing1");
		MailEntry outgoing2 = newEntry("me", "other", "outgoing2");
		MailEntry incoming = newEntry("other", "me", "incoming");
		
		mailbox.addSentMail(outgoing1);
		mailbox.addSentMail(outgoing2);
		mailbox.addReceivedMail(incoming);
		
		List<MailEntry> mails = mailbox.getLastNMails(3);
		assertEquals("all mail from mailbox should include 3 messages (2 out, 1 in)", 3, mails.size());
		
		assertTrue("all mail should include both sent+received messages", mails.containsAll(Arrays.asList(outgoing1, outgoing2, incoming)));
	}
	
	@Test
	public void allMailResultsAreOrderedFromNewestToOldest() throws InterruptedException {
		final int totalMailAmount = 10;
		final int requestedMailAmount = 10;
		
		for (int i = 0; i < totalMailAmount; i += 2) {
			mailbox.addSentMail(newEntry("me", "other", String.valueOf(i)));
			Thread.sleep(2);
			mailbox.addReceivedMail(newEntry("other", "me", String.valueOf(i + 1)));
		}
		
		List<MailEntry> mails = mailbox.getLastNMails(requestedMailAmount);
		
		assertMailOrder(mails);
	}

	private void assertMailOrder(List<MailEntry> mails) {
		int currentMsgIndex, prevMsgIndex = Integer.parseInt(mails.get(0).getMail().content);
		
		for (MailEntry entry : mails) {
			// extracted index from content is a number, they should go down
			currentMsgIndex = Integer.parseInt(entry.getMail().content);
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
			mailbox.addSentMail(newEntry("me", "other", ""));
		}
	}
	
	@Test
	public void noMoreThanMaximalAmountOfResults() {
		// ask for less entries than amount in existence
		final int mailCount = 5;
		final int requestAmount = mailCount - 1;

		for (int i = 0; i < mailCount; ++i) {
			mailbox.addSentMail(newEntry("me", "other", ""));
		}
		
		assertEquals("should only return at most requested amount, no more", requestAmount, mailbox.getLastNMails(requestAmount).size());
	}
	
	@Test
	public void returnedReceivedEntriesAreNewestAndMarkedAsRead() {
		// fetch entries with different queries and see if they are returned as unread
		MailEntry older = newEntry("you", "me", "ping");
		MailEntry newer = newEntry("you", "me", "pong");
		
		mailbox.addReceivedMail(older);
		mailbox.addReceivedMail(newer);
		
		List<MailEntry> onlyLastMail = mailbox.getLastNReceived(1); 
		
		assertTrue("returned mail list should contain the newer mail", onlyLastMail.contains(newer));
		assertTrue("returned mail list should NOT contain the older mail", !onlyLastMail.contains(older));
		
		assertEquals("only one mail should have remained unread", 1, mailbox.getUnread().size());
		assertEquals("no mail should have remained unread", 0, mailbox.getUnread().size());
	}
	
	@Test
	public void returnedEntriesAreNewestAndMarkedAsRead() throws InterruptedException {
		// fetch entries with different queries and see if they are returned as unread
		MailEntry oldest = newEntry("you", "me", "oldest");
		MailEntry older  = newEntry("me", "you", "older");
		MailEntry newer  = newEntry("you", "me", "newer");
		MailEntry newest = newEntry("me", "you", "newest");
		
		mailbox.addReceivedMail(oldest);
		mailbox.addSentMail(older);
		mailbox.addReceivedMail(newer);
		mailbox.addSentMail(newest);
		
		List<MailEntry> onlyLastMail = mailbox.getLastNMails(2); 
		
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
	
	@Test
	public void getCorrespondeceMarksAsUnread() {
		mailbox.addReceivedMail(newEntry("one", "me", "bla"));
		mailbox.addReceivedMail(newEntry("two", "me", "bla"));
		mailbox.addReceivedMail(newEntry("one", "me", "bla"));
		mailbox.addReceivedMail(newEntry("three", "me", "bla"));
		
		assertEquals("received two messages from \"one\"", 2 ,mailbox.getCorrespondeceWith("one", 2).size());
		assertEquals("should only get the mail from \"three\"", 1, mailbox.getLastNMails(1).size());
		assertEquals("one the single mail from \"two\" should remain", 1, mailbox.getUnread().size());
	}
}
