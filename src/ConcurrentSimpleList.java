import java.util.ArrayList;



public class ConcurrentSimpleList<T> {
	private ArrayList<T> internalList;
	private Object mutex;
	
	public ConcurrentSimpleList() {
		internalList = new ArrayList<T>();
		mutex = new Object();
	}
	
	public void add(T el) {
		synchronized (mutex) {
			internalList.add(el);
		}
	}
	
	public void remove(T el) {
		synchronized (mutex) {
			internalList.remove(el);
		}
	}
	
	public T get(int index) {
		synchronized (mutex) {
			return internalList.get(index);
		}
	}
	
	public int size() {
		synchronized (mutex) {
			return internalList.size();
		}
	}
}
