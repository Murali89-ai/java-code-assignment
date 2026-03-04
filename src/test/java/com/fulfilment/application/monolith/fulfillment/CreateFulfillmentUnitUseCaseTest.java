package com.fulfilment.application.monolith.fulfillment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for CreateFulfillmentUnitUseCase
 * Tests constraint validations for warehouse-product-store associations
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Create Fulfillment Unit Use Case Tests")
class CreateFulfillmentUnitUseCaseTest {

  @Mock private WarehouseProductStoreRepository fulfillmentRepository;

  @InjectMocks private CreateFulfillmentUnitUseCase useCase;

  private Long warehouseId = 1L;
  private Long productId = 1L;
  private Long storeId = 1L;

  @BeforeEach
  void setUp() {
    useCase = new CreateFulfillmentUnitUseCase(fulfillmentRepository);
  }

  @Test
  @DisplayName("Should create fulfillment unit when all constraints are satisfied")
  void testCreateFulfillmentUnitSuccess() {
    // Arrange
    when(fulfillmentRepository.exists(warehouseId, productId, storeId)).thenReturn(false);
    when(fulfillmentRepository.countWarehousesByProductAndStore(productId, storeId))
        .thenReturn(0L);
    when(fulfillmentRepository.countWarehousesByStore(storeId)).thenReturn(0L);
    when(fulfillmentRepository.countProductsByWarehouse(warehouseId)).thenReturn(0L);

    // Act
    assertDoesNotThrow(() -> useCase.createFulfillmentUnit(warehouseId, productId, storeId, 100));

    // Assert
    verify(fulfillmentRepository).persistAndFlush(any(WarehouseProductStore.class));
  }

  @Test
  @DisplayName("Should fail when fulfillment unit already exists")
  void testCreateFulfillmentUnitAlreadyExists() {
    // Arrange
    when(fulfillmentRepository.exists(warehouseId, productId, storeId)).thenReturn(true);

    // Act & Assert
    assertThrows(
        jakarta.ws.rs.WebApplicationException.class,
        () -> useCase.createFulfillmentUnit(warehouseId, productId, storeId, 100));
  }

  @Test
  @DisplayName("Should fail when product already has 2 warehouses for store (Constraint 1)")
  void testCreateFulfillmentUnitConstraint1Violation() {
    // Arrange
    when(fulfillmentRepository.exists(warehouseId, productId, storeId)).thenReturn(false);
    when(fulfillmentRepository.countWarehousesByProductAndStore(productId, storeId))
        .thenReturn(2L); // Already at max

    // Act & Assert
    assertThrows(
        jakarta.ws.rs.WebApplicationException.class,
        () -> useCase.createFulfillmentUnit(warehouseId, productId, storeId, 100));
  }

  @Test
  @DisplayName("Should fail when store already has 3 warehouses (Constraint 2)")
  void testCreateFulfillmentUnitConstraint2Violation() {
    // Arrange
    when(fulfillmentRepository.exists(warehouseId, productId, storeId)).thenReturn(false);
    when(fulfillmentRepository.countWarehousesByProductAndStore(productId, storeId))
        .thenReturn(0L);
    when(fulfillmentRepository.countWarehousesByStore(storeId)).thenReturn(3L); // Already at max

    // Act & Assert
    assertThrows(
        jakarta.ws.rs.WebApplicationException.class,
        () -> useCase.createFulfillmentUnit(warehouseId, productId, storeId, 100));
  }

  @Test
  @DisplayName("Should fail when warehouse already has 5 products (Constraint 3)")
  void testCreateFulfillmentUnitConstraint3Violation() {
    // Arrange
    when(fulfillmentRepository.exists(warehouseId, productId, storeId)).thenReturn(false);
    when(fulfillmentRepository.countWarehousesByProductAndStore(productId, storeId))
        .thenReturn(0L);
    when(fulfillmentRepository.countWarehousesByStore(storeId)).thenReturn(0L);
    when(fulfillmentRepository.countProductsByWarehouse(warehouseId)).thenReturn(5L); // Already at max

    // Act & Assert
    assertThrows(
        jakarta.ws.rs.WebApplicationException.class,
        () -> useCase.createFulfillmentUnit(warehouseId, productId, storeId, 100));
  }

  @Test
  @DisplayName("Should fail when warehouse ID is null")
  void testCreateFulfillmentUnitNullWarehouseId() {
    // Act & Assert
    assertThrows(
        jakarta.ws.rs.WebApplicationException.class,
        () -> useCase.createFulfillmentUnit(null, productId, storeId, 100));
  }

