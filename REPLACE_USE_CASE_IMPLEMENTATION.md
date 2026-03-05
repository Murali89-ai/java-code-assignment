# Replace Warehouse Use Case - Complete Implementation Guide

**Status**: ✅ FULLY IMPLEMENTED AND TESTED  
**Date**: March 5, 2026  
**Test Coverage**: 12/12 Tests Passing (100%)

---

## 1. Overview

The `ReplaceWarehouseUseCase` has been fully implemented with comprehensive validations to ensure warehouse replacement operations maintain data integrity and business constraints.

---

## 2. Implementation Architecture

### 2.1 Class Structure

```java
@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {
  
  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;
  
  public ReplaceWarehouseUseCase(
    WarehouseStore warehouseStore, 
    LocationResolver locationResolver
  ) { ... }
  
  @Override
  public void replace(Warehouse newWarehouse) { ... }
}
```

### 2.2 Dependencies Injected

1. **WarehouseStore** - Data persistence layer
   - Finds existing warehouses
   - Updates warehouse records
   - Retrieves all warehouses for validation

2. **LocationResolver** - Location validation service
   - Validates location existence
   - Provides location capacity constraints
   - Ensures business location rules

---

## 3. Validation Rules (7 Comprehensive Validations)

### 3.1 Validation 1: Warehouse Lookup
**Purpose**: Ensure the warehouse being replaced exists

```java
Warehouse oldWarehouse = warehouseStore.findByBusinessUnitCode(
  newWarehouse.businessUnitCode
);
if (oldWarehouse == null) {
  throw new WebApplicationException(
    "Warehouse to replace not found", 404
  );
}
```

**Error Code**: 404 Not Found  
**Business Rule**: Cannot replace a non-existent warehouse

---

### 3.2 Validation 2: Location Existence
**Purpose**: Verify the new location is valid and exists

```java
Location location = locationResolver.resolveByIdentifier(
  newWarehouse.location
);
if (location == null) {
  throw new WebApplicationException(
    "Location does not exist", 422
  );
}
```

**Error Code**: 422 Unprocessable Entity  
**Business Rule**: Warehouses can only be placed at valid locations

---

### 3.3 Validation 3: Positive Capacity
**Purpose**: Ensure warehouse capacity is valid

```java
if (newWarehouse.capacity <= 0) {
  throw new WebApplicationException(
    "Warehouse capacity must be a positive integer", 422
  );
}
```

**Error Code**: 422 Unprocessable Entity  
**Business Rule**: Warehouse capacity must be > 0

---

### 3.4 Validation 4: Non-Negative Stock
**Purpose**: Prevent invalid stock values

```java
if (newWarehouse.stock < 0) {
  throw new WebApplicationException(
    "Warehouse stock cannot be negative", 422
  );
}
```

**Error Code**: 422 Unprocessable Entity  
**Business Rule**: Stock must be >= 0

---

### 3.5 Validation 5: Capacity Accommodation
**Purpose**: Ensure new warehouse can hold old warehouse stock

```java
if (newWarehouse.capacity < oldWarehouse.stock) {
  throw new WebApplicationException(
    "New warehouse capacity cannot accommodate the stock " +
    "from the previous warehouse", 422
  );
}
```

**Error Code**: 422 Unprocessable Entity  
**Business Rule**: No stock loss during replacement  
**Example**: 
- Old warehouse: capacity=100, stock=50
- New warehouse: capacity=45, stock=50
- ❌ FAILS - Cannot accommodate 50 items in capacity of 45

---

### 3.6 Validation 6: Stock Matching
**Purpose**: Preserve exact stock levels during replacement

```java
if (!newWarehouse.stock.equals(oldWarehouse.stock)) {
  throw new WebApplicationException(
    "Stock of new warehouse must match the stock of " +
    "the previous warehouse", 422
  );
}
```

**Error Code**: 422 Unprocessable Entity  
**Business Rule**: Stock cannot change during replacement  
**Example**:
- Old warehouse: stock=50
- New warehouse: stock=45
- ❌ FAILS - Stock differs (must be 50)

---

### 3.7 Validation 7: Location Capacity Constraints
**Purpose**: Ensure location capacity limits are respected

#### Case A: Moving to Different Location
```java
if (!oldWarehouse.location.equals(newWarehouse.location)) {
  int totalCapacity = warehousesAtNewLocation.stream()
    .mapToInt(w -> w.capacity != null ? w.capacity : 0)
    .sum();
  
  if (totalCapacity + newWarehouse.capacity > location.maxCapacity) {
    throw new WebApplicationException(
      "Warehouse capacity exceeds maximum capacity " +
      "for the new location", 422
    );
  }
}
```

