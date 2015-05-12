package il.ac.technion.cs.sd.app.mail;

import java.util.Optional;

public interface DataSaver<T> {
	void save(T data);
	Optional<T> load();
	void clean();
}
