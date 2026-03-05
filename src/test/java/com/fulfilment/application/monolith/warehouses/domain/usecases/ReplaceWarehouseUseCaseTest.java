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

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReplaceWarehouseUseCaseTest {

  private ReplaceWarehouseUseCase replaceWarehouseUseCase;
  private WarehouseStore warehouseStore;
  private LocationResolver locationResolver;

  @BeforeEach
  public void setUp() {
    warehouseStore = mock(WarehouseStore.class);
    locationResolver = mock(LocationResolver.class);
    replaceWarehouseUseCase = new ReplaceWarehouseUseCase(warehouseStore, locationResolver);
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

    Location newLocation = new Location("AMSTERDAM-001", 5, 200);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(newLocation);
    when(warehouseStore.getAll()).thenReturn(List.of(oldWarehouse));

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
  public void testReplaceWarehouseWithInvalidLocationShouldFail() {
    // given
    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "MWH.001";
    oldWarehouse.location = "ZWOLLE-001";
    oldWarehouse.capacity = 100;
    oldWarehouse.stock = 10;
    oldWarehouse.createdAt = LocalDateTime.now().minusMonths(1);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "MWH.001";
    newWarehouse.location = "INVALID-LOC";
    newWarehouse.capacity = 150;
    newWarehouse.stock = 10;

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("INVALID-LOC")).thenReturn(null);

    // when & then
    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> replaceWarehouseUseCase.replace(newWarehouse));
    assertEquals(422, exception.getResponse().getStatus());
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

    Location newLocation = new Location("AMSTERDAM-001", 5, 100);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(newLocation);

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

    Location newLocation = new Location("AMSTERDAM-001", 5, 100);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(newLocation);

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
    oldWarehouse.location = "ZWOLLE-001";
    oldWarehouse.stock = 10;
    oldWarehouse.createdAt = createdTime;

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "MWH.001";
    newWarehouse.location = "ZWOLLE-001";
    newWarehouse.capacity = 100;
    newWarehouse.stock = 10;

    Location location = new Location("ZWOLLE-001", 2, 150);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(location);
    when(warehouseStore.getAll()).thenReturn(List.of(oldWarehouse));

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
    oldWarehouse.location = "ZWOLLE-001";
    oldWarehouse.createdAt = LocalDateTime.now().minusMonths(1);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "MWH.001";
    newWarehouse.location = "ZWOLLE-001";
    newWarehouse.capacity = 100;
    newWarehouse.stock = 10;
    newWarehouse.archivedAt = LocalDateTime.now(); // has archive timestamp

    Location location = new Location("ZWOLLE-001", 2, 150);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(location);
    when(warehouseStore.getAll()).thenReturn(List.of(oldWarehouse));

    // when
    replaceWarehouseUseCase.replace(newWarehouse);

    // then
    assertNull(newWarehouse.archivedAt); // should be cleared
    verify(warehouseStore, times(1)).update(newWarehouse);
  }

  @Test
  public void testReplaceWarehouseWithLocationCapacityExceededShouldFail() {
    // given
    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "MWH.001";
    oldWarehouse.location = "ZWOLLE-001";
    oldWarehouse.capacity = 40;
    oldWarehouse.stock = 10;
    oldWarehouse.createdAt = LocalDateTime.now().minusMonths(1);

    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "MWH.002";
    existingWarehouse.location = "AMSTERDAM-001";
    existingWarehouse.capacity = 90;
    existingWarehouse.stock = 20;
    existingWarehouse.archivedAt = null;

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "MWH.001";
    newWarehouse.location = "AMSTERDAM-001";
    newWarehouse.capacity = 50; // would exceed location max capacity of 100
    newWarehouse.stock = 10;

    Location newLocation = new Location("AMSTERDAM-001", 5, 100);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(newLocation);
    when(warehouseStore.getAll()).thenReturn(List.of(oldWarehouse, existingWarehouse));

    // when & then
    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> replaceWarehouseUseCase.replace(newWarehouse));
    assertEquals(422, exception.getResponse().getStatus());
  }

  @Test
  public void testReplaceWarehouseWithNegativeCapacityShouldFail() {
    // given
    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "MWH.001";
    oldWarehouse.location = "ZWOLLE-001";
    oldWarehouse.capacity = 100;
    oldWarehouse.stock = 10;

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "MWH.001";
    newWarehouse.location = "AMSTERDAM-001";
    newWarehouse.capacity = -10; // negative capacity
    newWarehouse.stock = 10;

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(oldWarehouse);

    // when & then
    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> replaceWarehouseUseCase.replace(newWarehouse));
    assertEquals(422, exception.getResponse().getStatus());
  }

  @Test
  public void testReplaceWarehouseWithNegativeStockShouldFail() {
    // given
    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "MWH.001";
    oldWarehouse.location = "ZWOLLE-001";
    oldWarehouse.capacity = 100;
    oldWarehouse.stock = 10;

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "MWH.001";
    newWarehouse.location = "AMSTERDAM-001";
    newWarehouse.capacity = 100;
    newWarehouse.stock = -5; // negative stock

    Location newLocation = new Location("AMSTERDAM-001", 5, 100);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(newLocation);

    // when & then
    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> replaceWarehouseUseCase.replace(newWarehouse));
    assertEquals(422, exception.getResponse().getStatus());
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

    Location newLocationObj = new Location("AMSTERDAM-001", 5, 100);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("AMSTERDAM-001")).thenReturn(newLocationObj);
    when(warehouseStore.getAll()).thenReturn(new ArrayList<>());

    // when
    replaceWarehouseUseCase.replace(newWarehouse);

    // then
    assertEquals(newLocation, newWarehouse.location);
    verify(warehouseStore, times(1)).update(newWarehouse);
  }

  @Test
  public void testReplaceWarehouseWithSameLocationAndCapacityIncreaseShouldSucceed() {
    // given
    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "MWH.001";
    oldWarehouse.location = "ZWOLLE-001";
    oldWarehouse.capacity = 40;
    oldWarehouse.stock = 10;
    oldWarehouse.createdAt = LocalDateTime.now().minusMonths(1);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "MWH.001";
    newWarehouse.location = "ZWOLLE-001"; // same location
    newWarehouse.capacity = 50; // increased capacity
    newWarehouse.stock = 10;

    Location location = new Location("ZWOLLE-001", 2, 60);

    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(oldWarehouse);
    when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(location);
    when(warehouseStore.getAll()).thenReturn(List.of(oldWarehouse));

    // when
    replaceWarehouseUseCase.replace(newWarehouse);

    // then
    verify(warehouseStore, times(1)).update(newWarehouse);
  }
}

