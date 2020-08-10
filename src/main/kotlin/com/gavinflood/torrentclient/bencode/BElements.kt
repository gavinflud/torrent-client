package com.gavinflood.torrentclient.bencode

import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Structure representing any data type in a bencoded string.
 */
interface BElement {

    /**
     * @return the encoded string value
     */
    fun encode(): String

}

/**
 * Structure representing a single piece of text data in a bencoded string. Text data is prefixed by the length of the
 * text followed by a colon (for example, "10:announcing").
 */
class BString(val value: String): BElement {

    override fun encode(): String {
        return "${value.length}:$value"
    }

    override fun equals(other: Any?): Boolean {
        return other is BString && other.value == value
    }

    override fun hashCode(): Int {
        return Objects.hash(value)
    }

}

/**
 * Structure representing a number in a bencoded string. Numeric data is prefixed by an 'i' and suffixed by an 'e'
 * (for example, i27e).
 */
class BNumber(val value: Int): BElement {

    override fun encode(): String {
        return "i${value}e"
    }

    override fun equals(other: Any?): Boolean {
        return other is BNumber && other.value == value
    }

    override fun hashCode(): Int {
        return Objects.hash(value)
    }

}

/**
 * Structure representing a list in a bencoded string. Lists are prefixed by an 'l' and suffixed by an 'e' and they must
 * consist of other [BElement] objects.
 */
class BList: ArrayList<BElement>(), BElement {

    override fun encode(): String {
        val builder = StringBuilder()
        builder.append("l")
        this.forEach { builder.append(it.encode()) }
        return builder.append("e").toString()
    }

}

/**
 * Structure representing a map in a bencoded string. Maps are prefixed by a 'd' and suffixed by an 'e' and they must
 * consist of a [BString] key with a [BElement] value.
 */
class BMap: HashMap<BString, BElement>(), BElement {

    override fun encode(): String {
        val builder = StringBuilder()
        builder.append("d")
        entries.forEach { builder.append(it.key.encode() + it.value.encode()) }
        return builder.append("e").toString()
    }

}