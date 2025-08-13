// file: src/main/kotlin/com/github/mwguerra/copyfilecontent/CopyFileContentAction.kt
package com.github.mwguerra.copyfilecontent

import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

class CopyFileContentAction : AnAction() {
    private var fileCount = 0
    private var fileLimitReached = false
    private val logger = Logger.getInstance(CopyFileContentAction::class.java)

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: run {
            showNotification("No project found. Action cannot proceed.", NotificationType.ERROR, null)
            return
        }
        val selectedFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY) ?: run {
            showNotification("No files selected.", NotificationType.ERROR, project)
            return
        }

        performCopyFilesContent(e, selectedFiles)
    }

    fun performCopyFilesContent(e: AnActionEvent, filesToCopy: Array<VirtualFile>) {
        fileCount = 0
        fileLimitReached = false
        var totalChars = 0
        var totalLines = 0
        var totalWords = 0
        var totalTokens = 0
        val copiedFilePaths = mutableSetOf<String>()

        val project = e.project ?: return
        val settings = CopyFileContentSettings.getInstance(project) ?: run {
            showNotification("Failed to load settings.", NotificationType.ERROR, project)
            return
        }

        val fileContents = mutableListOf<String>().apply {
            add(settings.state.preText)
        }

        for (file in filesToCopy) {
            // Check file limit only if the checkbox is selected.
            if (settings.state.setMaxFileCount && fileCount >= settings.state.fileCountLimit) {
                fileLimitReached = true
                break
            }

            val content = if (file.isDirectory) {
                processDirectory(file, fileContents, copiedFilePaths, project, settings.state.addExtraLineBetweenFiles)
            } else {
                processFile(file, fileContents, copiedFilePaths, project, settings.state.addExtraLineBetweenFiles)
            }

            totalChars += content.length
            totalLines += content.count { it == '\n' } + (if (content.isNotEmpty()) 1 else 0)
            totalWords += content.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
            totalTokens += estimateTokens(content)
        }

        fileContents.add(settings.state.postText)
        copyToClipboard(fileContents.joinToString(separator = "\n"))

        if (fileLimitReached) {
            val fileLimitWarningMessage = """
                <html>
                <b>File Limit Reached:</b> The file limit of ${settings.state.fileCountLimit} files was reached.
                </html>
            """.trimIndent()
            showNotificationWithSettingsAction(fileLimitWarningMessage, NotificationType.WARNING, project)
        }

        if (settings.state.showCopyNotification) {
            val fileCountMessage = when (fileCount) {
                1 -> "1 file copied."
                else -> "$fileCount files copied."
            }

            val statisticsMessage = """
                <html>
                Total characters: $totalChars<br>
                Total lines: $totalLines<br>
                Total words: $totalWords<br>
                Estimated tokens: $totalTokens
                </html>
            """.trimIndent()

            showNotification(statisticsMessage, NotificationType.INFORMATION, project)
            showNotification("<html><b>$fileCountMessage</b></html>", NotificationType.INFORMATION, project)
        }
    }

    private fun estimateTokens(content: String): Int {
        val words = content.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        val punctuation = Regex("[;{}()\\[\\],]").findAll(content).count()
        return words.size + punctuation
    }

    private fun processFile(file: VirtualFile, fileContents: MutableList<String>, copiedFilePaths: MutableSet<String>, project: Project, addExtraLine: Boolean): String {
        val settings = CopyFileContentSettings.getInstance(project) ?: return ""
        val repositoryRoot = getRepositoryRoot(project)
        val fileRelativePath = repositoryRoot?.let { root -> VfsUtil.getRelativePath(file, root, '/') } ?: file.path

        // Skip already copied files
        if (fileRelativePath in copiedFilePaths) {
            logger.info("Skipping already copied file: $fileRelativePath")
            return ""
        }

        copiedFilePaths.add(fileRelativePath)

        var content = ""

        // If filename filters are enabled and the file extension does not match any filter, return early
        if (settings.state.useFilenameFilters) {
            if (settings.state.filenameFilters.none { filter -> file.name.endsWith(filter) }) {
                logger.info("Skipping file: ${file.name} - Extension does not match any filter")
                return ""
            }
        }

        if (!isBinaryFile(file) && file.length <= 100 * 1024) {
            val header = settings.state.headerFormat.replace("\$FILE_PATH", fileRelativePath)
            // check if the file is already loaded in-memory by the editor
            val document = FileDocumentManager.getInstance().getCachedDocument(file)
            content = document?.text ?: readFileContents(file)
            fileContents.add(header)
            fileContents.add(content)
            fileCount++
            if (addExtraLine && content.isNotEmpty()) {
                fileContents.add("")
            }
        } else {
            logger.info("Skipping file: ${file.name} - Binary or size limit exceeded")
        }
        return content
    }

    private fun processDirectory(directory: VirtualFile, fileContents: MutableList<String>, copiedFilePaths: MutableSet<String>, project: Project, addExtraLine: Boolean): String {
        val directoryContent = StringBuilder()
        val settings = CopyFileContentSettings.getInstance(project) ?: return ""

        for (childFile in directory.children) {
            if (settings.state.setMaxFileCount && fileCount >= settings.state.fileCountLimit) {
                fileLimitReached = true
                break
            }
            val content = if (childFile.isDirectory) {
                processDirectory(childFile, fileContents, copiedFilePaths, project, addExtraLine)
            } else {
                processFile(childFile, fileContents, copiedFilePaths, project, addExtraLine)
            }
            if (content.isNotEmpty()) {
                directoryContent.append(content)
            }
        }

        return directoryContent.toString()
    }

    private fun copyToClipboard(text: String) {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val data = StringSelection(text)
        clipboard.setContents(data, null)
    }

    private fun readFileContents(file: VirtualFile): String {
        return try {
            String(file.contentsToByteArray(), Charsets.UTF_8)
        } catch (e: Exception) {
            logger.error("Failed to read file contents: ${e.message}")
            ""
        }
    }

    private fun isBinaryFile(file: VirtualFile): Boolean {
        return FileTypeManager.getInstance().getFileTypeByFile(file).isBinary
    }

    private fun getRepositoryRoot(project: Project): VirtualFile? {
        val projectRootManager = ProjectRootManager.getInstance(project)
        return projectRootManager.contentRoots.firstOrNull()
    }

    companion object {
        fun showNotification(
            message: String,
            notificationType: NotificationType,
            project: Project?
        ): com.intellij.notification.Notification {
            val notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup("Copy File Content")
            val notification = notificationGroup.createNotification(message, notificationType).setImportant(true)
            notification.notify(project)
            return notification
        }
    }

    private fun showNotificationWithSettingsAction(message: String, notificationType: NotificationType, project: Project?) {
        val notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup("Copy File Content")
        val notification = notificationGroup.createNotification(message, notificationType).setImportant(true)
        notification.addAction(NotificationAction.createSimple("Go to Settings") {
            ShowSettingsUtil.getInstance().showSettingsDialog(project, "Copy File Content Settings")
        })
        notification.notify(project)
    }
}
