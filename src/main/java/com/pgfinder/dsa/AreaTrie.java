package com.pgfinder.dsa;

import com.pgfinder.dao.PGDAO;
import java.util.List;

public class AreaTrie {
    private final Trie trie = new Trie();

    public void populateFromDatabase(PGDAO pgDao) {
        List<String> areas = pgDao.findDistinctAreas();
        for (String area : areas) {
            trie.insert(area);
        }
    }

    public List<String> getSuggestions(String prefix) {
        return trie.searchPrefix(prefix);
    }
}
