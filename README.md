# ERP Gestor Inteligente

**ERP Gestor Inteligente** é um sistema de gestão empresarial desenvolvido em Java com JavaFX, projetado para pequenas e médias empresas. O software oferece módulos para controle de vendas, gestão de estoque, cadastro de produtos e fornecedores, e geração de relatórios, utilizando uma arquitetura modular e escalável.

## Módulos do Sistema

- **Gestão de Vendas**  
  Controle completo de vendas, incluindo busca de produtos, carrinho dinâmico, aplicação de descontos e cálculo automático de totais.

- **Gestão de Estoque**  
  Gerenciamento em tempo real de entradas e saídas, alertas de reposição e controle de movimentações.

- **Cadastro de Produtos e Fornecedores**  
  Interface intuitiva para adicionar, editar e excluir produtos e fornecedores, com suporte à categorização e pesquisa avançada.

- **Relatórios e Análise**  
  Geração de relatórios detalhados e gráficos interativos para análise de vendas por períodos.

- **Pagamentos**  
  Tela dedicada para registro de pagamentos com suporte a múltiplos métodos e bloqueio de interação com outras janelas.

## Arquitetura do Projeto

- **Frontend**: Desenvolvido em **JavaFX**, com FXML para separação de design e lógica.  
- **Backend**: Java com aplicação de padrões de projeto como Strategy e Factory para modularidade e reuso de código.  
- **Banco de Dados**: PostgreSQL, com conexão gerenciada por **HikariCP** para alto desempenho.  
- **Relatórios**: Utiliza **ChartFX** para gráficos dinâmicos e **JasperReports** para relatórios detalhados.  
- **Camadas**:  
  - **Controller**: Gerencia a interação entre a interface e os serviços.  
  - **Service**: Regras de negócio e validações.  
  - **Repository**: Interação com o banco de dados utilizando o padrão Repository.

## Como Executar

1. Clone o repositório:
   ```bash
   git clone https://github.com/usuario/erp-gestor-inteligente.git
