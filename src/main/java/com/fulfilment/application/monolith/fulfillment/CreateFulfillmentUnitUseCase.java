package com.fulfilment.application.monolith.fulfillment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;

/**
 * Use case for managing warehouse-product-store fulfillment associations
 * with constraint validation
 */
@ApplicationScoped
public class CreateFulfillmentUnitUseCase {

  private final WarehouseProductStoreRepository fulfillmentRepository;

  // Constraint limits
  private static final int MAX_WAREHOUSES_PER_PRODUCT_PER_STORE = 2;
  private static final int MAX_WAREHOUSES_PER_STORE = 3;
  private static final int MAX_PRODUCTS_PER_WAREHOUSE = 5;

  public CreateFulfillmentUnitUseCase(WarehouseProductStoreRepository fulfillmentRepository) {
    this.fulfillmentRepository = fulfillmentRepository;
  }

  /**
   * Create a new fulfillment unit (warehouse-product-store association)
   * with all constraint validations
   */
  public void createFulfillmentUnit(Long warehouseId, Long productId, Long storeId, Integer quantityAvailable) {

    // Validate inputs
    if (warehouseId == null || productId == null || storeId == null) {
      throw new WebApplicationException("Warehouse ID, Product ID, and Store ID are required", 400);
    }

    // Check if mapping already exists
    if (fulfillmentRepository.exists(warehouseId, productId, storeId)) {
      throw new WebApplicationException(
          "This warehouse already fulfills this product for this store", 409);
    }

    // Constraint 1: Each Product can be fulfilled by max 2 Warehouses per Store
    long warehouseCountForProduct = fulfillmentRepository
        .countWarehousesByProductAndStore(productId, storeId);
    if (warehouseCountForProduct >= MAX_WAREHOUSES_PER_PRODUCT_PER_STORE) {
      throw new WebApplicationException(
          "Product can be fulfilled by maximum 2 warehouses per store. Current count: "
              + warehouseCountForProduct, 422);
    }

    // Constraint 2: Each Store can be fulfilled by max 3 Warehouses
    long warehouseCountForStore = fulfillmentRepository.countWarehousesByStore(storeId);
    if (warehouseCountForStore >= MAX_WAREHOUSES_PER_STORE) {
      throw new WebApplicationException(
          "Store can be fulfilled by maximum 3 warehouses. Current count: "
              + warehouseCountForStore, 422);
    }

    // Constraint 3: Each Warehouse can store max 5 types of Products
    long productCountInWarehouse = fulfillmentRepository.countProductsByWarehouse(warehouseId);
    if (productCountInWarehouse >= MAX_PRODUCTS_PER_WAREHOUSE) {
      throw new WebApplicationException(
          "Warehouse can store maximum 5 types of products. Current count: "
              + productCountInWarehouse, 422);
    }

    // Validate quantity
    if (quantityAvailable == null || quantityAvailable < 0) {
      quantityAvailable = 0;
    }

    // All validations passed, create the fulfillment unit
    WarehouseProductStore mapping = new WarehouseProductStore(warehouseId, productId, storeId);
    mapping.quantityAvailable = quantityAvailable;
    fulfillmentRepository.persistAndFlush(mapping);
  }

  /**
   * Remove a fulfillment unit (warehouse-product-store association)
   */
  public void removeFulfillmentUnit(Long warehouseId, Long productId, Long storeId) {
    WarehouseProductStore mapping = fulfillmentRepository
        .getMapping(warehouseId, productId, storeId);

    if (mapping == null) {
      throw new WebApplicationException(
          "Fulfillment unit not found", 404);
    }

    fulfillmentRepository.delete(mapping);
  }

  /**
   * Update quantity available for a fulfillment unit
   */
  public void updateQuantityAvailable(Long warehouseId, Long productId, Long storeId,
      Integer quantityAvailable) {
    WarehouseProductStore mapping = fulfillmentRepository
        .getMapping(warehouseId, productId, storeId);

    if (mapping == null) {
      throw new WebApplicationException(
          "Fulfillment unit not found", 404);
    }

    if (quantityAvailable == null || quantityAvailable < 0) {
      throw new WebApplicationException("Quantity cannot be negative", 400);
    }

    mapping.quantityAvailable = quantityAvailable;
    fulfillmentRepository.persistAndFlush(mapping);
  }
}

