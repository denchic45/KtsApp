package com.denchic45.kts.utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SearchKeysGenerator {

    private final List<String> searchKeys = new ArrayList<>();

    public List<String> generateKeys(@NotNull String text) {
        return createListOfKeys(text);
    }

    public List<String> generateKeys(@NotNull String text, Filter filter) {
        return createListOfKeys(text).stream().filter(filter).collect(Collectors.toList());
    }

    @NotNull
    private List<String> createListOfKeys(@NotNull String text) {
        searchKeys.clear();
        text = text.toLowerCase();
        List<List<String>> wordKeysLists = new ArrayList<>();
        for (String word : text.split("\\s+")) {
            wordKeysLists.add(generateKeysByWord(word));
        }
        permutationWordKeyLists(wordKeysLists, 0);
        return searchKeys;
    }

    private void permutationWordKeyLists(@NotNull List<List<String>> wordKeysLists, int pos) {
        if (pos == wordKeysLists.size() - 1) {
            printKeys(wordKeysLists);
            return;
        }
        for (int i = pos; i < wordKeysLists.size(); i++) {
            Collections.swap(wordKeysLists, i, pos);
            permutationWordKeyLists(wordKeysLists, pos + 1);
            Collections.swap(wordKeysLists, i, pos);
        }
    }

    private void printKeys(@NotNull List<List<String>> wordKeysLists) {
        StringBuilder appendedWords = new StringBuilder();
        for (List<String> wordKeys : wordKeysLists) {
            searchKeys.addAll(wordKeys.stream().map(key -> appendedWords.toString() + key).collect(Collectors.toList()));
            appendedWords.append(wordKeys.get(wordKeys.size() - 1));
        }
    }

    @NotNull
    private List<String> generateKeysByWord(@NotNull String word) {
        List<String> wordKeys = new ArrayList<>();
        StringBuilder str = new StringBuilder();
        for (char c : word.toCharArray()) {
            str.append(c);
            wordKeys.add(str.toString());
        }
        return wordKeys;
    }

    @NotNull
    public static String formatInput(@NotNull String input) {
        return input.toLowerCase().replaceAll("\\s+", "");
    }

    public interface Filter extends Predicate<String> {
        @Override
        boolean test(String predicate);
    }

}
