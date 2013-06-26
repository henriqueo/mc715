MC715 - LABORATORIO DE SISTEMAS DISTRIBUIDOS - PARTE 5 (FINAL)

1. Para esta etapa a ideia seria realizar uma aplicacao utilizando as caracteristicas de barreiras desenvolvidas nas etapas anteriores.

2. Campeonato
	Utilizamos barreiras aninhadas para simular um campeonato, diversos times tentariam participar do mesmo, sendo que somente alguns conseguiriam uma vaga. Para isso utilizou-se uma barreira externa (restrita) para se garantir que uma quantidade limitada de times participasse da competicao.
	Internamente foram utilizadas duas barreiras duplas que simulavam o primeiro e segundo turno do torneio, logo um time que ainda nao finalizou o primeiro turno nao poderia iniciar o segundo turno da competicao.

3. Todas as alteracoes foram realizadas no arquivo Main.java ja que as classes de barreiras e suas caracteristicas ja haviam sido implementadas.


MC 715 - LABORATÓRIO DE SISTEMAS DISTRIBUÍDOS - PARTE 3 e 4

1. INTRODUÇÃO

	Na parte 1 utilizamos o código já implementado da barreira, explicitando um dos bugs contidos no código. Este se referia a um caso específico, onde tínhamos uma barreira simples, e vários processos entravam nela.
	O último processo entrou na barreira, percebeu que atingiu o limite de processos da barreira e pediu para sair, apagando seu próprio nó. Com isso, entramos em um deadlock, onde o último processo está esperando os outros nós pedirem para sair, e os outros nós, por não terem percebido a existência do último processo, estão a espera dele.
	Isto foi resolvido na parte 2 do trabalho, onde implementamos uma barreira simples e uma barreira dupla, que, utilizando um líder, controla a saída dos elementos.

2. OBJETIVO

	Na parte 3 do trabalho, faremos com que nossa barreira seja reutilizável, ou seja, possa ser reaproveitada para outras instâncias.
	Na parte 4 do trabalho, faremos com que nossa barreira seja restrira, ou seja, não haja espaço para todos os processos entrarem nela, precisando criar uma “fila de espera”.


3. METODOLOGIA

	Para tornar nossa barreira reutilizável foi relativamente simples, pois, em nosso código original, simplesmente destruíamos a barreira ao final da execução.
	Ao invés de destruí-la, vamos agora reiniciar o lider_approval para false, e deletar todos os nós filhos de /barrier, deixando apenas o /barrier com lider_approval = false, sendo este o estado inicial da barreira.
	Para tornar nossa barreira restrita, devemos nos preocupar com alguns casos em específico:
	1) size+1 processo dá enter, mas o líder já autorizou a saída.
		Este é exatamente o caso mostrado no slide sobre barreiras restritas (http://www.ic.unicamp.br/~islene/1s2013-mc715/barreiras.pdf). Para solucionarmos este problema, verificaremos, ANTES DE CRIAR O NÓ, se o líder já autorizou a saída. Se sim, simplesmente o nó retorna, sem criar o seu nó. Caso contrário, ainda temos algumas condições de corridas a serem verificadas.
2) size+1 processo dá enter, e o líder não autorizou a saída.
Neste caso, pode ser que o número de processos já tenha atingido o máximo, mas o líder ainda não teve tempo de autorizar a saída. Para solucionar este caso, vamos verificar o número de processos. Caso seja size, o processo em questão simplesmente retorna. Caso contrário, continua o processamento.
Neste ponto há ainda mais uma condição de corrida, onde, antes dessa verificação de número de children >= size, o líder tenha conseguido dar o approval e um processo tenha saído da barreira. Neste caso, o tamanho vai ser menor que size, então precisamos verificar novamente se o líder autorizou a saída de processos.

	3) caso todos os processos entrem ao mesmo tempo, i.e., 5 processos entrando numa barreira de 4, exatamente ao mesmo tempo.
		Neste caso, pode ser que todas as condições nos casos anteriores sejam cumpridas, e todos os 5 processos criem seus nós. Assim, devemos fazer uma verificação de número de nós criados logo após no zk.create, para que, se houver mais de 4 nós, todos os que tiverem 4 nós com id menores que as deles serem deletados, restando sempre apenas 4 nós.
	Desta forma, com esses ajustes, conseguimos implementar uma barreira reutilizável e restrita.

