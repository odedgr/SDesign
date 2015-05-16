package il.ac.technion.cs.sd.app.mail;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("deprecation")
public class IntegrationBasicTest {

	private final String testClientName = "integrationBasicTester";
	private ServerMailApplication		server	= new ServerMailApplication("server");
	private ClientMailApplication       testClient = null;
	private List<ClientMailApplication>	clients	= new ArrayList<>();
	private Thread serverThread;
	private boolean useDefaultTesters = true;

	private Thread startServerInThread(final ServerMailApplication s) throws InterruptedException {
		Thread t = new Thread(() -> s.start());
		t.start();
		Thread.yield();
		Thread.sleep(10L);
		
		return t;
	}
	
	private ClientMailApplication buildClient(String login) {
		ClientMailApplication $ = new ClientMailApplication(server.getAddress(), login);
		clients.add($);
		return $;
	}
	
	@Before
	public void setUp() throws Exception {
		useDefaultTesters = true;
		serverThread = startServerInThread(server);
		testClient = buildClient(testClientName);
	}

	@After
	public void tearDown() throws Exception {
		if (useDefaultTesters) {
			shutDownDefaultTesters();
		}
	}

	private void shutDownDefaultTesters() throws InterruptedException {
		useDefaultTesters = false;
		Thread.sleep(5L);
		server.clean();
		server.stop();
		clients.forEach(c -> c.stop());
		clients.clear();
		serverThread.stop();
		Thread.sleep(5L);
	}
	
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
		shutDownDefaultTesters();
		
		// init - original server, and a client that sends some message
		final ServerMailApplication s1 = new ServerMailApplication("ps");
		ClientMailApplication c = new ClientMailApplication(s1.getAddress(), "c15");
		final Thread st1 = startServerInThread(s1);
		c.sendMail("other", "nothing");

		// give time to handle request
		Thread.yield();
		Thread.sleep(10L);
		
		// stop original server
		s1.stop();
		st1.stop();
		
		// create 2nd server with same address, load original's contents
		final ServerMailApplication s2 = new ServerMailApplication("ps");
		final Thread st2 = startServerInThread(s2);
		
		List<Mail> results = c.getAllMail(1);
		
		// cleanup
		s2.clean();
		s2.stop();
		st2.stop();
		c.stop();
		
		assertEquals("should have the original single message sent with previous server", 1, results.size());
	}
	
	
	@Test 
	public void serverPersistantContentsAreDeletedAfterClean() throws InterruptedException {
		shutDownDefaultTesters();
		
		// init - original server, and a client that sends some message
		final ServerMailApplication s = new ServerMailApplication("s");
		ClientMailApplication c = new ClientMailApplication(s.getAddress(), "c12");
		Thread st = startServerInThread(s);
		c.sendMail("other", "nothing");

		// give time to handle request
		Thread.yield();
		Thread.sleep(10L);
		
		// stop original server
		s.clean();
		s.stop();
		st.stop();
		
		// create 2nd server with same address, load original's contents
		final ServerMailApplication s2 = new ServerMailApplication("s");
		st = startServerInThread(s2);
		
		List<Mail> results = c.getAllMail(1);
		
		// cleanup
		s2.clean();
		s2.stop();
		st.stop();
		c.stop();
		
		assertEquals("should have no messages sent with previous server", 0, results.size());
	}
	
	@Test 
	public void serverRuntimeContentsAreDeletedAfterClean() throws InterruptedException {
		shutDownDefaultTesters();
		
		final ServerMailApplication s = new ServerMailApplication("s");
		ClientMailApplication c = new ClientMailApplication(s.getAddress(), "c13");
		Thread st = startServerInThread(s);
		c.sendMail("whoever", "whatever");

		// give time to handle request
		Thread.yield();
		Thread.sleep(10L);
		
		// clean server's contents
		s.clean();
		List<Mail> results = c.getAllMail(1);
		
		s.stop();
		st.stop();
		c.stop();
		
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
		shutDownDefaultTesters();
		final int iterations = 5;
		
		ServerMailApplication s = new ServerMailApplication("s");
		ClientMailApplication sender = new ClientMailApplication(s.getAddress(), "sender");
		ClientMailApplication receiver = new ClientMailApplication(s.getAddress(), "receiver");
		Thread st = startServerInThread(s);
		sender.sendMail("receiver", "whatever");

		// give time to handle request
		Thread.yield();
		Thread.sleep(10L);
		
		for (int i = 0; i < iterations; ++i) {
			s.stop();
			st.stop();
			Thread.sleep(5L);
			
			s = new ServerMailApplication("s");
			st = startServerInThread(s);
			
			Thread.sleep(5L);
			assertEquals("the receiver should always have a single mail", 1, receiver.getIncomingMail(5).size());
			assertEquals("the sender should always have a single mail", 1, sender.getSentMails(5).size());
		}

		// clean up
		s.clean();
		s.stop();
		st.stop();
		
		sender.stop();
		receiver.stop();
	}
	
	@Test
	public void contactsAreUnique() {
		testClient.sendMail("one", "bla");
		testClient.sendMail("two", "bla");
		testClient.sendMail("one", "yada");
		testClient.sendMail("two", "yada");
		
		assertEquals("wrote to exactly 2 other clients", 2, testClient.getContacts(5).size());
		assertTrue("contacts should include exactly 'one' and 'two'", testClient.getContacts(5).containsAll(Arrays.asList("one", "two")));
	}

}
