Feature: Bookstore API Tests / Gestão de Usuários

  Scenario: Validar a criação de usuário
    Given que crio um usuário "testeBookStore" com a senha "Teste@123"
    Then validar que o usuário foi criado com sucesso

  Scenario: Validar geração de token do usuário
    Given que crio um usuário aleatório
    When gerar um token para o usuário criado
    Then validar que o token foi gerado com sucesso

  Scenario: Validar recuperação de dados do usuário
    Given que crio um usuário aleatório
    And gerar um token para o usuário criado
    When buscar os detalhes do usuário
    Then validar que recuperei os dados do usuário com sucesso
