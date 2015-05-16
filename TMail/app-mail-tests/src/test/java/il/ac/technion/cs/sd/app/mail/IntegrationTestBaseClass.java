package il.ac.technion.cs.sd.app.mail;


import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;

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
	
	@SuppressWarnings("deprecation")
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
}