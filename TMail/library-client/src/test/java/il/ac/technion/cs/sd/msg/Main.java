package il.ac.technion.cs.sd.msg;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.*;

public class Main {
	
	static class A<T> {
		@Inject
		public A(List<T> list) {
			System.out.println(list.getClass());
		}
	}
	public static void main(String[] args) {
		Injector injector = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bind(new TypeLiteral<List<? extends Object>>(){})
				.to(new TypeLiteral<ArrayList<?>>(){});
			}
		});
		
		injector.getInstance(new Key<A<Integer>>(){});
	}
	
}