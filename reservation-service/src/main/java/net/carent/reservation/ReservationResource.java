package net.carent.reservation;

import io.quarkus.logging.Log;
import io.smallrye.graphql.client.GraphQLClient;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import net.carent.reservation.inventory.GraphQLInventoryClient;
import net.carent.reservation.inventory.InventoryClient;
import net.carent.reservation.model.Car;
import net.carent.reservation.model.Rental;
import net.carent.reservation.entity.Reservation;
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
  private final SecurityContext securityContext;
  private final ReservationsRepository reservationsRepository;

  InventoryClient inventoryClient;

  @Inject
  @RestClient RentalClient rentalClient;

  @Inject
  public ReservationResource(SecurityContext securityContext,
                             ReservationsRepository reservationsRepository,
                             @GraphQLClient("inventory") GraphQLInventoryClient inventoryClient) {
    this.securityContext = securityContext;
    this.reservationsRepository = reservationsRepository;
    this.inventoryClient = inventoryClient;
  }

  @GET
  @Path("all")
  public Collection<Reservation> allReservations() {
    String userId = securityContext.getUserPrincipal() != null ?
      securityContext.getUserPrincipal().getName() : null;

    return reservationsRepository.findAll().stream()
      .filter(reserv -> userId == null || userId.equals(reserv.userId))
      .toList();
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
    reservation.userId = securityContext.getUserPrincipal() != null ?
      securityContext.getUserPrincipal().getName() : "anonymous";

    Reservation result = reservationsRepository.save(reservation);

    if (reservation.startDay.equals(LocalDate.now())) {
      Rental rental = rentalClient.start(reservation.userId, result.id);
      Log.info("Successfully started rental: " + rental);
    }
    return result;
  }
}
