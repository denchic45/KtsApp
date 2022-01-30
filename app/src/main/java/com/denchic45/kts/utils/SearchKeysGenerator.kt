package com.denchic45.kts.utils

import java.util.*
import java.util.function.Predicate
import java.util.stream.Collectors

class SearchKeysGenerator {
    private val searchKeys: MutableList<String> = ArrayList()
    fun generateKeys(text: String): List<String> {
        return createListOfKeys(text)
    }

    fun generateKeys(text: String, filter: (String) -> Boolean): List<String> {
        return createListOfKeys(text).stream().filter(filter).collect(Collectors.toList())
    }

    private fun createListOfKeys(text: String): List<String> {
        var text = text
        searchKeys.clear()
        text = text.lowercase(Locale.getDefault())
        val wordKeysLists: MutableList<List<String>?> = ArrayList()
        for (word in text.split("\\s+").toTypedArray()) {
            wordKeysLists.add(generateKeysByWord(word))
        }
        permutationWordKeyLists(wordKeysLists, 0)
        return searchKeys
    }

    private fun permutationWordKeyLists(wordKeysLists: List<List<String>?>, pos: Int) {
        if (pos == wordKeysLists.size - 1) {
            printKeys(wordKeysLists)
            return
        }
        for (i in pos until wordKeysLists.size) {
            Collections.swap(wordKeysLists, i, pos)
            permutationWordKeyLists(wordKeysLists, pos + 1)
            Collections.swap(wordKeysLists, i, pos)
        }
    }

    private fun printKeys(wordKeysLists: List<List<String>?>) {
        val appendedWords = StringBuilder()
        for (wordKeys in wordKeysLists) {
            searchKeys.addAll(
                wordKeys!!.stream().map { key: String -> appendedWords.toString() + key }
                    .collect(Collectors.toList()))
            appendedWords.append(wordKeys[wordKeys.size - 1])
        }
    }

    private fun generateKeysByWord(word: String): List<String> {
        val wordKeys: MutableList<String> = ArrayList()
        val str = StringBuilder()
        for (c in word.toCharArray()) {
            str.append(c)
            wordKeys.add(str.toString())
        }
        return wordKeys
    }

    interface Filter : Predicate<String?> {
        override fun test(predicate: String?): Boolean
    }

    companion object {
        fun formatInput(input: String): String {
            return input.lowercase(Locale.getDefault()).replace("\\s+".toRegex(), "")
        }
    }
}