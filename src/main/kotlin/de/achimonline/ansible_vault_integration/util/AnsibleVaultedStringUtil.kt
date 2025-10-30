package de.achimonline.ansible_vault_integration.util

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.containers.stream
import java.util.stream.Collectors

object AnsibleVaultedStringUtil {
    private const val STRING_PREFIX = "!vault |"
    private const val FILE_PREFIX = $$"$ANSIBLE_VAULT"

    fun isVaultedString(input: String?): Boolean = input != null
            && input.trim { it <= ' ' }.startsWith(STRING_PREFIX)

    fun removeStringPrefix(vaultedString: String): String = vaultedString.splitLines()
        .stream()
        .skip(1)
        .map { obj: String -> obj.trim { it <= ' ' } }
        .collect(Collectors.joining("\n"))

    fun addStringPrefix(vaultedString: String): String {
        val rawLines = vaultedString.splitLines()
        val suffixedLines = arrayOfNulls<String>(rawLines.size + 1)

        suffixedLines[0] = STRING_PREFIX
        System.arraycopy(rawLines, 0, suffixedLines, 1, rawLines.size)

        return java.lang.String.join("\n", *suffixedLines)
    }

    fun isVaultedFile(virtualFile : VirtualFile): Boolean {
        val buffer = ByteArray(14)

        virtualFile.inputStream.use {
            it.read(buffer)
        }

        return String(buffer) == FILE_PREFIX
    }
}

private fun String.splitLines(): Array<String> = this.split('\n')
    .toTypedArray()
