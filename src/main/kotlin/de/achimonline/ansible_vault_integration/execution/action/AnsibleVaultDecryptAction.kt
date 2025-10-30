package de.achimonline.ansible_vault_integration.execution.action

import de.achimonline.ansible_vault_integration.util.AnsibleVaultedStringUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

open class AnsibleVaultDecryptAction(
    project: Project,
    contextFile: PsiFile,
    trimResult: Boolean,
    private val encrypted: String
) : AnsibleVaultAction(project, contextFile, trimResult) {
    override val actionName: String
        get() = "decrypt"

    override val stdin: ByteArray
        get() = AnsibleVaultedStringUtil.removeStringPrefix(encrypted).toByteArray()

    override val parameters: List<String>
        get() = listOf("--output=-")
}
