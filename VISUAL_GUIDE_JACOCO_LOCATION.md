# 🗺️ VISUAL GUIDE - How to Find JaCoCo Report on GitHub

## THE QUICK PATH

```
GitHub Repository
        ↓
    Click "Actions" Tab
        ↓
    Find "Build and Test" Workflow
        ↓
    Click Latest Workflow Run
        ↓
    Wait for ✅ GREEN CHECKMARK
        ↓
    Scroll Down to "Artifacts"
        ↓
    Click "jacoco-report"
        ↓
    Extract ZIP file
        ↓
    Open "index.html" in Browser
        ↓
    VIEW COVERAGE REPORT ✅
```

---

## STEP-BY-STEP SCREENSHOTS GUIDE

### Step 1: Go to Your GitHub Repository
```
URL: https://github.com/YOUR_USERNAME/YOUR_REPO
```

### Step 2: Click "Actions" Tab
```
┌─────────────────────────────────────────┐
│  Code  Issues  Pull Requests  [Actions] │  ← CLICK HERE
└─────────────────────────────────────────┘
```

### Step 3: You'll See Your Workflow Runs
```
┌─────────────────────────────────────────┐
│  Workflows                              │
│  ┌──────────────────────────────────────┐
│  │ Build and Test                 [✓]   │  ← Your workflow
│  │ b020bcd - feat: Complete Replace...  │
│  │ 2 minutes ago                        │
│  └──────────────────────────────────────┘
└─────────────────────────────────────────┘
```

### Step 4: Click the Workflow Run
- Click anywhere on the "Build and Test" row
- This opens the workflow details

### Step 5: See Workflow Steps
```
┌─────────────────────────────────────────┐
│ Build and Test                          │
│ Commit: b020bcd                         │
├─────────────────────────────────────────┤
│ ✅ Set up Java 17                       │
│ ✅ Build with Maven                     │
│ ✅ Run Tests                            │
│ ✅ Generate JaCoCo Report               │
│ ✅ Archive test reports                 │
│ ✅ Archive JaCoCo report                │
└─────────────────────────────────────────┘
```

### Step 6: Scroll Down to Artifacts Section
```
┌─────────────────────────────────────────┐
│ Artifacts                               │
│ ┌──────────────────────────────────────┐
│ │ 📦 jacoco-report              [↓]    │  ← CLICK DOWNLOAD
│ │    Generated in workflow             │
│ │    Size: ~2 MB                       │
│ └──────────────────────────────────────┘
│ ┌──────────────────────────────────────┐
│ │ 📦 surefire-reports           [↓]    │  ← Optional (test results)
│ └──────────────────────────────────────┘
└─────────────────────────────────────────┘
```

### Step 7: Extract the ZIP File
```
jacoco-report.zip
    ↓ (Extract)
    ↓
jacoco/ (folder)
    ├── index.html  ← OPEN THIS FILE
    ├── jacoco.css
    ├── jacoco.xml
    ├── jacoco.csv
    └── (package folders...)
```

### Step 8: Open index.html in Browser
- Right-click on index.html
- Select "Open with" → "Browser"
- Or drag index.html to browser window

### Step 9: View Coverage Report
```
┌────────────────────────────────────────────┐
│  Coverage Report                           │
├────────────────────────────────────────────┤
│  Overall Coverage: X%                      │
│                                            │
│  Packages:                                 │
│  ├─ com.fulfilment.application.monolith   │
│  │  ├─ fulfillment      (Coverage %)      │
│  │  ├─ location         (Coverage %)      │
│  │  ├─ stores           (Coverage %)      │
│  │  ├─ warehouses       (Coverage %)      │
│  │  └─ ...                                │
│                                            │
│  Lines Covered / Total Lines               │
│  Branches Covered / Total Branches         │
└────────────────────────────────────────────┘
```

---

## TIMING REFERENCE

```
Your Push ────→ Workflow Triggered
                    ↓
               +1 min: Build starts
                    ↓
               +3 min: Tests run
                    ↓
               +5 min: JaCoCo generates
                    ↓
               +6 min: Artifacts upload
                    ↓
               +7 min: READY TO DOWNLOAD ✅
                    ↓
               You can now download the report
```

---

## COMMON QUESTIONS

### Q: Where exactly is the "Artifacts" section?
**A**: Scroll down on the workflow run page. It's below all the step details.

### Q: What if I don't see "jacoco-report"?
**A**: 
- Workflow might still be running (wait 5-7 min)
- Refresh the page
- Check if workflow has green ✅ checkmark

### Q: Can I view the report online?
**A**: Not directly on GitHub. Download the ZIP, extract, and open index.html in your browser.

### Q: What files do I need from the artifact?
**A**: You mainly need:
- `index.html` - Main coverage report (open this)
- `jacoco.xml` - For CI/CD tools
- `jacoco.csv` - For metrics tracking

### Q: How long is the report?
**A**: Usually 1-5 MB depending on project size

---

## YOUR SPECIFIC PROJECT

**Repository URL**:
```
https://github.com/YOUR_USERNAME/YOUR_REPO
```

**Actions Tab**:
```
https://github.com/YOUR_USERNAME/YOUR_REPO/actions
```

**Your Latest Commit**:
```
b020bcd - feat: Complete Replace Use Case with location validation...
```

**Expected Workflow Status**:
- ✅ Build and Test workflow
- ✅ All steps pass
- ✅ Artifacts: jacoco-report.zip

---

## WHAT THE REPORT SHOWS

### Coverage Metrics
- **Line Coverage**: % of lines executed by tests
- **Branch Coverage**: % of branches tested
- **Complexity**: Cyclomatic complexity metrics

### Package Breakdown
- **Location Package**: coverage stats
- **Warehouse Package**: coverage stats (focus area)
- **Fulfillment Package**: coverage stats
- **Stores Package**: coverage stats

### Target
- **Minimum**: 80% (your configuration)
- **Expected**: High coverage on Replace Use Case

---

## QUICK CHECKLIST

- [ ] Went to GitHub repository
- [ ] Clicked "Actions" tab
- [ ] Found "Build and Test" workflow
- [ ] Workflow has green ✅ checkmark
- [ ] Scrolled to "Artifacts" section
- [ ] Downloaded "jacoco-report.zip"
- [ ] Extracted ZIP file
- [ ] Opened "index.html" in browser
- [ ] Viewed coverage report
- [ ] Shared with team (optional)

✅ All done!

---

**That's it!** You now have the JaCoCo coverage report in your hands. 🎉

