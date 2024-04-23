# Configurações do Projeto

## 1 - Execução do sistema

### Urls Base:
* `http://localhost:8080/orders` - Pedidos e itens de pedidos
* `http://localhost:8080/items`  - Itens(Produto/Serviço)
* `http://localhost:8080/swagger-ui/index.html`  - Documentação e funcionalidades do projeto

### Executar sistema via docker siga os passos abaixo:
* 1 - Na raiz do projeto execut: `mvn clean package` aguarde finalizar.
* 2 - Na raiz do projeto execute: `docker-compose up` quando finalizar verá a informação no console que o sistema subiu

### Executar manualmente requisitos:

* Tenha o postgreSQL instalado versão recomendada 15
* Para configurar o banco de dados olhe a seção 3
* Versão Java JDK 17
* Após os passos feitos compile e execute o projeto

## 2 - Documentação do sistema
* Para o projeto foi usado o swagger para documentar todos os endpoints, seguindo padrões de documentação e melhorando 
usabilidade para possíveis testes manuais.
* Todas as funcionalidades e regras estão descritas em cada recurso(endpoint) no swagger.
* Para acessar o swagger e ver informações completas do projeto, primeiro execute o sistema: `mvn spring-boot:run
  ` e acesse o endereço: `http://localhost:8080/swagger-ui/index.html`

## 3 - Configurações do banco de dados
#### O Sistema irá buscar variáveis de ambiente que representam configurações do data source, caso não encontre será usado os valores padrões:
* `POSTGRES_USER`: Nome de usuário do PostgreSQL (padrão: `postgres`)
* `POSTGRES_PASSWORD`: Nome de usuário do PostgreSQL (padrão: `postgres`)
* `POSTGRES_HOST`: Nome de usuário do PostgreSQL (padrão: `127.0.0.1`)
* `POSTGRES_PORT`: Nome de usuário do PostgreSQL (padrão: `5432`)
* Nome do banco de dados que o sistema se conecta: `assessment`

## 4 - Executar Testes
O projeto conta com testes unitários e de integração, para executar todos os testes é necessário ter instalado o 
docker na máquina, pois utilizo a dependência: `testcontainers`, somente é necessário o docker, a gestão do 
container é feita por configurações do projeto
* Comando para executar teste: `mvn verify`