package net.carent.inventory.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Car {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;
  public String licensePlateNumber;
  public String manufacturer;
  public String model;

  @Override
  public String toString() {
    return "Car{" +
      "id=" + id +
      ", licensePlateNumber='" + licensePlateNumber + '\'' +
      ", manufacturer='" + manufacturer + '\'' +
      ", model='" + model + '\'' +
      '}';
  }
}
