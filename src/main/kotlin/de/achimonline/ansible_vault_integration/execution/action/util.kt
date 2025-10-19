package de.achimonline.ansible_vault_integration.execution.action

import com.intellij.openapi.actionSystem.AnActionEvent

internal fun AnActionEvent.setVisible(visiblity: Boolean) {
    this.presentation.isEnabledAndVisible = visiblity
}
