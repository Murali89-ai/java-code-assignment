package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    // Validate 1: Find the warehouse being replaced by business unit code
    Warehouse oldWarehouse = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (oldWarehouse == null) {
      throw new WebApplicationException("Warehouse to replace not found", 404);
    }

    // Validate 2: Location exists and is valid
    Location location = locationResolver.resolveByIdentifier(newWarehouse.location);
    if (location == null) {
      throw new WebApplicationException("Location does not exist", 422);
    }

    // Validate 3: New warehouse capacity must be valid (positive integer)
    if (newWarehouse.capacity <= 0) {
      throw new WebApplicationException("Warehouse capacity must be a positive integer", 422);
    }

    // Validate 4: Stock must not be negative
    if (newWarehouse.stock < 0) {
      throw new WebApplicationException("Warehouse stock cannot be negative", 422);
    }

    // Validate 5: New warehouse capacity can accommodate old warehouse's stock
    if (newWarehouse.capacity < oldWarehouse.stock) {
      throw new WebApplicationException("New warehouse capacity cannot accommodate the stock from the previous warehouse", 422);
    }

    // Validate 6: Stock matching - stock must be equal in replacement
    if (!newWarehouse.stock.equals(oldWarehouse.stock)) {
      throw new WebApplicationException("Stock of new warehouse must match the stock of the previous warehouse", 422);
    }

    // Validate 7: Check if location capacity can accommodate the new warehouse capacity
    // Calculate total capacity at the new location excluding the old warehouse being replaced
    List<Warehouse> warehousesAtNewLocation = warehouseStore.getAll().stream()
        .filter(w -> w.location.equals(newWarehouse.location)
                && w.archivedAt == null
                && !w.businessUnitCode.equals(newWarehouse.businessUnitCode))
        .toList();

    int totalCapacityAtNewLocation = warehousesAtNewLocation.stream()
        .mapToInt(w -> w.capacity != null ? w.capacity : 0)
        .sum();

    // If moving to a different location, validate capacity constraint
    if (!oldWarehouse.location.equals(newWarehouse.location)) {
      if (totalCapacityAtNewLocation + newWarehouse.capacity > location.maxCapacity) {
        throw new WebApplicationException("Warehouse capacity exceeds maximum capacity for the new location", 422);
      }
    } else {
      // If replacing at the same location, check if capacity increase is within limits
      int oldCapacity = oldWarehouse.capacity != null ? oldWarehouse.capacity : 0;
      int capacityDifference = newWarehouse.capacity - oldCapacity;
      if (capacityDifference > 0) {
        if (totalCapacityAtNewLocation + newWarehouse.capacity > location.maxCapacity) {
          throw new WebApplicationException("Warehouse capacity exceeds maximum capacity for the location", 422);
        }
      }
    }

    // Preserve original creation timestamp and clear archived timestamp
    newWarehouse.createdAt = oldWarehouse.createdAt;
    newWarehouse.archivedAt = null;

    // Update the warehouse with new details
    warehouseStore.update(newWarehouse);
  }
}
