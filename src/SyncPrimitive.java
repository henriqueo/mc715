import java.io.IOException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public class SyncPrimitive implements Watcher {
    protected static ZooKeeper zk = null;
    protected static Integer mutex;

    protected String root;

    SyncPrimitive(String address) throws IOException {
        if(zk == null){
    		zk = new ZooKeeper(address, 3000, this);
            mutex = new Integer(-1);
        }
    }

    synchronized public void process(WatchedEvent event) {
        synchronized (mutex) {
            mutex.notify();
        }
    }
}