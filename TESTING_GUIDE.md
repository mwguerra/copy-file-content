# 🧪 Complete Testing Guide - Copy File Content Plugin

This guide shows how to test **all** plugin functionalities locally, including the new features from PRs #7 and #10.

---

## 🚀 Step 1: Launch the Plugin in Sandbox IDE

### Start the Development IDE

```bash
./gradlew runIde
```

**What happens:**
- Gradle downloads IntelliJ IDEA Community Edition (if not cached)
- Launches a sandboxed IntelliJ IDE with your plugin installed
- Takes ~30 seconds to 2 minutes on first run (faster on subsequent runs)
- Watch for: `BUILD SUCCESSFUL` and IDE window opening

**Note:** The sandbox IDE runs isolated from your main IntelliJ installation, so it's safe to experiment.

---

## 📋 Step 2: Create a Test Project in Sandbox

Once the sandbox IDE opens:

1. **Create New Project**
   - File → New → Project
   - Select "Empty Project" or "Java" (any type works)
   - Name: `plugin-test`
   - Click "Create"

2. **Create Test Files**
   - Right-click on project root → New → Directory → `src`
   - Inside `src`, create these files:
     - `Main.java` (add some code)
     - `Config.properties` (add some properties)
     - `README.md` (add some text)
   - Create a subdirectory: `src/utils`
     - Add: `Helper.java`

Your structure should look like:
```
plugin-test/
├── src/
│   ├── Main.java
│   ├── Config.properties
│   ├── README.md
│   └── utils/
│       └── Helper.java
```

---

## ✅ Step 3: Test Core Feature - Copy from Project Tree

### Test 3.1: Copy Single File

1. **Select file** in Project tree (left sidebar)
   - Click on `Main.java`

2. **Right-click** → Find "**Copy File Content to Clipboard**"
   - Should appear after "Copy Path" in the context menu

3. **Verify**:
   - Open any text editor (Notepad, VS Code, etc.)
   - Paste (Ctrl+V / Cmd+V)
   - ✅ You should see:
     ```
     // file: src/Main.java
     [your Java code here]
     ```

4. **Check notification** (bottom-right corner):
   - Should show "1 file copied"
   - Click notification to see statistics:
     - Total characters, lines, words, estimated tokens

### Test 3.2: Copy Multiple Files

1. **Multi-select files**:
   - Hold `Ctrl` (or `Cmd` on Mac)
   - Click `Main.java`, `Config.properties`, `README.md`

2. **Right-click** → "Copy File Content to Clipboard"

3. **Verify**:
   - Paste into text editor
   - ✅ Should see all 3 files with headers:
     ```
     // file: src/Main.java
     [Java code]

     // file: src/Config.properties
     [properties]

     // file: src/README.md
     [markdown]
     ```

### Test 3.3: Copy Entire Directory

1. **Right-click on `src` folder** → "Copy File Content to Clipboard"

2. **Verify**:
   - Paste into text editor
   - ✅ Should see all files in `src/` and `src/utils/`
   - Files appear in order with paths

---

## 🆕 Step 4: Test NEW Feature - Copy All Open Tabs (PR from v0.1.4)

### Test 4.1: Open Multiple Files

1. **Open files in editor**:
   - Double-click `Main.java` (opens in editor)
   - Double-click `Helper.java`
   - Double-click `README.md`

2. **Verify** you have 3 tabs open at the top

### Test 4.2: Copy All Open Tabs

1. **Right-click on any editor tab** (at the top)
   - Look for "**Copy All Open Tabs Content to Clipboard**"

2. **Click it**

3. **Verify**:
   - Paste into text editor
   - ✅ Should see content from all 3 open files
   - Order matches tab order (left to right)

### Test 4.3: Test with No Tabs Open

1. **Close all tabs** (click X on each tab)
2. **Right-click in tab bar** → "Copy All Open Tabs Content to Clipboard"
3. **Verify**: Should see notification "No open tabs found to copy"

---

## 🆕 Step 5: Test NEW Feature - Copy from VCS Changes Window (PR #7)

### Test 5.1: Initialize Git

1. **Enable Git**:
   - VCS → Enable Version Control Integration
   - Select "Git" → OK

2. **Make changes**:
   - Open `Main.java` in editor
   - Add a line: `// TODO: implement`
   - Save file (Ctrl+S / Cmd+S)

### Test 5.2: Copy from Changes View

1. **Open Git/Changes window**:
   - Click "Commit" tab (left sidebar) or
   - Alt+0 / Cmd+0 to toggle

