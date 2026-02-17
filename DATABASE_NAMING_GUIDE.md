# 🗄️ Database Configuration Guide

## Current Database Setup

### Test Environment (application.properties)
```properties
%test.quarkus.datasource.db-kind=postgresql
%test.quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/fulfillment_db
%test.quarkus.datasource.username=postgres
%test.quarkus.datasource.password=123456
```

**Current Database Name:** `fulfillment_db`

---

## 📋 Database Name Recommendations by Use Case

### 1. **Business Domain-Focused** (RECOMMENDED)
- `fulfillment_db` ✅ Current - Good!
- `warehouse_management_db` - Focused on warehouse operations
- `logistics_db` - Focused on logistics operations
- `supply_chain_db` - Focused on supply chain management

### 2. **Company/Organization-Focused**
- `fulfillment_service_db` - If this is a service within larger organization
- `inventory_db` - If focused on inventory management
- `order_fulfillment_db` - If handling order fulfillment

### 3. **Environment-Focused** (NOT RECOMMENDED)
- `fulfillment_prod_db` - Production environment
- `fulfillment_test_db` - Test environment
- `fulfillment_dev_db` - Development environment

### 4. **Technical-Focused** (NOT RECOMMENDED)
- `app_db` - Too generic
- `database` - Too generic
- `postgres_db` - Too generic

---

## 🎯 Recommended Database Names for THIS Project

### Best Choice: `fulfillment_db` ✅ (Already Using)
- **Why:** Clear business purpose
- **Domain:** Fulfillment operations
- **Scope:** Warehouse management, product inventory, store management
- **Scalability:** Can be extended with more entities
- **Professional:** Clearly states what the system manages

### Alternative Options:

#### Option 1: `warehouse_management_db`
```properties
%test.quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/warehouse_management_db
```
- **Pros:** Very specific to warehouse operations
- **Cons:** Doesn't include store/product management aspects

#### Option 2: `logistics_db`
```properties
%test.quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/logistics_db
```
- **Pros:** Broader business context
- **Cons:** Might be confused with shipping logistics

#### Option 3: `supply_chain_db`
```properties
%test.quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/supply_chain_db
```
- **Pros:** Encompasses entire supply chain
- **Cons:** Might be too broad if not actually managing full supply chain

#### Option 4: `inventory_management_db`
```properties
%test.quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/inventory_management_db
```
- **Pros:** Clear focus on inventory
- **Cons:** Doesn't capture store/fulfillment aspects

---

## 📊 Database Schema Overview

### Tables in fulfillment_db

```sql
-- Store Management
CREATE TABLE store (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    quantityProductsInStock INT NOT NULL
);

-- Product Inventory
CREATE TABLE product (
    id BIGINT PRIMARY KEY,
    name VARCHAR(40) UNIQUE NOT NULL,
    description VARCHAR(255),
    price DECIMAL(10,2),
    stock INT NOT NULL
);

-- Warehouse Management
CREATE TABLE warehouse (
    id BIGINT PRIMARY KEY,
    businessUnitCode VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    capacity INT NOT NULL,
    stock INT NOT NULL,
    createdAt TIMESTAMP,
    archivedAt TIMESTAMP
);
```

### Current Entities
- **Store** - Physical/virtual stores
- **Product** - Product inventory
- **Warehouse** - Fulfillment warehouses with locations

### Potential Future Entities (for Bonus Task)
- **ProductWarehouseStore** - Association table for product-warehouse-store relationship
- **Location** - Location metadata (currently hardcoded in code)
- **WarehouseConstraint** - Constraints for max products per warehouse

---

## 🔧 How to Change Database Name

### Option 1: Change Test Database Only

Edit `src/main/resources/application.properties`:

**Before:**
```properties
%test.quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/fulfillment_db
```

**After (Example - warehouse_management_db):**
```properties
%test.quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/warehouse_management_db
```

Then create the new database in PostgreSQL:
```sql
CREATE DATABASE warehouse_management_db;
```

### Option 2: Change Production Database Name

Edit `src/main/resources/application.properties`:

**Before:**
```properties
%prod.quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:15432/quarkus_test
```

