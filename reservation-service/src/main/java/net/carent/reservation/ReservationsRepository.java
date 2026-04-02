package net.carent.reservation;

import net.carent.reservation.model.Reservation;

import java.util.List;

public interface ReservationsRepository {
  List<Reservation> findAll();
  Reservation save(Reservation reservation);
}
