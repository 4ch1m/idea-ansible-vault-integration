package de.achimonline.ansible_vault_integration.config

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import org.apache.commons.io.FileUtils
import java.io.File

/**
 * Search for all possible configurations as listed in
 * https://docs.ansible.com/ansible/latest/reference_appendices/config.html#the-configuration-file
 */
class AnsibleConfigurationFileFinder(private val project: Project) {
    companion object {
        const val CONFIG_FILE_NAME = "ansible.cfg"
        const val ENVIRONMENT_VARIABLE_NAME = "ANSIBLE_CONFIG"
    }

    /**
     * Return all possible folders for ansible config files, where the order is important and will be mapped to priority
     */
    private fun getPossibleFolders(): MutableList<File> {
        val locations = mutableListOf<File>()

        val environmentVariableValue = System.getenv(ENVIRONMENT_VARIABLE_NAME)

        if (environmentVariableValue != null) {
            locations.add(File(environmentVariableValue))
        }

        locations.add(File(project.basePath!!, CONFIG_FILE_NAME))
        locations.add(File(FileUtils.getUserDirectory(), ".${CONFIG_FILE_NAME}"))

        if (SystemInfo.isLinux || SystemInfo.isMac || SystemInfo.isUnix) {
            locations.add(File("/etc/ansible/", CONFIG_FILE_NAME))
        }

        return locations
    }

    fun getAllProcessableConfigs(): List<File> = getPossibleFolders()
        .filter { it.isFile && it.exists() }
}
