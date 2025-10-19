package de.achimonline.ansible_vault_integration.runnable

import de.achimonline.ansible_vault_integration.bundle.AnsibleVaultIntegrationBundle

enum class VaultRunnableType {
    ENCRYPT,
    DECRYPT;

    override fun toString(): String {
        return when (this) {
            ENCRYPT -> AnsibleVaultIntegrationBundle.message("action.encrypt")
            DECRYPT -> AnsibleVaultIntegrationBundle.message("action.decrypt")
        }
    }

    fun stringify(fileName: String): String {
        return "$this $fileName"
    }
}

enum class VaultRunnableMode {
    INLINE,
    FILE
}

interface AnsibleVaultRunnable {
    @Throws(Exception::class)
    fun run()

    val fileName: String
    val type: VaultRunnableType
    val mode: VaultRunnableMode
}
