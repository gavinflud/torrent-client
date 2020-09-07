package com.gavinflood.torrentclient.bencode

import java.lang.RuntimeException

const val BENCODE_NUMBER_IDENTIFIER = 'i'.toByte()
const val BENCODE_LIST_IDENTIFIER = 'l'.toByte()
const val BENCODE_MAP_IDENTIFIER = 'd'.toByte()
const val BENCODE_END_IDENTIFIER = 'e'.toByte()
const val INITIAL_OFFSET = 0
const val INFO_KEY = "info"

/**
 * Decoder for bencoded content.
 *
 * @param encodedBytes The bencoded byte array to be decoded
 */
class BDecoder(private val encodedBytes: ByteArray) {

    /**
     * Parse a bencoded byte array into the individual elements that make up it.
     *
     * @return a bencode element
     */
    fun parse(): BElement {
        return read(INITIAL_OFFSET).element
    }

    /**
     * Read in a bencoded element.
     *
     * @param offset the index at which to start decoding
     * @return the resulting offset with the parsed element
     */
    private fun read(offset: Int): ParseResult<BElement> {
        val digitsAsBytes = ('0'..'9').map { it.toByte() }

        return when (encodedBytes[offset]) {
            in digitsAsBytes -> toBString(offset)
            BENCODE_NUMBER_IDENTIFIER -> toBNumber(offset)
            BENCODE_LIST_IDENTIFIER -> toBList(offset)
            BENCODE_MAP_IDENTIFIER -> toBMap(offset)
            else -> throw RuntimeException("Unrecognized byte '${encodedBytes[offset].toChar()}'")
        }
    }

    /**
     * Read in a bencoded string.
     *
     * @param offset the index at which to start decoding
     * @return the resulting offset with the parsed element
     */
    private fun toBString(offset: Int): ParseResult<BString> {
        var i = offset
        val sizeBuffer = StringBuffer()
        val digitsAsBytes = ('0'..'9').map { it.toByte() }

        while (encodedBytes[i] in digitsAsBytes) {
            sizeBuffer.append(encodedBytes[i].toChar())
            i++
        }

        i++
        val size = sizeBuffer.toString().toInt()
        val bytes = ByteArray(size)
        System.arraycopy(encodedBytes, i, bytes, 0, bytes.size)

        return ParseResult(i + size, BString(bytes))
    }

    /**
     * Read in a bencoded number.
     *
     * @param offset the index at which to start decoding
     * @return the resulting offset with the parsed number
     */
    private fun toBNumber(offset: Int): ParseResult<BNumber> {
        var i = offset + 1
        val buffer = StringBuffer()

        while (encodedBytes[i] != BENCODE_END_IDENTIFIER && encodedBytes.size > i) {
            buffer.append(encodedBytes[i].toChar())
            i++
        }

        i++

        return ParseResult(i, BNumber(buffer.toString().toLong()))
    }

    /**
     * Read in a bencoded list.
     *
     * @param offset the index at which to start decoding
     * @return the resulting offset with the parsed list
     */
    private fun toBList(offset: Int): ParseResult<BList> {
        var i = offset + 1
        val list = BList()

        while (encodedBytes[i] != BENCODE_END_IDENTIFIER) {
            val result = read(i)
            i = result.offset
            list.add(result.element)
        }

        i++

        return ParseResult(i, list)
    }

    /**
     * Read in a bencoded map (dictionary).
     *
     * @param offset the index at which to start decoding
     * @param isInfoMap indicates if this map is the info map that needs to be hashed for torrent files
     * @return the resulting offset with the parsed map
     */
    private fun toBMap(offset: Int, isInfoMap: Boolean = false): ParseResult<BMap> {
        var i = offset + 1
        val map = BMap()

        while (encodedBytes[i] != BENCODE_END_IDENTIFIER) {
            val keyResult = toBString(i)
            val key = String(keyResult.element.bytes)
            val valueResult = if (key == INFO_KEY) toBMap(keyResult.offset, true) else read(keyResult.offset)
            map[key] = valueResult.element
            i = valueResult.offset
        }

        i++

        if (isInfoMap) {
            val infoBytes = ByteArray(i - offset)
            System.arraycopy(encodedBytes, offset, infoBytes, 0, infoBytes.size)
            map.bytes = infoBytes
        }

        return ParseResult(i, map)
    }

}

data class ParseResult<out T: BElement>(val offset: Int, val element: T)