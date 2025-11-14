package de.achimonline.ansible_vault_integration.intention.menu

import com.intellij.ide.SaveAndSyncHandler
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import de.achimonline.ansible_vault_integration.bundle.AnsibleVaultIntegrationBundle.message
import de.achimonline.ansible_vault_integration.config.AnsibleConfigurationService
import de.achimonline.ansible_vault_integration.config.VaultIdentity
import de.achimonline.ansible_vault_integration.execution.AnsibleVaultTaskRunner
import de.achimonline.ansible_vault_integration.intention.AnsibleVaultIdentityPopup
import de.achimonline.ansible_vault_integration.runnable.file.DecryptFileAnsibleVaultRunnable
import de.achimonline.ansible_vault_integration.runnable.file.EncryptFileAnsibleVaultRunnable
import de.achimonline.ansible_vault_integration.util.AnsibleVaultedStringUtil
import kotlinx.coroutines.runBlocking

open class VaultFileMenuAction : AnAction() {
    private data class VaultFile(
        val psiFile: PsiFile,
        val isCurrentlyVaulted: Boolean
    )

    private val progressManager: ProgressManager = ProgressManager.getInstance()
    private val fileDocumentManager = FileDocumentManager.getInstance()

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        val project = anActionEvent.project ?: return
        val psiManager = PsiManager.getInstance(project)

        val vaultIdentities = AnsibleConfigurationService.getInstance(project).getAggregatedConfig().vaultIdentities ?: emptyList()
        val filesToProcess = mutableListOf<VaultFile>()

        anActionEvent.getData(LangDataKeys.VIRTUAL_FILE_ARRAY)?.forEach { virtualFile ->
            val psiFile = psiManager.findFile(virtualFile) ?: return@forEach

            fileDocumentManager.getDocument(virtualFile)?.let { document ->
                fileDocumentManager.saveDocument(document)
            }

            filesToProcess.add(
                VaultFile(psiFile, AnsibleVaultedStringUtil.isVaultedFile(psiFile.virtualFile))
            )
        }

        if (filesToProcess.isNotEmpty()) {
            if (vaultIdentities.isNotEmpty() && filesToProcess.any { !it.isCurrentlyVaulted }) {
                AnsibleVaultIdentityPopup(vaultIdentities) {
                    process(
                        project,
                        filesToProcess,
                        it
                    )
                }.showCentered()
            } else {
                process(project, filesToProcess)
            }
        }
    }

    private fun process(
        project: Project,
        vaultFiles: List<VaultFile>,
        vaultIdentity: VaultIdentity? = null
    ) {
        runBlocking {
            val runnables = vaultFiles.map { vaultFile ->
                if (vaultFile.isCurrentlyVaulted) {
                    DecryptFileAnsibleVaultRunnable(vaultFile.psiFile)
                } else {
                    EncryptFileAnsibleVaultRunnable(vaultFile.psiFile, vaultIdentity, false)
                }
            }

            progressManager.run(
                AnsibleVaultTaskRunner(
                    project,
                    message("background_task.multiple.initial"),
                    runnables
                )
            )

            SaveAndSyncHandler.getInstance().refreshOpenFiles()
        }
    }
}
