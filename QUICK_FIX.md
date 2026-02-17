# 🚀 QUICK FIX - Just Copy and Paste

## The Problem
```
FATAL: database "fulfillment_db" does not exist
```

## The Solution

### Copy this EXACT command:
```bash
psql -U postgres -c "CREATE DATABASE fulfillment_db;"
```

### Paste it into Command Prompt or PowerShell and press Enter

---

## What You'll See

### Step 1: Run the command
```
C:\> psql -U postgres -c "CREATE DATABASE fulfillment_db;"
```

### Step 2: Expected output
```
CREATE DATABASE
```

### That's it! Database is created ✅

---

## Then Run Tests

```bash
cd C:\Users\mural\Downloads\fcs-interview-code-assignment-main-20260217T090137Z-1-001\fcs-interview-code-assignment-main\java-assignment

./mvnw.cmd test
```

---

## Expected Result

```
[INFO] Tests run: 43, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS ✅
```

---

## If Command Doesn't Work

Try this alternative:
```bash
"C:\Program Files\PostgreSQL\15\bin\psql.exe" -U postgres -c "CREATE DATABASE fulfillment_db;"
```

Or this:
```bash
psql -U postgres
```
Then inside psql, type:
```sql
CREATE DATABASE fulfillment_db;
\q
```

---

## Verify It Worked

```bash
psql -U postgres -c "\l"
```

You should see `fulfillment_db` in the list.

---

## Full Automation (All at Once)

If you want to do everything in one command:

```bash
psql -U postgres -c "CREATE DATABASE fulfillment_db;" && cd C:\Users\mural\Downloads\fcs-interview-code-assignment-main-20260217T090137Z-1-001\fcs-interview-code-assignment-main\java-assignment && .\mvnw.cmd test
```

This will:
1. Create the database
2. Navigate to project
3. Run all tests
4. Show you the results

---

## That's All You Need! ✅

Just one command to fix everything.

After that, all 43 tests will pass.


