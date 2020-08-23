package com.gavinflood.torrentclient.manager

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.lang.Exception
import java.lang.RuntimeException

class TorrentFileTest {

    lateinit var torrentPath: String

    @BeforeEach
    fun init() {
        val url = TorrentFileTest::class.java.classLoader.getResource("test.torrent")
        torrentPath = if (url != null) url.path else throw Exception("Cannot find torrent file")
    }

    @Test
    fun `test a valid torrent file opens successfully`() {
        assertDoesNotThrow { TorrentFile(torrentPath) }
    }

    @Test
    fun `test an invalid torrent file throws an exception`() {
        assertThrows<RuntimeException> { TorrentFile("/path/to/invalid/torrent/file.torrent") }
    }

    @Test
    fun `test length is parsed correctly from torrent file`() {
        val torrentFile = TorrentFile(torrentPath)
        assertEquals(torrentFile.length, 365953024)
    }

    @Test
    fun `test name is parsed correctly from torrent file`() {
        val torrentFile = TorrentFile(torrentPath)
        assertEquals(torrentFile.name, "test.iso")
    }

    @Test
    fun `test pieces is parsed correctly from torrent file`() {
        val torrentFile = TorrentFile(torrentPath)
        assertEquals(torrentFile.pieces.size, 5)
        torrentFile.pieces.forEach { assertEquals(it.size, 20) }
    }

    @Test
    fun `test announce is parsed correctly from torrent file`() {
        val torrentFile = TorrentFile(torrentPath)
        assertEquals(torrentFile.announce, "http://gavinflood.com:6969/announce")
    }

}