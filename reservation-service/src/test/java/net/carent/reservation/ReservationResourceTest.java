package net.carent.reservation;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import net.carent.reservation.inventory.GraphQLInventoryClient;
import net.carent.reservation.model.Car;
import net.carent.reservation.entity.Reservation;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.URL;
import java.time.LocalDate;
import java.util.Collections;

import static org.hamcrest.Matchers.*;

@QuarkusTest
public class ReservationResourceTest {
  @TestHTTPEndpoint(ReservationResource.class)
  @TestHTTPResource
  URL underTest;

  @TestHTTPEndpoint(ReservationResource.class)
  @TestHTTPResource("availability")
  URL availability;

  @Test
  public void testMakingAReservationAndCheckAvailability() {
    GraphQLInventoryClient mock = Mockito.mock(GraphQLInventoryClient.class);

    Car peugeot = new Car(42L, "XYZ-123", "Peugeot", "406");

    Mockito.when(mock.allCars()).thenReturn(Collections.singletonList(peugeot));

    QuarkusMock.installMockForType(mock, GraphQLInventoryClient.class);

    String startDate = "2026-04-06";
    String endDate = "2026-04-16";

    Car[] cars = RestAssured.given()
      .queryParam("startDate", startDate)
      .queryParam("endDate", endDate)
      .when()
      .get(availability)
      .then()
      .statusCode(200)
      .extract()
      .as(Car[].class);

    Car car = cars[0];

    Reservation reservation = new Reservation();
    reservation.carId = car.id;
    reservation.startDay = LocalDate.parse(startDate);
    reservation.endDay = LocalDate.parse(endDate);

    RestAssured.given()
      .contentType(ContentType.JSON)
      .body(reservation)
      .when()
      .post(underTest)
      .then()
      .statusCode(200)
      .body("carId", is(car.id.intValue()));

    RestAssured.given()
      .queryParam("startDate", startDate)
      .queryParam("endDate", endDate)
      .when()
      .get(availability)
      .then()
      .statusCode(200)
      .body("findAll { car -> car.id == " + car.id + " }", hasSize(0));

  }

  @Test
  public void testReservationIds() {
    var reservation = new Reservation();
    reservation.carId = 12345L;
    reservation.startDay = LocalDate.parse("2026-04-06");
    reservation.endDay = LocalDate.parse("2026-04-26");

    RestAssured
      .given()
      .contentType(ContentType.JSON)
      .body(reservation)
      .when()
      .post(underTest)
      .then()
      .statusCode(200)
      .body("id", notNullValue());
  }
}
