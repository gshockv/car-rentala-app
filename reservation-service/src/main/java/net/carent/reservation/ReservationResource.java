package net.carent.reservation;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.mutiny.Uni;
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
import java.util.stream.Collectors;

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
  public Uni<Collection<Reservation>> allReservations() {
    String userId = securityContext.getUserPrincipal() != null ?
      securityContext.getUserPrincipal().getName() : null;

    return Reservation.<Reservation>listAll()
      .onItem()
      .transform(reservations ->
        reservations.stream()
          .filter(r -> userId == null || userId.equals(r.userId))
          .toList()
      );
  }

  @GET
  @Path("availability")
  public Uni<Collection<Car>> availability(@RestQuery LocalDate startDate,
                                      @RestQuery LocalDate endDate) {
    Uni<List<Car>> allCarsUni = inventoryClient.allCars();
    Uni<List<Reservation>> reservationsUni = Reservation.listAll();

    return Uni.combine().all().unis(allCarsUni, reservationsUni)
      .with((availableCars, reservations) -> {
        Map<Long, Car> carsById = new HashMap<>();
        for (var car : availableCars) {
          carsById.put(car.id, car);
        }

        for (var reservation : reservations) {
          if (reservation.isReserved(startDate, endDate)) {
            carsById.remove(reservation.carId);
          }
        }
        return carsById.values();
      });
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @WithTransaction
  public Uni<Reservation> make(Reservation reservation) {
    reservation.userId = securityContext.getUserPrincipal() != null ?
      securityContext.getUserPrincipal().getName() : "anonymous";

    return reservation.<Reservation>persist().onItem()
      .call(persisted -> {
        Log.info("Successfully reserved reservation " + persisted);

        if (persisted.startDay.equals(LocalDate.now())) {
          return rentalClient.start(persisted.userId, persisted.id)
            .onItem().invoke(rental ->
              Log.info("Successfully started rental: " + rental)
            ).replaceWith(persisted);
        }
        return Uni.createFrom().item(persisted);
      });
  }
}
