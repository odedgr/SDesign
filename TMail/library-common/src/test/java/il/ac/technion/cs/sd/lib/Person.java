package il.ac.technion.cs.sd.lib;

import java.io.Serializable;

/**
 * Just some generic serializable class, to test the DefaultSerializer with.
 */
class Person implements Serializable {
	private static final long serialVersionUID = 8494530965021917044L;
	
	public String name;
	public int age;
	public double height;
}