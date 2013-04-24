import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;


/**
 * Barrier
 */
public class DoubleBarrier extends SyncPrimitive {
    private int size;
    private String nodePath;

    /**
     * Barrier constructor
     *
     * @param address
     * @param root
     * @param size
     * @throws InterruptedException 
     * @throws KeeperException 
     * @throws IOException 
     */
    public DoubleBarrier(String address, String root, int size) throws KeeperException, InterruptedException, IOException {
        super(address);
        this.root = root;
        this.size = size;

        // Create barrier node (or not)
        try {
            zk.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE,
                    CreateMode.PERSISTENT);
        }
        catch (KeeperException e) {
        	if (e.code() != KeeperException.Code.NODEEXISTS)
        		throw e;
        }
    }
    
    /*
    public int countChildren() throws KeeperException, InterruptedException {
    	return zk.getChildren(root, false).size();
    }
    
    
    public ZooKeeper getZK() {
    	return zk;
    }*/
    
    private String oldestNode(List<String> nodes) {
    	int smallestNumber = Integer.MAX_VALUE;
    	String oldestNode = null;
    	for (String node : nodes) {
    		String node_str_no = node.split("_")[1];
    		
    		int node_no = Integer.parseInt(node_str_no); 
    		if (node_no < smallestNumber)
    		{
    			smallestNumber = node_no;
    			oldestNode = node;
    		}
    	}
    	
    	return root + "/" + oldestNode;
    }
    
    private void authorizeLeave() throws KeeperException, InterruptedException {
    	byte[] data = new byte[1];
    	data[0] = (byte)1;
    	zk.setData(root, data, 0);
    }
    
    private boolean isLeaveAuthorized() throws KeeperException, InterruptedException {
    	byte[] data = zk.getData(root, true, null);
    	if (data == null)
    		return false;
    	else if (data.length == 0)
    		return false;
    	
    	return data[0] == (byte)1;
    }
    
    /**
     * Join barrier
     *
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    void enter() throws KeeperException, InterruptedException{
        nodePath = zk.create(root + "/node_", new byte[0], Ids.OPEN_ACL_UNSAFE,
                					 CreateMode.EPHEMERAL_SEQUENTIAL);
        boolean isLeader = false;
        while (true) {
            List<String> list = zk.getChildren(root, true);
            String oldest_node = oldestNode(list);
            isLeader = oldest_node.equals(nodePath);

            // A saída está autorizada?
            boolean authorized_to_leave = isLeaveAuthorized();
            
            if (authorized_to_leave)
            {
            	System.out.println("Nó barreira marcado com saída autorizada");
            	return;
            }
            else if (list.size() < size)
            {
            	synchronized (mutex) {
            		mutex.wait(200);
            	}
            }
            else
            {
            	
            	// Se a lista de nós chegou à contagem necessária e este nó é o líder, autorizar a saída e sair do método!
            	if (isLeader)
            	{
            		System.out.println("Sou líder! Estou autorizando a saída.");
            		authorizeLeave();
            		return;
            	}
            	
            	// Se chegamos à contagem esperada mas não somos líder, devemos esperar o líder autorizar a saída
            	else
            	{
            		synchronized (mutex) {
            			mutex.wait(200);
            		}
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
    void leave() throws KeeperException, InterruptedException{
        zk.delete(nodePath, 0);
        
        System.out.println("Apaguei meu próprio nó e estou esperando todos fazerem o mesmo.");
        
        while (true) {
            List<String> list = null;
            try
            {
            	list = zk.getChildren(root, true);
            }
            catch (KeeperException e)
            {
            	// Se o nó raíz não existe mais, é hora de sair!
            	if (e.code() == KeeperException.Code.NONODE)
            	{
            		System.out.println("Saí.");
            		return;
            	}
            }
            
            if (list.size() > 0) {
            	synchronized (mutex) {
            		mutex.wait(250);
            	}
            } else {
            	// Apaga o barrier node!
            	try
            	{
            		zk.delete(root, -1);
            	}
            	catch (KeeperException e)
            	{
            		if (e.code() != KeeperException.Code.NONODE)
            			throw e;
            	}
            	
            	System.out.println("Saí.");
                return;
            }
        }
    }
}