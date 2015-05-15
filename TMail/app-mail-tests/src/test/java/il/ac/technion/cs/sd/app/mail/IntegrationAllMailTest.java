package il.ac.technion.cs.sd.app.mail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IntegrationAllMailTest {

	private ServerMailApplication		server	= new ServerMailApplication("server");
	private ClientMailApplication       testClient = null;
	private List<ClientMailApplication>	clients	= new ArrayList<>();
	private Thread serverThread;
	
	private ClientMailApplication buildClient(String login) {
		ClientMailApplication $ = new ClientMailApplication(server.getAddress(), login);
		clients.add($);
		return $;
	}
	
	@Before
	public void setUp() throws Exception {
		serverThread = new Thread(() -> server.start());
		serverThread.start();
		Thread.yield(); 
		Thread.sleep(10L);
		testClient = buildClient("tester");
	}

	@SuppressWarnings("deprecation")
	@After
	public void tearDown() throws Exception {
		server.clean();
		server.stop();
		clients.forEach(c -> c.stop());
		serverThread.stop();
	}

	@Test
	public void allMailQueryMarksAsRead() {
		ClientMailApplication otherClient = buildClient("other");
		
		otherClient.sendMail("tester", "1");
		otherClient.sendMail("tester", "2");
		testClient.sendMail("other", "back at'ya");
		testClient.getAllMail(2);
		
		assertEquals("first mail should have been marked as read", 1, testClient.getNewMail().size());
	}
	
	@Test
	public void allMailQueryIsOrderedFromNewToOld() {
		ClientMailApplication otherClient = buildClient("other");
		
		List<Mail> mails = Arrays.asList(
				new Mail("other", "tester", "oldest"),
				new Mail("other", "tester", "slightly used"),
				new Mail("other", "tester", "brand new"));
		
		for (Mail mail : mails) {
			otherClient.sendMail(mail.to, mail.content);
		}
		
		Collections.reverse(mails);
		
		assertEquals(mails, testClient.getAllMail(3));
	}
	
	@Test
	public void allMailQueryContainsBothSendAndReceive() {
		ClientMailApplication otherClient = buildClient("other");
		
		List<Mail> mails = Arrays.asList(
				new Mail("other", "tester", "bla bla"),
				new Mail("tester", "other", "yada yada"));
		
		otherClient.sendMail(mails.get(0).to, mails.get(0).content);
		testClient.sendMail(mails.get(1).to, mails.get(1).content);
		
		Collections.reverse(mails);
		
		assertEquals(mails, testClient.getAllMail(2));
	}
	
	@Test
	public void allMailQueryReturnsNoMoreThanAskedItems() {
		final int smallPositiveAmount = 7;
		ClientMailApplication otherClient = buildClient("other");
		
		for (int i = 0; i < smallPositiveAmount; ++i) {
			otherClient.sendMail("tester", "Boo!");
		}
		
		int smallerAmount = smallPositiveAmount - 1;
		assertEquals("should get no more results than amount asked for", smallerAmount, testClient.getAllMail(smallerAmount).size());
	}
	
	@Test
	public void allMailQueryReturnsAmountExistingLowerThanAsked() {
		ClientMailApplication otherClient = buildClient("other");
		
		otherClient.sendMail("tester", "bla bla");
		otherClient.sendMail("tester", "yada yada");
		
		assertEquals("only 2 items exist", 2, testClient.getAllMail(3).size());
	}
	
	@Test
	public void allMailQueryReturnsEmptyWhenNoActivity() {
		assertTrue("should get an empty list", testClient.getAllMail(1).isEmpty());
	}
	
}
