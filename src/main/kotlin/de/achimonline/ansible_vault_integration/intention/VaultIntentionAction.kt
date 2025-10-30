package de.achimonline.ansible_vault_integration.intention

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.util.IncorrectOperationException
import de.achimonline.ansible_vault_integration.bundle.AnsibleVaultIntegrationBundle.message
import de.achimonline.ansible_vault_integration.config.AnsibleConfigurationService
import de.achimonline.ansible_vault_integration.runnable.string.EncryptStringAnsibleVaultRunnable
import org.jetbrains.yaml.YAMLTokenTypes
import org.jetbrains.yaml.psi.YAMLKeyValue

class VaultIntentionAction : BaseIntentionAction(message("intention.vault.text")) {
    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (!super.isAvailable(project, editor, element)) {
            return false
        }

        // allow any kind of text in single or double quotes or without quotes
        val elementType = element.node.elementType

        return (elementType == YAMLTokenTypes.TEXT
                || elementType == YAMLTokenTypes.SCALAR_STRING
                || elementType == YAMLTokenTypes.SCALAR_DSTRING) &&
                element.parent?.parent is YAMLKeyValue
    }

    private fun extractValue(element: PsiElement): String {
        val elementType = element.node.elementType
        val text = element.node.text

        return when (elementType) {
            YAMLTokenTypes.TEXT -> text // plain text -> no modification required
            else -> text.substring(1, text.length - 1) // remove quotes
        }
    }

    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor, element: PsiElement) {
        val content = extractValue(element)
        val containingFile = element.containingFile

        val vaultIdentities = AnsibleConfigurationService.getInstance(project)
            .getAggregatedConfig()
            .vaultIdentities

        if (!vaultIdentities.isNullOrEmpty()) {
            AnsibleVaultIdentityPopup(vaultIdentities) {
                runTask(project, EncryptStringAnsibleVaultRunnable(project, containingFile, content, element, it))
            }.showInEditor(editor)
        } else {
            runTask(project, EncryptStringAnsibleVaultRunnable(project, containingFile, content, element))
        }
    }
}