4. PROBLEMAS NA IMPLEMENTAÇÃO

	Na apresentação passada foi descoberto um bug no nosso programa, que solucionamos nesta etapa. O bug acontecia quando um processo entrava enquanto os outros processos saíam. Isso foi resolvido no caso 1) desta etapa.
	Também tivemos alguns problemas com a entrada de mais processos do que o limite da barreira, mas isso foi resolvido com o caso 3) desta etapa.

MC 715 - LABORATÓRIO DE SISTEMAS DISTRIBUÍDOS - PARTE 2

1. INTRODUÇÃO

	Na parte 1 utilizamos o código já implementado da barreira, explicitando um dos bugs contidos no código. Este se referia a um caso específico, onde tínhamos uma barreira simples, e vários processos entravam nela.
	O último processo entrou na barreira, percebeu que atingiu o limite de processos da barreira e pediu para sair, apagando seu próprio nó. Com isso, entramos em um deadlock, onde o último processo está esperando os outros nós pedirem para sair, e os outros nós, por não terem percebido a existência do último processo, estão a espera dele.

2. OBJETIVO

	Vamos tentar solucionar esse deadlock na parte 2 do trabalho, e implementar uma barreira simples e uma barreira dupla.

3. METODOLOGIA

	Para solucionar este problema, precisamos basicamente sincronizar a saída dos processos.
	Na nossa abordagem, vamos utilizar um líder, que, no caso, vai ser o primeiro elemento a entrar na barreira.
	Esse líder vai controlar a saída dos processos da nossa barreira, visto que o bug é causado exatamente pelo fato de existir falta de sincronização entre os elementos. 
	O nosso líder vai verificar sempre o número de elementos na barreira, assim como o código já tinha implementado. Porém, ao perceber que o número de processos já está no limite, ele ativa um booleano, chamado de lider_approval, para true.
	Cada um dos processos que entram depois do líder vai ter uma implementação levemente diferente da dele. Simplesmente vamos colocar uma condição para que:
-Quando lider_approval == true, eles podem sair da barreira, senão, eles ficam esperando.
	Com isso, evitamos que um nó inserido por último saia antes dos outros saberem de sua existência.
	
4. PROBLEMAS NA IMPLEMENTAÇÃO

	Ao analisarmos mais a fundo o problema, percebemos que mesmo sendo aparentemente uma boa solução, ainda há uma falha.
	Apesar de ser bem improvável, podemos considerar o seguinte:
	Um dos processos passa pela verificação de número de processos, mas, antes de entrar no mutex(wait), o líder verifica que existem todos os processos dentro da barreira, e sai. Com isso, esse processo em questão não receberia a autorização do líder, e o líder já teria saído da barreira. Entraríamos em um novo deadlock.
	A solução para este problema é relativamente simples, colocaremos um timeout para o mutex(wait), de, digamos, 200 ms. Com isso, sempre que o processo ficar sem resposta durante 200 ms, ele atualiza suas informações. Assim, ao ficar 200 ms sem informações, o processo vai atualizar a autorização do líder, e vai perceber que, mesmo sem o líder, ele passou pela barreira.
	Dessa forma, resolvemos o problema desse deadlock.

	Outra propriedade interessante da nossa implementação é que, mesmo que o líder teoricamente seja o primeiro a sair, isso não acontece sempre. (e isso não vai ser um empecilho). Pode acontecer do líder setar a autorização, e nesse tempo entre autorizar e sair, um outro processo receber a autorização e sair antes do líder. Mas como nosso lider ja setou a autorização, e temos timeout em todos os processos, não entraremos em deadlock.
	
	
