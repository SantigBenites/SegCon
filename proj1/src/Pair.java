import java.io.Serializable;

/**
 * @author fc55312
 *
 */

public class Pair<T,K> implements Serializable {
	
	private T f;
	private K l;
	
	public Pair(T first, K last) {
		f = first;
		l = last;
	}
	
	public T getF() {
		return f;
	}
	
	public K getL() {
		return l;
	}
	
	public void setL(K last) {
		l = last;
	}
}