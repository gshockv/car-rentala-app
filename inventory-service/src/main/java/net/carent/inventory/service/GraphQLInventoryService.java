package net.carent.inventory.service;

import jakarta.inject.Inject;
import net.carent.inventory.database.CarInventory;
import net.carent.inventory.model.Car;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import java.util.List;
import java.util.Optional;

@GraphQLApi
public class GraphQLInventoryService {
  private final CarInventory inventory;

  @Inject
  public GraphQLInventoryService(CarInventory inventory) {
    this.inventory = inventory;
  }

  @Query
  public List<Car> cars() {
    return inventory.getCars();
  }

  @Mutation
  public Car register(Car car) {
    car.id = CarInventory.ids.incrementAndGet();
    inventory.getCars().add(car);
    return car;
  }

  @Mutation
  public boolean remove(String licensePlateNumber) {
    List<Car> cars = inventory.getCars();
    Optional<Car> toBeremoved = cars.stream()
      .filter(car -> car.licensePlateNumber.equals(licensePlateNumber))
      .findAny();
    return toBeremoved.map(cars::remove).orElse(false);
  }
}
