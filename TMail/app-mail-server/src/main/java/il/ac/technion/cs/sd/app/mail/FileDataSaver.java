package il.ac.technion.cs.sd.app.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Optional;

public class FileDataSaver<T extends Serializable> implements DataSaver<T> {

	final String fileName;
	
	FileDataSaver(String fileName) {
		this.fileName = fileName;
	}
	
	@Override
	public void save(T data) {
		ObjectOutputStream oos = null;
		try {
			FileOutputStream fos = new FileOutputStream(fileName);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(data);
			oos.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (oos != null) {
					oos.close();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public Optional<T> load() {
		File f = new File(fileName);
		if (!f.exists()) {
			return Optional.empty();
		}
		FileInputStream fis;
		try {
			fis = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(fis);
			T obj = (T) ois.readObject();
			return Optional.of(obj);
		} catch (IOException | ClassNotFoundException e ) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		} finally {
			try {
				if (ois != null) {
					ois.close();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}
}
