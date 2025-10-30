package de.achimonline.ansible_vault_integration.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import de.achimonline.ansible_vault_integration.bundle.AnsibleVaultIntegrationBundle.message
import java.io.File

open class AnsibleVaultSettingsConfigurable(project: Project) : BoundConfigurable(message("settings.display_name")) {
    private lateinit var executionTimeoutField: JBTextField
    private lateinit var argumentsField: JBTextField
    private lateinit var executableField: TextFieldWithBrowseButton

    private val pluginSettings: AnsibleVaultSettings = AnsibleVaultSettings.getInstance(project)

    private val heartIcon = IconLoader.getIcon("/icons/heart-solid.svg", AnsibleVaultSettingsConfigurable::class.java)

    override fun createPanel(): DialogPanel {
        return panel {
            row(message("settings.executable_section.title")) {
                textFieldWithBrowseButton(
                    fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor()
                )
                    .resizableColumn()
                    .comment(message("settings.executable_section.comment"))
                    .align(Align.FILL)
                    .bindText(pluginSettings::vaultExecutable)
                    .also { executableField = it.component }
            }

            row(message("settings.args_section.title")) {
                textField()
                    .bindText(pluginSettings::vaultArguments)
                    .comment(message("settings.args_section.comment"))
                    .align(Align.FILL)
                    .focused()
                    .also { argumentsField = it.component }
            }

            row(message("settings.execution_timeout_section.title")) {
                intTextField()
                    .bindText(
                        { pluginSettings.timeout.toString() },
                        { pluginSettings.timeout = it.tryParseInt() ?: return@bindText }
                    )
                    .comment(message("settings.execution_timeout_section.comment"))
                    .align(Align.FILL)
                    .also { executionTimeoutField = it.component }
            }

            group {
                row {
                    icon(heartIcon)
                    text(
                        message(
                            "settings.feedback",
                            "https://paypal.me/AchimSeufert",
                            "https://github.com/4ch1m/idea-ansible-vault-integration",
                            "https://plugins.jetbrains.com/plugin/14353-ansible-vault-integration"
                        )
                    )
                }
            }
        }
    }

    override fun getHelpTopic(): String = message("settings.help_topic")

    override fun apply() {
        if (!File(executableField.text).exists()) {
            throw ConfigurationException(
                message("settings.executable_section.validation.invalid_executable")
            )
        }

        val timeout = executionTimeoutField.text.tryParseInt()

        if (timeout == null || timeout < 1) {
            throw ConfigurationException(
                message("settings.executable_section.validation.invalid_timeout")
            )
        }

        super.apply()
    }
}

private fun String.tryParseInt(): Int? = try {
    Integer.parseInt(this)
} catch (_: Exception) {
    null
}
