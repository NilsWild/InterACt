package de.rwth.swc.interact.junit.jupiter

import de.rwth.swc.interact.junit.jupiter.InterACtTestNameFormatterConstants.ELLIPSIS
import de.rwth.swc.interact.junit.jupiter.InterACtTestNameFormatterConstants.TEMPORARY_DISPLAY_NAME_PLACEHOLDER
import de.rwth.swc.interact.junit.jupiter.annotation.InterACtTestConstants.ARGUMENTS_PLACEHOLDER
import de.rwth.swc.interact.junit.jupiter.annotation.InterACtTestConstants.ARGUMENTS_WITH_NAMES_PLACEHOLDER
import de.rwth.swc.interact.junit.jupiter.annotation.InterACtTestConstants.DISPLAY_NAME_PLACEHOLDER
import org.junit.jupiter.api.Named
import org.junit.platform.commons.JUnitException
import org.junit.platform.commons.util.StringUtils
import java.text.MessageFormat
import java.util.stream.Collectors.joining
import java.util.stream.IntStream
import kotlin.math.min

object InterACtTestNameFormatterConstants {
    const val ELLIPSIS = '\u2026'
    const val TEMPORARY_DISPLAY_NAME_PLACEHOLDER = "~~~JUNIT_DISPLAY_NAME~~~"
}

class InterACtTestNameFormatter(
    private val pattern: String,
    private val displayName: String,
    private val methodContext: InterACtTestMethodContext,
    private val argumentMaxLength: Int
) {

    fun format(vararg arguments: Any): String {
        return try {
            formatSafely(arguments)
        } catch (ex: Exception) {
            val message = ("The display name pattern defined for the parameterized test is invalid. "
                    + "See nested exception for further details.")
            throw JUnitException(message, ex)
        }
    }

    private fun formatSafely(arguments: Array<out Any>): String {
        val namedArguments = extractNamedArguments(arguments)
        val pattern = prepareMessageFormatPattern(namedArguments)
        val format = MessageFormat(pattern)
        val humanReadableArguments = makeReadable(format, namedArguments)
        val formatted = format.format(humanReadableArguments)
        return formatted.replace(TEMPORARY_DISPLAY_NAME_PLACEHOLDER, displayName)
    }

    private fun extractNamedArguments(arguments: Array<out Any>): Array<Any> {
        return arguments.map { argument -> if (argument is Named<*>) argument.name else argument }.toTypedArray()
    }

    private fun prepareMessageFormatPattern(arguments: Array<Any>): String {
        var result = pattern
            .replace(DISPLAY_NAME_PLACEHOLDER, TEMPORARY_DISPLAY_NAME_PLACEHOLDER)
        if (result.contains(ARGUMENTS_WITH_NAMES_PLACEHOLDER)) {
            result = result.replace(
                ARGUMENTS_WITH_NAMES_PLACEHOLDER,
                argumentsWithNamesPattern(arguments)
            )
        }
        if (result.contains(ARGUMENTS_PLACEHOLDER)) {
            result = result.replace(ARGUMENTS_PLACEHOLDER, argumentsPattern(arguments))
        }
        return result
    }

    private fun argumentsWithNamesPattern(arguments: Array<Any>): String {
        return IntStream.range(0, arguments.size)
            .mapToObj { index: Int ->
                methodContext.getParameterName(index)?.map { name -> "$name={$index}" }?.toString()
                    ?: "{$index}"
            }.collect(joining(", "))
    }

    private fun argumentsPattern(arguments: Array<Any>): String {
        return IntStream.range(0, arguments.size)
            .mapToObj { index: Int -> "{$index}" }
            .collect(joining(", "))
    }

    private fun makeReadable(format: MessageFormat, arguments: Array<Any>): Array<Any> {
        val formats = format.formatsByArgumentIndex
        val result = arguments.copyOfRange(0, min(arguments.size, formats.size))
        for (i in result.indices) {
            if (formats.getOrNull(i) == null) {
                result[i] = truncateIfExceedsMaxLength(StringUtils.nullSafeToString(arguments[i]))
            }
        }
        return result
    }

    private fun truncateIfExceedsMaxLength(argument: String): String {
        return if (argument.length > argumentMaxLength) {
            argument.substring(0, argumentMaxLength - 1) + ELLIPSIS
        } else argument
    }
}
