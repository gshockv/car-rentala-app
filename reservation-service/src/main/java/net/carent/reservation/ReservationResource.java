package net.carent.reservation;

import io.quarkus.logging.Log;
import io.smallrye.graphql.client.GraphQLClient;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import net.carent.reservation.inventory.GraphQLInventoryClient;
import net.carent.reservation.inventory.InventoryClient;
import net.carent.reservation.model.Car;
import net.carent.reservation.model.Reservation;
import net.carent.reservation.rental.Rental;
import net.carent.reservation.rental.RentalClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestQuery;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("reservation")
@Produces(MediaType.APPLICATION_JSON)
public class ReservationResource {
  private final ReservationsRepository reservationsRepository;
  private final InventoryClient inventoryClient;
  private final RentalClient rentalClient;

  @Inject
  public ReservationResource(ReservationsRepository reservationsRepository,
                             @GraphQLClient("inventory") GraphQLInventoryClient inventoryClient,
                             @RestClient RentalClient rentalClient) {
    this.reservationsRepository = reservationsRepository;
    this.inventoryClient = inventoryClient;
    this.rentalClient = rentalClient;
  }

  @GET
  @Path("availability")
  public Collection<Car> availability(@RestQuery LocalDate startDate,
                                      @RestQuery LocalDate endDate) {
    List<Car> availableCars = inventoryClient.allCars();
    Map<Long, Car> carsById = new HashMap<>();
    availableCars.forEach(car -> {
      carsById.put(car.id, car);
    });

    // get all current reservations
    List<Reservation> reservations = reservationsRepository.findAll();
    reservations.forEach(reserv -> {
      if (reserv.isReserved(startDate, endDate)) {
        carsById.remove(reserv.carId);
      }
    });
    return carsById.values();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public Reservation make(Reservation reservation) {
    Reservation result = reservationsRepository.save(reservation);

    String userId = "x";
    if (reservation.startDay.equals(LocalDate.now())) {
      Rental rental = rentalClient.start(userId, result.id);
      Log.info("Successfully started rental: " + rental);
    }
    return reservationsRepository.save(reservation);
  }
}
