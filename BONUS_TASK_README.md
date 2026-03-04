# Bonus Task: Product-Warehouse-Store Fulfillment System

## Overview

The Bonus Task implements a sophisticated fulfillment unit management system that associates warehouses as fulfillment sources for products within specific stores. This feature enables retailers to track and manage multi-warehouse fulfillment strategies while enforcing strict business constraints.

---

## Business Requirements

### Objective
Enable the warehouse management system to track product fulfillment across multiple warehouses, allowing flexible fulfillment strategies while preventing resource overallocation.

### Key Constraints

#### 1. **Product-Store Fulfillment Limit**
- **Constraint**: Each Product can be fulfilled by a maximum of 2 different Warehouses per Store
- **Purpose**: Prevent over-distribution of a product to a single store
- **Example**: Product A for Store 1 can be fulfilled by Warehouse 1 and Warehouse 2, but not Warehouse 3

#### 2. **Store Fulfillment Limit**
- **Constraint**: Each Store can be fulfilled by a maximum of 3 different Warehouses
- **Purpose**: Maintain manageable supplier relationships and logistics complexity
- **Example**: Store 1 can receive inventory from Warehouse 1, 2, and 3, but not Warehouse 4

#### 3. **Warehouse Capacity Limit (Product Types)**
- **Constraint**: Each Warehouse can store a maximum of 5 types of Products
- **Purpose**: Ensure warehouse doesn't become overloaded with diverse SKUs
- **Example**: Warehouse 1 can stock Products A, B, C, D, E, but not Product F

---

## Domain Model

### Entity: WarehouseProductStore

```java
public class WarehouseProductStore extends PanacheEntity {
    public Long warehouseId;           // Reference to Warehouse
    public Long productId;              // Reference to Product
    public Long storeId;                // Reference to Store
    public Integer quantityAvailable;   // Available quantity at this fulfillment point
    public LocalDateTime createdAt;     // Creation timestamp
}
```

### Unique Constraint
- **Composite Unique Key**: (warehouseId, productId, storeId)
- **Purpose**: Prevent duplicate fulfillment associations

---

## Architecture

### Components

#### 1. **Entity: WarehouseProductStore**
- JPA entity with Panache integration
- Represents warehouse-product-store association
- Tracks quantity available at fulfillment point
- Immutable creation timestamp

#### 2. **Repository: WarehouseProductStoreRepository**
- Data access layer with Panache
- Custom queries for constraint validation
- Methods:
  - `getProductsByWarehouseAndStore()` - Get products in warehouse for store
  - `getWarehousesByProductAndStore()` - Get warehouses fulfilling product in store
  - `getWarehousesByStore()` - Get all warehouses fulfilling store
  - `countWarehousesByProductAndStore()` - Check product-store fulfillment count
  - `countWarehousesByStore()` - Check store fulfillment count
  - `countProductsByWarehouse()` - Check warehouse product diversity
  - `exists()` - Check duplicate association
  - `getMapping()` - Retrieve specific fulfillment unit

#### 3. **Use Case: CreateFulfillmentUnitUseCase**
- Orchestrates fulfillment unit creation with constraint validation
- Validates all three constraints before persistence
- Methods:
  - `createFulfillmentUnit()` - Create with full validation
  - `removeFulfillmentUnit()` - Safe removal
  - `updateQuantityAvailable()` - Update quantity with validation

#### 4. **REST API: FulfillmentResource**
- RESTful endpoints for fulfillment management
- Comprehensive error handling
- Endpoints:
  - `POST /fulfillment/warehouse/{id}/product/{id}/store/{id}` - Create
  - `GET /fulfillment/warehouse/{id}` - List warehouse fulfillments
  - `GET /fulfillment/store/{id}` - List store fulfillments
  - `GET /fulfillment/product/{id}/store/{id}` - List product-store warehouses
  - `GET /fulfillment/warehouse/{id}/product/{id}/store/{id}` - Get details
  - `PUT /fulfillment/warehouse/{id}/product/{id}/store/{id}` - Update quantity
  - `DELETE /fulfillment/warehouse/{id}/product/{id}/store/{id}` - Remove
  - `GET /fulfillment/constraints` - Get constraint info

