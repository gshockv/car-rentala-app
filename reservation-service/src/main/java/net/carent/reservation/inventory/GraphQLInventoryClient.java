package net.carent.reservation.inventory;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import net.carent.reservation.model.Car;
import org.eclipse.microprofile.graphql.Query;

import java.util.List;

@GraphQLClientApi(configKey = "inventory")
public interface GraphQLInventoryClient extends InventoryClient {
  @Override
  @Query("cars")
  List<Car> allCars();
}
