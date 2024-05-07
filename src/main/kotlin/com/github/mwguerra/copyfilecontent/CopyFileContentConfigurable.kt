package com.github.mwguerra.copyfilecontent

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.JBColor
import com.intellij.ui.components.*
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import java.awt.*
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.table.DefaultTableModel
import com.intellij.ui.RoundedLineBorder;

class CopyFileContentConfigurable(private val project: Project) : Configurable {
    private var settings: CopyFileContentSettings? = null
    private val headerFormatArea = JBTextArea(4, 20).apply {
        border = JBUI.Borders.merge(
            JBUI.Borders.empty(5),
            RoundedLineBorder(JBColor.LIGHT_GRAY, 4, 1),
            true
        )
    }

    private val preTextArea = JBTextArea(4, 20).apply {
        border = JBUI.Borders.merge(
            JBUI.Borders.empty(5),
            RoundedLineBorder(JBColor.LIGHT_GRAY, 4, 1),
            true
        )
    }

    private val postTextArea = JBTextArea(4, 20).apply {
        border = JBUI.Borders.merge(
            JBUI.Borders.empty(5),
            RoundedLineBorder(JBColor.LIGHT_GRAY, 4, 1),
            true
        )
    }
    private val extraLineCheckBox = JBCheckBox("Add an extra line between files")
    private val setMaxFilesCheckBox = JBCheckBox("Set maximum number of files to have their content copied")
    private val maxFilesField = JBTextField(10)
    private val showNotificationCheckBox = JBCheckBox("Show notification after copying")
    private val useFilenameFiltersCheckBox = JBCheckBox("Enable file extension filtering")
    private val tableModel = DefaultTableModel()
    private val table = JBTable(tableModel)
    private val addButton = JButton("Add")
    private val removeButton = JButton("Remove")
    private val filenameFiltersPanel = createFilenameFiltersPanel()

    init {
        tableModel.addColumn("File Extensions")
        setupTable()

        setMaxFilesCheckBox.addActionListener {
            maxFilesField.isVisible = setMaxFilesCheckBox.isSelected
        }

        useFilenameFiltersCheckBox.addActionListener {
            filenameFiltersPanel.isVisible = useFilenameFiltersCheckBox.isSelected
        }
    }

    private fun setupTable() {
        settings?.state?.filenameFilters?.forEach {
            tableModel.addRow(arrayOf(it))
        }

        addButton.addActionListener {
            val extension = Messages.showInputDialog("Enter file extension:", "Add Filter", null)
            if (!extension.isNullOrBlank()) {
                tableModel.addRow(arrayOf(extension.trim()))
            }
        }

        removeButton.addActionListener {
            val selectedRow = table.selectedRow
            if (selectedRow != -1) {
                tableModel.removeRow(selectedRow)
            }
        }
    }

    override fun createComponent(): JComponent {
        settings = CopyFileContentSettings.getInstance(project)

        maxFilesField.isVisible = setMaxFilesCheckBox.isSelected
        filenameFiltersPanel.isVisible = useFilenameFiltersCheckBox.isSelected

        return FormBuilder.createFormBuilder()
            .addComponentFillVertically(createSection("Text structure of whats going to the clipboard") {
                it.add(createLabeledPanel("Pre Text:", preTextArea), BorderLayout.NORTH)
                it.add(createLabeledPanel("File Header Format:", headerFormatArea), BorderLayout.CENTER)
                it.add(createLabeledPanel("Post Text:", postTextArea), BorderLayout.SOUTH)
                it.add(createLabeledPanel("", extraLineCheckBox))
            }, 0)
            .addComponentFillVertically(createSection("Constraints for copying") {
                it.add(createInlinePanel(createWrappedCheckBoxPanel(setMaxFilesCheckBox), maxFilesField))
                it.add(createInlinePanel(createWrappedCheckBoxPanel(useFilenameFiltersCheckBox), filenameFiltersPanel))
            }, 0)
            .addComponentFillVertically(createSection("Information on what have been copied") {
                it.add(showNotificationCheckBox)
            }, 0)
            .panel
    }

