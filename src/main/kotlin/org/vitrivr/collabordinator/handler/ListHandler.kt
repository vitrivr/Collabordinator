package org.vitrivr.collabordinator.handler

import java.util.concurrent.ConcurrentSkipListSet

object ListHandler {

    private val allEntries = ConcurrentSkipListSet<String>()

    fun add(entries: List<String>): List<String> {
        val newEntries = entries.subtract(allEntries)
        allEntries.addAll(newEntries)
        return newEntries.toList()
    }

    fun remove(entries: List<String>): List<String> {
        val removedEntries = allEntries.intersect(entries)
        allEntries.removeAll(entries)
        return removedEntries.toList()
    }

    fun list(): List<String> {
        return allEntries.toList()
    }

    fun clear() {
        allEntries.clear()
    }

}