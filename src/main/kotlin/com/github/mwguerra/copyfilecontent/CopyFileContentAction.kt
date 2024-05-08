package com.github.mwguerra.copyfilecontent

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VfsUtil
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import javax.swing.JOptionPane

class CopyFileContentAction : AnAction() {
    private var fileCount = 0
    private var fileLimitReached = false
    private val logger = Logger.getInstance(CopyFileContentAction::class.java)

    override fun actionPerformed(e: AnActionEvent) {
        fileCount = 0
        fileLimitReached = false
        var totalChars = 0
        var totalLines = 0
        var totalWords = 0
        var totalTokens = 0

        val project = e.project ?: run {
            JOptionPane.showMessageDialog(null, "No project found. Action cannot proceed.", "Error", JOptionPane.ERROR_MESSAGE)
            return
        }
        val selectedFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY) ?: run {
            JOptionPane.showMessageDialog(null, "No files selected.", "Error", JOptionPane.ERROR_MESSAGE)
            return
        }
        val settings = CopyFileContentSettings.getInstance(project) ?: run {
            JOptionPane.showMessageDialog(null, "Failed to load settings.", "Error", JOptionPane.ERROR_MESSAGE)
            return
        }

        val fileContents = mutableListOf<String>().apply {
            add(settings.state.preText)
        }

        for (file in selectedFiles) {
            // Check file limit only if the checkbox is selected.
            if (settings.state.setMaxFileCount && fileCount >= settings.state.fileCountLimit) {
                fileLimitReached = true
                break
            }

            val content = if (file.isDirectory) {
                processDirectory(file, fileContents, project, settings.state.addExtraLineBetweenFiles)
            } else {
                processFile(file, fileContents, project, settings.state.addExtraLineBetweenFiles)
            }

            totalChars += content.length
            totalLines += content.count { it == '\n' } + 1  // +1 because the last line doesn't end in \n.
            totalWords += content.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
            totalTokens += estimateTokens(content)
        }

        fileContents.add(settings.state.postText)
        copyToClipboard(fileContents.joinToString(separator = "\n"))

        if (settings.state.showCopyNotification) {
            val fileLimitMessage = if (fileLimitReached) {
                "\n(Note: File limit of ${settings.state.fileCountLimit} files was reached.)"
            } else {
                ""
            }
            JOptionPane.showMessageDialog(
                null,
                "Copied $fileCount files.\nTotal characters: $totalChars\nTotal lines: $totalLines\nTotal words: $totalWords\nEstimated tokens: $totalTokens$fileLimitMessage",
                "Copy Summary",
                JOptionPane.INFORMATION_MESSAGE
            )
        }
    }

    private fun estimateTokens(content: String): Int {
        val words = content.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        val punctuation = Regex("[;{}()\\[\\],]").findAll(content).count()
        return words.size + punctuation
    }

    private fun processFile(file: VirtualFile, fileContents: MutableList<String>, project: Project, addExtraLine: Boolean): String {
        val settings = CopyFileContentSettings.getInstance(project) ?: return ""
        val repositoryRoot = getRepositoryRoot(project)
        val fileRelativePath = repositoryRoot?.let { root -> VfsUtil.getRelativePath(file, root, '/') } ?: file.name
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
            content = readFileContents(file)
            fileContents.add(header)
            fileContents.add(content)
            fileCount++
            if (addExtraLine) {
                fileContents.add("")
            }
        } else {
            logger.info("Skipping file: ${file.name} - Binary or size limit exceeded")
        }
        return content
    }

    private fun processDirectory(directory: VirtualFile, fileContents: MutableList<String>, project: Project, addExtraLine: Boolean): String {
        val directoryContent = StringBuilder()
        val settings = CopyFileContentSettings.getInstance(project) ?: return ""

        for (childFile in directory.children) {
            if (settings.state.setMaxFileCount && fileCount >= settings.state.fileCountLimit) break
            val content = if (childFile.isDirectory) {
                processDirectory(childFile, fileContents, project, addExtraLine)
            } else {
                processFile(childFile, fileContents, project, addExtraLine)
            }
            directoryContent.append(content)
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
}
