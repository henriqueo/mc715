import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;


/**
 * Barrier
 */
public class Barrier extends SyncPrimitive {
    int size;

    /**
     * Barrier constructor
     *
     * @param address
     * @param root
     * @param size
     * @throws InterruptedException 
     * @throws KeeperException 
     */
    Barrier(String address, String root, int size) throws KeeperException, InterruptedException {
        super(address);
        this.root = root;
        this.size = size;

        // Create barrier node
        try {
            Stat s = zk.exists(root, false);
            if (s == null) {
                zk.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
            }
        }
        catch (NullPointerException e) {
        	System.out.println("Zk was not created");
        }
    }

    public int countChildren() throws KeeperException, InterruptedException {
    	return zk.getChildren(root, false).size();
    }
    
    public ZooKeeper getZK() {
    	return zk;
    }
    
    /**
     * Join barrier
     *
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    String enter(int sleepTime) throws KeeperException, InterruptedException{
        String node_path = zk.create(root + "/node", new byte[0], Ids.OPEN_ACL_UNSAFE,
                					 CreateMode.EPHEMERAL_SEQUENTIAL);
        while (true) {
        	Thread.sleep(sleepTime);
            synchronized (mutex) {
                List<String> list = zk.getChildren(root, true);

                if (list.size() < size) {
                    mutex.wait();
                } else {
                    return node_path;
                }
            }
        }
    }

    /**
     * Wait until all reach barrier
     *
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    void leave(String node_path) throws KeeperException, InterruptedException{
        zk.delete(node_path, 0);
        
        while (true) {
            synchronized (mutex) {
                List<String> list = zk.getChildren(root, true);
                
                System.out.println("Tentativa de sair, ainda há " + list.size() + " nós filhos");
                
                if (list.size() > 0) {
                    mutex.wait();
                } else {
                    return;
                }
            }
        }
    }
}