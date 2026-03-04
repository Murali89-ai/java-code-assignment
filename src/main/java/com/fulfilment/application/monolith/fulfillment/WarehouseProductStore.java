package com.fulfilment.application.monolith.fulfillment;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents the association between a Warehouse, Product, and Store.
 * This entity tracks which warehouses fulfill specific products for specific stores.
 *
 * Constraints:
 * - Each Product can be fulfilled by max 2 Warehouses per Store
 * - Each Store can be fulfilled by max 3 Warehouses
 * - Each Warehouse can store max 5 types of Products
 */
@Entity
@Table(name = "warehouse_product_store", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"warehouseId", "productId", "storeId"})
})
public class WarehouseProductStore extends PanacheEntity {

  @Column(nullable = false, name = "warehouseId")
  public Long warehouseId;

  @Column(nullable = false, name = "productId")
  public Long productId;

  @Column(nullable = false, name = "storeId")
  public Long storeId;

  @Column(name = "quantity_available", columnDefinition = "INTEGER DEFAULT 0")
  public Integer quantityAvailable;

  @Column(name = "created_at", nullable = false, updatable = false)
  public LocalDateTime createdAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }

  public WarehouseProductStore() {
    this.quantityAvailable = 0;
  }

  public WarehouseProductStore(Long warehouseId, Long productId, Long storeId) {
    this.warehouseId = warehouseId;
    this.productId = productId;
    this.storeId = storeId;
    this.quantityAvailable = 0;
  }
}

