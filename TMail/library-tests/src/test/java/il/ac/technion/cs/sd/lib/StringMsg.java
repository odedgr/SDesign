package il.ac.technion.cs.sd.lib;


/**
 * A non-serializable message helper class.
 * Used in the tests as a the message class sent between the client and the server,
 * with StringMsgCodec as a custom codec.
 */
public class StringMsg {
	
	/**
	 * InitiateStringMsg 
	 * @param str the string to hold in this StringMsg.
	 */
	StringMsg(String str) {
		this.str = str;
	}
	
	public final String str;
}
