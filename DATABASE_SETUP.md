# 🗄️ Database Setup - Create fulfillment_db

## ❌ Current Error

```
[ERROR] FATAL: database "fulfillment_db" does not exist
[ERROR] Tests run: 39, Failures: 0, Errors: 1, Skipped: 0
[ERROR] BUILD FAILURE
```

**Cause:** The PostgreSQL database `fulfillment_db` doesn't exist on your system.

---

## ✅ Solution: Create the Database

### Option 1: Using psql Command Line (Recommended)

**Step 1: Open PostgreSQL Command Line**
```bash
# Open Command Prompt or PowerShell
# Then run:
psql -U postgres
```

**Step 2: Enter PostgreSQL Password**
```
Password for user postgres: 123456
```

**Step 3: Create the Database**
```sql
CREATE DATABASE fulfillment_db;
```

**Expected Output:**
```
CREATE DATABASE
```

**Step 4: Verify Creation**
```sql
\l
```

You should see `fulfillment_db` in the list of databases.

**Step 5: Exit psql**
```sql
\q
```

---

### Option 2: Using One Command (Windows)

```bash
psql -U postgres -c "CREATE DATABASE fulfillment_db;"
```

**Expected Output:**
```
CREATE DATABASE
```

---

### Option 3: Using pgAdmin GUI

1. Open pgAdmin (usually at http://localhost:5050)
2. Right-click "Databases" in the left panel
3. Click "Create" → "Database"
4. Enter name: `fulfillment_db`
5. Click "Save"

---

## 🔍 Verify Database Was Created

```bash
# Connect to PostgreSQL
psql -U postgres

# Inside psql, list all databases:
\l
```

You should see:
```
                                 List of databases
        Name        |  Owner   | Encoding |   Collate   |    Ctype    |
--------------------+----------+----------+-------------+-------------+
 fulfillment_db     | postgres | UTF8     | en_US.UTF-8 | en_US.UTF-8 |
 postgres           | postgres | UTF8     | en_US.UTF-8 | en_US.UTF-8 |
 template0          | postgres | UTF8     | en_US.UTF-8 | en_US.UTF-8 |
 template1          | postgres | UTF8     | en_US.UTF-8 | en_US.UTF-8 |
```

---

## 📋 PostgreSQL Connection Info

Make sure your PostgreSQL is running with these settings:

| Setting | Value |
|---------|-------|
| Host | localhost |
| Port | 5432 |
| Username | postgres |
| Password | 123456 |
| Database | fulfillment_db |

---

## 🚀 After Creating the Database

Once the database is created, run the tests again:

```bash
cd C:\Users\mural\Downloads\fcs-interview-code-assignment-main-20260217T090137Z-1-001\fcs-interview-code-assignment-main\java-assignment

./mvnw.cmd test
```

**Expected Result:**
```
[INFO] Tests run: 43, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 📊 What Happens When Tests Run

When you run tests, Quarkus automatically:

1. ✅ Drops existing tables (if any)
2. ✅ Creates new tables from entity definitions
3. ✅ Inserts test data from `import.sql`
4. ✅ Runs tests
5. ✅ Cleans up

The database will be reset for each test run, so you don't need to manually manage data.

---

## ⚠️ Troubleshooting

### Error: "psql: command not found"
**Solution:** PostgreSQL is not in your PATH. Try:
```bash
"C:\Program Files\PostgreSQL\15\bin\psql.exe" -U postgres -c "CREATE DATABASE fulfillment_db;"
```
(Adjust the version number if needed)

### Error: "password authentication failed"
**Solution:** The password is incorrect. Default is `123456`. If different:
```bash
psql -U postgres -h localhost -p 5432
```

### Error: "FATAL: role 'postgres' does not exist"
**Solution:** PostgreSQL installation is missing. Install PostgreSQL:
1. Download from https://www.postgresql.org/download/windows/
2. Run installer with default settings
3. Use password: `123456`

### PostgreSQL is not running
**Solution:** Start PostgreSQL:
- **Windows:** Search for "Services" → Find "postgresql" → Right-click "Start"
- **Or:** `pg_ctl -D "C:\Program Files\PostgreSQL\15\data" start`

---

## ✅ Complete Setup Checklist

- [ ] PostgreSQL is installed
- [ ] PostgreSQL is running
- [ ] You can run `psql -U postgres`
- [ ] Database `fulfillment_db` exists (verify with `\l`)
- [ ] Run `./mvnw.cmd test` successfully
- [ ] All 43 tests pass

---

## 📝 Connection String

The application uses this connection string:

```
jdbc:postgresql://localhost:5432/fulfillment_db
```

With credentials:
- User: `postgres`
- Password: `123456`

This is configured in `src/main/resources/application.properties`.

---

## 🎯 Next Steps

1. **Create the database** (see above)
2. **Run tests:**
   ```bash
   ./mvnw.cmd test
   ```
3. **Verify all 43 tests pass:**
   ```
   [INFO] Tests run: 43, Failures: 0, Errors: 0, Skipped: 0
   [INFO] BUILD SUCCESS
   ```

---

**Once database is created, all tests should pass! ✅**


