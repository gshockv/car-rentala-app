package net.carent.users;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

@Path("/whoami")
public class WhoAmIResource {
  private final Template whoami;
  private final SecurityContext securityContext;

  @Inject
  public WhoAmIResource(Template whoami, SecurityContext securityContext) {
    this.whoami = whoami;
    this.securityContext = securityContext;
  }

  @GET
  @Produces(MediaType.TEXT_HTML)
  public TemplateInstance get() {
    String userId = securityContext.getUserPrincipal() != null ?
      securityContext.getUserPrincipal().getName() : null;

    return whoami.data("name", userId);
  }
}
