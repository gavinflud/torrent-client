package com.gavinflood.torrentclient.manager

import com.gavinflood.torrentclient.bencode.BDecoder
import com.gavinflood.torrentclient.bencode.BMap
import com.gavinflood.torrentclient.bencode.BNumber
import com.gavinflood.torrentclient.bencode.BString
import java.io.File
import java.lang.RuntimeException
import java.nio.charset.Charset
import java.security.MessageDigest

/**
 * Represents the data in a torrent file.
 *
 * @param path the path to the file
 * @param charset the character set of the file (defaults to ASCII)
 */
class TorrentFile(private val path: String, private val charset: Charset = Charset.forName("ASCII")) {

    var length = 0L
    lateinit var name: String
    lateinit var announce: String
    lateinit var pieces: Array<ByteArray>
    lateinit var infoHash: ByteArray

    init {
        open()
    }

    /**
     * Open and read the file.
     */
    private fun open() {
        val file = File(path)

        if (!file.exists()) {
            throw RuntimeException("Torrent file cannot be found at $path")
        }

        val inputStream = file.inputStream()
        val fileData = ByteArray(file.length().toInt())
        inputStream.use {
            inputStream.read(fileData)
        }

        populateInfo(fileData)
    }

    /**
     * Decode the file and pull out the necessary information from it.
     *
     * @param data byte array of all the data in the file
     */
    private fun populateInfo(data: ByteArray) {
        val map = BDecoder(data).parse()

        if (map !is BMap) {
            throw RuntimeException("Bencoded file needs to contain a dictionary as the root element")
        }

        // Pull out the basic info from the file
        announce = map.getEntry<BString>("announce").toString()
        val info = map.getEntry<BMap>("info")
        name = info.getEntry<BString>("name").toString()
        length = info.getEntry<BNumber>("length").value

        // Calculate hash of info dictionary to be used later during request to tracker
        val messageDigest = MessageDigest.getInstance("SHA-1")
        messageDigest.update(info.bytes)
        infoHash = messageDigest.digest()

        // Split the "pieces" byte array into a slice of hashes rather than one big byte array
        val piecesByteArray = info.getEntry<BString>("pieces").bytes
        if (piecesByteArray.size % 20 != 0) {
            throw RuntimeException("Piece hashes length is not a multiple of 20. The file may be corrupt.")
        }
        pieces = Array(piecesByteArray.size / 20) { i -> piecesByteArray.sliceArray(i * 20..i * 20 + 19) }
    }

}