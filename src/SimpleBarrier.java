import java.io.IOException;

import org.apache.zookeeper.KeeperException;


public class SimpleBarrier {
	private DoubleBarrier doubleBarrier;
	
	public SimpleBarrier(String address, String root, int size) throws KeeperException, InterruptedException, IOException {
		doubleBarrier = new DoubleBarrier(address, root, size);
	}
	
	public void Wait() throws KeeperException, InterruptedException {
		doubleBarrier.enter();
		doubleBarrier.leave();
	}
}
