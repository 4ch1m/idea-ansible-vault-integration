package de.achimonline.ansible_vault_integration.execution.action

import de.achimonline.ansible_vault_integration.execution.AnsibleVaultWrapperCallFailedException
import de.achimonline.ansible_vault_integration.util.AnsibleVaultedStringUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import de.achimonline.ansible_vault_integration.config.VaultIdentity

open class AnsibleVaultEncryptAction(
    project: Project,
    contextFile: PsiFile,
    override val stdin: ByteArray,
    private val vaultIdentity: VaultIdentity?,
    private val addStringPrefix: Boolean = true
) : AnsibleVaultAction(project, contextFile, trimResult = true) {
    override val actionName: String
        get() = "encrypt"

    override val parameters: List<String>
        get() {
            val args = mutableListOf("--output=-")

            if (vaultIdentity != null) {
                args.add("--encrypt-vault-id")
                args.add(vaultIdentity.name)
            }

            return args
        }

    @Throws(AnsibleVaultWrapperCallFailedException::class)
    override fun execute(): String {
        val result = super.execute()

        return when {
            addStringPrefix -> AnsibleVaultedStringUtil.addStringPrefix(result)
            else -> result
        }
    }
}