    private fun createSectionDivider(title: String = ""): JPanel {
        val panel = JPanel(BorderLayout())
        panel.border = IdeBorderFactory.createTitledBorder(title, false, JBUI.insetsTop(20))

        return panel
    }

    private fun createInlinePanel(leftComponent: JComponent, rightComponent: JComponent, spacing: Int = 10): JPanel {
        val panel = JPanel(BorderLayout())

        val leftWrapper = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        leftWrapper.add(leftComponent)
        leftWrapper.border = JBUI.Borders.emptyRight(spacing)

        val rightWrapper = JPanel(BorderLayout())
        rightWrapper.add(rightComponent, BorderLayout.CENTER)

        panel.add(leftWrapper, BorderLayout.WEST)
        panel.add(rightWrapper, BorderLayout.CENTER)

        return panel
    }

    private fun createWrappedCheckBoxPanel(checkBox: JBCheckBox, paddingTop: Int = 4): JPanel {
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.emptyTop(paddingTop)
        panel.add(checkBox)
        return panel
    }

    private fun createCollapsibleSection(title: String = "", content: (JPanel) -> Unit): JPanel {
        val collapsiblePanel = JPanel(BorderLayout())
        content(collapsiblePanel)

        val panel = JPanel(BorderLayout())
        val titleBorder = IdeBorderFactory.createTitledBorder(title, false, JBUI.insetsTop(8))
        panel.border = titleBorder
        panel.add(CollapsiblePanel(title, collapsiblePanel), BorderLayout.CENTER)

        return panel
    }

    private fun createSection(title: String = "", content: (JPanel) -> Unit): JPanel {
        val panel = JPanel(BorderLayout())
        val sectionContent = Box.createVerticalBox()

        // Panel to contain the inner content
        val contentPanel = JPanel()
        contentPanel.layout = BoxLayout(contentPanel, BoxLayout.Y_AXIS)
        contentPanel.border = JBUI.Borders.emptyRight(10)
        content(contentPanel)

        // Add margin to each component inside the content panel
        for (component in contentPanel.components) {
            if (component is JComponent) {
                component.border = JBUI.Borders.emptyBottom(10)
            }
        }

        sectionContent.add(contentPanel)

        val divider = createSectionDivider(title)
        panel.add(divider, BorderLayout.NORTH)
        panel.add(sectionContent, BorderLayout.CENTER)

        return panel
    }

    private fun createCollapsibleTextInputsPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        val titleBorder = IdeBorderFactory.createTitledBorder("Text Options", false, JBUI.insetsTop(8))
        panel.border = titleBorder

        val collapsiblePanel = JPanel(BorderLayout())
        collapsiblePanel.add(createLabeledPanel("Pre Text:", preTextArea), BorderLayout.NORTH)
        collapsiblePanel.add(createLabeledPanel("File Header Format:", headerFormatArea), BorderLayout.CENTER)
        collapsiblePanel.add(createLabeledPanel("Post Text:", postTextArea), BorderLayout.SOUTH)

        panel.add(CollapsiblePanel("Text Options", collapsiblePanel), BorderLayout.CENTER)

