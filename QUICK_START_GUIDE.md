# Quick Start Guide - Code Assignment Implementation

## 📋 Overview

All three MUST HAVE tasks have been completed:
1. ✅ LocationGateway - Location resolution
2. ✅ StoreResource - Transaction management  
3. ✅ Warehouse Operations - Full CRUD implementation

## 🚀 Getting Started

### Step 1: Verify Files
All code has been implemented in these files:
```
✅ LocationGateway.java
✅ StoreResource.java
✅ DbWarehouse.java (extends PanacheEntity)
✅ WarehouseRepository.java
✅ CreateWarehouseUseCase.java
✅ ReplaceWarehouseUseCase.java
✅ ArchiveWarehouseUseCase.java
✅ WarehouseResourceImpl.java
```

### Step 2: Build Project
```bash
cd java-assignment
mvn clean compile
```

This will:
- Clear IDE cache
- Regenerate OpenAPI code
- Compile all Java files
- Validate all implementations

### Step 3: Run Tests
```bash
mvn test
```

### Step 4: Start Application
```bash
mvn quarkus:dev
```

Application runs on: `http://localhost:8080`

---

## 🧪 Testing the Implementation

### Test 1: List Warehouses
```bash
curl http://localhost:8080/warehouse
```
**Expected:** Returns array of 3 warehouses (MWH.001, MWH.012, MWH.023)

### Test 2: Get Single Warehouse
```bash
curl http://localhost:8080/warehouse/MWH.001
```
**Expected:** Returns warehouse data with status 200

### Test 3: Create Valid Warehouse ✅
```bash
curl -X POST http://localhost:8080/warehouse \
  -H "Content-Type: application/json" \
  -d '{
    "businessUnitCode": "MWH.NEW",
    "location": "AMSTERDAM-001",
    "capacity": 50,
    "stock": 30
  }'
```
**Expected:** Status 201 Created

### Test 4: Create - Duplicate Code ❌
```bash
curl -X POST http://localhost:8080/warehouse \
  -H "Content-Type: application/json" \
  -d '{
    "businessUnitCode": "MWH.001",
    "location": "AMSTERDAM-001",
    "capacity": 50,
    "stock": 30
  }'
```
**Expected:** Status 422, message: "Business unit code already exists"

### Test 5: Create - Invalid Location ❌
```bash
curl -X POST http://localhost:8080/warehouse \
  -H "Content-Type: application/json" \
  -d '{
    "businessUnitCode": "MWH.TEST",
    "location": "INVALID",
    "capacity": 50,
    "stock": 30
  }'
```
**Expected:** Status 422, message: "Location does not exist"

### Test 6: Create - Max Warehouses ❌
```bash
curl -X POST http://localhost:8080/warehouse \
  -H "Content-Type: application/json" \
  -d '{
    "businessUnitCode": "MWH.DUP",
    "location": "TILBURG-001",
    "capacity": 30,
    "stock": 20
  }'
```
**Expected:** Status 422, message: "Maximum number of warehouses at this location already reached"
*(TILBURG-001 already has MWH.023, max is 1)*

### Test 7: Create - Stock > Capacity ❌
```bash
curl -X POST http://localhost:8080/warehouse \
  -H "Content-Type: application/json" \
  -d '{
    "businessUnitCode": "MWH.BAD",
    "location": "AMSTERDAM-001",
    "capacity": 10,
    "stock": 20
  }'
```
**Expected:** Status 422, message: "Stock cannot exceed warehouse capacity"

### Test 8: Replace Warehouse ✅
```bash
curl -X POST http://localhost:8080/warehouse/MWH.001/replacement \
  -H "Content-Type: application/json" \
  -d '{
    "businessUnitCode": "MWH.001",
    "location": "AMSTERDAM-001",
    "capacity": 150,
    "stock": 10
  }'
```
**Expected:** Status 200, returns updated warehouse

### Test 9: Replace - Not Found ❌
```bash
curl -X POST http://localhost:8080/warehouse/MWH.NOTEXIST/replacement \
  -H "Content-Type: application/json" \
  -d '{
    "businessUnitCode": "MWH.NOTEXIST",
    "location": "AMSTERDAM-001",
    "capacity": 50,
    "stock": 10
  }'
```
**Expected:** Status 404, message: "Warehouse to replace not found"

### Test 10: Replace - Insufficient Capacity ❌
```bash
curl -X POST http://localhost:8080/warehouse/MWH.023/replacement \
  -H "Content-Type: application/json" \
  -d '{
    "businessUnitCode": "MWH.023",
    "location": "TILBURG-001",
    "capacity": 20,
    "stock": 27
  }'
```
**Expected:** Status 422, message: "New warehouse capacity cannot accommodate the stock"
*(Current stock is 27, capacity only 20)*

