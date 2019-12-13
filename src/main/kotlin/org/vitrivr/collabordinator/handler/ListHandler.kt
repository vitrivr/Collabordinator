package org.vitrivr.collabordinator.handler

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet

object ListHandler {

    private val lists = ConcurrentHashMap<String, ConcurrentSkipListSet<String>>()

    private fun allEntries(key: String): ConcurrentSkipListSet<String> {
        if (!lists.containsKey(key)) {
            lists[key] = ConcurrentSkipListSet()
        }
        return lists[key]!!
    }

    fun add(key: String, entries: List<String>): List<String> {
        val newEntries = entries.subtract(allEntries(key))
        allEntries(key).addAll(newEntries)
        return newEntries.toList()
    }

    fun remove(key: String, entries: List<String>): List<String> {
        val removedEntries = allEntries(key).intersect(entries)
        allEntries(key).removeAll(entries)
        return removedEntries.toList()
    }

    fun list(key: String): List<String> {
        return allEntries(key).toList()
    }

    fun clear(key: String) {
        allEntries(key).clear()
    }

    fun getKeys(): List<String> {
        return lists.keys.toList()
    }

}