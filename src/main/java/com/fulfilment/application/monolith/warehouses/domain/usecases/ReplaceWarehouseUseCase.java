package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    // Find the warehouse being replaced by business unit code
    Warehouse oldWarehouse = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (oldWarehouse == null) {
      throw new WebApplicationException("Warehouse to replace not found", 404);
    }

    // Validate: New warehouse capacity can accommodate old warehouse's stock
    if (newWarehouse.capacity < oldWarehouse.stock) {
      throw new WebApplicationException("New warehouse capacity cannot accommodate the stock from the previous warehouse", 422);
    }

    // Validate: Stock matching - stock must be equal in replacement
    if (!newWarehouse.stock.equals(oldWarehouse.stock)) {
      throw new WebApplicationException("Stock of new warehouse must match the stock of the previous warehouse", 422);
    }

    // Validate: New warehouse capacity must be valid (positive integer)
    if (newWarehouse.capacity <= 0) {
      throw new WebApplicationException("Warehouse capacity must be a positive integer", 422);
    }

    // Validate: Stock must not be negative
    if (newWarehouse.stock < 0) {
      throw new WebApplicationException("Warehouse stock cannot be negative", 422);
    }

    // Preserve original creation timestamp and clear archived timestamp
    newWarehouse.createdAt = oldWarehouse.createdAt;
    newWarehouse.archivedAt = null;

    // Update the warehouse with new details
    warehouseStore.update(newWarehouse);
  }
}
