package com.pgfinder.dsa;

import com.pgfinder.dao.PGDAO;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class AreaTrieTest {
    @Test
    public void testAreaTrieAutocomplete() {
        PGDAO pgDao = new PGDAO();
        AreaTrie areaTrie = new AreaTrie();
        areaTrie.populateFromDatabase(pgDao);

        // Test prefix "ko" -> should match "kothrud" (lowercase insert/search)
        List<String> koResults = areaTrie.getSuggestions("ko");
        assertEquals(1, koResults.size(), "Should find 1 match for prefix 'ko'");
        assertEquals("kothrud", koResults.get(0), "Match should be 'kothrud'");

        // Test prefix "d" -> should match "deccan" and "dadar"
        List<String> dResults = areaTrie.getSuggestions("d");
        assertEquals(2, dResults.size(), "Should find 2 matches for prefix 'd' ('deccan', 'dadar')");
        assertTrue(dResults.contains("deccan"), "Matches should contain 'deccan'");
        assertTrue(dResults.contains("dadar"), "Matches should contain 'dadar'");

        // Test prefix with no matches -> should return empty list (not null, no exception)
        List<String> emptyResults = areaTrie.getSuggestions("nonexistent");
        assertNotNull(emptyResults, "Should return a non-null list");
        assertTrue(emptyResults.isEmpty(), "Should return an empty list for nonexistent prefix");
    }
}