**Error Code**: 422 Unprocessable Entity  
**Business Rule**: Total warehouse capacity at location ≤ max allowed  
**Example**:
- Location AMSTERDAM-001: maxCapacity=100
- Existing warehouses: MWH.002 (90 capacity)
- Moving warehouse: 50 capacity
- Total would be: 90 + 50 = 140 > 100
- ❌ FAILS

#### Case B: Same Location with Capacity Increase
```java
} else {
  int capacityDifference = newWarehouse.capacity - oldCapacity;
  if (capacityDifference > 0) {
    if (totalCapacity + newWarehouse.capacity > location.maxCapacity) {
      throw new WebApplicationException(
        "Warehouse capacity exceeds maximum capacity " +
        "for the location", 422
      );
    }
  }
}
```

**Business Rule**: Ensure increased capacity fits within location limits  
**Example**:
- Location ZWOLLE-001: maxCapacity=100
- Old warehouse (MWH.001): 40 capacity
- New warehouse (MWH.001): 70 capacity (increase of 30)
- Other warehouses at ZWOLLE: 50 capacity
- Total would be: 70 + 50 = 120 > 100
- ❌ FAILS

---

## 4. Data Integrity Rules

### 4.1 Timestamp Preservation
**Rule**: Keep original `createdAt` timestamp

```java
newWarehouse.createdAt = oldWarehouse.createdAt;
```

**Purpose**: Maintain audit trail of warehouse creation  
**Example**:
- Old warehouse: createdAt=2024-01-15 10:00:00
- New warehouse: createdAt=(any value)
- Result: createdAt=2024-01-15 10:00:00

---

### 4.2 Archive Timestamp Clearing
**Rule**: Clear `archivedAt` when replacing

```java
newWarehouse.archivedAt = null;
```

**Purpose**: Mark warehouse as active again  
**Example**:
- Old warehouse: archivedAt=2024-03-01
- New warehouse: archivedAt=(any value)
- Result: archivedAt=null (active)

---

## 5. Test Coverage

### Test Class: `ReplaceWarehouseUseCaseTest`
**File**: `src/test/java/com/fulfilment/application/monolith/warehouses/domain/usecases/ReplaceWarehouseUseCaseTest.java`

### 12 Test Cases (100% Pass Rate)

| # | Test Name | Validates | Status |
|---|-----------|-----------|--------|
| 1 | testReplaceWarehouseWithValidDataShouldSucceed | Happy path | ✅ |
| 2 | testReplaceNonExistentWarehouseShouldFail | Validation 1 (404) | ✅ |
| 3 | testReplaceWarehouseWithInvalidLocationShouldFail | Validation 2 (422) | ✅ |
| 4 | testReplaceWarehouseWithInsufficientCapacityShouldFail | Validation 5 (422) | ✅ |
| 5 | testReplaceWarehouseWithStockMismatchShouldFail | Validation 6 (422) | ✅ |
| 6 | testReplaceWarehousePreservesCreatedTimestamp | Data integrity | ✅ |
| 7 | testReplaceWarehouseClearsArchivedTimestamp | Data integrity | ✅ |
| 8 | testReplaceWarehouseWithLocationCapacityExceededShouldFail | Validation 7 (422) | ✅ |
| 9 | testReplaceWarehouseWithNegativeCapacityShouldFail | Validation 3 (422) | ✅ |
| 10 | testReplaceWarehouseWithNegativeStockShouldFail | Validation 4 (422) | ✅ |
| 11 | testReplaceWarehouseChangesLocationSuccessfully | Location change | ✅ |
| 12 | testReplaceWarehouseWithSameLocationAndCapacityIncreaseShouldSucceed | Location increase | ✅ |

---

## 6. Integration with REST API

### Endpoint: `PUT /warehouses/{businessUnitCode}/replace`

```java
@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {
  
  @Inject private ReplaceWarehouseUseCase replaceWarehouseUseCase;
  @Inject private LocationGateway locationGateway;
  
  @Override
  @Transactional
  public Warehouse replaceTheCurrentActiveWarehouse(
    String businessUnitCode, 
    @NotNull Warehouse data
  ) {
    var domainWarehouse = toDomainWarehouse(data);
    domainWarehouse.businessUnitCode = businessUnitCode;
    replaceWarehouseUseCase.replace(domainWarehouse);
    return toWarehouseResponse(domainWarehouse);
  }
}
```

### Request Body Example
```json
{
  "businessUnitCode": "MWH.001",
  "location": "AMSTERDAM-001",
  "capacity": 150,
  "stock": 10
}
```

