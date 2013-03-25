import org.apache.zookeeper.KeeperException;

public class BarrierLeaveRunnable implements Runnable {
	private Barrier b;
	private String nodeName;
	private ConcurrentSimpleList<String> nodeList;
	
	public BarrierLeaveRunnable(Barrier b, ConcurrentSimpleList<String> nodeList, String node_name) {
		this.b = b;
		this.nodeList = nodeList;
		this.nodeName = node_name;
	}
	
	public void run() {
		try {
			System.out.print("-");
			b.leave(nodeName);
			System.out.println("Removendo " + nodeName);
			//nodeList.remove(nodeName);
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
