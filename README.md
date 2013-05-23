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
	
	
