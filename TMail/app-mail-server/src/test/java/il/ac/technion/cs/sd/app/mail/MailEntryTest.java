package il.ac.technion.cs.sd.app.mail;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MailEntryTest {

	Mail mail = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		this.mail = new Mail("from", "to", "bla bla");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void entryCreatedContainsMailObject() {
		MailEntry entry = new MailEntry(this.mail);
		assertNotNull(entry.mail());
		assertEquals("Entry should contain the exact mail it was initialized with", this.mail, entry.mail());
	}
	
	@Test
	public void entryCreatedWithCurrentTime() {
		MailEntry entry = new MailEntry(this.mail);
		assert(System.currentTimeMillis() > entry.time());
	}
	
	@Test
	public void entryCreatedWithCustomTime() {
		MailEntry entry = new MailEntry(this.mail, 1L);
		assertEquals("Custom time was manually set upon creation.", 1L, entry.time());
	}

}
