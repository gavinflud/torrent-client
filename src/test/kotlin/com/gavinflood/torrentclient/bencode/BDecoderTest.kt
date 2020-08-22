package com.gavinflood.torrentclient.bencode

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class BDecoderTest {

    @Test
    fun `single string value results in one BString`() {
        val value = "test"
        val element = BDecoder("${value.length}:$value").parse()
        assertTrue(element is BString) { "Element should be a BString" }
        assertEquals((element as BString).value, value)
    }

    @Test
    fun `single int value results in one BNumber`() {
        val value = 27
        val element = BDecoder("i${value}e").parse()
        assertTrue(element is BNumber) { "Element should be a BNumber" }
        assertEquals((element as BNumber).value, value)
    }

    @Test
    fun `list with three elements results in BList of size three`() {
        val str1 = "test"
        val str2 = "announcing"
        val num = 39
        val element = BDecoder("l${str1.length}:${str1}i${num}e${str2.length}:${str2}e").parse()
        assertTrue(element is BList) { "Element should be a BList" }
        assertEquals((element as BList).size, 3) { "BList should have 3 elements" }
    }

    @Test
    fun `map with string entry results in BMap with one BString entry`() {
        val key = "test"
        val value = "value"
        val element = BDecoder("d${key.length}:$key${value.length}:${value}e").parse()
        assertTrue(element is BMap) { "Element should be a BMap" }

        val map = element as BMap
        assertEquals(map.size, 1) { "BMap should only have 1 entry" }
        assertEquals(map.entries.first().key, BString(key)) { "Entry key should equal '$key'" }
        assertEquals(map.entries.first().value, BString(value)) { "Entry value should equal '$value'" }
    }

}