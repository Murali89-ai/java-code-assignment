package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArchiveWarehouseUseCaseTest {

  private ArchiveWarehouseUseCase archiveWarehouseUseCase;
  private WarehouseStore warehouseStore;

  @BeforeEach
  public void setUp() {
    warehouseStore = mock(WarehouseStore.class);
    archiveWarehouseUseCase = new ArchiveWarehouseUseCase(warehouseStore);
  }

  @Test
  public void testArchiveWarehouseShouldSetArchivedTimestamp() {
    // given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.001";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 100;
    warehouse.stock = 10;
    warehouse.createdAt = LocalDateTime.now().minusMonths(1);
    warehouse.archivedAt = null;

    LocalDateTime before = LocalDateTime.now();

    // when
    archiveWarehouseUseCase.archive(warehouse);

    LocalDateTime after = LocalDateTime.now();

    // then
    assertNotNull(warehouse.archivedAt);
    assert (warehouse.archivedAt.isAfter(before.minusSeconds(1)));
    assert (warehouse.archivedAt.isBefore(after.plusSeconds(1)));
    verify(warehouseStore, times(1)).update(warehouse);
  }

  @Test
  public void testArchiveWarehousePreservesOtherFields() {
    // given
    String businessUnitCode = "MWH.001";
    String location = "ZWOLLE-001";
    int capacity = 100;
    int stock = 10;
    LocalDateTime createdAt = LocalDateTime.now().minusMonths(1);

    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = businessUnitCode;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    warehouse.createdAt = createdAt;
    warehouse.archivedAt = null;

    // when
    archiveWarehouseUseCase.archive(warehouse);

    // then - verify all other fields are preserved
    assert warehouse.businessUnitCode.equals(businessUnitCode);
    assert warehouse.location.equals(location);
    assert warehouse.capacity == capacity;
    assert warehouse.stock == stock;
    assert warehouse.createdAt.equals(createdAt);
  }

  @Test
  public void testArchiveWarehouseCallsRepository() {
    // given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.001";
    warehouse.createdAt = LocalDateTime.now().minusMonths(1);

    // when
    archiveWarehouseUseCase.archive(warehouse);

    // then
    verify(warehouseStore, times(1)).update(any(Warehouse.class));
  }

  @Test
  public void testArchiveAlreadyArchivedWarehouseShouldUpdateTimestamp() {
    // given
    LocalDateTime oldArchiveTime = LocalDateTime.now().minusDays(1);
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.001";
    warehouse.createdAt = LocalDateTime.now().minusMonths(1);
    warehouse.archivedAt = oldArchiveTime;

    LocalDateTime before = LocalDateTime.now();

    // when
    archiveWarehouseUseCase.archive(warehouse);

    LocalDateTime after = LocalDateTime.now();

    // then - archived timestamp should be updated to current time
    assertNotNull(warehouse.archivedAt);
    assert (warehouse.archivedAt.isAfter(oldArchiveTime)); // changed
    assert (warehouse.archivedAt.isAfter(before.minusSeconds(1)));
    assert (warehouse.archivedAt.isBefore(after.plusSeconds(1)));
  }

  @Test
  public void testArchiveMultipleWarehousesShouldHaveDifferentTimestamps()
      throws InterruptedException {
    // given
    Warehouse warehouse1 = new Warehouse();
    warehouse1.businessUnitCode = "MWH.001";
    warehouse1.createdAt = LocalDateTime.now().minusMonths(1);

    Warehouse warehouse2 = new Warehouse();
    warehouse2.businessUnitCode = "MWH.002";
    warehouse2.createdAt = LocalDateTime.now().minusMonths(1);

    // when
    archiveWarehouseUseCase.archive(warehouse1);
    Thread.sleep(10); // ensure different timestamps
    archiveWarehouseUseCase.archive(warehouse2);

    // then - both should have archived timestamps
    assertNotNull(warehouse1.archivedAt);
    assertNotNull(warehouse2.archivedAt);
  }

  @Test
  public void testArchiveWarehouseWithZeroStockShouldSucceed() {
    // given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.EMPTY";
    warehouse.stock = 0;
    warehouse.createdAt = LocalDateTime.now().minusMonths(1);
    warehouse.archivedAt = null;

    // when
    archiveWarehouseUseCase.archive(warehouse);

    // then
    assertNotNull(warehouse.archivedAt);
    verify(warehouseStore, times(1)).update(warehouse);
  }

  @Test
  public void testArchiveWarehouseWithFullCapacityShouldSucceed() {
    // given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.FULL";
    warehouse.capacity = 100;
    warehouse.stock = 100;
    warehouse.createdAt = LocalDateTime.now().minusMonths(1);
    warehouse.archivedAt = null;

    // when
    archiveWarehouseUseCase.archive(warehouse);

    // then
    assertNotNull(warehouse.archivedAt);
    verify(warehouseStore, times(1)).update(warehouse);
  }

  @Test
  public void testSoftDeletePatternPreservesData() {
    // given
    String businessUnitCode = "MWH.001";
    String location = "ZWOLLE-001";
    int capacity = 100;
    int stock = 50;
    LocalDateTime createdAt = LocalDateTime.now().minusMonths(1);

    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = businessUnitCode;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    warehouse.createdAt = createdAt;
    warehouse.archivedAt = null;

    // when
    archiveWarehouseUseCase.archive(warehouse);

    // then - soft delete pattern should preserve all data
    assert warehouse.businessUnitCode.equals(businessUnitCode);
    assert warehouse.location.equals(location);
    assert warehouse.capacity == capacity;
    assert warehouse.stock == stock;
    assert warehouse.createdAt.equals(createdAt);
    assertNotNull(warehouse.archivedAt); // only archive timestamp added
  }
}

