package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    // TODO implement this method

    // Find the warehouse being replaced
    Warehouse oldWarehouse = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (oldWarehouse == null) {
      throw new WebApplicationException("Warehouse to replace not found", 404);
    }

    // Validate: New warehouse capacity can accommodate old warehouse's stock
    if (newWarehouse.capacity < oldWarehouse.stock) {
      throw new WebApplicationException("New warehouse capacity cannot accommodate the stock from the previous warehouse", 422);
    }

    // Validate: Stock matching
    if (!newWarehouse.stock.equals(oldWarehouse.stock)) {
      throw new WebApplicationException("Stock of new warehouse must match the stock of the previous warehouse", 422);
    }

    // Set timestamps
    newWarehouse.createdAt = oldWarehouse.createdAt;
    newWarehouse.archivedAt = null;

    warehouseStore.update(newWarehouse);
  }
}
