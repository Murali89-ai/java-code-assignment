package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {
    // Validate: Business Unit Code is unique
    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      throw new WebApplicationException("Business unit code already exists", 422);
    }

    // Validate: Location exists
    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) {
      throw new WebApplicationException("Location does not exist", 422);
    }

    // Get all warehouses at this location
    List<Warehouse> warehousesAtLocation = warehouseStore.getAll().stream()
        .filter(w -> w.location.equals(warehouse.location) && w.archivedAt == null)
        .toList();

    // Validate: Max number of warehouses not exceeded
    if (warehousesAtLocation.size() >= location.maxNumberOfWarehouses) {
      throw new WebApplicationException("Maximum number of warehouses at this location already reached", 422);
    }

    // Validate: Total capacity at location doesn't exceed max capacity
    int totalCapacityAtLocation = warehousesAtLocation.stream()
        .mapToInt(w -> w.capacity != null ? w.capacity : 0)
        .sum();
    if (totalCapacityAtLocation + warehouse.capacity > location.maxCapacity) {
      throw new WebApplicationException("Warehouse capacity exceeds maximum capacity for location", 422);
    }

    // Validate: Capacity can handle the stock
    if (warehouse.stock > warehouse.capacity) {
      throw new WebApplicationException("Stock cannot exceed warehouse capacity", 422);
    }

    // Set creation timestamp
    warehouse.createdAt = LocalDateTime.now();

    // if all went well, create the warehouse
    warehouseStore.create(warehouse);
  }
}
