package com.example.myandroidapplication.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WatchlistRepositoryTest {

    @Test
    fun addRemoveContainsAndInsertionOrder() {
        val repo = MockWatchlistRepository()
        assertEquals(emptyList(), repo.getAll())
        assertFalse(repo.contains("600519"))

        repo.add("600519")
        repo.add("000001")
        repo.add("600519")
        assertTrue(repo.contains("600519"))
        assertEquals(listOf("600519", "000001"), repo.getAll())

        repo.remove("600519")
        assertFalse(repo.contains("600519"))
        assertEquals(listOf("000001"), repo.getAll())

        repo.remove("missing")
        assertEquals(listOf("000001"), repo.getAll())
    }

    @Test
    fun stateSurvivesAcrossReads() {
        val repo = MockWatchlistRepository()
        repo.add("AAPL")
        repo.add("00700")
        val first = repo.getAll()
        val second = repo.getAll()
        assertEquals(first, second)
        assertEquals(listOf("AAPL", "00700"), second)
    }
}
