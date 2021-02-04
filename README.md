Projeto usado para procurar todas as URL's dentro de uma pagina que pertencem ao Host informado.

O projeto necessita de dois parametros enviados na variável de ambiente

●
BASE_URL: Determina a URL base do website em que as buscas devem ser feitas pela
aplicação. Seu efeito é duplo: 1) delimita o escopo da análise (apenas links cujas URLs iniciam
com o valor da URL base devem ser visitados); 2) determina a página inicial a ser visitada ao
executar uma análise. Sua definição é obrigatória. O valor deve conter uma URL (HTTP ou
HTTPS) válida e absoluta de acordo com a implementação da classe ​ java.net.URI​ .
●
MAX_RESULTS:​ Determina o número máximo de resultados que devem ser retornados por uma
análise. Sua definição é opcional. O valor deve representar um número inteiro igual a -1
(indicando limite não definido) ou maior do que 0. Quando o valor não é especificado, ou é
especificado um valor inválido, a aplicação deve assumir o valor ​ default​ -1. Quando uma análise
atinge o limite de resultados, ela deve ser concluída, deixando de visitar novos links
encontrados.

Executar via docker
docker build . -t search/backend
docker run -e BASE_URL=https://www.kernel.org/doc/man-pages/ -p 4567:4567 --rm search/backend

