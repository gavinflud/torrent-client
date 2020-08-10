package com.gavinflood.torrentclient.bencode

import java.lang.RuntimeException
import java.util.concurrent.atomic.AtomicInteger

/**
 * Decoder for bencoded content.
 *
 * @param encodedValue The bencoded string to be decoded
 */
class BDecoder(private val encodedValue: String) {

    /**
     * Parse a bencoded string into the individual elements that make up it.
     *
     * @return an array of bencoded elements
     */
    fun parse(): Array<BElement> {
        val index = AtomicInteger(0)
        val elements = arrayListOf<BElement>()

        while (index.get() < encodedValue.length) {
            elements.add(read(index))
        }

        return elements.toTypedArray()
    }

    /**
     * Read in a bencoded element.
     *
     * @param index the index in the bencoded string to start at
     * @return a bencoded element
     */
    private fun read(index: AtomicInteger): BElement {
        val validStringValues = (0..9).map { num -> Character.forDigit(num, 10) }

        return when (encodedValue[index.get()]) {
            in validStringValues -> toBString(index)
            'i' -> toBNumber(index)
            'l' -> toBList(index)
            'd' -> toBMap(index)
            else -> throw RuntimeException("Unrecognized type '${encodedValue[index.get()]}' at index ${index.get()}")
        }
    }

    /**
     * Read in a bencoded string.
     *
     * @param index the index in the bencoded string to start at
     * @return a bencoded string
     */
    private fun toBString(index: AtomicInteger): BString {
        val colonIndex = encodedValue.indexOf(":", index.get())
        val length = encodedValue.substring(index.get() until colonIndex).toInt()
        index.set(colonIndex + 1)
        val value = encodedValue.substring(index.get(), index.get() + length)
        index.set(index.get() + length)
        return BString(value)
    }

    /**
     * Read in a bencoded number.
     *
     * @param index the index in the bencoded string to start at
     * @return a bencoded number
     */
    private fun toBNumber(index: AtomicInteger): BNumber {
        index.set(index.get() + 1)
        val endIndex = encodedValue.indexOf('e', index.get())
        val value = encodedValue.substring(index.get(), endIndex).toInt()
        index.set(endIndex + 1)
        return BNumber(value)
    }

    /**
     * Read in a bencoded list.
     *
     * @param index the index in the bencoded string to start at
     * @return a bencoded list of bencoded elements
     */
    private fun toBList(index: AtomicInteger): BList {
        index.set(index.get() + 1)
        val list = BList()

        while (encodedValue[index.get()] != 'e') {
            list.add(read(index))
        }

        index.set(index.get() + 1)
        return list
    }

    /**
     * Read in a bencoded map (dictionary).
     *
     * @param index the index in the bencoded string to start at
     * @return a bencoded map of bencoded element entries
     */
    private fun toBMap(index: AtomicInteger): BMap {
        index.set(index.get() + 1)
        val map = BMap()

        while (encodedValue[index.get()] != 'e') {
            map[toBString(index)] = read(index)
        }

        index.set(index.get() + 1)
        return map
    }

}