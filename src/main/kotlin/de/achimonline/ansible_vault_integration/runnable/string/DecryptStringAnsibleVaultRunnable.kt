package de.achimonline.ansible_vault_integration.runnable.string

import com.intellij.ide.ClipboardSynchronizer
import com.intellij.ide.CopyPasteManagerEx
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.util.ui.TextTransferable
import de.achimonline.ansible_vault_integration.execution.action.AnsibleVaultDecryptAction
import de.achimonline.ansible_vault_integration.runnable.AnsibleVaultRunnable
import de.achimonline.ansible_vault_integration.runnable.VaultRunnableMode
import de.achimonline.ansible_vault_integration.runnable.VaultRunnableType

class DecryptStringAnsibleVaultRunnable(
    private val project: Project,
    private val containingFile: PsiFile,
    private val raw: String
) : AnsibleVaultRunnable {
    @Throws(Exception::class)
    override fun run() {
        val decrypted = AnsibleVaultDecryptAction(
            project = project,
            contextFile = containingFile,
            trimResult = true,
            encrypted = raw
        ).execute()

        ClipboardSynchronizer.getInstance()
            .setContent(TextTransferable(decrypted, decrypted), CopyPasteManagerEx.getInstanceEx())
    }

    override val fileName: String
        get() = containingFile.name

    override val type: VaultRunnableType
        get() = VaultRunnableType.DECRYPT

    override val mode: VaultRunnableMode
        get() = VaultRunnableMode.INLINE
}
