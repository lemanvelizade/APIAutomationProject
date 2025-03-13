package apiTests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class RestfulBookerApiTests {

    private String token;

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = "http://restful-booker.herokuapp.com";
        generateToken();
    }

    private void generateToken() {
        Map<String, String> authPayload = new HashMap<>();
        authPayload.put("username", "admin");
        authPayload.put("password", "password123");
        Response response = sendPostRequest("/auth", authPayload);
        token = response.jsonPath().getString("token");
    }

    private Response sendGetRequest(String endpoint) {
        Response response = given().when().get(endpoint).then().extract().response();
        System.out.println("GET Response: " + response.asString());
        return response;
    }

    private Response sendPostRequest(String endpoint, Object body) {
        Response response = given()
                .header("Content-Type", "application/json")
                .body(body)
                .when()
                .post(endpoint)
                .then()
                .extract()
                .response();
        System.out.println("POST Response: " + response.asString());
        return response;
    }

    private Response sendPutRequest(String endpoint, Object body) {
        Response response = given()
                .header("Content-Type", "application/json")
                .header("Cookie", "token=" + token)
                .body(body)
                .when()
                .put(endpoint)
                .then()
                .extract()
                .response();
        System.out.println("PUT Response: " + response.asString());
        return response;
    }

    private Response sendPatchRequest(String endpoint, Object body) {
        Response response = given()
                .header("Content-Type", "application/json")
                .header("Cookie", "token=" + token)
                .body(body)
                .when()
                .patch(endpoint)
                .then()
                .extract()
                .response();
        System.out.println("PATCH Response: " + response.asString());
        return response;
    }

    private Response sendDeleteRequest(String endpoint) {
        Response response = given()
                .header("Cookie", "token=" + token)
                .when()
                .delete(endpoint)
                .then()
                .extract()
                .response();
        System.out.println("DELETE Response: " + response.asString());
        return response;
    }

    private void validateStatusCode(Response response, int expectedStatus) {
        Assert.assertEquals(response.getStatusCode(), expectedStatus, "Status code doesn't match");
    }

    @Test
    public void testGetBookingIds() {
        Response response = sendGetRequest("/booking");
        validateStatusCode(response, 200);
    }

    @Test
    public void testGetBooking() {
        Response response = sendGetRequest("/booking");
        validateStatusCode(response, 200);
        int bookingId = response.jsonPath().getInt("[0].bookingid");
        response = sendGetRequest("/booking/" + bookingId);
        validateStatusCode(response, 200);
    }

    @Test
    public void testCreateBooking() {
        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("firstname", "Jim");
        bookingData.put("lastname", "Brown");
        bookingData.put("totalprice", 111);
        bookingData.put("depositpaid", true);

        Map<String, String> bookingDates = new HashMap<>();
        bookingDates.put("checkin", "2025-06-01");
        bookingDates.put("checkout", "2025-06-05");
        bookingData.put("bookingdates", bookingDates);
        bookingData.put("additionalneeds", "Breakfast");

        Response response = sendPostRequest("/booking", bookingData);
        validateStatusCode(response, 200);
    }

    @Test
    public void testUpdateBooking() {
        Response response = sendGetRequest("/booking");
        int bookingId = response.jsonPath().getInt("[0].bookingid");
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("firstname", "UpdatedName");
        updateData.put("lastname", "UpdatedLast");
        updateData.put("totalprice", 200);
        updateData.put("depositpaid", true);

        Map<String, String> bookingDates = new HashMap<>();
        bookingDates.put("checkin", "2025-06-01");
        bookingDates.put("checkout", "2025-06-05");
        updateData.put("bookingdates", bookingDates);
        updateData.put("additionalneeds", "Breakfast");

        response = sendPutRequest("/booking/" + bookingId, updateData);
        validateStatusCode(response, 200);
    }

    @Test
    public void testPartialUpdateBooking() {
        Response response = sendGetRequest("/booking");
        int bookingId = response.jsonPath().getInt("[0].bookingid");
        Map<String, Object> partialUpdateData = new HashMap<>();
        partialUpdateData.put("firstname", "PartiallyUpdatedName");

        response = sendPatchRequest("/booking/" + bookingId, partialUpdateData);
        validateStatusCode(response, 200);
    }

    @Test
    public void testDeleteBooking() {
        Response response = sendGetRequest("/booking");
        int bookingId = response.jsonPath().getInt("[0].bookingid");
        response = sendDeleteRequest("/booking/" + bookingId);
        validateStatusCode(response, 201);
    }

    @Test
    public void testPing() {
        Response response = sendGetRequest("/ping");
        validateStatusCode(response, 201);
    }
}