---

## Implementation Details

### Validation Flow

```
┌─────────────────────────────────────────────────┐
│  POST /fulfillment/warehouse/.../product/.../   │
│  store/...                                      │
└──────────────┬──────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────┐
│  Validate: Null Parameters Check                │
│  - Warehouse ID not null                        │
│  - Product ID not null                          │
│  - Store ID not null                            │
└──────────────┬──────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────┐
│  Validate: Duplicate Check                      │
│  - Mapping doesn't already exist                │
└──────────────┬──────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────┐
│  Validate: Constraint 1                         │
│  - Product < 2 warehouses for store             │
└──────────────┬──────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────┐
│  Validate: Constraint 2                         │
│  - Store < 3 warehouses                         │
└──────────────┬──────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────┐
│  Validate: Constraint 3                         │
│  - Warehouse < 5 product types                  │
└──────────────┬──────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────┐
│  All Validations Pass                           │
│  - Create WarehouseProductStore entity          │
│  - Persist to database                          │
│  - Return 201 Created                           │
└─────────────────────────────────────────────────┘
```

### Error Handling

| Error Condition | HTTP Status | Message |
|---|---|---|
| Null parameters | 400 Bad Request | "Warehouse ID, Product ID, and Store ID are required" |
| Duplicate mapping | 409 Conflict | "This warehouse already fulfills this product for this store" |
| Constraint 1 violation | 422 Unprocessable Entity | "Product can be fulfilled by maximum 2 warehouses per store..." |
| Constraint 2 violation | 422 Unprocessable Entity | "Store can be fulfilled by maximum 3 warehouses..." |
| Constraint 3 violation | 422 Unprocessable Entity | "Warehouse can store maximum 5 types of products..." |
| Not found (delete/update) | 404 Not Found | "Fulfillment unit not found" |
| Negative quantity | 400 Bad Request | "Quantity cannot be negative" |

---

## API Usage Examples

### 1. Create Fulfillment Unit

**Request:**
```bash
POST /fulfillment/warehouse/1/product/1/store/1
Content-Type: application/json

{
  "quantityAvailable": 100
}
```

**Response (201 Created):**
```json
{
  "message": "Fulfillment unit created successfully",
  "statusCode": 201
}
```

**Error Response (422 Unprocessable Entity):**
```json
{
  "exceptionType": "jakarta.ws.rs.WebApplicationException",
  "code": 422,
  "error": "Product can be fulfilled by maximum 2 warehouses per store. Current count: 2"
}
```

### 2. Get Warehouse Fulfillments

**Request:**
```bash
GET /fulfillment/warehouse/1
```

**Response:**
```json
[
  {
    "id": 1,
    "warehouseId": 1,
    "productId": 1,
    "storeId": 1,
    "quantityAvailable": 100,
    "createdAt": "2026-03-04T10:30:00"
  },
  {
    "id": 2,
    "warehouseId": 1,
    "productId": 2,
    "storeId": 1,
    "quantityAvailable": 50,
    "createdAt": "2026-03-04T10:31:00"
  }
]
```

### 3. Get Store Fulfillments

**Request:**
```bash
GET /fulfillment/store/1
```

**Response:**
```json
[
  {
    "id": 1,
    "warehouseId": 1,
    "productId": 1,
    "storeId": 1,
    "quantityAvailable": 100,
    "createdAt": "2026-03-04T10:30:00"
  },
  {
    "id": 3,
    "warehouseId": 2,
    "productId": 1,
    "storeId": 1,
    "quantityAvailable": 75,
    "createdAt": "2026-03-04T10:32:00"
  }
]
```

### 4. Get Product Fulfillment for Store

**Request:**
```bash
GET /fulfillment/product/1/store/1
```

