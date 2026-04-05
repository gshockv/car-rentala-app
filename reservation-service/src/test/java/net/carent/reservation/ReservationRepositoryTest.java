package net.carent.reservation;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import net.carent.reservation.model.Reservation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@QuarkusTest
public class ReservationRepositoryTest {
  @Inject
  ReservationsRepository underTest;

  @Test
  public void testCreateReservation() {
    var reservation = new Reservation();
    reservation.startDay = LocalDate.now().plus(5, ChronoUnit.DAYS);
    reservation.endDay = LocalDate.now().plus(12, ChronoUnit.DAYS);
    reservation.carId = 42L;
    underTest.save(reservation);

    Assertions.assertNotNull(reservation.id);
    Assertions.assertTrue(underTest.findAll().contains(reservation));
  }
}
