package il.ac.technion.cs.sd.app.mail;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class IntegrationBasicTest {

	private ServerMailApplication		server	= new ServerMailApplication("server");
	private List<ClientMailApplication>	clients	= new ArrayList<>();
	
	private ClientMailApplication buildClient(String login) {
		ClientMailApplication $ = new ClientMailApplication(server.getAddress(), login);
		clients.add($);
		return $;
	}
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		server.start();
	}

	@After
	public void tearDown() throws Exception {
		server.stop();
		server.clean();
		clients.forEach(c -> c.stop());
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
	

}