        return panel
    }

    class CollapsiblePanel(private val title: String, content: JPanel) : JPanel(BorderLayout()) {
        private val toggleButton: JButton = JButton(title)

        init {
            toggleButton.isContentAreaFilled = false
            toggleButton.isOpaque = false
            toggleButton.border = BorderFactory.createEmptyBorder()
            toggleButton.margin = JBUI.emptyInsets()
            toggleButton.horizontalAlignment = SwingConstants.LEFT
            toggleButton.preferredSize = Dimension(0, 24)

            toggleButton.addActionListener {
                content.isVisible = !content.isVisible
                updateToggleButtonText(content.isVisible)
            }
            updateToggleButtonText(content.isVisible)

            val headerPanel = JPanel(BorderLayout())
            headerPanel.add(toggleButton, BorderLayout.WEST)
            headerPanel.border = JBUI.Borders.empty(4, 0)

            add(headerPanel, BorderLayout.NORTH)
            add(content, BorderLayout.CENTER)
        }

        private fun updateToggleButtonText(expanded: Boolean) {
            toggleButton.text = if (expanded) "▼ $title" else "▶ $title"
        }
    }

    private fun createFilenameFiltersPanel(): JPanel {
        val panel = JPanel(BorderLayout())

        val scrollPane = JBScrollPane(table)
        scrollPane.preferredSize = Dimension(250, 100)
        panel.add(scrollPane, BorderLayout.CENTER)

        val buttonPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        buttonPanel.add(addButton)
        buttonPanel.add(removeButton)
        panel.add(buttonPanel, BorderLayout.SOUTH)

        return panel
    }

    private fun createLabeledPanel(title: String, component: JComponent): JPanel {
        val label = JLabel(title)
        label.border = JBUI.Borders.emptyBottom(4)

        val panel = JPanel(BorderLayout())
        panel.add(label, BorderLayout.NORTH)
        panel.add(component, BorderLayout.CENTER)

        return panel
    }

    override fun isModified(): Boolean {
        return settings?.let {
            val currentFilters = List(tableModel.rowCount) { row -> tableModel.getValueAt(row, 0) as String }
            it.state.filenameFilters != currentFilters ||
                    headerFormatArea.text != it.state.headerFormat ||
                    preTextArea.text != it.state.preText ||
                    postTextArea.text != it.state.postText ||
                    extraLineCheckBox.isSelected != it.state.addExtraLineBetweenFiles ||
                    setMaxFilesCheckBox.isSelected != it.state.setMaxFileCount ||
                    (setMaxFilesCheckBox.isSelected && maxFilesField.text.toIntOrNull() != it.state.fileCountLimit) ||
                    showNotificationCheckBox.isSelected != it.state.showCopyNotification ||
                    useFilenameFiltersCheckBox.isSelected != it.state.useFilenameFilters
        } ?: false
    }

    override fun apply() {
        settings?.let {
            it.state.filenameFilters = List(tableModel.rowCount) { row -> tableModel.getValueAt(row, 0) as String }
            it.state.headerFormat = headerFormatArea.text
            it.state.preText = preTextArea.text
            it.state.postText = postTextArea.text
            it.state.addExtraLineBetweenFiles = extraLineCheckBox.isSelected
            it.state.setMaxFileCount = setMaxFilesCheckBox.isSelected
            it.state.fileCountLimit = maxFilesField.text.toIntOrNull() ?: 50
            it.state.showCopyNotification = showNotificationCheckBox.isSelected
            it.state.useFilenameFilters = useFilenameFiltersCheckBox.isSelected
        }
    }

    override fun getDisplayName(): String = "Copy File Content Settings"

    override fun reset() {
        settings?.let {
            headerFormatArea.text = it.state.headerFormat
            preTextArea.text = it.state.preText
            postTextArea.text = it.state.postText
            extraLineCheckBox.isSelected = it.state.addExtraLineBetweenFiles
            setMaxFilesCheckBox.isSelected = it.state.setMaxFileCount
            maxFilesField.text = it.state.fileCountLimit.toString()
            showNotificationCheckBox.isSelected = it.state.showCopyNotification
            useFilenameFiltersCheckBox.isSelected = it.state.useFilenameFilters
            tableModel.setRowCount(0)
            it.state.filenameFilters.forEach { filter ->
                tableModel.addRow(arrayOf(filter))
            }
            maxFilesField.isVisible = it.state.setMaxFileCount
            filenameFiltersPanel.isVisible = it.state.useFilenameFilters
        }
    }
}
