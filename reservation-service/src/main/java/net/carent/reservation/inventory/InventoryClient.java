package net.carent.reservation.inventory;

import net.carent.reservation.model.Car;

import java.util.List;

public interface InventoryClient {
  List<Car> allCars();
}