2. **Find changed file** in "Changes" section
   - Should see `Main.java` listed

3. **Right-click on the file** in Changes view
   - Look for "**Copy File Content to Clipboard**"
   - ✅ This is the NEW feature from PR #7!

4. **Click it**

5. **Verify**:
   - Paste into text editor
   - ✅ Should see current content of `Main.java`

### Test 5.3: Copy Multiple Changed Files

1. **Modify another file**:
   - Open `README.md`, add a line, save

2. **In Commit/Changes window**:
   - Select both `Main.java` and `README.md` (Ctrl+Click)
   - Right-click → "Copy File Content to Clipboard"

3. **Verify**: Both files' content copied

---

## 🆕 Step 6: Test NEW Feature - In-Memory Copy (PR #10)

This is the most important new feature - it copies **unsaved changes**!

### Test 6.1: Copy Unsaved Changes

1. **Open file**: Double-click `Main.java`

2. **Modify WITHOUT saving**:
   - Add these lines:
     ```java
     // UNSAVED CHANGE - TEST
     System.out.println("This is NOT saved to disk!");
     ```
   - **DO NOT SAVE** (notice the white dot on tab = unsaved)

3. **Copy the file**:
   - In Project tree, right-click `Main.java`
   - "Copy File Content to Clipboard"

4. **Verify**:
   - Paste into text editor
   - ✅ **Should see your UNSAVED changes!**
   - The line "This is NOT saved to disk!" should appear

5. **Compare with old behavior**:
   - Without PR #10, you'd only see the saved version
   - PR #10 reads from editor memory first, then falls back to disk

### Test 6.2: Mix of Saved and Unsaved Files

1. **Set up**:
   - Open `Helper.java` - modify it - SAVE IT (Ctrl+S)
   - Open `Main.java` - modify it - DON'T SAVE
   - Open `README.md` - don't modify

2. **Select all three files** in Project tree

3. **Right-click** → "Copy File Content to Clipboard"

4. **Verify**:
   - ✅ `Helper.java` = saved changes
   - ✅ `Main.java` = UNSAVED changes (in-memory)
   - ✅ `README.md` = original content

### Test 6.3: File Not Open (Fallback to Disk)

1. **Close all tabs**

2. **Copy file from Project tree**
   - Right-click `Main.java` → "Copy File Content to Clipboard"

3. **Verify**:
   - ✅ Still works! Reads from disk
   - Shows last saved version (unsaved changes are lost because editor is closed)

---

## ⚙️ Step 7: Test Settings Configuration

### Test 7.1: Open Settings

1. **Open Settings**:
   - File → Settings (Windows/Linux) or
   - IntelliJ IDEA → Preferences (Mac)

2. **Navigate to**:
   - Search: "Copy File Content"
   - Or: Tools → Copy File Content Settings

### Test 7.2: Test Header Format

1. **Change header format**:
   - Default: `// file: $FILE_PATH`
   - Change to: `### $FILE_PATH ###`
   - Click "Apply"

2. **Copy a file**

3. **Verify**:
   - ✅ Header now shows `### src/Main.java ###`

### Test 7.3: Test Pre/Post Text

1. **In Settings**:
   - Pre-text: `=== START OF FILES ===`
   - Post-text: `=== END OF FILES ===`
   - Click "Apply"

2. **Copy multiple files**

3. **Verify**:
   - ✅ Output starts with `=== START OF FILES ===`
   - ✅ Output ends with `=== END OF FILES ===`

### Test 7.4: Test File Count Limit

1. **Create many files**:
   - Create 5+ files in your test project

2. **In Settings**:
   - Enable "Set maximum file count"
   - Set limit to `2`
   - Click "Apply"

3. **Try to copy 5 files**

4. **Verify**:
   - ✅ Only 2 files copied
   - ✅ Warning notification: "File Limit Reached: The file limit of 2 files was reached"
   - ✅ Notification has "Go to Settings" action

### Test 7.5: Test File Extension Filters

1. **In Settings**:
   - Enable "Use filename filters"
   - Add filters: `.java`, `.kt`
   - Click "Apply"

2. **Try to copy mixed files**:
   - Select `Main.java`, `README.md`, `Config.properties`
   - Right-click → "Copy File Content to Clipboard"

3. **Verify**:
   - ✅ Only `Main.java` is copied
   - ✅ `.md` and `.properties` files are skipped

### Test 7.6: Test Extra Line Between Files

