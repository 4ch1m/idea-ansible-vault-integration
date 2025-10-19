package de.achimonline.ansible_vault_integration.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import de.achimonline.ansible_vault_integration.bundle.AnsibleVaultIntegrationBundle
import org.jetbrains.annotations.Nls
import java.io.File
import javax.swing.JComponent

open class AnsibleVaultSettingsConfigurable(project: Project) : Configurable {
    private lateinit var executionTimeoutField: JBTextField
    private lateinit var argumentsField: JBTextField
    private lateinit var executableField: TextFieldWithBrowseButton

    private val pluginSettings: AnsibleVaultSettings = AnsibleVaultSettings.getInstance(project)

    private val heartIcon = IconLoader.getIcon("/icons/heart-solid.svg", AnsibleVaultSettingsConfigurable::class.java)

    private val panel = panel {
        row(AnsibleVaultIntegrationBundle.message("settings.executable_section.title")) {
            textFieldWithBrowseButton(
                fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor(),
                project = project
            )
                .comment(AnsibleVaultIntegrationBundle.message("settings.executable_section.comment"))
                .align(Align.FILL)
                .bindText(pluginSettings::vaultExecutable)
                .also { executableField = it.component }
        }

        row(AnsibleVaultIntegrationBundle.message("settings.args_section.title")) {
            textField()
                .bindText(pluginSettings::vaultArguments)
                .comment(AnsibleVaultIntegrationBundle.getMessage("settings.args_section.comment"))
                .align(Align.FILL)
                .focused()
                .also { argumentsField = it.component }
        }

        row("Execution Timeout") {
            textField()
                .bindText(
                    { pluginSettings.timeout.toString() },
                    { pluginSettings.timeout = it.tryParseInt() ?: return@bindText }
                )
                .comment("Amount in seconds to wait before stopping execution forcefully")
                .align(Align.FILL)
                .also { executionTimeoutField = it.component }
        }
        group {
            row {
                icon(heartIcon)
                text(
                    AnsibleVaultIntegrationBundle.getMessage(
                        "settings.feedback",
                        "https://paypal.me/AchimSeufert",
                        "https://github.com/4ch1m/idea-ansible-vault-integration",
                        "https://plugins.jetbrains.com/plugin/14353-ansible-vault-integration"
                    )
                )
            }
        }

    }

    override fun getDisplayName(): @Nls String =  AnsibleVaultIntegrationBundle.message("settings.display_name")
    override fun getHelpTopic(): String = AnsibleVaultIntegrationBundle.message("settings.help_topic")
    override fun createComponent(): JComponent = panel
    override fun isModified(): Boolean = panel.isModified()

    override fun apply() {
        if (!File(executableField.text).exists()) {
            throw ConfigurationException(
                AnsibleVaultIntegrationBundle.message("settings.executable_section.validation.invalid_executable")
            )
        }

        val timeout = executionTimeoutField.text.tryParseInt()

        if (timeout == null || timeout < 1) {
            throw ConfigurationException(
                AnsibleVaultIntegrationBundle.message("settings.executable_section.validation.invalid_timeout")
            )
        }

        panel.apply()
    }

    override fun reset() = panel.reset()
}

private fun String.tryParseInt(): Int? = try {
    Integer.parseInt(this)
} catch (_: Exception) {
    null
}
