# ![Copy File Content Plugin Icon](src/main/resources/META-INF/pluginIcon.svg) Copy File Content Plugin

## Description
<!-- Plugin description -->
Copy File Content is a plugin that enhances your workflow by allowing you to quickly copy the contents of selected files and directories to the clipboard, formatted according to customizable settings.
<!-- Plugin description end -->

**Perfect for:**
- 📝 Sharing code snippets via chat or email
- 🤖 Building context for LLM apps (ChatGPT, Claude, Gemini, etc.)
- 📋 Code reviews and documentation
- 🔄 Quickly gathering code from multiple files

---

## ✨ Features

### Core Features

- **📁 Copy from Project Tree**: Right-click files or directories in the project explorer to copy their content
- **📑 Copy All Open Tabs**: Copy content from all currently open editor tabs at once
- **🔄 VCS Integration**: Copy files directly from the Git Changes/Commit window and VCS Log
- **💾 In-Memory Reading**: Automatically captures unsaved changes from open files
- **📊 Statistics**: Shows file count, lines, words, and estimated tokens after copying
- **🎨 Customizable Formatting**: Configure headers, pre/post text, and line spacing
- **🎯 Smart Filtering**: Filter by file extension and set maximum file count limits
- **⚡ Performance Options**: Choose between strict memory reading or cached reading for better performance

---

## 📥 Installation

### Install from JetBrains Marketplace

1. Open IntelliJ IDEA
2. Go to <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd>
3. Search for **"Copy File Content"**
4. Click <kbd>Install</kbd>

### Install Manually