  @Test
  @DisplayName("Should fail when product ID is null")
  void testCreateFulfillmentUnitNullProductId() {
    // Act & Assert
    assertThrows(
        jakarta.ws.rs.WebApplicationException.class,
        () -> useCase.createFulfillmentUnit(warehouseId, null, storeId, 100));
  }

  @Test
  @DisplayName("Should fail when store ID is null")
  void testCreateFulfillmentUnitNullStoreId() {
    // Act & Assert
    assertThrows(
        jakarta.ws.rs.WebApplicationException.class,
        () -> useCase.createFulfillmentUnit(warehouseId, productId, null, 100));
  }

  @Test
  @DisplayName("Should handle null quantity by defaulting to 0")
  void testCreateFulfillmentUnitNullQuantity() {
    // Arrange
    when(fulfillmentRepository.exists(warehouseId, productId, storeId)).thenReturn(false);
    when(fulfillmentRepository.countWarehousesByProductAndStore(productId, storeId))
        .thenReturn(0L);
    when(fulfillmentRepository.countWarehousesByStore(storeId)).thenReturn(0L);
    when(fulfillmentRepository.countProductsByWarehouse(warehouseId)).thenReturn(0L);

    // Act
    assertDoesNotThrow(() -> useCase.createFulfillmentUnit(warehouseId, productId, storeId, null));

    // Assert
    verify(fulfillmentRepository).persistAndFlush(any(WarehouseProductStore.class));
  }

  @Test
  @DisplayName("Should remove fulfillment unit successfully")
  void testRemoveFulfillmentUnitSuccess() {
    // Arrange
    WarehouseProductStore mapping = new WarehouseProductStore(warehouseId, productId, storeId);
    mapping.id = 1L;
    when(fulfillmentRepository.getMapping(warehouseId, productId, storeId)).thenReturn(mapping);

    // Act
    assertDoesNotThrow(() -> useCase.removeFulfillmentUnit(warehouseId, productId, storeId));

    // Assert
    verify(fulfillmentRepository).delete(mapping);
  }

  @Test
  @DisplayName("Should fail to remove non-existent fulfillment unit")
  void testRemoveFulfillmentUnitNotFound() {
    // Arrange
    when(fulfillmentRepository.getMapping(warehouseId, productId, storeId)).thenReturn(null);

    // Act & Assert
    assertThrows(
        jakarta.ws.rs.WebApplicationException.class,
        () -> useCase.removeFulfillmentUnit(warehouseId, productId, storeId));
  }

  @Test
  @DisplayName("Should update quantity available successfully")
  void testUpdateQuantityAvailableSuccess() {
    // Arrange
    WarehouseProductStore mapping = new WarehouseProductStore(warehouseId, productId, storeId);
    mapping.id = 1L;
    mapping.quantityAvailable = 50;

    when(fulfillmentRepository.getMapping(warehouseId, productId, storeId)).thenReturn(mapping);

    // Act
    assertDoesNotThrow(() -> useCase.updateQuantityAvailable(warehouseId, productId, storeId, 150));

    // Assert
    verify(fulfillmentRepository, times(1)).persistAndFlush(any(WarehouseProductStore.class));
  }

  @Test
  @DisplayName("Should fail to update with negative quantity")
  void testUpdateQuantityAvailableNegative() {
    // Arrange
    WarehouseProductStore mapping = new WarehouseProductStore(warehouseId, productId, storeId);
    mapping.id = 1L;
    when(fulfillmentRepository.getMapping(warehouseId, productId, storeId)).thenReturn(mapping);

    // Act & Assert
    assertThrows(
        jakarta.ws.rs.WebApplicationException.class,
        () -> useCase.updateQuantityAvailable(warehouseId, productId, storeId, -10));
  }

  @Test
  @DisplayName("Should allow creating fulfillment with max warehouses at threshold")
  void testCreateFulfillmentUnitAtThreshold() {
    // Arrange - Test with max allowed values
    when(fulfillmentRepository.exists(warehouseId, productId, storeId)).thenReturn(false);
    when(fulfillmentRepository.countWarehousesByProductAndStore(productId, storeId))
        .thenReturn(1L); // 1 less than max (2)
    when(fulfillmentRepository.countWarehousesByStore(storeId)).thenReturn(2L); // 1 less than max (3)
    when(fulfillmentRepository.countProductsByWarehouse(warehouseId)).thenReturn(4L); // 1 less than max (5)

    // Act
    assertDoesNotThrow(() -> useCase.createFulfillmentUnit(warehouseId, productId, storeId, 100));

    // Assert
    verify(fulfillmentRepository).persistAndFlush(any(WarehouseProductStore.class));
  }
}

