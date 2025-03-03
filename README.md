[//]: # (![Build]&#40;https://github.com/mwguerra/copy-file-content/workflows/Build/badge.svg&#41;)

[//]: # ([![Version]&#40;https://img.shields.io/jetbrains/plugin/v/PLUGIN_ID.svg&#41;]&#40;https://plugins.jetbrains.com/plugin/PLUGIN_ID&#41;)

[//]: # ([![Downloads]&#40;https://img.shields.io/jetbrains/plugin/d/PLUGIN_ID.svg&#41;]&#40;https://plugins.jetbrains.com/plugin/PLUGIN_ID&#41;)

# ![Copy File Content Plugin Icon](src/main/resources/META-INF/pluginIcon.svg) Copy File Content Plugin

## Description
<!-- Plugin description -->
Copy File Content is a plugin that enhances your workflow by allowing you to quickly copy the contents of selected files and directories to the clipboard, formatted according to customizable settings.
<!-- Plugin description end -->

## Installation

To install the Copy File Content plugin in IntelliJ IDEA, follow these steps:

### Install using the IDE built-in plugin system

<kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "copy-file-content"</kbd> >
<kbd>Install</kbd>

### Install manually

Download the [latest release](https://github.com/mwguerra/copy-file-content/releases/latest) and install it manually using
<kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Usage

Once the plugin is installed, you can use it to copy file content in the following way:

1. Select the files or directories whose content you want to copy.
2. Right-click on the selection.
3. Choose the "Copy File Content to Clipboard" option from the context menu.

Settings can be found searching for `Copy File Content Settings` under `Preferences`.

## Development and Testing

### Requirements
- Java is required to build and run the plugin.

### Testing the UI
To test the plugin UI in the IDE, run:
```
./gradlew runIde
```

### Creating a New Version and Release
1. Update the change notes in `plugin.xml`
2. Run the provided release script:
```
./release.sh
```
3. The script will:
  - Ask for confirmation that you've updated the change notes
  - Allow you to specify a new version (or use the default incremented version)
  - Update version numbers in both `plugin.xml` and `gradle.properties`
  - Build the project
  - Create a git tag for the new version
  - Optionally push changes to your repository

4. After running the script, the binary will be generated at:
```
build/distributions/Copy_File_Content-{version}.zip
```

5. Upload this `.zip` file to the JetBrains Plugin Directory through your developer account.

## Features

- **Copy Content**: Copy the content of multiple files to the clipboard.
- **Customizable Text Structure**: Define pre-text, file header text, and post-text for copied content.
- **File Copying Constraints**: Set a maximum number of files to copy content from to prevent memory issues.
- **File Extension Filters**: Specify which file extensions should be copied.
- **Copy Information**: Receive information about the copied items, including the number of files copied, total lines, total words, and an estimate of tokens for the copied content.

## License

This plugin is licensed under the [MIT License](LICENSE).

## Support

For any issues or feature requests, please [open an issue](https://github.com/mwguerra/copy-file-content/issues) on GitHub.

## Author

This plugin is developed by Marcelo W. Guerra. You can contact the author via email at [mwguerra@gmail.com](mailto:mwguerra@gmail.com) or visit his [website](https://mwguerra.com) for more information.

## Contributors

For simplicity, contributors are shown on the [GitHub repository page](https://github.com/mwguerra/copy-file-content/graphs/contributors). A heartfelt thank you to all the contributors who have helped make this plugin better!

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation