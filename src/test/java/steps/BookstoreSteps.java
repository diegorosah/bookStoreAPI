package steps;

import org.apache.commons.lang3.RandomStringUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import org.json.JSONObject;

import io.cucumber.java.After;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;

public class BookstoreSteps {

    private static final String BASE_URI = "https://bookstore.toolsqa.com/Account/v1";
    private String userId;
    private String token;
    private String username;
    private String password;

    private void setupBaseURI() {
        RestAssured.baseURI = BASE_URI;
    }

    @Given("que crio um usuário {string} com a senha {string}")
    public void createUser(String username, String password) {
        this.username = username;
        this.password = password;

        createUserWithCredentials();
    }

    @Given("que crio um usuário aleatório")
    public void createRandomUser() {
        this.username = generateRandomUsername();
        this.password = generateRandomPassword();

        createUserWithCredentials();
    }

    private void createUserWithCredentials() {
        setupBaseURI();

        JSONObject requestBody = new JSONObject();
        requestBody.put("userName", username);
        requestBody.put("password", password);

        try {
            Response response = RestAssured.given().contentType("application/json").accept("application/json")
                    .body(requestBody.toString()).when().post("/User").then().statusCode(201).extract().response();

            userId = response.jsonPath().getString("userID");
            assertThat(userId, is(not(emptyString())));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar usuário", e);
        }
    }

    @When("gerar um token para o usuário criado")
    public void generateToken() {
        setupBaseURI();

        JSONObject requestBody = new JSONObject();
        requestBody.put("userName", username);
        requestBody.put("password", password);

        try {
            Response response = RestAssured.given().contentType("application/json").body(requestBody.toString()).when()
                    .post("/GenerateToken").then().statusCode(200).extract().response();

            token = response.jsonPath().getString("token");
            assertThat(token, is(not(emptyString())));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar token", e);
        }
    }

    @When("buscar os detalhes do usuário")
    public void getUserDetails() {
        setupBaseURI();

        try {
            Response response = RestAssured.given().header("Authorization", "Bearer " + token).when()
                    .get("/User/" + userId).then().statusCode(200).extract().response();

            String retrievedUserId = response.jsonPath().getString("userId");
            assertThat(retrievedUserId, is(equalTo(userId)));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar detalhes do usuário", e);
        }
    }

    @Then("validar que o usuário foi criado com sucesso")
    public void userCreatedSuccessfully() {
        assertThat(userId, is(not(emptyString())));
    }

    @Then("validar que o token foi gerado com sucesso")
    public void tokenGeneratedSuccessfully() {
        assertThat(token, is(not(emptyString())));
    }

    @Then("validar que recuperei os dados do usuário com sucesso")
    public void userDetailsRetrievedSuccessfully() {
        assertThat(userId, is(not(emptyString())));
    }

    @After
    public void afterScenario(Scenario scenario) {
        if (scenario.isFailed()) {
            System.out.println("Cenário falhou! Removendo usuário criado...");
        } else {
            System.out.println("Cenário passou! Removendo usuário criado...");
        }

        removeCreatedUser();
    }

    private void removeCreatedUser() {
        setupBaseURI();

        try {
            if (token == null) {
                token = generateNewToken();
            }

            Response response = RestAssured.given().header("Authorization", "Bearer " + token).when()
                    .delete("/User/" + userId).then().statusCode(204).extract().response();

            int statusCode = response.statusCode();
            assertThat(statusCode, is(equalTo(204)));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao remover usuário", e);
        }
    }

    private String generateRandomUsername() {
        String usuario = RandomStringUtils.randomAlphanumeric(8).toLowerCase();
        System.out.println("usuário gerado: " + usuario);
        return usuario;
    }

    private String generateRandomPassword() {
        String numeros = "0123456789";
        // Gerar quatro números aleatórios
        String quatroNumeros = RandomStringUtils.random(4, numeros);

        String senha = "Teste@" + quatroNumeros;
        System.out.println("senha gerada: " + senha);
        return senha;
    }

    private String generateNewToken() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("userName", username);
        requestBody.put("password", password);

        try {
            Response response = RestAssured.given().contentType("application/json").body(requestBody.toString()).when()
                    .post("/GenerateToken").then().statusCode(200).extract().response();

            return response.jsonPath().getString("token");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar token", e);
        }
    }
}
