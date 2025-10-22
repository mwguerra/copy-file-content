# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an IntelliJ IDEA plugin called "Copy File Content" that allows users to copy the contents of selected files and directories to the clipboard with customizable formatting. The plugin is built using Kotlin and the IntelliJ Platform Plugin SDK.

## Build & Development Commands

### Running the Plugin in Development
```bash
./gradlew runIde
```
This launches a sandboxed IDE instance with the plugin installed for testing.

### Building the Plugin
```bash
./gradlew build
```
The distributable ZIP file will be created at: `build/distributions/Copy_File_Content-{version}.zip`

### Running Tests
```bash
./gradlew test
```

### Code Coverage
```bash
./gradlew koverReport
```

### Running UI Tests
```bash
./gradlew runIdeForUiTests
```

## Version Management & Release Process

### Creating a New Release
Use the automated version update script:
```bash
./scripts/update_version.sh
```

This script:
1. Prompts to confirm change notes in `plugin.xml` were updated
2. Increments version (or accepts custom version)
3. Updates version in both `plugin.xml` and `gradle.properties`
4. Commits any uncommitted changes
5. Commits the version change with message: `chore: version changed to {version}`
6. Builds the project
7. Creates a git tag
8. Optionally pushes changes and tags to origin

**IMPORTANT**: Always update the `<change-notes>` section in `src/main/resources/META-INF/plugin.xml` before running the release script.

## Architecture

### Core Components

**CopyFileContentAction** (`src/main/kotlin/com/github/mwguerra/copyfilecontent/CopyFileContentAction.kt`)
- Main action triggered from the context menu on selected files/directories
- Recursively processes files and directories
- Respects file count limits and extension filters
- Skips binary files and files larger than 100KB
- Provides clipboard content with customizable formatting
- Shows notifications with statistics (file count, lines, words, estimated tokens)

**CopyAllOpenTabsAction** (`src/main/kotlin/com/github/mwguerra/copyfilecontent/CopyAllOpenTabsAction.kt`)
- Copies content from all currently open editor tabs
- Reuses `CopyFileContentAction.performCopyFilesContent()` for consistent formatting

**CopyFileContentSettings** (`src/main/kotlin/com/github/mwguerra/copyfilecontent/CopyFileContentSettings.kt`)
- Project-level persistent settings component
- Stores configuration in `.idea/CopyFileContentSettings.xml`
- Settings include:
  - `headerFormat`: Template for file headers (default: `// file: $FILE_PATH`)
  - `preText` / `postText`: Text added before/after copied content
  - `fileCountLimit`: Maximum files to copy (default: 30)
  - `filenameFilters`: List of file extensions to include
  - `addExtraLineBetweenFiles`: Spacing between files
  - `setMaxFileCount`: Whether to enforce file limit
  - `showCopyNotification`: Whether to show success notifications
  - `useFilenameFilters`: Whether to apply extension filters

**CopyFileContentConfigurable** (`src/main/kotlin/com/github/mwguerra/copyfilecontent/CopyFileContentConfigurable.kt`)
- Settings UI panel accessible via IDE Preferences → Copy File Content Settings

### Plugin Configuration

The plugin is configured in `src/main/resources/META-INF/plugin.xml`:
- Registers two actions: one for the file tree context menu, one for the editor tab context menu
- Defines notification group for displaying messages
- Plugin metadata (version, description, change notes) are maintained here
- Version must be kept in sync with `gradle.properties`

### Key Behaviors

- **File Path Resolution**: Uses project content root as the base for relative paths
- **Deduplication**: Tracks copied files by path to avoid duplicates when processing directories
- **Token Estimation**: Simple heuristic based on word count + punctuation count
- **Binary Detection**: Uses IntelliJ's `FileTypeManager` to skip binary files
- **Context Menu Integration**: Actions appear in `CutCopyPasteGroup` and `EditorTabPopupMenu`

## Version Management

The plugin version is maintained in two locations and must be kept in sync:
- `src/main/resources/META-INF/plugin.xml` (`<version>` tag)
- `gradle.properties` (`pluginVersion` property)

The build process uses `gradle.properties` as the source of truth and patches `plugin.xml` during the build.

## Platform Compatibility

- **Min Build**: 223 (IntelliJ IDEA 2022.3.3+)
- **Target Platform**: IntelliJ Community (`IC`)
- **JVM Toolchain**: Java 17
- **Kotlin**: Managed via Gradle version catalog

## Important Conventions

- When committing version changes, use the format: `chore: version changed to X.Y.Z`
- Always update change notes in `plugin.xml` before releasing
- The plugin description in `plugin.xml` is extracted from README.md during build
- Change notes from CHANGELOG.md or `plugin.xml` are used in the release
