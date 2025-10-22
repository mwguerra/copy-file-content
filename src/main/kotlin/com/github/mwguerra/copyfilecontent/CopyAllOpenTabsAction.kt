package com.github.mwguerra.copyfilecontent

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager

class CopyAllOpenTabsAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        // Gather all open files (open tabs) in the current project
        val openFiles = FileEditorManager.getInstance(project).openFiles
        if (openFiles.isEmpty()) {
            CopyFileContentAction.showNotification(
                "No open tabs found to copy.",
                com.intellij.notification.NotificationType.INFORMATION,
                project
            )
            return
        }

        // Reuse the existing CopyFileContentAction but bypass its direct usage of CommonDataKeys
        CopyFileContentAction().performCopyFilesContent(e, openFiles)
    }
}
