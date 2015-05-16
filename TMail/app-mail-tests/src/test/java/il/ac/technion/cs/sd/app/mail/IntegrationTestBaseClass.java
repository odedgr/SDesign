package il.ac.technion.cs.sd.app.mail;


import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;

public class IntegrationTestBaseClass {
	
	private static final int SLEEP_MILLISECODS_AFTER_START = 50;
	private static final int SLEEP_MILLISECODS_BEFORE_STOP = 20;
	private static final int SLEEP_MILLISECODS_BEFORE_CLEAN = 20;
	
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
		Thread.sleep(SLEEP_MILLISECODS_AFTER_START);
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
		Thread.sleep(SLEEP_MILLISECODS_BEFORE_STOP);
		server.stop();
		serverThread.stop();
		setup();
	}
	protected void cleanServer() throws InterruptedException {
		Thread.sleep(SLEEP_MILLISECODS_BEFORE_CLEAN);
		server.clean();
	}
}