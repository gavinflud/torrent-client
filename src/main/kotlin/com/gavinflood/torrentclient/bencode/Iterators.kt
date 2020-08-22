package com.gavinflood.torrentclient.bencode

import com.google.common.collect.PeekingIterator

class BencodeIterationException(message: String): Exception(message)

/**
 * Extension function for a [PeekingIterator] to call [PeekingIterator.next] if the next character is equal to the
 * character passed in.
 *
 * @param char move the iterator if the next character is equal to this
 */
fun PeekingIterator<Char>.skip(char: Char) {
    if (peek() != char) {
        throw BencodeIterationException("Cannot skip: '${peek()}' does not match '$char'")
    }

    next()
}