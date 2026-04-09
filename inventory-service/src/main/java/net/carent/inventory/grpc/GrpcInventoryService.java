package net.carent.inventory.grpc;

import io.quarkus.grpc.GrpcService;
import io.quarkus.logging.Log;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import net.carent.inventory.model.*;
import net.carent.inventory.repository.CarRepository;

import java.util.Optional;

@GrpcService
public class GrpcInventoryService implements InventoryService {
  private final CarRepository carRepository;

  @Inject
  public GrpcInventoryService(CarRepository carRepository) {
    this.carRepository = carRepository;
  }

//  @Override
//  @Blocking
//  @Transactional
//  public Uni<CarResponse> add(InsertCardRequest request) {
//    Car car = new Car();
//    car.licensePlateNumber = request.getLicensePlateNumber();
//    car.manufacturer = request.getManufacturer();
//    car.model = request.getModel();
//
//    Log.info("Persisting car: " + car);
//
//    carRepository.persist(car);
//
//    return Uni.createFrom().item(
//      CarResponse.newBuilder()
//        .setLicensePlateNumber(car.licensePlateNumber)
//        .setManufacturer(car.manufacturer)
//        .setModel(car.model)
//        .setId(car.id)
//        .build()
//    );
//  }

  @Override
  @Blocking
  public Multi<CarResponse> add(Multi<InsertCardRequest> requests) {
    return requests
      .map(request -> {
        Car car = new Car();
        car.licensePlateNumber = request.getLicensePlateNumber();
        car.manufacturer = request.getManufacturer();
        car.model = request.getModel();
        return car;
      })
      .onItem()
      .invoke(car -> {
        QuarkusTransaction.requiringNew().run(() -> {
          carRepository.persist(car);
          Log.info("Persisting car: " + car);
        });
      })
      .map(car -> CarResponse.newBuilder()
        .setLicensePlateNumber(car.licensePlateNumber)
        .setManufacturer(car.manufacturer)
        .setModel(car.model)
        .setId(car.id)
        .build());
  }

  @Override
  @Blocking
  @Transactional
  public Uni<CarResponse> remove(RemoveCarRequest request) {
    Optional<Car> toBeRemoved = carRepository.findByLicensePlateNumberOptional(
      request.getLicensePlateNumber());

    if (toBeRemoved.isPresent()) {
      Car removedCar = toBeRemoved.get();;
      carRepository.delete(removedCar);

      return Uni.createFrom().item(CarResponse.newBuilder()
        .setLicensePlateNumber(removedCar.licensePlateNumber)
        .setManufacturer(removedCar.manufacturer)
        .setModel(removedCar.model)
        .setId(removedCar.id)
        .build());
    }

    return Uni.createFrom().nullItem();
  }
}
