package de.interact.domain.shared

import java.security.MessageDigest

fun hashedSha256(vararg elements: Any): String {
    return MessageDigest
        .getInstance("SHA-256")
        .digest(elements.joinToString { it.toString() }.toByteArray())
        .fold(StringBuilder()) { sb, it -> sb.append("%02x".format(it)) }.toString()
}