package de.achimonline.ansible_vault_integration.execution

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.project.Project
import de.achimonline.ansible_vault_integration.bundle.AnsibleVaultIntegrationBundle.message
import de.achimonline.ansible_vault_integration.runnable.AnsibleVaultRunnable
import de.achimonline.ansible_vault_integration.runnable.VaultRunnableMode
import de.achimonline.ansible_vault_integration.runnable.VaultRunnableType
import org.jetbrains.annotations.Nls

/**
 * Background task abstraction for executing ansible vault operations
 */
class AnsibleVaultTaskRunner(
    project: Project?,
    private val initialTitle: @Nls(capitalization = Nls.Capitalization.Sentence) String,
    private val tasks: List<AnsibleVaultRunnable>
) : Backgroundable(project, initialTitle) {
    override fun run(indicator: ProgressIndicator) {
        indicator.isIndeterminate = true
        indicator.text = this.initialTitle

        val succeeded = mutableListOf<AnsibleVaultRunnable>()
        val failed = mutableListOf<Pair<AnsibleVaultRunnable, Exception>>()

        tasks.forEach {
            try {
                runTask(indicator, it)
                succeeded += it
            } catch (e: Exception) {
                failed += Pair(it, e)
            }
        }

        var notification: Notification? = null

        val actionsExecuted = toHtmList(
            message("notification.multiple.heading.successful_actions"),
            succeeded.map { it.type.stringify(it.fileName) }
        )

        val actionsFailed = toHtmList(
            message("notification.multiple.heading.failed_actions"),
            failed.map { "${it.first.type.stringify(it.first.fileName)}: <code>${it.second.message ?: "Unexpected error"}</code>" }
        )

        if (failed.size > 1 || succeeded.size > 1) { // for multiple selected
            when {
                failed.isEmpty() -> { // no failed tasks
                    notification = Notification(
                        NOTIFICATION_GROUP_ID,
                        message(
                            "notification.multiple.title.success",
                            succeeded.size
                        ),
                        actionsExecuted,
                        NotificationType.INFORMATION
                    )
                }

                succeeded.isNotEmpty() -> { // partially failed
                    notification = Notification(
                        NOTIFICATION_GROUP_ID,
                        message(
                            "notification.multiple.title.partial",
                            succeeded.size + failed.size,
                            failed.size
                        ),
                        "$actionsExecuted\n<br />$actionsFailed",
                        NotificationType.WARNING
                    )
                }

                else -> { // all failed
                    notification = Notification(
                        NOTIFICATION_GROUP_ID,
                        message(
                            "notification.multiple.title.failure",
                            failed.size
                        ),
                        actionsFailed,
                        NotificationType.WARNING
                    )
                }
            }
        } else { // for a single file
            when {
                failed.size == 1 -> {
                    notification = Notification(
                        NOTIFICATION_GROUP_ID,
                        message(
                            "notifications.single.title.failure",
                            failed[0].first.type,
                            failed[0].first.fileName
                        ),
                        failed[0].second.message ?: "Unknown error",
                        NotificationType.ERROR
                    )
                }

                succeeded.size == 1 -> {
                    val isInline = succeeded[0].mode == VaultRunnableMode.INLINE

                    notification = Notification(
                        NOTIFICATION_GROUP_ID,
                        message(
                            "notifications.single.title.success",
                            succeeded[0].type,
                            succeeded[0].fileName
                        ),
                        if (isInline && succeeded[0].type == VaultRunnableType.DECRYPT) message(
                            "notifications.single.body.clipboard"
                        ) else "",
                        NotificationType.INFORMATION
                    )
                }
            }
        }

        if (notification != null) {
            Notifications.Bus.notify(notification)
        }
    }

    private fun toHtmList(title: String, items: List<String>): String {
        return """
            <p>${message("notifications.multiple.report_teaser")}</p>
            <br />
            <strong>${title}</strong>
            <ul>
                ${items.joinToString("\n") { "<li>${it}</li>" }}
            </ul>
        """.trimIndent()
    }

    private fun runTask(indicator: ProgressIndicator, task: AnsibleVaultRunnable) {
        indicator.text = message("notification.multiple.indicator", task.type, task.fileName)
        task.run()
    }

    companion object {
        val NOTIFICATION_GROUP_ID: String = AnsibleVaultTaskRunner::class.java.canonicalName
    }
}
