package com.example.myandroidapplication.data.repository

/**
 * 自选股仓库。进程内内存实现，不做持久化。
 */
interface WatchlistRepository {
    /**
     * 加入自选。已存在时保持原顺序，不重复插入。
     */
    fun add(symbol: String)

    /**
     * 取消自选。不存在时忽略。
     */
    fun remove(symbol: String)

    /**
     * 按加入顺序返回证券代码快照。
     */
    fun getAll(): List<String>

    /**
     * 是否已在自选中。
     */
    fun contains(symbol: String): Boolean
}

/**
 * Mock 自选：进程内 [LinkedHashSet]，退出页面再进入仍保留。
 */
class MockWatchlistRepository : WatchlistRepository {
    private val symbols = LinkedHashSet<String>()

    override fun add(symbol: String) {
        if (symbol.isBlank()) return
        symbols.add(symbol)
    }

    override fun remove(symbol: String) {
        symbols.remove(symbol)
    }

    override fun getAll(): List<String> = symbols.toList()

    override fun contains(symbol: String): Boolean = symbols.contains(symbol)
}
