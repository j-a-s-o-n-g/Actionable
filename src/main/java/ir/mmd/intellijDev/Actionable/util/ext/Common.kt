package ir.mmd.intellijDev.Actionable.util.ext

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

fun String.copyToClipboard() = Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(this), null)
fun String.isAllDistinct() = toCharArray().distinct().size == length

inline operator fun String.contains(char: Char?): Boolean = char != null && indexOf(char) >= 0
inline operator fun IntRange?.contains(i: Int): Boolean = this != null && contains(i)

inline val Int.isPositive: Boolean get() = this > 0

inline fun <T> T.runOnly(block: T.() -> Unit) = block()
inline fun <T> T.letOnly(block: (T) -> Unit) = block(this)

inline fun <T> Iterable<T>.withEach(block: T.() -> Unit) = forEach { it.block() }
inline fun <T> Iterable<T>.forEachIf(condition: T.() -> Boolean, block: (T) -> Unit) = forEach { if (it.condition()) block(it) }

inline fun <T> T.letIf(condition: T.() -> Boolean, block: (T) -> Unit) {
	if (condition()) block(this)
}

inline fun doIf(condition: () -> Boolean, block: () -> Unit) {
	if (condition()) block()
}
