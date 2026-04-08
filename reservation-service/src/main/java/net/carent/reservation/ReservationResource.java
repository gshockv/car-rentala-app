package net.carent.reservation;

import io.quarkus.logging.Log;
import io.smallrye.graphql.client.GraphQLClient;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
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

  private final InventoryClient inventoryClient;
  private final RentalClient rentalClient;

  @Inject
  public ReservationResource(SecurityContext securityContext,
                             @GraphQLClient("inventory") GraphQLInventoryClient inventoryClient,
                             @RestClient RentalClient rentalClient) {
    this.securityContext = securityContext;
    this.inventoryClient = inventoryClient;
    this.rentalClient = rentalClient;
  }

  @GET
  @Path("all")
  public Collection<Reservation> allReservations() {
    String userId = securityContext.getUserPrincipal() != null ?
      securityContext.getUserPrincipal().getName() : null;

    return Reservation.<Reservation>streamAll()
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
    List<Reservation> reservations = Reservation.listAll();
    reservations.forEach(reserv -> {
      if (reserv.isReserved(startDate, endDate)) {
        carsById.remove(reserv.carId);
      }
    });
    return carsById.values();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Transactional
  public Reservation make(Reservation reservation) {
    reservation.userId = securityContext.getUserPrincipal() != null ?
      securityContext.getUserPrincipal().getName() : "anonymous";

    reservation.persist();

    Log.info("Successfully reserved reservation " + reservation);

    if (reservation.startDay.equals(LocalDate.now())) {
      Rental rental = rentalClient.start(reservation.userId, reservation.id);
      Log.info("Successfully started rental: " + rental);
    }

    return reservation;
  }
}
