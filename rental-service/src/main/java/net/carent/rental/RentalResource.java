package net.carent.rental;

import io.quarkus.logging.Log;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import net.carent.rental.entity.Rental;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Path("/rental")
public class RentalResource {
  private final AtomicLong id = new AtomicLong(0);

  @GET
  public List<Rental> list() {
    return Rental.listAll();
  }

  @GET
  @Path("/active")
  public List<Rental> listActive() {
    return Rental.listActive();
  }

  @POST
  @Path("/start/{userId}/{reservationId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Rental start(String userId, Long reservationId) {
    Log.infof("Starting rental for %s with reservation %s", userId, reservationId);

    var newRental = new Rental();
    newRental.userId = userId;
    newRental.reservationId = reservationId;
    newRental.startDate = LocalDate.now();
    newRental.active = true;

    newRental.persist();

    return newRental;
  }

  @PUT
  @Path("/end/{userId}/{reservationId}")
  public Rental end(String userId, Long reservationId) {
    Log.infof("Ending rental for %s with reservation %d", userId, reservationId);

    Optional<Rental> toBeEnded = Rental.findByUserAndResevrationIdsOptional(userId, reservationId);
    if (toBeEnded.isPresent()) {
      var rental = toBeEnded.get();
      rental.endDate = LocalDate.now();
      rental.active = false;
      rental.update();
      return rental;
    } else {
      throw new NotFoundException("Rental not found");
    }
  }
}
