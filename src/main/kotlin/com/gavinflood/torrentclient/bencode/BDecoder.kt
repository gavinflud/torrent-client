package com.gavinflood.torrentclient.bencode

import com.google.common.collect.Iterators
import com.google.common.collect.PeekingIterator
import java.lang.RuntimeException
import java.lang.StringBuilder

const val BENCODE_NUMBER_IDENTIFIER = 'i'
const val BENCODE_LIST_IDENTIFIER = 'l'
const val BENCODE_MAP_IDENTIFIER = 'd'
const val BENCODE_END_IDENTIFIER = 'e'
const val BENCODE_SEPARATOR = ':'

/**
 * Decoder for bencoded content.
 *
 * @param encodedValue The bencoded string to be decoded
 */
class BDecoder(private val encodedValue: String) {

    val iterator: PeekingIterator<Char> = Iterators.peekingIterator(encodedValue.iterator())

    /**
     * Parse a bencoded string into the individual elements that make up it.
     *
     * @return a bencode element
     */
    fun parse(): BElement {
        return read()
    }

    /**
     * Read in a bencoded element.
     *
     * @return a bencoded element
     */
    private fun read(): BElement {
        if (iterator.hasNext()) {
            return when (val nextChar = iterator.peek()) {
                in '0'..'9' -> toBString()
                BENCODE_NUMBER_IDENTIFIER -> toBNumber()
                BENCODE_LIST_IDENTIFIER -> toBList()
                BENCODE_MAP_IDENTIFIER -> toBMap()
                else -> throw RuntimeException("Unrecognized type '$nextChar'")
            }
        }

        throw RuntimeException("No content")
    }

    /**
     * Read in a bencoded string.
     *
     * @return a bencoded string
     */
    private fun toBString(): BString {
        val builder = StringBuilder()

        while (iterator.peek().isDigit()) {
            builder.append(iterator.next())
        }

        val length = builder.toString().toInt()
        iterator
            .skip(BENCODE_SEPARATOR)
            .apply {
                val charArray = CharArray(length)

                for (i in 0 until length) {
                    charArray[i] = iterator.next()
                }

                return BString(String(charArray))
            }
    }

    /**
     * Read in a bencoded number.
     *
     * @return a bencoded number
     */
    private fun toBNumber(): BNumber {
        iterator
            .skip(BENCODE_NUMBER_IDENTIFIER)
            .apply {
                val builder = StringBuilder()

                while (iterator.peek() != BENCODE_END_IDENTIFIER) {
                    builder.append(iterator.next())
                }

                iterator.next()
                return BNumber(builder.toString().toInt())
            }
    }

    /**
     * Read in a bencoded list.
     *
     * @return a bencoded list of bencoded elements
     */
    private fun toBList(): BList {
        iterator
            .skip(BENCODE_LIST_IDENTIFIER)
            .apply {
                val list = BList()

                while (iterator.peek() != BENCODE_END_IDENTIFIER) {
                    list.add(read())
                }

                iterator.next()
                return list
            }
    }

    /**
     * Read in a bencoded map (dictionary).
     *
     * @return a bencoded map of bencoded element entries
     */
    private fun toBMap(): BMap {
        iterator
            .skip(BENCODE_MAP_IDENTIFIER)
            .apply {
                val map = BMap()

                while (iterator.peek() != BENCODE_END_IDENTIFIER) {
                    map[toBString()] = read()
                }

                iterator.next()
                return map
            }
    }

}