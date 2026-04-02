package net.carent.inventory.database;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import net.carent.inventory.model.Car;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
public class CarInventory {
  public static final AtomicLong ids = new AtomicLong(0);

  private List<Car> cars;

  @PostConstruct
  void initialize() {
    cars = new CopyOnWriteArrayList<>();
    initialData();
  }

  public List<Car> getCars() {
    return cars;
  }

  private void initialData() {
    Car mazda = new Car();
    mazda.id = ids.incrementAndGet();
    mazda.manufacturer = "Mazda";
    mazda.model = "CX-5";
    mazda.licensePlateNumber = "WYE-123";
    cars.add(mazda);

    Car ford = new Car();
    ford.id = ids.incrementAndGet();
    ford.manufacturer = "Ford";
    ford.model = "Mustang";
    ford.licensePlateNumber = "ASD-124";
    cars.add(ford);
  }
}