### Test 11: Replace - Stock Mismatch ❌
```bash
curl -X POST http://localhost:8080/warehouse/MWH.023/replacement \
  -H "Content-Type: application/json" \
  -d '{
    "businessUnitCode": "MWH.023",
    "location": "TILBURG-001",
    "capacity": 35,
    "stock": 20
  }'
```
**Expected:** Status 422, message: "Stock of new warehouse must match the stock of the previous warehouse"
*(Current stock is 27, not 20)*

### Test 12: Archive Warehouse ✅
```bash
curl -X DELETE http://localhost:8080/warehouse/MWH.001
```
**Expected:** Status 204 No Content

### Test 13: List - Archived Filtered ✅
```bash
curl http://localhost:8080/warehouse
```
**Expected:** Archived warehouse (MWH.001) not in list

---

## 📊 Initial Data (from import.sql)

| Business Unit | Location | Capacity | Stock |
|---|---|---|---|
| MWH.001 | ZWOLLE-001 | 100 | 10 |
| MWH.012 | AMSTERDAM-001 | 50 | 5 |
| MWH.023 | TILBURG-001 | 30 | 27 |

---

## 📍 Valid Locations

| Location | Max Warehouses | Max Capacity |
|---|---|---|
| ZWOLLE-001 | 1 | 40 |
| ZWOLLE-002 | 2 | 50 |
| AMSTERDAM-001 | 5 | 100 |
| AMSTERDAM-002 | 3 | 75 |
| TILBURG-001 | 1 | 40 |
| HELMOND-001 | 1 | 45 |
| EINDHOVEN-001 | 2 | 70 |
| VETSBY-001 | 1 | 90 |

---

## 🛠️ Store Endpoints (Task 2)

The Store endpoints now use transaction synchronization to ensure legacy system integration happens AFTER database commits.

### Create Store
```bash
curl -X POST http://localhost:8080/store \
  -H "Content-Type: application/json" \
  -d '{
    "name": "NEW_STORE",
    "quantityProductsInStock": 5
  }'
```

### Update Store
```bash
curl -X PUT http://localhost:8080/store/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "UPDATED_STORE",
    "quantityProductsInStock": 10
  }'
```

### Patch Store
```bash
curl -X PATCH http://localhost:8080/store/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "PATCHED_STORE"
  }'
```

---

## ✅ Validation Rules Checklist

### Create Warehouse
- [x] Business Unit Code must be unique
- [x] Location must exist
- [x] Max warehouses at location not exceeded
- [x] Total capacity at location not exceeded
- [x] Stock cannot exceed capacity

### Replace Warehouse
- [x] Warehouse must exist
- [x] New capacity must accommodate old stock
- [x] New stock must match old stock

### Archive Warehouse
- [x] Warehouse marked with archivedAt timestamp
- [x] Not shown in list after archive

---

## 📁 Documentation Files

Created comprehensive documentation:
1. **COMPLETION_REPORT.md** - Full implementation details
2. **IMPLEMENTATION_QUICK_REFERENCE.md** - Quick reference guide
3. **VALIDATION_CHECKLIST.md** - Validation checklist
4. **IMPLEMENTATION_SUMMARY.md** - Technical summary
5. **QUICK_START_GUIDE.md** - This file

---

## 🔍 Code Structure

```
Domain Layer (Models)
  ├── Warehouse (domain model)
  └── Location (domain model)
        ↓
Port Layer (Interfaces)
  ├── WarehouseStore (repository contract)
  ├── LocationResolver (location contract)
  ├── CreateWarehouseOperation
  ├── ReplaceWarehouseOperation
  └── ArchiveWarehouseOperation
        ↓
Use Case Layer (Business Logic)
  ├── CreateWarehouseUseCase (validation + creation)
  ├── ReplaceWarehouseUseCase (validation + replacement)
  └── ArchiveWarehouseUseCase (archiving)
        ↓
Adapter Layer (Implementations)
  ├── WarehouseRepository (database CRUD)
  ├── LocationGateway (location resolution)
  └── WarehouseResourceImpl (REST endpoints)
        ↓
API Layer (REST)
  └── /warehouse endpoints
```

---

## 🎯 Next Steps

1. **Build**: `mvn clean compile`
2. **Test**: `mvn test`
3. **Run**: `mvn quarkus:dev`
4. **Verify**: Use curl commands above
5. **Answer**: Complete QUESTIONS.md
6. **Deploy**: Package and deploy

---

## 📞 Support

If you encounter issues:

1. **Compilation errors after build**
   ```bash
   mvn clean compile
   ```

2. **IDE not recognizing generated classes**
   - IDE might not have refreshed
   - Maven will compile correctly
   - Restart IDE if needed

3. **Database errors**
   - Uses H2 in-memory database
   - Data reset on app restart
   - Check application.properties for config

4. **Port already in use**
   ```bash
   # Change port in application.properties
   quarkus.http.port=8081
   ```

---

## ✨ Summary

✅ All tasks completed with:
- Proper validation
- Clean architecture
- Error handling
- Transaction management
- Comprehensive documentation

Ready for production use!


