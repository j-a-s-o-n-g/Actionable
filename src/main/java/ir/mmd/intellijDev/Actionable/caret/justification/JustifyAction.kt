package ir.mmd.intellijDev.Actionable.caret.justification

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import ir.mmd.intellijDev.Actionable.util.ext.*

abstract class JustifyAction(private val justify: JustifyCaretUtil.() -> Unit) : AnAction() {
	override fun getActionUpdateThread() = ActionUpdateThread.BGT
	override fun isDumbAware() = true
	override fun actionPerformed(e: AnActionEvent) = e.editor.justifier.justify()
	override fun update(e: AnActionEvent) = e.enableIf { hasProject and hasEditorWith { caretCount > 1 && hasNotAnySelections } }
}
