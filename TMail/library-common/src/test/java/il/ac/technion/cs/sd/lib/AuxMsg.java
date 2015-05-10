package il.ac.technion.cs.sd.lib;

import java.io.Serializable;

/**
 * Just some generic serializable class, to test the DefaultSerializer with.
 */
class AuxMsg implements Serializable {
	private static final long serialVersionUID = 8494530965021917044L;
	
	public String str;
	public int num;
	public double doubl;
	public boolean bool;
	
	AuxMsg() {}
	AuxMsg(String str, int num, double doubl, boolean bool) {
		this.str = str;
		this.num = num;
		this.doubl = doubl;
		this.bool = bool;
	}
	
	static AuxMsg msg1() {
		return new AuxMsg("Hello", 5, 6.7, true);
	}
	
	static AuxMsg msg2() {
		return new AuxMsg("World", 12, -19.2, false);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;

		AuxMsg other = (AuxMsg) obj;

		return str.equals(other.str) && num == other.num
				&& doubl == other.doubl && bool == other.bool;
	}
}