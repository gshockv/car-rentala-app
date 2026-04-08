package net.carent.inventory.service;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import net.carent.inventory.model.Car;
import net.carent.inventory.repository.CarRepository;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import java.util.List;
import java.util.Optional;

@GraphQLApi
public class GraphQLInventoryService {
  private final CarRepository carRepository;

  @Inject
  public GraphQLInventoryService(CarRepository carRepository) {
    this.carRepository = carRepository;
  }

  @Query
  public List<Car> cars() {
    return carRepository.listAll();
  }

  @Mutation
  @Transactional
  public Car register(Car car) {
    carRepository.persist(car);
    Log.info("Persisting " + car);
    return car;
  }

  @Mutation
  @Transactional
  public boolean remove(String licensePlateNumber) {
    Optional<Car> toBeRemoved = carRepository.findByLicensePlateNumberOptional(licensePlateNumber);
    if (toBeRemoved.isPresent()) {
      carRepository.delete(toBeRemoved.get());
      return true;
    } else {
      return false;
    }
  }
}
