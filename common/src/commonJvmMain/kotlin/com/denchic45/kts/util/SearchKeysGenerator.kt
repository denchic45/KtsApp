package com.denchic45.kts.util

import java.util.*

class SearchKeysGenerator {
    private val searchKeys: MutableList<String> = ArrayList()
    fun generateKeys(text: String): List<String> {
        return createKeys(text)
    }

    fun generateKeys(text: String, filter: (String) -> Boolean): List<String> {
        return createKeys(text).filter(filter)
    }

    private fun createKeys(text: String): List<String> {
        var text = text
        searchKeys.clear()
        text = text.lowercase(Locale.getDefault())
        val wordKeysLists: MutableList<List<String>> = ArrayList()
        for (word in text.split("\\s+".toRegex()).toTypedArray()) {
            wordKeysLists.add(generateKeysByWord(word))
        }
        permutationWordKeyLists(wordKeysLists, 0)
        return searchKeys
    }

    private fun permutationWordKeyLists(wordKeysLists: MutableList<List<String>>, pos: Int) {
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

    private fun printKeys(wordKeysLists: List<List<String>>) {
        val appendedWords = StringBuilder()
        for (wordKeys in wordKeysLists) {
            searchKeys.addAll(
                wordKeys.map { key: String -> appendedWords.toString() + key }
            )
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

    companion object {
        fun formatInput(input: String): String {
            return input.lowercase(Locale.getDefault()).replace("\\s+".toRegex(), "")
        }
    }
}