package com.gavinflood.torrentclient.manager

import com.gavinflood.torrentclient.bencode.BDecoder
import java.io.File
import java.lang.RuntimeException
import java.nio.charset.Charset

class TorrentFile(private val path: String, private val charset: Charset = Charset.forName("ASCII")) {

    init {
        open()
    }

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

    private fun populateInfo(data: ByteArray) {
        val elements = BDecoder(String(data, charset)).parse()
    }

}