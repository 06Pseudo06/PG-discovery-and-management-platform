package com.pgfinder.dsa;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Trie {
    private final TrieNode root = new TrieNode();

    public void insert(String word) {
        if (word == null || word.trim().isEmpty()) return;
        TrieNode current = root;
        for (char ch : word.toLowerCase().toCharArray()) {
            current = current.getChildren().computeIfAbsent(ch, c -> new TrieNode());
        }
        current.setEndOfWord(true);
    }

    public List<String> searchPrefix(String prefix) {
        List<String> results = new ArrayList<>();
        if (prefix == null) return results;
        TrieNode current = root;
        for (char ch : prefix.toLowerCase().toCharArray()) {
            current = current.getChildren().get(ch);
            if (current == null) {
                return results;
            }
        }
        collectWords(current, prefix.toLowerCase(), results);
        return results;
    }

    private void collectWords(TrieNode node, String prefix, List<String> results) {
        if (node.isEndOfWord()) {
            results.add(prefix);
        }
        for (Map.Entry<Character, TrieNode> entry : node.getChildren().entrySet()) {
            collectWords(entry.getValue(), prefix + entry.getKey(), results);
        }
    }
}
