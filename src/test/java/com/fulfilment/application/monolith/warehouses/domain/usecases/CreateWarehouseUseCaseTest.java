package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.ws.rs.WebApplicationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CreateWarehouseUseCaseTest {

  private CreateWarehouseUseCase createWarehouseUseCase;
  private WarehouseStore warehouseStore;
  private LocationGateway locationGateway;

  @BeforeEach
  public void setUp() {
    warehouseStore = mock(WarehouseStore.class);
    locationGateway = new LocationGateway();
    createWarehouseUseCase = new CreateWarehouseUseCase(warehouseStore, locationGateway);
  }

  @Test
  public void testCreateWarehouseWithValidDataShouldSucceed() {
    // given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.NEW";
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 50;
    warehouse.stock = 30;

    when(warehouseStore.findByBusinessUnitCode("MWH.NEW")).thenReturn(null);
    when(warehouseStore.getAll()).thenReturn(new ArrayList<>());

    // when
    createWarehouseUseCase.create(warehouse);

    // then
    assertNotNull(warehouse.createdAt);
    verify(warehouseStore, times(1)).create(any(Warehouse.class));
  }

  @Test
  public void testCreateWarehouseWithDuplicateBusinessUnitCodeShouldFail() {
    // given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.001";
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 50;
    warehouse.stock = 30;

    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "MWH.001";
    when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existingWarehouse);

    // when & then
    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> createWarehouseUseCase.create(warehouse));
    assertEquals(422, exception.getResponse().getStatus());
  }

  @Test
  public void testCreateWarehouseWithInvalidLocationShouldFail() {
    // given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.NEW";
    warehouse.location = "INVALID-LOCATION";
    warehouse.capacity = 50;
    warehouse.stock = 30;

    when(warehouseStore.findByBusinessUnitCode("MWH.NEW")).thenReturn(null);

    // when & then
    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> createWarehouseUseCase.create(warehouse));
    assertEquals(422, exception.getResponse().getStatus());
  }

  @Test
  public void testCreateWarehouseExceedingMaxWarehousesAtLocationShouldFail() {
    // given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.NEW";
    warehouse.location = "TILBURG-001"; // max 1 warehouse
    warehouse.capacity = 30;
    warehouse.stock = 20;

    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "MWH.023";
    existingWarehouse.location = "TILBURG-001";
    existingWarehouse.capacity = 30;
    existingWarehouse.stock = 27;
    existingWarehouse.archivedAt = null;

    when(warehouseStore.findByBusinessUnitCode("MWH.NEW")).thenReturn(null);
    when(warehouseStore.getAll()).thenReturn(List.of(existingWarehouse));

    // when & then
    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> createWarehouseUseCase.create(warehouse));
    assertEquals(422, exception.getResponse().getStatus());
  }

  @Test
  public void testCreateWarehouseExceedingLocationCapacityShouldFail() {
    // given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.NEW";
    warehouse.location = "ZWOLLE-001"; // max capacity 40
    warehouse.capacity = 50; // exceeds max
    warehouse.stock = 30;

    when(warehouseStore.findByBusinessUnitCode("MWH.NEW")).thenReturn(null);
    when(warehouseStore.getAll()).thenReturn(new ArrayList<>());

    // when & then
    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> createWarehouseUseCase.create(warehouse));
    assertEquals(422, exception.getResponse().getStatus());
  }

  @Test
  public void testCreateWarehouseWithStockExceedingCapacityShouldFail() {
    // given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.NEW";
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 30;
    warehouse.stock = 50; // exceeds capacity

    when(warehouseStore.findByBusinessUnitCode("MWH.NEW")).thenReturn(null);
    when(warehouseStore.getAll()).thenReturn(new ArrayList<>());

    // when & then
    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> createWarehouseUseCase.create(warehouse));
    assertEquals(422, exception.getResponse().getStatus());
  }

  @Test
  public void testCreateWarehouseWithMaxCapacityAllowedShouldSucceed() {
    // given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.NEW";
    warehouse.location = "AMSTERDAM-001"; // max capacity 100
    warehouse.capacity = 100; // uses all available capacity
    warehouse.stock = 50;

    when(warehouseStore.findByBusinessUnitCode("MWH.NEW")).thenReturn(null);
    when(warehouseStore.getAll()).thenReturn(new ArrayList<>());

    // when
    createWarehouseUseCase.create(warehouse);

    // then
    assertNotNull(warehouse.createdAt);
    verify(warehouseStore, times(1)).create(warehouse);
  }

  @Test
  public void testCreateWarehouseTimestampIsSet() {
    // given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.NEW";
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 50;
    warehouse.stock = 30;

    when(warehouseStore.findByBusinessUnitCode("MWH.NEW")).thenReturn(null);
    when(warehouseStore.getAll()).thenReturn(new ArrayList<>());

    LocalDateTime before = LocalDateTime.now();

    // when
    createWarehouseUseCase.create(warehouse);

    LocalDateTime after = LocalDateTime.now();

    // then
    assertNotNull(warehouse.createdAt);
    assert (warehouse.createdAt.isAfter(before.minusSeconds(1)));
    assert (warehouse.createdAt.isBefore(after.plusSeconds(1)));
  }

  @Test
  public void testCreateWarehouseWithZeroStockShouldSucceed() {
    // given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.EMPTY";
    warehouse.location = "AMSTERDAM-001";
    warehouse.capacity = 50;
    warehouse.stock = 0;

    when(warehouseStore.findByBusinessUnitCode("MWH.EMPTY")).thenReturn(null);
    when(warehouseStore.getAll()).thenReturn(new ArrayList<>());

    // when
    createWarehouseUseCase.create(warehouse);

    // then
    verify(warehouseStore, times(1)).create(warehouse);
  }

  @Test
  public void testCreateWarehouseWithMultipleExistingWarehousesAtLocationShouldRespectLimit() {
    // given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.NEW";
    warehouse.location = "AMSTERDAM-001"; // max 5 warehouses
    warehouse.capacity = 20;
    warehouse.stock = 10;

    List<Warehouse> existingWarehouses = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      Warehouse existing = new Warehouse();
      existing.businessUnitCode = "MWH.EXISTING" + i;
      existing.location = "AMSTERDAM-001";
      existing.capacity = 20;
      existing.archivedAt = null;
      existingWarehouses.add(existing);
    }

    when(warehouseStore.findByBusinessUnitCode("MWH.NEW")).thenReturn(null);
    when(warehouseStore.getAll()).thenReturn(existingWarehouses);

    // when & then
    WebApplicationException exception =
        assertThrows(WebApplicationException.class, () -> createWarehouseUseCase.create(warehouse));
    assertEquals(422, exception.getResponse().getStatus());
  }

  @Test
  public void testCreateWarehouseIgnoresArchivedWarehousesInCount() {
    // given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.NEW";
    warehouse.location = "TILBURG-001"; // max 1 active warehouse
    warehouse.capacity = 30;
    warehouse.stock = 20;

    Warehouse archivedWarehouse = new Warehouse();
    archivedWarehouse.businessUnitCode = "MWH.ARCHIVED";
    archivedWarehouse.location = "TILBURG-001";
    archivedWarehouse.capacity = 30;
    archivedWarehouse.archivedAt = LocalDateTime.now().minusDays(1); // archived

    when(warehouseStore.findByBusinessUnitCode("MWH.NEW")).thenReturn(null);
    when(warehouseStore.getAll()).thenReturn(List.of(archivedWarehouse));

    // when
    createWarehouseUseCase.create(warehouse);

    // then - should succeed because archived warehouse is not counted
    verify(warehouseStore, times(1)).create(warehouse);
  }
}

