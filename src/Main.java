import java.io.IOException;

import org.apache.zookeeper.KeeperException;

class Main {
	public static void main(String[] args) {
    	if (args.length < 2)
    	{
    		System.out.println("Por favor especifique a quantidade total de times que tentarão participar e o total de times permitidos no campeonato, nesta ordem, como parâmetros, e.g.:");
    		System.out.println("java -jar barreira.jar 10 5");
    		
    		return;
    	}
		
		try
    	{
    		barrierTest("localhost:2181", Integer.parseInt(args[1]), Integer.parseInt(args[0]));
    	}
    	catch (Exception e) {
    		System.out.println("Exceção: " + e.toString());
    	}
    }
    
    private static void barrierTest(String host, int max_teams, int total_teams) throws KeeperException, InterruptedException, IOException {
    	DoubleBarrier campeonato = new DoubleBarrier(host, "/championship", max_teams);
    	
    	if (campeonato.enter() == false) {
    		System.out.println("Não consegui entrar no campeonato :(");
    		return;
    	}
    	
    	DoubleBarrier round = new DoubleBarrier(host, "/round", max_teams);

    	for (int i = 1; i <= 2; ++i) {
    		System.out.println("Rodada: " + i);
	    	
	    	round.enter();
	    	System.out.println("Estou participando da " + (i == 1 ? "primeira" : "segunda") + " rodada!");
	    	
	    	round.leave();
	    	System.out.println("Rodada acabou!");
    	}
    	
    	campeonato.leave();
    }
}
