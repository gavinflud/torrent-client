package com.gavinflood.torrentclient.bencode

import java.lang.StringBuilder
import java.nio.ByteBuffer
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
class BString(val bytes: ByteArray): BElement {

    override fun encode(): String {
        val value = String(bytes)
        return "${value.length}:$value"
    }

    override fun equals(other: Any?): Boolean {
        return other is BString && other.bytes.contentEquals(bytes)
    }

    override fun hashCode(): Int {
        return Objects.hash(bytes)
    }

    override fun toString(): String {
        return String(bytes)
    }

}

/**
 * Structure representing a number in a bencoded string. Numeric data is prefixed by an 'i' and suffixed by an 'e'
 * (for example, i27e).
 */
class BNumber(val value: Long): BElement {

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
class BMap: HashMap<String, BElement>(), BElement {

    var bytes: ByteArray? = null

    override fun encode(): String {
        val builder = StringBuilder()
        builder.append("d")
        keys.forEach { builder.append(it + this[it]?.encode()) }
        return builder.append("e").toString()
    }

    /**
     * Utility function to get an element from the map by passing in the String key. You specify the type of the
     * element you expect and it will return that type (assuming it exists in the map and is that type).
     *
     * @param key the String key identifying the element you want
     * @return the element associated with the key
     */
    inline fun <reified T: BElement> getEntry(key: String): T {
        return this[key] as T
    }

}