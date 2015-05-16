package il.ac.technion.cs.sd.app.mail;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class IntegrationTestBaseClass {
	private ServerMailApplication		server	= new ServerMailApplication("server");
	private List<ClientMailApplication>	clients	= new ArrayList<>();
	private Thread						serverThread;
	
	protected ClientMailApplication buildClient(String login) {
		ClientMailApplication $ = new ClientMailApplication(server.getAddress(), login);
		clients.add($);
		return $;
	}
	
	@Before 
	public void setup() throws InterruptedException {
		serverThread = new Thread(() -> server.start());
		serverThread.start();
		Thread.yield(); // STRONG hints to the OS to start the server thread, though nothing can be *truly* deterministic
		Thread.sleep(100);
	}
	
	@SuppressWarnings("deprecation") // "I know what I'm doing"
	@After 
	public void teardown() {
		server.stop();
		server.clean();
		clients.forEach(x -> x.stop());
		serverThread.stop();
	}
	
	protected void restartServer() throws InterruptedException {
		Thread.sleep(100);
		server.stop();
		serverThread.stop();
		setup();
	}
	protected void cleanServer() throws InterruptedException {
		Thread.sleep(100);
		server.clean();
	}
	
	
//	@Test 
//	public void basicTest() throws Exception {
//		ClientMailApplication gal = buildClient("Gal");
//		gal.sendMail("Itay", "Hi");
//		assertEquals(gal.getContacts(1), Arrays.asList("Itay"));
//		ClientMailApplication itay = buildClient("Itay");
//		assertEquals(itay.getNewMail(), Arrays.asList(new Mail("Gal", "Itay", "Hi")));
//		itay.sendMail("Gal", "sup");
//		
//		restartServer();
//		
//		assertEquals(gal.getAllMail(3), Arrays.asList(
//				new Mail("Itay", "Gal", "sup"),
//				new Mail("Gal", "Itay", "Hi")));
//	}
}