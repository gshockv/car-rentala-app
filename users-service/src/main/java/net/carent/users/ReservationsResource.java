package net.carent.users;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;
import net.carent.users.model.Car;
import net.carent.users.model.Reservation;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestQuery;
import org.jboss.resteasy.reactive.RestResponse;

import java.time.LocalDate;
import java.util.Collection;

@Path("/")
public class ReservationsResource {
  private final SecurityContext securityContext;
  private final ReservationsClient client;

  @CheckedTemplate
  public static class Templates {
    public static native TemplateInstance index(
      String name,
      LocalDate startDate,
      LocalDate endDate
    );

    public static native TemplateInstance listofreservations(Collection<Reservation> reservations);

    public static native TemplateInstance availablecars(Collection<Car> cars,
                                                        LocalDate startDate,
                                                        LocalDate endDate);
  }

  @Inject
  public ReservationsResource(SecurityContext securityContext,
                              @RestClient ReservationsClient client) {
    this.securityContext = securityContext;
    this.client = client;
  }

  @GET
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance index(@RestQuery LocalDate startDate, @RestQuery LocalDate endDate) {
    if (startDate == null) {
      startDate = LocalDate.now().plusDays(1L);
    }
    if (endDate == null) {
      endDate = LocalDate.now().plusDays(7L);
    }
    return Templates.index(securityContext.getUserPrincipal().getName(), startDate, endDate);
  }

  @GET
  @Path("/get")
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getReservations() {
    Collection<Reservation> reservations = client.allReservations();
    return Templates.listofreservations(reservations);
  }

  @GET
  @Path("/available")
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance getAvailableCars(@RestQuery LocalDate startDate, @RestQuery LocalDate endDate) {
    Collection<Car> availableCars = client.availability(startDate, endDate);
    return Templates.availablecars(availableCars, startDate, endDate);
  }

  @POST
  @Path("/reserve")
  @Produces(MediaType.TEXT_HTML)
  public RestResponse<TemplateInstance> create(
    @RestForm LocalDate startDate,
    @RestForm LocalDate endDate,
    @RestForm Long carId) {

    var reservation = new Reservation();
    reservation.startDay = startDate;
    reservation.endDay = endDate;
    reservation.carId = carId;

    client.make(reservation);

    return RestResponse.ResponseBuilder
      .ok(getReservations())
      .header("HX-Trigger-After-Swap", "update-available-cars-list")
      .build();
  }
}
