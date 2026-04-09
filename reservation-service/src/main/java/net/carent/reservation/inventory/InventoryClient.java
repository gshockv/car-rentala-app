package net.carent.reservation.inventory;

import io.smallrye.mutiny.Uni;
import net.carent.reservation.model.Car;

import java.util.List;

public interface InventoryClient {
  Uni<List<Car>> allCars();
}
