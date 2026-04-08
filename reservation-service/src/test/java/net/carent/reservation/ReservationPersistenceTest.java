package net.carent.reservation;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import net.carent.reservation.entity.Reservation;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class ReservationPersistenceTest {

  @Test
  @Transactional
  public void testCreateReservation() {
    var reservation = new Reservation();
    reservation.startDay = LocalDate.now().plus(5, ChronoUnit.DAYS);
    reservation.endDay = LocalDate.now().plus(12, ChronoUnit.DAYS);
    reservation.carId = 42L;
    reservation.persist();

    assertNotNull(reservation.id);
    assertEquals(1, Reservation.count());

    Reservation persisted = Reservation.findById(reservation.id);
    assertNotNull(persisted);
    assertEquals(reservation.carId, persisted.carId);
  }
}