### Success Response (200 OK)
```json
{
  "businessUnitCode": "MWH.001",
  "location": "AMSTERDAM-001",
  "capacity": 150,
  "stock": 10
}
```

### Error Responses

**404 Not Found**
```json
{
  "error": "Warehouse to replace not found"
}
```

**422 Unprocessable Entity**
```json
{
  "error": "Location does not exist"
}
```

---

## 7. Flow Diagram

```
PUT /warehouses/{businessUnitCode}/replace
            ↓
    ReplaceWarehouseUseCase.replace()
            ↓
    ┌─────────────────────────────────────┐
    │   Validate 1: Warehouse exists      │
    │   (404 if not found)                │
    └─────────────────────────────────────┘
            ↓
    ┌─────────────────────────────────────┐
    │   Validate 2: Location exists       │
    │   (422 if invalid)                  │
    └─────────────────────────────────────┘
            ↓
    ┌─────────────────────────────────────┐
    │   Validate 3: Positive capacity     │
    │   (422 if invalid)                  │
    └─────────────────────────────────────┘
            ↓
    ┌─────────────────────────────────────┐
    │   Validate 4: Non-negative stock    │
    │   (422 if invalid)                  │
    └─────────────────────────────────────┘
            ↓
    ┌─────────────────────────────────────┐
    │   Validate 5: Capacity accommodation│
    │   (422 if insufficient)             │
    └─────────────────────────────────────┘
            ↓
    ┌─────────────────────────────────────┐
    │   Validate 6: Stock matching        │
    │   (422 if mismatch)                 │
    └─────────────────────────────────────┘
            ↓
    ┌─────────────────────────────────────┐
    │   Validate 7: Location capacity     │
    │   constraints (422 if exceeded)     │
    └─────────────────────────────────────┘
            ↓
    ┌─────────────────────────────────────┐
    │   Preserve createdAt timestamp      │
    │   Clear archivedAt timestamp        │
    └─────────────────────────────────────┘
            ↓
    ┌─────────────────────────────────────┐
    │   Update warehouse in database      │
    │   (within @Transactional)           │
    └─────────────────────────────────────┘
            ↓
    Return 200 OK with updated warehouse
```

---

## 8. Key Implementation Improvements

### What Was Added (Compared to Original)

1. **LocationResolver Dependency** ✅
   - Validates location before replacement
   - Checks location capacity constraints
   - Ensures business location rules

2. **Location Capacity Validation** ✅
   - Prevents total warehouse capacity from exceeding location limits
   - Handles both location change and same-location increase scenarios
   - Excludes archived warehouses from calculations

3. **Enhanced Error Messages** ✅
   - Clear, descriptive error messages for each validation
   - Consistent error codes (404, 422)
   - Helps API consumers understand what went wrong

4. **Comprehensive Test Coverage** ✅
   - 12 unit tests covering all validation scenarios
   - Tests for both happy path and error cases
   - Validates data integrity (timestamps)
   - Tests location constraint scenarios

---

## 9. Deployment Checklist

- ✅ Implementation complete
- ✅ 12 unit tests passing (100%)
- ✅ All validations in place
- ✅ Data integrity preserved
- ✅ Integrated with REST API
- ✅ LocationGateway injected
- ✅ GitHub Actions workflow configured
- ✅ JaCoCo coverage report generated
- ✅ Build succeeds with no errors

---

## 10. GitHub Actions Integration

The Replace Use Case is automatically tested via GitHub Actions workflow:

```yaml
- name: Build with Maven
  run: mvn clean package -DskipTests

- name: Run Tests
  run: mvn test

- name: Generate JaCoCo Code Coverage Report
  run: mvn jacoco:report

- name: Archive JaCoCo coverage reports
  uses: actions/upload-artifact@v3
  with:
    name: jacoco-report
    path: target/site/jacoco/
    retention-days: 30
```

**Coverage Target**: 80% and above

---

## 11. Running Tests Locally

### Run All Tests
```bash
mvn clean test
```

### Run Only Replace Use Case Tests
```bash
mvn test -Dtest=ReplaceWarehouseUseCaseTest
```

### Generate Coverage Report
```bash
mvn clean test jacoco:report
```

### View Coverage Report
Open: `target/site/jacoco/index.html` in your browser

---

## 12. Next Steps

1. ✅ Push changes to GitHub
2. ✅ Monitor GitHub Actions build
3. ✅ Verify coverage report is generated
4. ✅ Review test results

---

## Summary

The Replace Warehouse Use Case has been fully implemented with:
- 7 comprehensive validations
- 12 passing unit tests (100%)
- Complete data integrity preservation
- Location constraint enforcement
- Full REST API integration
- Comprehensive documentation

**Status**: Ready for production deployment ✅

