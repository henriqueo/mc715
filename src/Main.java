import java.io.IOException;

import org.apache.zookeeper.KeeperException;

class Main {
	public static void main(String[] args) {
    	if (args.length < 2)
    	{
    		System.out.println("Por favor especifique o tamanho da barreira e o total de processos tentando utilizá-la, nesta ordem, como parâmetros, e.g.:");
    		System.out.println("java -jar barreira.jar 3 5");
    		
    		return;
    	}
		
		try
    	{
    		barrierTest("localhost:2181", Integer.parseInt(args[0]), Integer.parseInt(args[1]));
    	}
    	catch (Exception e) {
    		System.out.println("Exceção: " + e.toString());
    	}
    }
    
    private static void barrierTest(String host, int barrier_size, int total_procs) throws KeeperException, InterruptedException, IOException {
    	DoubleBarrier b = new DoubleBarrier(host, "/barriernode", barrier_size);
    	SimpleBarrier syncBarrier = new SimpleBarrier(host, "/syncingbarrier", total_procs);
    	
    	for (int i = 1; i <= 7; ++i) {
    		System.out.println("PARTIDA: " + i);
	    	System.out.print ("- Vou correndo pegar um controle... ");
	    	
	    	boolean got_in_barrier = b.enter();
	    	if (got_in_barrier == false) {
	    		System.out.println("Droga! Cheguei tarde e fiquei de fora, vou esperar terminar o jogo.");
	    	} else {
	    		System.out.println("Consegui!");
	    	}
	    	
	    	System.out.println("NARRADOR: Os " + barrier_size + " jogadores jogam uma partida..");
	    	
	    	if (got_in_barrier)
	    		b.leave();
	    	
	    	System.out.println("- Acabou a partida! Vamos à competição pelos controles novamente!\n\n");
	    	syncBarrier.Wait();
    	}
    }
}