1. Download the [latest release](https://github.com/mwguerra/copy-file-content/releases/latest)
2. Go to <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>
3. Select the downloaded ZIP file

---

## 🚀 Usage

### Method 1: Copy from Project Tree

1. Select one or more files/directories in the Project explorer
2. Right-click on the selection
3. Choose **"Copy File Content to Clipboard"**
4. Content is now in your clipboard with formatted headers!

### Method 2: Copy All Open Tabs

1. Open multiple files in editor tabs
2. Right-click on any tab
3. Choose **"Copy All Open Tabs Content to Clipboard"**

### Method 3: Copy from VCS Changes Window

1. Enable VCS (Git, SVN, etc.)
2. Make changes to files
3. Open the **Commit** window (left sidebar or <kbd>Alt+0</kbd>)
4. Right-click on changed files
5. Choose **"Copy File Content to Clipboard"**

---

## ⚙️ Settings

Access settings at: <kbd>Settings/Preferences</kbd> > Search for **"Copy File Content Settings"**

### Text Structure

#### Header Format
- **Default**: `// file: $FILE_PATH`
- **Description**: Template for file headers in the output
- **Variables**: `$FILE_PATH` - relative path from project root
- **Example**: Change to `### $FILE_PATH ###` for Markdown headers

#### Pre Text
- **Default**: *(empty)*
- **Description**: Text added at the very beginning of the copied content
- **Use case**: Add context like "=== CODE CONTEXT ===" or project information

#### Post Text
- **Default**: *(empty)*
- **Description**: Text added at the very end of the copied content
- **Use case**: Add footers or closing markers

#### Add Extra Line Between Files
- **Default**: Enabled ✅
- **Description**: Adds an empty line between each file's content for better readability

---

### Constraints for Copying

#### Set Maximum Number of Files
- **Default**: Enabled ✅ (30 files)
- **Description**: Limits the maximum number of files to copy
- **Purpose**: Prevents excessive memory usage and clipboard overflow
- **Recommendation**: Keep enabled for safety

#### Enable File Extension Filtering
- **Default**: Disabled
- **Description**: When enabled, only copies files with specified extensions
- **How to use**:
  1. Check "Enable file extension filtering"
  2. Click **Add** to add extensions
  3. Enter extensions like `.java`, `.kt`, `.md`, etc.
  4. Use **Remove** to delete unwanted filters

---

### File Reading Behavior

#### Strict Memory Reading ⭐ NEW in v0.1.6
- **Default**: Enabled ✅ (Strict mode)
- **Description**: Controls how the plugin reads file content

**Strict Mode (Enabled)**:
- ✅ Only reads from memory if file is **currently open in an editor tab**
- ✅ Reads from disk if file is closed
- ✅ More predictable behavior
- ❌ Slightly slower for closed files

**Non-Strict Mode (Disabled)**:
- ✅ Reads from IntelliJ's document cache even if tab is closed
- ✅ Better performance (avoids disk I/O)
- ✅ Still captures unsaved changes from recently edited files
- ❌ May read cached version instead of disk version for closed files

**Recommendation**: Keep strict mode enabled unless you need the extra performance and understand the caching behavior.

---

### Information Display

#### Show Notification After Copying
- **Default**: Enabled ✅
- **Description**: Shows a notification balloon with copy statistics
- **Statistics shown**:
  - Number of files copied
  - Total characters
  - Total lines
  - Total words
  - Estimated token count (useful for LLM context limits)

---

## 📖 Examples

### Example 1: Basic File Copy

**Input**: Right-click on `Main.java`

**Output**:
```
// file: src/Main.java
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
}
```

### Example 2: Multiple Files with Custom Header

**Settings**: Header format = `### File: $FILE_PATH`

**Input**: Select `Main.java` and `Helper.java`

**Output**:
```
### File: src/Main.java
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
}

### File: src/utils/Helper.java
public class Helper {
    public static String format(String text) {
        return text.trim();
    }
}
```

### Example 3: With Pre and Post Text

**Settings**:
- Pre-text: `=== PROJECT CONTEXT ===`
- Post-text: `=== END OF CONTEXT ===`

**Output**:
```
=== PROJECT CONTEXT ===
// file: src/Main.java
[file content]

=== END OF CONTEXT ===
```

### Example 4: Unsaved Changes (v0.1.6+)

**Scenario**:
1. Open `Main.java` in editor
2. Add line: `// TODO: implement feature`
3. **Don't save** (white dot on tab)
4. Right-click file in project tree → Copy

**Result**: ✅ **Unsaved changes are included in the output!**

---

## 🔧 Development and Testing

### Requirements
- Java 21 or higher
- Gradle 9.0+

### Build the Plugin
```bash
./gradlew build
```

Output: `build/distributions/Copy_File_Content-{version}.zip`

### Test in Sandbox IDE
```bash
./gradlew runIde
```

This launches a sandboxed IntelliJ IDEA with the plugin installed for testing.

### Run Tests
```bash
./gradlew test
```

### Code Coverage
```bash
./gradlew koverXmlReport
```

### Plugin Verification
```bash
./gradlew verifyPlugin
```

Verifies compatibility with multiple IntelliJ IDEA versions.

---

## 📦 Creating a Release

### Automated Version Update

Use the version update script:
```bash
./scripts/update_version.sh
```

The script will:
1. Prompt to confirm change notes are updated in `plugin.xml`
2. Increment version (or accept custom version)
3. Update `plugin.xml` and `gradle.properties`
4. Commit changes
5. Build the project
6. Create a git tag
7. Optionally push to origin

### Manual Steps

1. Update `<change-notes>` in `src/main/resources/META-INF/plugin.xml`
2. Update version in `gradle.properties`
3. Update version in `plugin.xml`
4. Build: `./gradlew build`
5. Upload `build/distributions/Copy_File_Content-{version}.zip` to JetBrains Marketplace

---

## 🎯 Use Cases

### For LLM Context Building
Perfect for ChatGPT, Claude, Gemini, etc.:
1. Select relevant files
2. Copy with one click
3. Paste into LLM chat
4. Token count helps manage context limits

### For Code Reviews
1. Select files in pull request
2. Copy content
3. Paste into review comments
4. Include file paths automatically

### For Documentation
1. Copy code examples from multiple files
2. Formatted headers make it clear which file each snippet is from
3. Add custom pre/post text for context

### For Sharing via Chat
1. Copy files
2. Share formatted code with teammates
3. Recipients can see exactly which files the code is from

---

## 🆕 What's New in v0.1.6

### New Features
- **Strict Memory Reading Mode**: Control whether files are read only when open in editor (strict) or from document cache (non-strict)
- **VCS Integration**: Copy files from VCS Changes/Commit window and VCS Log
- **In-Memory File Reading**: Automatically captures unsaved changes in open files

### Improvements
- Enhanced settings UI with "File Reading Behavior" section
- Improved documentation and help text
- Fixed action group warnings for better IDE compatibility

---

## 🛠️ Technical Details

### Supported IDEs
- IntelliJ IDEA (Community & Ultimate)
- WebStorm
- PyCharm
- PhpStorm
- RubyMine
- CLion
- GoLand
- Rider
- Android Studio
- And all other JetBrains IDEs!

### Compatibility
- **Minimum version**: IntelliJ IDEA 2022.3.3 (Build 223)
- **Tested up to**: IntelliJ IDEA 2025.2
- **JVM**: Requires Java 21 runtime (automatically provided by IDE)

### Platform
- **Language**: Kotlin
- **Build System**: Gradle with IntelliJ Platform Plugin 2.x
- **Plugin SDK**: IntelliJ Platform SDK

---

## 📄 License

This plugin is licensed under the [MIT License](LICENSE).

---

## 💬 Support

- **Issues**: [GitHub Issues](https://github.com/mwguerra/copy-file-content/issues)
- **Feature Requests**: [GitHub Issues](https://github.com/mwguerra/copy-file-content/issues)
- **Discussions**: [GitHub Discussions](https://github.com/mwguerra/copy-file-content/discussions)

---

## 👤 Author

**Marcelo W. Guerra**
- Email: [mwguerra@gmail.com](mailto:mwguerra@gmail.com)
- Website: [mwguerra.com](https://mwguerra.com)

---

## 🙏 Contributors

Thank you to all contributors who have helped make this plugin better!

See the [Contributors page](https://github.com/mwguerra/copy-file-content/graphs/contributors) for the full list.

---

## 🔗 Links

- [JetBrains Plugin Marketplace](#) *(Coming soon)*
- [GitHub Repository](https://github.com/mwguerra/copy-file-content)
- [Issue Tracker](https://github.com/mwguerra/copy-file-content/issues)
- [Latest Release](https://github.com/mwguerra/copy-file-content/releases/latest)

---

*Plugin based on the [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)*
