package net.carent.reservation.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

import java.time.LocalDate;
import java.util.List;

@Entity
public class Reservation extends PanacheEntity {
  public Long id;
  public String userId;
  public Long carId;
  public LocalDate startDay;
  public LocalDate endDay;

  public boolean isReserved(LocalDate startDay, LocalDate endDay) {
    return (!(this.endDay.isBefore(startDay) || this.startDay.isAfter(endDay)));
  }

  public static List<Reservation> findByCar(Long carId) {
    return list("carId", carId);
  }

  @Override
  public String toString() {
    return "Reservation{" +
      "id=" + id +
      ", userId='" + userId + '\'' +
      ", carId=" + carId +
      ", startDay=" + startDay +
      ", endDay=" + endDay +
      '}';
  }
}
