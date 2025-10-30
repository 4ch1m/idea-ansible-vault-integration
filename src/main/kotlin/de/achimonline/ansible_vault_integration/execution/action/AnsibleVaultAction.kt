package de.achimonline.ansible_vault_integration.execution.action

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessOutputType
import com.intellij.execution.wsl.WslPath
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import com.intellij.psi.PsiFile
import com.intellij.util.containers.stream
import de.achimonline.ansible_vault_integration.bundle.AnsibleVaultIntegrationBundle.message
import de.achimonline.ansible_vault_integration.commandline.AnsibleCommandLineTransformer
import de.achimonline.ansible_vault_integration.commandline.AnsibleVaultCommandLineBuilder
import de.achimonline.ansible_vault_integration.commandline.NoOpAnsibleCommandLineTransformer
import de.achimonline.ansible_vault_integration.commandline.WslAnsibleCommandLineTransformer
import de.achimonline.ansible_vault_integration.execution.AnsibleVaultWrapperCallFailedException
import de.achimonline.ansible_vault_integration.settings.AnsibleVaultSettings
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
import java.util.stream.Collectors
import kotlin.io.path.setPosixFilePermissions
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
abstract class AnsibleVaultAction(
    protected val project: Project,
    protected val contextFile: PsiFile,
    protected val trimResult: Boolean
) {
    protected abstract val actionName: String
    protected abstract val stdin: ByteArray
    protected abstract val parameters: List<String>

    @Throws(IOException::class)
    protected fun createTempFile(input: ByteArray): File {
        val tempFile = FileUtil.createTempFile("idea-ansible-vault-integration_${Uuid.random()}", ".tmp", true)
        tempFile.toPath().setPosixFilePermissions(setOf(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE
        ))

        FileUtil.writeToFile(tempFile, input)

        return tempFile
    }

    @Throws(AnsibleVaultWrapperCallFailedException::class)
    open fun execute(): String = executeCommand()

    @Throws(AnsibleVaultWrapperCallFailedException::class)
    private fun executeCommand(): String {
        val processHandler: ProcessHandler
        val stderr = StringBuffer()
        val stdout = StringBuffer()

        try {
            val contextPath = contextFile.virtualFile.toNioPath()
            processHandler = OSProcessHandler(getVaultCommandLine(project, contextPath, actionName, parameters, stdin))

            // output
            processHandler.addProcessListener(object : ProcessListener {
                @Synchronized
                override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                    if (ProcessOutputType.isStderr(outputType)) {
                        stderr.append(event.text)
                    }

                    if (ProcessOutputType.isStdout(outputType)) {
                        stdout.append(event.text)
                    }
                }
            })

            processHandler.startNotify()

            // error waiting for graceful exit
            if (!processHandler.waitFor(AnsibleVaultSettings.getInstance(project).state.timeout * 1000L)) {
                processHandler.destroyProcess()
                throw AnsibleVaultWrapperCallFailedException(
                    message(
                        "exception.AnsibleVaultWrapperCallFailedException.time_out",
                        "${if (stderr.toString().isNotEmpty()) "$stderr\n" else ""}$stdout"
                    )
                )
            }

            if (processHandler.exitCode != null && processHandler.exitCode != 0) {
                throw AnsibleVaultWrapperCallFailedException(
                    message(
                        "exception.AnsibleVaultWrapperCallFailedException.exit_code",
                        processHandler.exitCode!!,
                        "${if (stderr.toString().isNotEmpty()) "$stderr\n" else ""}$stdout"
                    )
                )
            }
        } catch (e: ExecutionException) {
            wrapException(e)
        } catch (e: NullPointerException) {
            wrapException(e)
        } catch (e: IOException) {
            wrapException(e)
        }

        return if (trimResult) stdout.toString().trim() else stdout.toString()
    }

    private fun wrapException(e : Exception) : Any = throw AnsibleVaultWrapperCallFailedException(
        message(
            "exception.AnsibleVaultWrapperCallFailedException.internal_error",
            e.message ?: "No message available"
        )
    )

    private fun runsInWsl(vaultExecutable: String): Boolean =
        SystemInfo.isWin10OrNewer && WslPath.parseWindowsUncPath(vaultExecutable) != null

    @Throws(AnsibleVaultWrapperCallFailedException::class, IOException::class, ExecutionException::class)
    private fun getVaultCommandLine(
        project: Project,
        contextPath: Path,
        action: String,
        parameters: List<String>,
        stdin: ByteArray
    ): GeneralCommandLine {
        val state = AnsibleVaultSettings.getInstance(project).state
        val vaultExecutable = state.vaultExecutable
        val vaultArguments = getVaultArguments(state)
        val stdinFile = createTempFile(stdin)
        val ansibleCommandLineTransformer: AnsibleCommandLineTransformer = getCommandLineTransformer(vaultExecutable)

        if (!File(vaultExecutable).exists()) {
            throw AnsibleVaultWrapperCallFailedException(
                message("exception.AnsibleVaultWrapperCallFailedException.executable_not_found")
            )
        }

        return AnsibleVaultCommandLineBuilder(vaultExecutable, ansibleCommandLineTransformer)
            .withParameter(action)
            .withFilePathParameter(stdinFile.absolutePath)
            .withEnv(ENVIRONMENT_CONTEXT_DIRECTORY, contextPath.toFile().parentFile.name)
            .withFilePathEnv(ENVIRONMENT_CONTEXT_FILE, contextPath.toString())
            .withEnv(ENVIRONMENT_CONTEXT_PROJECT_BASE_PATH, project.basePath)
            .withEnv(ENVIRONMENT_CONTEXT_PROJECT_NAME, project.name)
            .getCommandLine(project)!!
            .withWorkDirectory(project.basePath)
            .withParameters(parameters)
            .withParameters(vaultArguments)
    }

    private fun getCommandLineTransformer(vaultExecutable: String) = when {
        runsInWsl(vaultExecutable) -> WslAnsibleCommandLineTransformer(
            WslPath.parseWindowsUncPath(vaultExecutable)!!.distribution
        )

        else -> NoOpAnsibleCommandLineTransformer()
    }

    private fun getVaultArguments(state: AnsibleVaultSettings) =
        state.vaultArguments
            .split(" ")
            .toTypedArray()
            .stream()
            .filter { `val`: String -> `val`.trim { it <= ' ' } != "" }
            .collect(Collectors.toList())

    companion object {
        private const val ENVIRONMENT_PREFIX = "IDEA_ANSIBLE_VAULT_"

        private const val ENVIRONMENT_CONTEXT_FILE = ENVIRONMENT_PREFIX + "CONTEXT_FILE"
        private const val ENVIRONMENT_CONTEXT_DIRECTORY = ENVIRONMENT_PREFIX + "CONTEXT_DIRECTORY"

        private const val ENVIRONMENT_CONTEXT_PROJECT_PREFIX = ENVIRONMENT_PREFIX + "CONTEXT_PROJECT_"
        private const val ENVIRONMENT_CONTEXT_PROJECT_BASE_PATH = ENVIRONMENT_CONTEXT_PROJECT_PREFIX + "BASE_PATH"
        private const val ENVIRONMENT_CONTEXT_PROJECT_NAME = ENVIRONMENT_CONTEXT_PROJECT_PREFIX + "NAME"
    }
}
