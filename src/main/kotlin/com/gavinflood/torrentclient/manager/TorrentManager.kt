package com.gavinflood.torrentclient.manager

class TorrentManager(private val file: TorrentFile) {

    val tracker = Tracker(file.announce, file.infoHash, file.length)

    fun download(outputPath: String) {
        tracker.requestPeers()
    }

}