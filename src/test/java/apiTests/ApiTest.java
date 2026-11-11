package apiTests;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class ApiTest {
    private RequestSpecification spec;

    @BeforeEach
    void setUp() {
        spec = new RequestSpecBuilder()
                .setBaseUri("http://localhost:9999")
                .setContentType(ContentType.JSON)
                .build();
    }

    @Test
    void shouldPostRequestWithApprovedCardToPaymentGateAndGetApprovedStatus() {
        given().spec(spec)
                .body(Map.of("number", "4444 4444 4444 4441"))
                .when().log().all()
                .post("/payment")
                .then().log().all()
                .statusCode(200).body("status", equalTo("APPROVED"));
    }

    @Test
    void shouldPostRequestWithDeclinedCardToPaymentGateAndGetDeclinedStatus() {
        given().spec(spec)
                .body(Map.of("number", "4444 4444 4444 4442"))
                .when().log().all()
                .post("/payment")
                .then().log().all().statusCode(200).body("status", equalTo("DECLINED"));
    }

    @Test
    void shouldPostRequestWithApprovedCardToCreditGateAndGetApprovedStatus() {
        given().spec(spec).body(Map.of("number", "4444 4444 4444 4441"))
                .when().log().all()
                .post("/credit")
                .then().log().all()
                .statusCode(200).body("status", equalTo("APPROVED"));
    }

    @Test
    void shouldPostRequestWithDeclinedCardToCreditGateAndGetDeclinedStatus() {
        given().spec(spec)
                .body(Map.of("number", "4444 4444 4444 4442"))
                .when().log().all()
                .post("/credit")
                .then().log().all().statusCode(200).body("status", equalTo("DECLINED"));
    }

    @Test
    void shouldGetStatus400IfCardNumberInNonStandardFormatting() {
        given().spec(spec)
                .body(Map.of("number", "4444444444444441"))
                .when().log().all()
                .post("/credit")
                .then().log().all().statusCode(400);
    }
}
