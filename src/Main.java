import java.io.IOException;

import org.apache.zookeeper.KeeperException;

class Main {
	public static void main(String[] args) {
    	if (args.length == 0)
    	{
    		System.out.println("Por favor especifique o tamanho da barreira como parâmetro, e.g.:");
    		System.out.println("java -jar barreira.jar 3");
    		
    		return;
    	}
		
		try
    	{
    		barrierTest("localhost:2181", Integer.parseInt((args[0])));
    	}
    	catch (Exception e) {
    		System.out.println("Exceção: " + e.toString());
    	}
    }
    
    private static void barrierTest(String host, int barrier_size) throws KeeperException, InterruptedException, IOException {
    	System.out.println("Criando ou entrando em barreira de tamanho " + barrier_size);
    	DoubleBarrier b = new DoubleBarrier(host, "/barriernode", barrier_size);
    	
    	System.out.println("- Eu depositei o dinheiro para o aluguel.");
    	
    	b.enter();
    	
    	System.out.println("- Chama o bixo pra ir pagar a conta que o aluguel está completo, e manda ele voltar que o filme vai começar.");
    	
    	b.leave();
    	
    	System.out.println("- Todos chegaram, liga a TV!");
    }
}