**Response:**
```json
[
  {
    "id": 1,
    "warehouseId": 1,
    "productId": 1,
    "storeId": 1,
    "quantityAvailable": 100,
    "createdAt": "2026-03-04T10:30:00"
  },
  {
    "id": 3,
    "warehouseId": 2,
    "productId": 1,
    "storeId": 1,
    "quantityAvailable": 75,
    "createdAt": "2026-03-04T10:32:00"
  }
]
```

### 5. Update Quantity Available

**Request:**
```bash
PUT /fulfillment/warehouse/1/product/1/store/1
Content-Type: application/json

{
  "quantityAvailable": 150
}
```

**Response:**
```json
{
  "id": 1,
  "warehouseId": 1,
  "productId": 1,
  "storeId": 1,
  "quantityAvailable": 150,
  "createdAt": "2026-03-04T10:30:00"
}
```

### 6. Remove Fulfillment Unit

**Request:**
```bash
DELETE /fulfillment/warehouse/1/product/1/store/1
```

**Response:**
```
204 No Content
```

### 7. Get Constraints Info

**Request:**
```bash
GET /fulfillment/constraints
```

**Response:**
```json
{
  "constraint1": "Each Product can be fulfilled by max 2 warehouses per Store",
  "constraint2": "Each Store can be fulfilled by max 3 warehouses",
  "constraint3": "Each Warehouse can store max 5 types of Products"
}
```

---

## Testing

### Test Coverage

**Test Class**: `CreateFulfillmentUnitUseCaseTest`

**Test Cases** (15 total):

1. **Positive Tests**
   - ✅ Create fulfillment unit with all constraints satisfied
   - ✅ Create with null quantity (defaults to 0)
   - ✅ Create at constraint threshold (max allowed values)
   - ✅ Remove fulfillment unit successfully
   - ✅ Update quantity successfully

2. **Constraint Violation Tests**
   - ✅ Constraint 1 violation (product max 2 warehouses per store)
   - ✅ Constraint 2 violation (store max 3 warehouses)
   - ✅ Constraint 3 violation (warehouse max 5 products)

3. **Duplicate/Not Found Tests**
   - ✅ Duplicate mapping rejected
   - ✅ Remove non-existent fulfillment unit
   - ✅ Update non-existent fulfillment unit

4. **Input Validation Tests**
   - ✅ Null warehouse ID rejected
   - ✅ Null product ID rejected
   - ✅ Null store ID rejected
   - ✅ Negative quantity rejected

### Running Tests

```bash
# Run all fulfillment tests
mvn test -Dtest=CreateFulfillmentUnitUseCaseTest

# Run all tests with coverage
mvn clean test jacoco:report

# View coverage report
# Open target/site/jacoco/index.html in browser
```

---

## Database Schema

### SQL Table Definition

```sql
CREATE TABLE warehouse_product_store (
  id BIGINT PRIMARY KEY,
  warehouse_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  store_id BIGINT NOT NULL,
  quantity_available INTEGER DEFAULT 0,
  created_at TIMESTAMP NOT NULL,
  
  UNIQUE(warehouse_id, product_id, store_id),
  
  FOREIGN KEY (warehouse_id) REFERENCES db_warehouse(id),
  FOREIGN KEY (product_id) REFERENCES product(id),
  FOREIGN KEY (store_id) REFERENCES store(id)
);

CREATE INDEX idx_wps_warehouse ON warehouse_product_store(warehouse_id);
CREATE INDEX idx_wps_product ON warehouse_product_store(product_id);
CREATE INDEX idx_wps_store ON warehouse_product_store(store_id);
CREATE INDEX idx_wps_composite ON warehouse_product_store(product_id, store_id);
```

---

## Use Case Scenarios

### Scenario 1: Multi-Warehouse Fulfillment

**Situation**: Store A wants Product X fulfilled from two warehouses for redundancy

