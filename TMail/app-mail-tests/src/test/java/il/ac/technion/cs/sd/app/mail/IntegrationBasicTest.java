package il.ac.technion.cs.sd.app.mail;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class IntegrationBasicTest extends IntegrationTestBaseClass {

	@Test
	public void singleSendAndReceive() {
		ClientMailApplication one = buildClient("one");
		ClientMailApplication two = buildClient("two");
		one.sendMail("two", "hi");
		
		List<Mail> newMail = two.getNewMail();
		assertEquals("client two should have the incoming mail \"hi\" from one", "hi" , newMail.get(0).content);
		assertEquals("client two should have a single incoming mail from one", 1 , newMail.size());
		assertEquals("client two should have the incoming mail \"hi\" from one", "hi" , two.getIncomingMail(5).get(0).content);
		assertEquals("client two should have a single incoming mail from one", 1 , two.getIncomingMail(5).size());
	}
	
	@Test 
	public void receiveMailSentToSelf() {
		ClientMailApplication c = buildClient("self");
		c.sendMail("self", "hi myself");
		List<Mail> myMail = c.getNewMail();
		assertEquals("should have gotten the new mail from self to self", "hi myself", myMail.get(0).content);
	}
	
	@Test 
	public void serversContentsRemainsAfterStopAndStart() throws InterruptedException {
		ClientMailApplication c = buildClient("c");
		c.sendMail("other", "nothing");
		
		restartServer();
		
		List<Mail> results = c.getAllMail(1);
		assertEquals("should have the original single message sent with previous server", 1, results.size());
	}
	
	
	@Test 
	public void serverPersistantContentsAreDeletedAfterClean() throws InterruptedException {
		ClientMailApplication c = buildClient("c12");
		c.sendMail("other", "nothing");

		restartServer();
		cleanServer();
		
		List<Mail> results = c.getAllMail(1);
		assertEquals("should have no messages sent with previous server", 0, results.size());
	}
	
	@Test 
	public void serverRuntimeContentsAreDeletedAfterClean() throws InterruptedException {
		ClientMailApplication c = buildClient("c13");
		c.sendMail("whoever", "whatever");

		cleanServer();
		List<Mail> results = c.getAllMail(1);
		assertEquals("should have no messages after server clean", 0, results.size());
	}
	
	@Test 
	public void duplicateMailIsReturnedAsUniqueItems() throws InterruptedException {
		ClientMailApplication cl1 = buildClient("cl1");
		ClientMailApplication cl2 = buildClient("cl2");

		cl1.sendMail("cl2", "same mail");
		cl1.sendMail("cl2", "same mail");

		List<Mail> results = cl2.getNewMail();
		assertEquals("should have received 2 separate duplicates of mail", 2, results.size());
		results = cl2.getIncomingMail(5);
		assertEquals("should have received 2 separate duplicates of mail", 2, results.size());
		results = cl1.getSentMails(5);
		assertEquals("should have sent 2 separate duplicates of mail", 2, results.size());
	}
	
	@Test 
	public void serverDataIsPersistentUnderManyRestarts() throws InterruptedException {
		final int iterations = 5;
		
		ClientMailApplication sender = buildClient("sender");
		ClientMailApplication receiver = buildClient("receiver");
		sender.sendMail("receiver", "whatever");

		for (int i = 0; i < iterations; ++i) {
			restartServer();
			assertEquals("the receiver should always have a single mail", 1, receiver.getIncomingMail(5).size());
			assertEquals("the sender should always have a single mail", 1, sender.getSentMails(5).size());
		}
	}
	
	@Test
	public void contactsAreUnique() {
		ClientMailApplication c = buildClient("Moti");
		c.sendMail("one", "bla");
		c.sendMail("two", "bla");
		c.sendMail("one", "yada");
		c.sendMail("two", "yada");
		
		assertEquals("wrote to exactly 2 other clients", 2, c.getContacts(5).size());
		assertTrue("contacts should include exactly 'one' and 'two'", c.getContacts(5).containsAll(Arrays.asList("one", "two")));
	}
	
	@Test
	public void getEmptyContacts() {
		ClientMailApplication c1 = buildClient("a");
		List<String> contacts = c1.getContacts(10);
		assertTrue(contacts.isEmpty());
	}
	
	@Test
	public void contactsAreInAlphabeticalOrder() {
		ClientMailApplication c1 = buildClient("a");
		ClientMailApplication c2 = buildClient("b");
		ClientMailApplication c3 = buildClient("c");
		ClientMailApplication c4 = buildClient("d");

		c1.sendMail("b", "hi");
		c4.sendMail("a", "hi");
		{
			List<String> contacts = c1.getContacts(10);
			assertEquals(2, contacts.size());
			assertEquals("b", contacts.get(0));
			assertEquals("d", contacts.get(1));
		}
		{
			c3.sendMail("a", "hi");
			List<String> contacts = c1.getContacts(10);
			assertEquals(3, contacts.size());
			assertEquals("b", contacts.get(0));
			assertEquals("c", contacts.get(1));
			assertEquals("d", contacts.get(2));
		}
		{
			List<String> contacts = c2.getContacts(10);
			assertEquals(1, contacts.size());
			assertEquals("a", contacts.get(0));
		}
	}
	
	@Test
	public void selfCanBeContact() {
		ClientMailApplication c1 = buildClient("a");
		ClientMailApplication c2 = buildClient("b");

		c1.sendMail("a", "hi");
		c2.sendMail("a", "hi");
		{
			List<String> contacts = c1.getContacts(10);
			assertEquals(2, contacts.size());
			assertEquals("a", contacts.get(0));
			assertEquals("b", contacts.get(1));
		}
	}
	
	@Test
	public void unreadRemainUnreadAfterServerRestart() throws InterruptedException {
		ClientMailApplication arik = buildClient("arik");
		ClientMailApplication benz = buildClient("benz");
		
		arik.sendMail("benz", "benz");
		arik.sendMail("benz", "you hear me?");
		
		restartServer();
		assertEquals("both mails should be unread", 2, benz.getNewMail().size());
		
		arik.sendMail("benz", "hello?");
		
		restartServer();
		restartServer();
		assertEquals("only the last mail should be unread", 1, benz.getNewMail().size());
		
		restartServer();
		assertEquals("all mail should have been read", 0, benz.getNewMail().size());
	}
}
