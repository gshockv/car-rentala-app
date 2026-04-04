package net.carent.inventory.cli;

import io.quarkus.grpc.GrpcClient;
import io.quarkus.logging.Log;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import net.carent.inventory.model.InsertCardRequest;
import net.carent.inventory.model.InventoryService;
import net.carent.inventory.model.RemoveCarRequest;

@QuarkusMain
public class InventoryCommand implements QuarkusApplication {
  private static final String USAGE = "Usage: inventory <add>|<remove> <license plate number> <manufacturer> <model>";

  @GrpcClient("inventory")
  InventoryService inventory;

  @Override
  public int run(String... args) throws Exception {
    String action = args.length > 0 ? args[0] : null;

    if ("add".equals(action) && args.length >= 4) {
      var licensePlateNumber = args[1];
      var manufacturer = args[2];
      var model = args[3];
      add(licensePlateNumber, manufacturer, model);
      return 0;
    } else if ("remove".equals(action) && args.length >= 2) {
      var licensePlateNumber = args[1];
      remove(licensePlateNumber);
      return 0;
    }

    System.err.println(USAGE);
    return 1;
  }

  private void add(String licensePlateNumber, String manufacturer, String model) {
    inventory.add(
        InsertCardRequest.newBuilder()
          .setLicensePlateNumber(licensePlateNumber)
          .setManufacturer(manufacturer)
          .setModel(model)
          .build()
      )
      .onItem()
      .invoke(carResponse ->
        Log.info("Inserted new car + " + carResponse))
      .await()
      .indefinitely();
  }

  private void remove(String licensePlateNumber) {
    inventory.remove(RemoveCarRequest.newBuilder()
        .setLicensePlateNumber(licensePlateNumber)
      .build())
      .onItem()
      .invoke(carResponse ->
        Log.info("Removed car: " + carResponse))
      .await()
      .indefinitely();
  }
}
