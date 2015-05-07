package il.ac.technion.cs.sd.app.mail;

import il.ac.technion.cs.sd.app.mail.Constants.Opcode;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


/**
 * A wrapper for messages that are passed around between clients and a server.
 * An Envelope object is used for both sending a single mail from one client to another, as well as
 * sending a task request to its server and getting the appropriate results.
 * 
 * Envelopes are created using the appropriate wrap...() methods, which are built specifically to accept
 * the required parameters for each type of request. Similarly, an incoming envelope's message type is identifiable
 * via it's opcode (retrievable using the opcode() method).
 *
 */
public class Envelope implements Serializable {

	private static final long serialVersionUID = 8146517154357247722L;
	
	private static final List<Opcode> mailResultOpcodes = 
			Arrays.asList(Opcode.SENT_MAIL,
						  Opcode.INCOMING_MAIL,
						  Opcode.LAST_MAIL, 
						  Opcode.CORRESPONDANCE_WITH_CLIENT,
						  Opcode.UNREAD_MAIL);
	
	private static final List<Opcode> requestWithAmountOpcodes = 
			Arrays.asList(Opcode.SENT_MAIL,
						  Opcode.INCOMING_MAIL,
						  Opcode.LAST_MAIL);
	
	private static final List<Opcode> requestWithoutAmountOpcodes = 
			Arrays.asList(Opcode.CORRESPONDANCE_WITH_CLIENT,
						  Opcode.UNREAD_MAIL);
	
	
	private Opcode opcode;
	private int amount; // indicates either the amount requested, or the amount in returned result (list)
	List<Mail> mailList = new ArrayList<Mail>();
	List<String> contactsList = new ArrayList<String>();

	/**
	 * Block default empty constructor.
	 */
	private Envelope() {
		
	}
	
	/**
	 * Get this envelope's opcode.
	 * 
	 * @return Opcode of envelope.
	 */
	public Opcode opcode() {
		return this.opcode;
	}
	
	/**
	 * Get this envelope's requested amount (when sent by client) / result count (when sent from server).
	 * 
	 * @return amount requested or result count.
	 */
	public int requestedAmount() {
		return this.amount;
	}
	
	/**
	 * Get the mail list in the envelope. Returned list will be empty if this envelope is of an incorrect type (e.g: not the
	 * response for a task that yields results as a list of mails) or if there were no matching results.
	 * 
	 * @return The list of mails contained in this Envelope.
	 */
	public List<Mail> getMailResults() {
		return this.mailList;
	}
	
	/**
	 * Get the contacts list in the envelope. Returned list will be empty if this envelope is of an incorrect type (e.g: not the
	 * response for a contacts list request) or if there were no matching results.
	 * 
	 * @return The list of contacts contained in this Envelope.
	 */
	public List<String> getContactsResults() {
		return this.contactsList;
	}
	
	/**
	 * Get an envelope wrapping a request for a server task, which does not require a specified requested amount.
	 * 
	 * @param requestedOpcode Opcode matching type of requested server task.
	 * @return Envelope to be sent to server for requesting a task execution (and causing the server to send a response).
	 */
	public static Envelope wrapRequestWithoutAmount(Opcode requestedOpcode) {
		if (!requestWithoutAmountOpcodes.contains(requestedOpcode)) {
			throw new InvalidParameterException("incorrect request opcode - either it requires an amount, or it is not a request opcode");
		}
		
		Envelope e = new Envelope();
		e.opcode = requestedOpcode;
		
		return e;
	}
	
	/**
	 * Get an envelope wrapping a request for a server task, which requires a specified requested amount.
	 * 
	 * @param requestedOpcode Opcode matching type of requested server task.
	 * @param n Requested amount of results from the server.
	 * @return Envelope to be sent to server for requesting a task execution (and causing the server to send a response).
	 */
	public static Envelope wrapRequestWithAmount(Opcode requestedOpcode, int n) {
		if (!requestWithAmountOpcodes.contains(requestedOpcode)) {
			throw new InvalidParameterException("incorrect request opcode - either it does not require an amount, or it is not a request opcode");
		}
		
		if (1 > n) {
			throw new InvalidParameterException("cannot request less than a single result.");
		}
		
		Envelope e = new Envelope();
		e.opcode = requestedOpcode;
		e.amount = n;
		
		return e;
	}
	
	/**
	 * Wrap a Mail for sending to another client.
	 * 
	 * @param mail Mail object to be wrapped for sending.
	 */
	public static Envelope wrapMail(Mail mail) {
		if (null == mail) {
			throw new InvalidParameterException("cannot wrap a null mail in an envelope");
		}
		
		Envelope e = new Envelope();
		e.opcode = Opcode.SEND_MAIL;
		e.amount = 1;
		e.mailList.add(mail);
		
		return e;
	}
	
	/**
	 * Wrap a server task execution's results of tasks requiring Mails as a result, for sending back to the requesting client.
	 * 
	 * @param opcode Opcode matching type of task, for which results will be wrapped in the envelope. 
	 * @param results task results to be wrapped in the envelope.
	 * @return Envelope containing the task results.
	 */
	public static Envelope wrapResults(Opcode opcode, Collection<Mail> results) {
		if (null == opcode || null == results) {
			throw new InvalidParameterException("cannot wrap with null value.");
		}
		
		if (!mailResultOpcodes.contains(opcode)) {
			throw new InvalidParameterException("requested opcode " + opcode + " does not match a response that contains mails");
		}
		
		Envelope envelope = new Envelope();
		
		envelope.opcode = opcode;
		envelope.amount = results.size();
		envelope.mailList.addAll(results);
		
		return envelope;
	}
	
	/**
	 * Wrap a server task execution's results of the contacts request, for sending back to the requesting client.
	 * 
	 * @param contacts Contacts returned as this task's results, to be wrapped in envelope.
	 * @return Envelope containing the task results.
	 */
	public static Envelope wrapResults(Collection<String> contacts) {
		if (null == contacts) {
			throw new InvalidParameterException("cannot wrap with null value");
		}
		
		Envelope envelope = new Envelope();
		
		envelope.opcode = Opcode.CONTACTS;
		envelope.amount = contacts.size();
		envelope.contactsList.addAll(contacts);
		
		return envelope;
	}
	
}
