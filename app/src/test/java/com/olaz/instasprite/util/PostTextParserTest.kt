package com.olaz.instasprite.util

import com.olaz.instasprite.ui.components.composable.HASHTAG_REGEX
import com.olaz.instasprite.ui.components.composable.MENTION_REGEX
import org.junit.Assert.*
import org.junit.Test

class PostTextParserTest {

    // ============================
    // Hashtag Detection
    // ============================

    @Test
    fun `detects single hashtag`() {
        val matches = HASHTAG_REGEX.findAll("#pixelart").toList()
        assertEquals(1, matches.size)
        assertEquals("#pixelart", matches[0].value)
    }

    @Test
    fun `detects multiple hashtags`() {
        val text = "Check out my #pixelart and #retrogaming post!"
        val matches = HASHTAG_REGEX.findAll(text).toList()
        assertEquals(2, matches.size)
        assertEquals("#pixelart", matches[0].value)
        assertEquals("#retrogaming", matches[1].value)
    }

    @Test
    fun `detects hashtag at end of text`() {
        val text = "Love this #art"
        val matches = HASHTAG_REGEX.findAll(text).toList()
        assertEquals(1, matches.size)
        assertEquals("#art", matches[0].value)
    }

    @Test
    fun `detects hashtag at start of text`() {
        val text = "#pixelart is awesome"
        val matches = HASHTAG_REGEX.findAll(text).toList()
        assertEquals(1, matches.size)
        assertEquals("#pixelart", matches[0].value)
    }

    @Test
    fun `hashtag with underscores`() {
        val matches = HASHTAG_REGEX.findAll("#pixel_art_2024").toList()
        assertEquals(1, matches.size)
        assertEquals("#pixel_art_2024", matches[0].value)
    }

    @Test
    fun `hashtag with numbers`() {
        val matches = HASHTAG_REGEX.findAll("#art123").toList()
        assertEquals(1, matches.size)
        assertEquals("#art123", matches[0].value)
    }

    @Test
    fun `does not match hash alone`() {
        val matches = HASHTAG_REGEX.findAll("# ").toList()
        assertTrue(matches.isEmpty())
    }

    @Test
    fun `does not match hash with special characters`() {
        // Should only match the word-char portion
        val matches = HASHTAG_REGEX.findAll("#hello!world").toList()
        assertEquals(1, matches.size)
        assertEquals("#hello", matches[0].value) // stops at '!'
    }

    @Test
    fun `empty string returns no hashtags`() {
        val matches = HASHTAG_REGEX.findAll("").toList()
        assertTrue(matches.isEmpty())
    }

    @Test
    fun `text without hashtags returns empty`() {
        val matches = HASHTAG_REGEX.findAll("Just a plain text post").toList()
        assertTrue(matches.isEmpty())
    }

    // ============================
    // Mention Detection
    // ============================

    @Test
    fun `detects single mention`() {
        val matches = MENTION_REGEX.findAll("@john").toList()
        assertEquals(1, matches.size)
        assertEquals("@john", matches[0].value)
    }

    @Test
    fun `detects multiple mentions`() {
        val text = "Hey @alice and @bob check this out"
        val matches = MENTION_REGEX.findAll(text).toList()
        assertEquals(2, matches.size)
        assertEquals("@alice", matches[0].value)
        assertEquals("@bob", matches[1].value)
    }

    @Test
    fun `mention with underscores`() {
        val matches = MENTION_REGEX.findAll("@cool_user_99").toList()
        assertEquals(1, matches.size)
        assertEquals("@cool_user_99", matches[0].value)
    }

    @Test
    fun `empty string returns no mentions`() {
        val matches = MENTION_REGEX.findAll("").toList()
        assertTrue(matches.isEmpty())
    }

    // ============================
    // Mixed Content
    // ============================

    @Test
    fun `detects both hashtags and mentions in same text`() {
        val text = "Hey @alice check out #pixelart! Also @bob loves #retrogaming"
        val hashtags = HASHTAG_REGEX.findAll(text).toList()
        val mentions = MENTION_REGEX.findAll(text).toList()
        assertEquals(2, hashtags.size)
        assertEquals(2, mentions.size)
    }

    @Test
    fun `hashtag and mention next to each other`() {
        val text = "#art@user"
        val hashtags = HASHTAG_REGEX.findAll(text).toList()
        // The regex #[\w]+ will match "#art" and then @[\w]+ might overlap
        assertTrue(hashtags.isNotEmpty())
    }
}
