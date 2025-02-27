package ir.mmd.intellijDev.Actionable.util.ext

import com.intellij.openapi.util.TextRange

/**
 * Returns [TextRange.getStartOffset]
 */
@Suppress("NOTHING_TO_INLINE")
inline operator fun TextRange.component1(): Int = startOffset

/**
 * Returns [TextRange.getEndOffset]
 */
@Suppress("NOTHING_TO_INLINE")
inline operator fun TextRange.component2(): Int = endOffset

/**
 * Converts a [TextRange] into an [IntRange]
 */
inline val TextRange.intRange get() = startOffset..endOffset
