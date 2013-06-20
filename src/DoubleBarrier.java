import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
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
    
    private int getNodeSeqNumber(String nodePath) {
    	String node_str_no = nodePath.split("_")[1];
		
		return Integer.parseInt(node_str_no); 
    }
    
    private String oldestNode(List<String> nodes) {
    	int smallestNumber = Integer.MAX_VALUE;
    	String oldestNode = null;
    	for (String node : nodes) {
    		//String node_str_no = node.split("_")[1];
    		
    		int node_no = getNodeSeqNumber(node);
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
    	zk.setData(root, data, -1);
    }
    
    private void unauthorizeLeave() throws KeeperException, InterruptedException {
    	byte[] data = new byte[1];
    	data[0] = (byte)0;
    	zk.setData(root, data, -1);
    }
    
    private boolean isLeaveAuthorized() throws KeeperException, InterruptedException {
    	byte[] data = zk.getData(root, true, null);
    	if (data == null)
    		return false;
    	else if (data.length == 0)
    		return false;
    	
    	return data[0] == (byte)1;
    }
    
    private int numChildren() throws KeeperException, InterruptedException {
    	return zk.getChildren(root, true).size();
    }
    
    private List<String> getOrderedChildren() throws KeeperException, InterruptedException {
    	List<String> list = zk.getChildren(root, true);
    	
    	Collections.sort(list, new Comparator<String>() {
			@Override
			public int compare(String arg0, String arg1) {
				return getNodeSeqNumber(arg0) - getNodeSeqNumber(arg1);
			}
    	});
    	
    	return list;
    }
    
    /**
     * Join barrier
     *
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    boolean enter() throws KeeperException, InterruptedException{
    	boolean authorized_to_leave = isLeaveAuthorized();
    	
    	if (authorized_to_leave) {
    		// Não criamos nó, o pessoal está saindo
    		return false;
    	} else {
    		// Talvez a barreira já esteja cheia mas o líder ainda não tenha tido tempo
    		// de autorizar a saída. Para este caso, contar os filhos
    		if (numChildren() >= size)
    			return false;
    		
    		// Talvez o líder tenha tido tempo de autorizar e alguém tenha saído neste meio tempo. Neste caso, verificar
    		// novamente
    		if (isLeaveAuthorized())
    			return false;
    	}
    	
        nodePath = zk.create(root + "/node_", new byte[0], Ids.OPEN_ACL_UNSAFE,
                					 CreateMode.EPHEMERAL_SEQUENTIAL);
        
        // Em casos de execução simultânea de vários processos, precisamos olhar os nós criados e sumir
        // caso haja "size" ou mais nós menores que o nosso
        int thisNodeSeqNumber = getNodeSeqNumber(nodePath);
        List<String> list = getOrderedChildren();
        for (int i = 1; i <= list.size(); ++i) {
        	if (getNodeSeqNumber(list.get(i - 1)) < thisNodeSeqNumber && i == size) {
        		// Essa a deixa para desaparecer, chegamos depois dos "size" primeiros
        		zk.delete(nodePath, 0);
        		return false;
        	}
        }
        
        
        boolean isLeader = false;
        while (true) {
            list = zk.getChildren(root, true);
            String oldest_node = oldestNode(list);
            isLeader = oldest_node.equals(nodePath);

            // A saída está autorizada?
            authorized_to_leave = isLeaveAuthorized();
            
            if (authorized_to_leave)
            {
            	//System.out.println("Nó barreira marcado com saída autorizada");
            	return true;
            }
            else if (list.size() < size)
            {
            	synchronized (mutex) {
            		mutex.wait(200);
            	}
            }
            else if (list.size() == size)
            {
            	// Se a lista de nós chegou à contagem necessária e este nó é o líder, autorizar a saída e sair do método!
            	if (isLeader)
            	{
            		//System.out.println("Sou líder! Estou autorizando a saída.");
            		authorizeLeave();
            		return true;
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
        
        //System.out.println("Apaguei meu próprio nó e estou esperando todos fazerem o mesmo.");
        
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
            		//System.out.println("Saí.");
            		return;
            	}
            }
            
            if (list.size() > 0) {
            	synchronized (mutex) {
            		mutex.wait(250);
            	}
            } else {
            	/*// Apaga o barrier node!
            	try
            	{
            		zk.delete(root, -1);
            	}
            	catch (KeeperException e)
            	{
            		if (e.code() != KeeperException.Code.NONODE)
            			throw e;
            	}*/
            	// Bloqueia a saída da primeira etapa da barreira
            	unauthorizeLeave();
            	
            	//System.out.println("Saí.");
                return;
            }
        }
    }
}