1. **In Settings**:
   - Toggle "Add extra line between files"
   - Test with ON, then with OFF

2. **Copy 2 files**

3. **Verify**:
   - ✅ With ON: Empty line between files
   - ✅ With OFF: No empty line

### Test 7.7: Test Notification Toggle

1. **In Settings**:
   - Uncheck "Show copy notification"
   - Click "Apply"

2. **Copy files**

3. **Verify**:
   - ✅ No notification appears (silent copy)

4. **Re-enable**:
   - Check "Show copy notification"
   - ✅ Notifications appear again

---

## 🔍 Step 8: Advanced Testing Scenarios

### Test 8.1: Large File Handling

1. **Create large file**:
   - Create `LargeFile.txt`
   - Add 2000+ lines (paste lorem ipsum repeatedly)

2. **Try to copy it**

3. **Verify**:
   - ✅ If > 100KB, file is skipped
   - Check logs: VCS → Show Log → look for "size limit exceeded"

### Test 8.2: Binary File Handling

1. **Add binary file**:
   - Copy an image (`.png`, `.jpg`) or `.class` file to project

2. **Try to copy it**

3. **Verify**:
   - ✅ Binary files are skipped automatically
   - Check logs for "Binary or size limit exceeded"

### Test 8.3: Empty File

1. **Create empty file**: `Empty.txt` (no content)

2. **Copy it**

3. **Verify**:
   - ✅ Header appears: `// file: Empty.txt`
   - ✅ Content section is empty (no crash)

### Test 8.4: Special Characters in Path

1. **Create file with spaces**: `My File.java`

2. **Copy it**

3. **Verify**:
   - ✅ Path correctly shows: `// file: src/My File.java`

---

## 🐛 Troubleshooting

### Issue: Context menu option not appearing

**Solution:**
- Restart the sandbox IDE (close and run `./gradlew runIde` again)
- Check you're right-clicking on a file/folder, not empty space

### Issue: Notification not showing

**Solution:**
- Check Settings → "Show copy notification" is enabled
- Notifications appear bottom-right - look for balloon popup

### Issue: "No project found" error

**Solution:**
- Make sure you created a project in the sandbox IDE
- Plugin requires an active project context

### Issue: Clipboard not updating

**Solution:**
- Try pasting in a different application
- On Linux, try both Ctrl+V and middle-click paste

---

## 📊 Testing Checklist

Use this checklist to verify all features work:

**Core Features:**
- [ ] Copy single file from Project tree
- [ ] Copy multiple files from Project tree
- [ ] Copy entire directory recursively
- [ ] Statistics notification appears
- [ ] File paths are relative to project root

**PR #7 - VCS Changes Window:**
- [ ] Copy single file from VCS Changes view
- [ ] Copy multiple files from Changes view
- [ ] Works in Commit window

**PR #10 - In-Memory Copy:**
- [ ] Copies unsaved changes from open files
- [ ] Falls back to disk for closed files
- [ ] Works with mix of open/closed files

**Copy All Open Tabs:**
- [ ] Copies content from all open editor tabs
- [ ] Correct order (left to right)
- [ ] Handles "no tabs open" gracefully

**Settings:**
- [ ] Header format customization works
- [ ] Pre/post text appears correctly
- [ ] File count limit enforced
- [ ] Extension filters work
- [ ] Extra line toggle works
- [ ] Notification toggle works

**Edge Cases:**
- [ ] Large files skipped (>100KB)
- [ ] Binary files skipped
- [ ] Empty files handled
- [ ] Files with spaces in name work

---

## 🎯 Quick Test (2 Minutes)

If you're short on time, run this minimal test:

```bash
# 1. Start sandbox
./gradlew runIde

# 2. Create test project with 2-3 files
# 3. Test CORE: Copy file from Project tree → verify in notepad
# 4. Test PR #7: Make change → Copy from VCS Changes window → verify
# 5. Test PR #10: Modify file WITHOUT saving → Copy → verify unsaved content appears
# 6. Test Settings: Change header format → copy file → verify new format
```

---

## 🎉 Done!

You've now tested all plugin features, including the latest enhancements from PRs #7 and #10!

**To stop the sandbox IDE:**
- Simply close the IDE window
- Gradle task will terminate automatically
- Or press `Ctrl+C` in the terminal running `./gradlew runIde`

**Next Steps:**
- If you found bugs, fix them and test again
- If everything works, the PRs are ready to merge!
- Plugin build is at: `build/distributions/Copy_File_Content-0.1.4.zip`