**After:**
```properties
%prod.quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:15432/fulfillment_prod
```

### Option 3: Unified Name for Dev/Test/Prod

```properties
# Development/Test Database
%test.quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/fulfillment_db

# Production Database
%prod.quarkus.datasource.jdbc.url=jdbc:postgresql://prod-host:5432/fulfillment_db

# Same logical name, different hosts/instances
```

---

## 🗂️ Database Naming Conventions - Best Practices

### DO ✅
- ✅ Use meaningful business domain names
- ✅ Use lowercase with underscores: `fulfillment_db`
- ✅ Keep names concise but descriptive
- ✅ Use snake_case for consistency
- ✅ Make environment clear if needed: `fulfillment_prod`, `fulfillment_dev`

### DON'T ❌
- ❌ Use single letters: `a`, `db`
- ❌ Use camelCase: `fulfillmentDb`
- ❌ Use spaces: `fulfillment db`
- ❌ Use special characters: `fulfillment-db`, `fulfillment.db`
- ❌ Use overly generic names: `database`, `data`, `app`
- ❌ Use abbreviations without context: `fs_db`, `wm_db`

---

## 📝 Current Setup Summary

### Databases in This Project

```
fulfillment_db
├── Tables:
│   ├── store (3 initial records)
│   ├── product (3 initial records)
│   └── warehouse (3 initial records)
├── Environment: TEST
├── Host: localhost:5432
└── Credentials: postgres / 123456
```

### Data Loaded on Startup

**Stores:**
- TONSTAD (10 products in stock)
- KALLAX (5 products in stock)
- BESTÅ (3 products in stock)

**Products:**
- TONSTAD (10 stock)
- KALLAX (5 stock)
- BESTÅ (3 stock)

**Warehouses:**
- MWH.001 (ZWOLLE-001, capacity 100, stock 10, created 2024-07-01)
- MWH.012 (AMSTERDAM-001, capacity 50, stock 5, created 2023-07-01)
- MWH.023 (TILBURG-001, capacity 30, stock 27, created 2021-02-01)

---

## 🚀 Recommended Setup for Different Scenarios

### Scenario 1: Single Application (Recommended)
```properties
# Single database for all purposes
%dev.quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/fulfillment_db
%test.quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/fulfillment_db
%prod.quarkus.datasource.jdbc.url=jdbc:postgresql://prod-server:5432/fulfillment_db
```

### Scenario 2: Microservices Architecture
```properties
# Separate database per microservice
%test.quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/warehouse_service_db
%prod.quarkus.datasource.jdbc.url=jdbc:postgresql://prod-server:5432/warehouse_service_db
```

### Scenario 3: Multi-Tenant System
```properties
# Database naming includes tenant
%test.quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/tenant_001_fulfillment_db
%prod.quarkus.datasource.jdbc.url=jdbc:postgresql://prod-server:5432/tenant_001_fulfillment_db
```

---

## ✅ Current Configuration Status

### Strengths of Current Setup
- ✅ Clear, descriptive database name: `fulfillment_db`
- ✅ Proper environment separation (%test, %prod)
- ✅ Secure credentials handling
- ✅ Proper schema generation (drop-and-create)
- ✅ Automatic data loading via import.sql

### No Changes Needed Unless:
- ❌ Organization uses different naming convention
- ❌ Database needs to represent different business domain
- ❌ Multi-tenant system requires tenant-specific names
- ❌ Database naming must match enterprise standards

---

## 📖 Summary

**Current Database Name:** `fulfillment_db`

**Recommendation:** KEEP AS IS ✅

**Why:** The name accurately reflects the business purpose (fulfillment operations) and follows SQL naming best practices. It's clear, concise, and professional.

**Alternative Names (if change is needed):**
1. `warehouse_management_db` - If focusing on warehouse aspects
2. `logistics_db` - If focusing on logistics operations
3. `supply_chain_db` - If managing entire supply chain
4. `inventory_management_db` - If focusing on inventory

**To Change:**
1. Edit `application.properties`
2. Create new database in PostgreSQL
3. Rebuild and run tests


