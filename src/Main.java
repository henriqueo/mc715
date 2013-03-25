import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Main {
	private static ExecutorService threadPool = Executors.newCachedThreadPool();
	
	public static void main(String[] args) {
    	try
    	{
    		barrierTest("localhost:2181", 5);
    	}
    	catch (Exception e) {
    		System.out.println("Exceção: " + e.toString());
    	}
    }
    
    private static void barrierTest(String host, int barrier_size) throws KeeperException, InterruptedException {
        System.out.println("Criando barreira");
    	Barrier b = new Barrier(host, "/barriernode", barrier_size);
    	
    	System.out.println("Criando barreira de " + barrier_size);
    	
    	ConcurrentSimpleList<String> nodes = new ConcurrentSimpleList<String>();
    	
    	// Entrar assincronamente com todos menos um e delay alto
    	for (int i = 0; i < barrier_size - 1; i++) {
    		enterNodeAsync(b, nodes, 1500);
    	}
    	
    	// Entra rapidamente com um nó e sai!
    	String last_node = b.enter(0);
    	System.out.println("entrou e vai pedir pra sair, entrando em DEAD LOCK");
    	b.leave(last_node);
    	
    	System.out.println("ESTE CÓdigo não é executado!");
    	
    	// Agora vamos tentar sair com todos
    	/*for (int i = 0; i < barrier_size - 1; i++) {
    		leaveNodeAsync(b, nodes, nodes.get(i));
    	}*/
    	
		// Mesmo 2 segundos depois, os outros nós ainda estão lá..
		/*Thread.sleep(2000);
		System.out.println();
		System.out.println("2 segundos depois de " + barrier_size + " nós terem entrado e todos terem saído ainda há " + nodes.size() + " nós que não saíram de fato.");*/
    }
    
    private static void enterNodeAsync(Barrier b, ConcurrentSimpleList<String> nodeList, int timeOut) throws KeeperException, InterruptedException {
    	// Rodar numa background thread
    	threadPool.submit(new BarrierEnterRunnable(b, nodeList, timeOut));
    }
    
    private static void leaveNodeAsync(Barrier b, ConcurrentSimpleList<String> nodeList, String nodeName) throws KeeperException, InterruptedException {
    	// Rodar numa background thread
    	threadPool.submit(new BarrierLeaveRunnable(b, nodeList, nodeName));
    }
}
