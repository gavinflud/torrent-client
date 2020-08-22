package com.gavinflood.torrentclient

import com.gavinflood.torrentclient.manager.TorrentFile
import com.gavinflood.torrentclient.manager.TorrentManager

fun main(args: Array<String>) {
    if (args.size < 0) {
        throw RuntimeException("You must specify the path to the torrent file and the directory to download to")
    }

    val torrentFilePath = args[0]
    val outputDirectory = args[1]

    val file = TorrentFile(torrentFilePath)
    TorrentManager(file).download(outputDirectory)
}