import org.apache.zookeeper.KeeperException;


public class BarrierEnterRunnable implements Runnable {
	private Barrier b;
	private ConcurrentSimpleList<String> nodeList;
	private int timeOut;
	
	public BarrierEnterRunnable(Barrier b, ConcurrentSimpleList<String> nodeList, int timeOut) {
		this.b = b;
		this.nodeList = nodeList;
		this.timeOut = timeOut;
	}
	
	public void run() {
		try {
			String node_name = b.enter(timeOut);
			//System.out.print("+");
			nodeList.add(node_name);
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
