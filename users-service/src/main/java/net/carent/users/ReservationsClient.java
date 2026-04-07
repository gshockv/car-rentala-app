package net.carent.users;

import io.quarkus.oidc.token.propagation.common.AccessToken;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import net.carent.users.model.Car;
import net.carent.users.model.Reservation;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestQuery;

import java.time.LocalDate;
import java.util.Collection;

@RegisterRestClient(baseUri = "http://localhost:8081")
@AccessToken
@Path("reservation")
public interface ReservationsClient {

  @GET
  @Path("all")
  Collection<Reservation> allReservations();

  @POST
  Reservation make(Reservation reservation);

  @GET
  @Path("availability")
  Collection<Car> availability(
    @RestQuery LocalDate startDate,
    @RestQuery LocalDate endDate);
}
