package com.fulfilment.application.monolith.fulfillment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Integration tests for FulfillmentResource API endpoints
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Fulfillment Resource API Tests")
class FulfillmentResourceTest {

  @Mock private WarehouseProductStoreRepository fulfillmentRepository;
  @Mock private CreateFulfillmentUnitUseCase createFulfillmentUnitUseCase;
  @Mock private PanacheQuery<WarehouseProductStore> panacheQuery;

  private FulfillmentResource fulfillmentResource;

  private Long warehouseId = 1L;
  private Long productId = 1L;
  private Long storeId = 1L;

  @BeforeEach
  void setUp() {
    fulfillmentResource = new FulfillmentResource();
    fulfillmentResource.fulfillmentRepository = fulfillmentRepository;
    fulfillmentResource.createFulfillmentUnitUseCase = createFulfillmentUnitUseCase;
  }

  @Test
  @DisplayName("Should get warehouses by store")
  void testGetWarehousesByStore() {
    // Arrange
    WarehouseProductStore mapping = new WarehouseProductStore(warehouseId, productId, storeId);
    mapping.id = 1L;
    List<WarehouseProductStore> mappings = new ArrayList<>();
    mappings.add(mapping);
    when(fulfillmentRepository.find(eq("storeId"), any(Long.class))).thenReturn(panacheQuery);
    when(panacheQuery.list()).thenReturn(mappings);

    // Act
    List<FulfillmentResource.FulfillmentDTO> result =
        fulfillmentResource.getStoreFulfillments(storeId);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(warehouseId, result.get(0).warehouseId);
  }

  @Test
  @DisplayName("Should get warehouses by warehouse")
  void testGetWarehousesByWarehouse() {
    // Arrange
    WarehouseProductStore mapping = new WarehouseProductStore(warehouseId, productId, storeId);
    mapping.id = 1L;
    List<WarehouseProductStore> mappings = new ArrayList<>();
    mappings.add(mapping);
    when(fulfillmentRepository.find(eq("warehouseId"), any(Long.class))).thenReturn(panacheQuery);
    when(panacheQuery.list()).thenReturn(mappings);

    // Act
    List<FulfillmentResource.FulfillmentDTO> result =
        fulfillmentResource.getWarehouseFulfillments(warehouseId);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(storeId, result.get(0).storeId);
  }

  @Test
  @DisplayName("Should get products for warehouse-store")
  void testGetProductsForWarehouseStore() {
    // Arrange
    WarehouseProductStore mapping = new WarehouseProductStore(warehouseId, productId, storeId);
    mapping.id = 1L;
    List<WarehouseProductStore> mappings = new ArrayList<>();
    mappings.add(mapping);
    when(fulfillmentRepository.getWarehousesByProductAndStore(productId, storeId))
        .thenReturn(mappings);

    // Act
    List<FulfillmentResource.FulfillmentDTO> result =
        fulfillmentResource.getProductFulfillments(productId, storeId);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(warehouseId, result.get(0).warehouseId);
  }

  @Test
  @DisplayName("Should get fulfillment unit details")
  void testGetFulfillmentUnitDetails() {
    // Arrange
    WarehouseProductStore mapping = new WarehouseProductStore(warehouseId, productId, storeId);
    mapping.id = 1L;
    when(fulfillmentRepository.getMapping(warehouseId, productId, storeId)).thenReturn(mapping);

    // Act
    FulfillmentResource.FulfillmentDTO result =
        fulfillmentResource.getFulfillmentUnit(warehouseId, productId, storeId);

    // Assert
    assertNotNull(result);
    assertEquals(warehouseId, result.warehouseId);
    assertEquals(productId, result.productId);
    assertEquals(storeId, result.storeId);
  }

  @Test
  @DisplayName("Should get constraints info")
  void testGetConstraints() {
    // Act
    FulfillmentResource.ConstraintsInfo result = fulfillmentResource.getConstraints();

    // Assert
    assertNotNull(result);
    assertNotNull(result.constraint1);
    assertNotNull(result.constraint2);
    assertNotNull(result.constraint3);
    assertTrue(result.constraint1.contains("2"));
    assertTrue(result.constraint2.contains("3"));
    assertTrue(result.constraint3.contains("5"));
  }

  @Test
  @DisplayName("Should handle fulfillment not found on get")
  void testGetFulfillmentNotFound() {
    // Arrange
    when(fulfillmentRepository.getMapping(warehouseId, productId, storeId)).thenReturn(null);

    // Act & Assert
    assertThrows(
        jakarta.ws.rs.WebApplicationException.class,
        () -> fulfillmentResource.getFulfillmentUnit(warehouseId, productId, storeId));
  }

  @Test
  @DisplayName("Should return empty list when no fulfillments exist")
  void testEmptyFulfillmentsList() {
    // Arrange
    when(fulfillmentRepository.find(eq("storeId"), any(Long.class))).thenReturn(panacheQuery);
    when(panacheQuery.list()).thenReturn(new ArrayList<>());

    // Act
    List<FulfillmentResource.FulfillmentDTO> result =
        fulfillmentResource.getStoreFulfillments(storeId);

    // Assert
    assertNotNull(result);
    assertEquals(0, result.size());
  }
}