**Process**:
1. Create fulfillment for Warehouse 1 → Product X → Store A
2. Create fulfillment for Warehouse 2 → Product X → Store A
3. Reject addition of Warehouse 3 → Product X → Store A (exceeds limit)

**Database State**:
```
warehouse_id | product_id | store_id | quantity_available
      1      |     1      |    1     |       100
      2      |     1      |    1     |        75
```

### Scenario 2: Warehouse Product Diversity Limit

**Situation**: Warehouse 1 is small and should only stock certain products

**Process**:
1. Add Products A, B, C, D, E to Warehouse 1 (all for Store 1)
2. Reject adding Product F (exceeds 5-product limit)

**Database State**:
```
warehouse_id | product_id | store_id
      1      |     A      |    1
      1      |     B      |    1
      1      |     C      |    1
      1      |     D      |    1
      1      |     E      |    1
      (can't add F)
```

### Scenario 3: Store Warehouse Limit

**Situation**: Store management wants to limit supplier relationships

**Process**:
1. Add Warehouse 1 to Store 1 (for products A, B)
2. Add Warehouse 2 to Store 1 (for products C, D)
3. Add Warehouse 3 to Store 1 (for product E)
4. Reject adding Warehouse 4 (exceeds 3-warehouse limit)

**Database State**:
```
warehouse_id | product_id | store_id
      1      |     A      |    1
      1      |     B      |    1
      2      |     C      |    1
      2      |     D      |    1
      3      |     E      |    1
      (can't add Warehouse 4)
```

---

## Performance Considerations

### Query Optimization

1. **Constraint Checks**: Use COUNT queries with specific WHERE clauses
   - Indexed by (warehouse_id), (product_id, store_id), (store_id)
   - Fast constraint validation

2. **Retrieval Operations**: Use indexed lookups
   - By warehouse ID
   - By store ID
   - By product and store combination

3. **Indexes Created**:
   ```sql
   idx_wps_warehouse: warehouse_id
   idx_wps_product: product_id
   idx_wps_store: store_id
   idx_wps_composite: (product_id, store_id)
   ```

### Scalability

- Constraint validation uses COUNT operations (O(1) with proper indexing)
- Composite unique key prevents duplicates
- No N+1 query problems
- Suitable for millions of fulfillment records

---

## Integration Points

### With Existing System

1. **Warehouse Module**
   - References warehouse by ID
   - Validates warehouse existence (can be added)

2. **Product Module**
   - References product by ID
   - No product modification needed

3. **Store Module**
   - References store by ID
   - Supports multi-channel fulfillment

### Future Enhancements

1. **Fulfillment Order Processing**
   - Auto-select warehouses based on fulfillment unit availability
   - Optimize fulfillment costs

2. **Inventory Synchronization**
   - Sync quantity_available with warehouse stock
   - Prevent overselling

3. **Analytics & Reporting**
   - Fulfillment performance by warehouse
   - Product concentration analysis
   - Store supplier health

4. **Geo-based Fulfillment**
   - Optimize based on warehouse location
   - Calculate fulfillment costs

---

## Troubleshooting

### Common Issues

| Issue | Cause | Solution |
|---|---|---|
| Constraint 1 violation | Too many warehouses for product in store | Remove one warehouse or use different product/store |
| Constraint 2 violation | Store has too many warehouse suppliers | Remove least-used warehouse from store |
| Constraint 3 violation | Warehouse has too many product types | Distribute products to other warehouses |
| Duplicate error | Fulfillment already exists | Check existing fulfillment units first |
| Not found error | Record was deleted | Verify IDs and recreate if needed |

---

## Conclusion

The Bonus Task implements a sophisticated fulfillment management system that:
- ✅ Tracks multi-warehouse fulfillment strategies
- ✅ Enforces business constraints automatically
- ✅ Provides comprehensive API for fulfillment operations
- ✅ Includes extensive test coverage
- ✅ Enables scalable product-store-warehouse associations

This feature is production-ready and can be extended with additional fulfillment optimization logic.

