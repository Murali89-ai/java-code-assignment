package com.fulfilment.application.monolith.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LocationGatewayTest {

  private LocationGateway locationGateway;

  @BeforeEach
  public void setUp() {
    locationGateway = new LocationGateway();
  }

  @Test
  public void testWhenResolveExistingLocationShouldReturnLocation() {
    // when
    Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");

    // then
    assertNotNull(location);
    assertEquals("ZWOLLE-001", location.identification);
  }

  @Test
  public void testWhenResolveValidLocationZWOLLE002ShouldReturnCorrectData() {
    // when
    Location location = locationGateway.resolveByIdentifier("ZWOLLE-002");

    // then
    assertNotNull(location);
    assertEquals("ZWOLLE-002", location.identification);
    assertEquals(2, location.maxNumberOfWarehouses);
    assertEquals(50, location.maxCapacity);
  }

  @Test
  public void testWhenResolveValidLocationAMSTERDAM001ShouldReturnCorrectData() {
    // when
    Location location = locationGateway.resolveByIdentifier("AMSTERDAM-001");

    // then
    assertNotNull(location);
    assertEquals("AMSTERDAM-001", location.identification);
    assertEquals(5, location.maxNumberOfWarehouses);
    assertEquals(100, location.maxCapacity);
  }

  @Test
  public void testWhenResolveValidLocationTILBURG001ShouldReturnCorrectData() {
    // when
    Location location = locationGateway.resolveByIdentifier("TILBURG-001");

    // then
    assertNotNull(location);
    assertEquals("TILBURG-001", location.identification);
    assertEquals(1, location.maxNumberOfWarehouses);
    assertEquals(40, location.maxCapacity);
  }

  @Test
  public void testWhenResolveInvalidLocationShouldReturnNull() {
    // when
    Location location = locationGateway.resolveByIdentifier("INVALID-001");

    // then
    assertNull(location);
  }

  @Test
  public void testWhenResolveNullIdentifierShouldReturnNull() {
    // when
    Location location = locationGateway.resolveByIdentifier(null);

    // then
    assertNull(location);
  }

  @Test
  public void testWhenResolveEmptyStringLocationShouldReturnNull() {
    // when
    Location location = locationGateway.resolveByIdentifier("");

    // then
    assertNull(location);
  }

  @Test
  public void testAllValidLocationsAreResolvable() {
    // All valid locations should be resolved
    assertNotNull(locationGateway.resolveByIdentifier("ZWOLLE-001"));
    assertNotNull(locationGateway.resolveByIdentifier("ZWOLLE-002"));
    assertNotNull(locationGateway.resolveByIdentifier("AMSTERDAM-001"));
    assertNotNull(locationGateway.resolveByIdentifier("AMSTERDAM-002"));
    assertNotNull(locationGateway.resolveByIdentifier("TILBURG-001"));
    assertNotNull(locationGateway.resolveByIdentifier("HELMOND-001"));
    assertNotNull(locationGateway.resolveByIdentifier("EINDHOVEN-001"));
    assertNotNull(locationGateway.resolveByIdentifier("VETSBY-001"));
  }

  @Test
  public void testLocationPropertiesAreConsistent() {
    // when
    Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");

    // then - verify immutability (properties don't change)
    Location location2 = locationGateway.resolveByIdentifier("ZWOLLE-001");
    assertEquals(location.identification, location2.identification);
    assertEquals(location.maxNumberOfWarehouses, location2.maxNumberOfWarehouses);
    assertEquals(location.maxCapacity, location2.maxCapacity);
  }
}


