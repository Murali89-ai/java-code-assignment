package com.fulfilment.application.monolith.fulfillment;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Repository for managing Warehouse-Product-Store fulfillment mappings
 */
@ApplicationScoped
public class WarehouseProductStoreRepository implements PanacheRepository<WarehouseProductStore> {

  /**
   * Get all products fulfilled by a warehouse for a store
   */
  public List<WarehouseProductStore> getProductsByWarehouseAndStore(Long warehouseId, Long storeId) {
    return find("warehouseId = ?1 and storeId = ?2", warehouseId, storeId).list();
  }

  /**
   * Get all warehouses fulfilling a product for a store
   */
  public List<WarehouseProductStore> getWarehousesByProductAndStore(Long productId, Long storeId) {
    return find("productId = ?1 and storeId = ?2", productId, storeId).list();
  }

  /**
   * Get all warehouses fulfilling a store
   */
  public List<WarehouseProductStore> getWarehousesByStore(Long storeId) {
    return find("storeId", storeId).list();
  }

  /**
   * Get count of warehouses fulfilling a product in a store
   */
  public long countWarehousesByProductAndStore(Long productId, Long storeId) {
    return count("productId = ?1 and storeId = ?2", productId, storeId);
  }

  /**
   * Get count of warehouses fulfilling a store
   */
  public long countWarehousesByStore(Long storeId) {
    return count("storeId", storeId);
  }

  /**
   * Get count of distinct products in a warehouse
   */
  public long countProductsByWarehouse(Long warehouseId) {
    return count("select count(distinct productId) from WarehouseProductStore where warehouseId = ?1",
        warehouseId);
  }

  /**
   * Check if fulfillment mapping already exists
   */
  public boolean exists(Long warehouseId, Long productId, Long storeId) {
    return count("warehouseId = ?1 and productId = ?2 and storeId = ?3",
        warehouseId, productId, storeId) > 0;
  }

  /**
   * Get fulfillment mapping details
   */
  public WarehouseProductStore getMapping(Long warehouseId, Long productId, Long storeId) {
    return find("warehouseId = ?1 and productId = ?2 and storeId = ?3",
        warehouseId, productId, storeId).firstResult();
  }
}

