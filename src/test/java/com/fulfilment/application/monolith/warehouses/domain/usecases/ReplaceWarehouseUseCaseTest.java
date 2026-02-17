package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReplaceWarehouseUseCaseTest {

  private ReplaceWarehouseUseCase replaceWarehouseUseCase;
  private WarehouseStore warehouseStore;

  @BeforeEach
  public void setUp() {
    warehouseStore = mock(WarehouseStore.class);
    replaceWarehouseUseCase = new ReplaceWarehouseUseCase(warehouseStore);
  }

  @Test
  public void testReplaceWarehouseWithValidDataShouldSucceed() {
    // given
    LocalDateTime originalCreatedTime = LocalDateTime.now().minusMonths(1);

    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "MWH.001";
    oldWarehouse.location = "ZWOLLE-001";
    oldWarehouse.capacity = 100;
    oldWarehouse.stock = 10;
    oldWarehouse.createdAt = originalCreatedTime;
    oldWarehouse.archivedAt = null;

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "MWH.001";
    newWarehouse.location = "AMSTERDAM-001";
    newWarehouse.capacity = 150;
    newWarehouse.stock = 10;

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(oldWarehouse);

    // when
    replaceWarehouseUseCase.replace(newWarehouse);

    // then
    assertEquals(originalCreatedTime, newWarehouse.createdAt); // timestamp preserved
    assertNull(newWarehouse.archivedAt); // cleared
    verify(warehouseStore, times(1)).update(newWarehouse);
  }

  @Test
  public void testReplaceNonExistentWarehouseShouldFail() {
    // given
    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "MWH.NOTEXIST";
    newWarehouse.location = "AMSTERDAM-001";
    newWarehouse.capacity = 100;
    newWarehouse.stock = 10;

    when(warehouseStore.findByBusinessUnitCode("MWH.NOTEXIST")).thenReturn(null);

    // when & then
    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> replaceWarehouseUseCase.replace(newWarehouse));
    assertEquals(404, exception.getResponse().getStatus());
  }

  @Test
  public void testReplaceWarehouseWithInsufficientCapacityShouldFail() {
    // given
    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "MWH.001";
    oldWarehouse.location = "ZWOLLE-001";
    oldWarehouse.capacity = 100;
    oldWarehouse.stock = 50; // current stock
    oldWarehouse.createdAt = LocalDateTime.now().minusMonths(1);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "MWH.001";
    newWarehouse.location = "AMSTERDAM-001";
    newWarehouse.capacity = 30; // less than current stock (50)
    newWarehouse.stock = 50;

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(oldWarehouse);

    // when & then
    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> replaceWarehouseUseCase.replace(newWarehouse));
    assertEquals(422, exception.getResponse().getStatus());
  }

  @Test
  public void testReplaceWarehouseWithStockMismatchShouldFail() {
    // given
    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "MWH.001";
    oldWarehouse.location = "ZWOLLE-001";
    oldWarehouse.capacity = 100;
    oldWarehouse.stock = 27;
    oldWarehouse.createdAt = LocalDateTime.now().minusMonths(1);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "MWH.001";
    newWarehouse.location = "AMSTERDAM-001";
    newWarehouse.capacity = 100;
    newWarehouse.stock = 20; // different from old stock (27)

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(oldWarehouse);

    // when & then
    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> replaceWarehouseUseCase.replace(newWarehouse));
    assertEquals(422, exception.getResponse().getStatus());
  }

  @Test
  public void testReplaceWarehousePreservesCreatedTimestamp() {
    // given
    LocalDateTime createdTime = LocalDateTime.of(2024, 7, 1, 10, 30, 0);

    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "MWH.001";
    oldWarehouse.stock = 10;
    oldWarehouse.createdAt = createdTime;

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "MWH.001";
    newWarehouse.capacity = 100;
    newWarehouse.stock = 10;

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(oldWarehouse);

    // when
    replaceWarehouseUseCase.replace(newWarehouse);

    // then
    assertEquals(createdTime, newWarehouse.createdAt);
    verify(warehouseStore, times(1)).update(newWarehouse);
  }

  @Test
  public void testReplaceWarehouseClearsArchivedTimestamp() {
    // given
    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "MWH.001";
    oldWarehouse.stock = 10;
    oldWarehouse.createdAt = LocalDateTime.now().minusMonths(1);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "MWH.001";
    newWarehouse.capacity = 100;
    newWarehouse.stock = 10;
    newWarehouse.archivedAt = LocalDateTime.now(); // has archive timestamp

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(oldWarehouse);

    // when
    replaceWarehouseUseCase.replace(newWarehouse);

    // then
    assertNull(newWarehouse.archivedAt); // should be cleared
    verify(warehouseStore, times(1)).update(newWarehouse);
  }

  @Test
  public void testReplaceWarehouseWithExactCapacityMatchShouldSucceed() {
    // given
    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "MWH.001";
    oldWarehouse.stock = 50;
    oldWarehouse.createdAt = LocalDateTime.now().minusMonths(1);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "MWH.001";
    newWarehouse.capacity = 50; // exactly matches stock
    newWarehouse.stock = 50;

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(oldWarehouse);

    // when
    replaceWarehouseUseCase.replace(newWarehouse);

    // then
    verify(warehouseStore, times(1)).update(newWarehouse);
  }

  @Test
  public void testReplaceWarehouseWithHigherCapacityShouldSucceed() {
    // given
    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "MWH.001";
    oldWarehouse.stock = 30;
    oldWarehouse.createdAt = LocalDateTime.now().minusMonths(1);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "MWH.001";
    newWarehouse.capacity = 100; // much higher than stock
    newWarehouse.stock = 30;

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(oldWarehouse);

    // when
    replaceWarehouseUseCase.replace(newWarehouse);

    // then
    verify(warehouseStore, times(1)).update(newWarehouse);
  }

  @Test
  public void testReplaceWarehouseWithZeroStockShouldSucceed() {
    // given
    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "MWH.EMPTY";
    oldWarehouse.stock = 0;
    oldWarehouse.createdAt = LocalDateTime.now().minusMonths(1);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "MWH.EMPTY";
    newWarehouse.capacity = 100;
    newWarehouse.stock = 0;

    when(warehouseStore.findByBusinessUnitCode("MWH.EMPTY")).thenReturn(oldWarehouse);

    // when
    replaceWarehouseUseCase.replace(newWarehouse);

    // then
    verify(warehouseStore, times(1)).update(newWarehouse);
  }

  @Test
  public void testReplaceWarehouseChangesLocationSuccessfully() {
    // given
    String oldLocation = "ZWOLLE-001";
    String newLocation = "AMSTERDAM-001";

    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "MWH.001";
    oldWarehouse.location = oldLocation;
    oldWarehouse.stock = 10;
    oldWarehouse.createdAt = LocalDateTime.now().minusMonths(1);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "MWH.001";
    newWarehouse.location = newLocation;
    newWarehouse.capacity = 100;
    newWarehouse.stock = 10;

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(oldWarehouse);

    // when
    replaceWarehouseUseCase.replace(newWarehouse);

    // then
    assertEquals(newLocation, newWarehouse.location);
    verify(warehouseStore, times(1)).update(newWarehouse);
  }
}

