package il.ac.technion.cs.sd.app.mail;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("deprecation")
public class IntegrationBasicTest {

	private ServerMailApplication		server	= new ServerMailApplication("server");
	private ClientMailApplication       testClient = null;
	private List<ClientMailApplication>	clients	= new ArrayList<>();
	private Thread serverThread;

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
		serverThread = startServerInThread(server);
		testClient = buildClient("tester");
	}

	@After
	public void tearDown() throws Exception {
		server.clean();
		server.stop();
		clients.forEach(c -> c.stop());
		serverThread.stop();
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
		// init - original server, and a client that sends some message
		final ServerMailApplication s1 = new ServerMailApplication("s");
		s1.clean();
		ClientMailApplication c = new ClientMailApplication(s1.getAddress(), "c");
		final Thread st1 = startServerInThread(s1);
		c.sendMail("other", "nothing");

		// stop original server
		s1.stop();
		st1.stop();
		
		// create 2nd server with same address, load original's contents
		final ServerMailApplication s2 = new ServerMailApplication("s");
		final Thread st2 = startServerInThread(s2);
		
		assertEquals("should have the original single message sent with previous server", 1, c.getAllMail(1).size());
		
		// cleanup
		s2.stop();
		s2.clean();
		st2.stop();
	}
	
	
	@Test 
	public void serverPersistantContentsAreDeletedAfterClean() throws InterruptedException {
		// init - original server, and a client that sends some message
		final ServerMailApplication s = new ServerMailApplication("s");
		ClientMailApplication c = new ClientMailApplication(s.getAddress(), "c");
		Thread st = startServerInThread(s);
		c.sendMail("other", "nothing");

		// stop original server
		s.stop();
		s.clean();
		st.stop();
		
		// create 2nd server with same address, load original's contents
		final ServerMailApplication s2 = new ServerMailApplication("s");
		st = startServerInThread(s2);
		
		assertEquals("should have no messages sent with previous server", 0, c.getAllMail(1).size());
		
		// cleanup
		s2.start();
		s2.clean();
		st.stop();
	}
	
	@Test 
	public void serverRuntimeContentsAreDeletedAfterClean() throws InterruptedException {
		final ServerMailApplication s = new ServerMailApplication("s");
		ClientMailApplication c = new ClientMailApplication(s.getAddress(), "c");
		Thread st = startServerInThread(s);
		c.sendMail("whoever", "whatever");

		// clean server's contents
		s.clean();
		assertEquals("should have no messages after server clean", 0, c.getAllMail(1).size());
		
		s.stop();
		st.stop();
	}
	
	

}
