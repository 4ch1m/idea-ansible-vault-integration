package de.achimonline.ansible_vault_integration.runnable

import de.achimonline.ansible_vault_integration.bundle.AnsibleVaultIntegrationBundle.message

enum class VaultRunnableType {
    ENCRYPT,
    DECRYPT;

    override fun toString(): String {
        return when (this) {
            ENCRYPT -> message("action.vault")
            DECRYPT -> message("action.unvault")
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
