package il.ac.technion.cs.sd.app.mail;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ClientMailBoxTest {
	
	ClientMailBox mailbox = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		mailbox = new ClientMailBox();
	}

	@After
	public void tearDown() throws Exception {
		mailbox = null;
	}

//////////////////////////////////////////////////
	
	@Test
	public void sentMailReceiverAddedToContacts() {
		MailEntry entry = new MailEntry(new Mail("from", "to", "bla bla"));
		mailbox.sentMailEntry(entry);
		assertTrue("contacts should include the sender of an incomming mail.", mailbox.getContacts().contains("to"));
	}
	
	@Test
	public void receivedMailSenderAddedToContacts() {
		MailEntry entry = new MailEntry(new Mail("from", "to", "bla bla"));
		mailbox.receivedMailEntry(entry);
		assertTrue("contacts should include the receiver of an outgoing mail.", mailbox.getContacts().contains("from"));
	}
	
	@Test
	public void correspondenceIncludesSentMail() {
		Mail mail = new Mail("from", "to", "bla bla");
		MailEntry entry = new MailEntry(mail);
		mailbox.sentMailEntry(entry);
		System.out.println(mailbox.getCorrespondeceWith("to", 1));
		assertTrue("correspondence should include sent mail", mailbox.getCorrespondeceWith("to", 1).contains(mail));
	}
	
	@Test
	public void correspondenceIncludesReceivedMail() {
		Mail mail = new Mail("from", "to", "bla bla");
		MailEntry entry = new MailEntry(mail);
		mailbox.receivedMailEntry(entry);
		assertTrue("correspondence should include received mail", mailbox.getCorrespondeceWith("from", 1).contains(mail));
	}
	
	@Test
	public void correspondenceIncludesBothSentAndReceived() {
		Mail in_mail = new Mail("other", "myself", "bla bla");
		MailEntry in_entry = new MailEntry(in_mail);
		mailbox.receivedMailEntry(in_entry);
		
		Mail out_mail = new Mail("myself", "other", "yada yada");
		MailEntry out_entry = new MailEntry(out_mail);
		mailbox.sentMailEntry(out_entry);
		
		assertTrue("correspondence should include received mail", 
				mailbox.getCorrespondeceWith("other", 2).containsAll(Arrays.asList(in_mail, out_mail)));
	}
	
	@Test
	public void sentMailsAreOrderedFromNewestToOldest() {
		List<Mail> mails

	}
	
	@Test
	public void unreadMailsAreOrderedFromNewestToOldest() {
		fail("Not yet implemented");
	}
	
	@Test
	public void incomingMailsAreOrderedFromNewestToOldest() {
		fail("Not yet implemented");
	}
	
	@Test
	public void allMailIncludesBothSentAndReceived() {
		fail("Not yet implemented");
	}
	
	@Test
	public void allMailResultsAreOrderedFromNewestToOldest() {
		fail("Not yet implemented");
	}
	
	@Test
	public void getAllResultsWhenAskingMoreThanExists() {
		// ask for more entries than amount in existence, make sure to get all that exist
		fail("Not yet implemented");
	}
	
	@Test
	public void noMoreThanMaximalAmountOfResults() {
		// ask for less entries than amount in existence
		fail("Not yet implemented");
	}
	
	@Test
	public void returnedResultsAreNewest() {
		// ask for less entries than amount in existence, make sure to get newest
		fail("Not yet implemented");
	}
	
	@Test
	public void returnedEntriesAreMarkedAsRead() {
		// fetch entries with different queries and see if they are returned as unread
		fail("Not yet implemented");
	}
	
	@Test
	public void onlyReturnedEntriesAreMarkedAsUnread() {
		fail("Not yet implemented");
	}

}
