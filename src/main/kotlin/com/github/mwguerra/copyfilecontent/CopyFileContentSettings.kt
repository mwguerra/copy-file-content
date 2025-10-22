package com.github.mwguerra.copyfilecontent

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
@State(
    name = "CopyFileContentSettings",
    storages = [Storage("CopyFileContentSettings.xml")]
)
class CopyFileContentSettings : PersistentStateComponent<CopyFileContentSettings.State> {
    data class State(
        var headerFormat: String = "// file: \$FILE_PATH",
        var preText: String = "",
        var postText: String = "",
        var fileCountLimit: Int = 30,
        var filenameFilters: List<String> = listOf(),
        var addExtraLineBetweenFiles: Boolean = true,
        var setMaxFileCount: Boolean = true,
        var showCopyNotification: Boolean = true,
        var useFilenameFilters: Boolean = false,
        var strictMemoryRead: Boolean = true,  // Only read from memory if file is open in editor
        var maxFileSizeKB: Int = 500  // Maximum file size in KB to copy (default 500KB)
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    companion object {
        fun getInstance(project: Project): CopyFileContentSettings? {
            return project.getService(CopyFileContentSettings::class.java)
        }
    }
}