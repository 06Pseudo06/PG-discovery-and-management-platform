package com.pgfinder.dao;

import com.pgfinder.model.PG;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class PGDAOTest {
    private PGDAO pgDao;

    @BeforeEach
    public void setUp() {
        pgDao = new PGDAO();
    }

    @Test
    public void testSearchByCityAlone() {
        // Search Pune alone
        SearchFilters puneFilters = new SearchFilters("Pune", null, null, null, null, null);
        List<PG> puneResults = pgDao.search(puneFilters);
        assertEquals(5, puneResults.size(), "Should find 5 PGs in Pune");
        for (PG pg : puneResults) {
            assertEquals("Pune", pg.getCity(), "All search results should be in Pune");
        }

        // Search Mumbai alone
        SearchFilters mumbaiFilters = new SearchFilters("Mumbai", null, null, null, null, null);
        List<PG> mumbaiResults = pgDao.search(mumbaiFilters);
        assertEquals(4, mumbaiResults.size(), "Should find 4 PGs in Mumbai");
        for (PG pg : mumbaiResults) {
            assertEquals("Mumbai", pg.getCity(), "All search results should be in Mumbai");
        }
    }

    @Test
    public void testSearchByBudgetRangeRespectsBoundaries() {
        // Test lower bound boundary (min and max set to exactly 6000.00)
        // Dadar Central PG has room with rent 6000.00
        SearchFilters boundaryMinFilter = new SearchFilters(null, 6000.00, 6000.00, null, null, null);
        List<PG> minResults = pgDao.search(boundaryMinFilter);
        assertFalse(minResults.isEmpty(), "Should find PGs with rent exactly equal to 6000.00");
        boolean foundDadar = false;
        for (PG pg : minResults) {
            if (pg.getId() == 8 && pg.getName().equals("Dadar Central PG")) {
                foundDadar = true;
            }
        }
        assertTrue(foundDadar, "Should match Dadar Central PG which has a rent of 6000.00");

        // Test upper bound boundary (min and max set to exactly 15000.00)
        // Elite PG and Sea View PG have rooms with rent 15000.00
        SearchFilters boundaryMaxFilter = new SearchFilters(null, 15000.00, 15000.00, null, null, null);
        List<PG> maxResults = pgDao.search(boundaryMaxFilter);
        assertEquals(2, maxResults.size(), "Should find exactly 2 PGs with room rent of 15000.00");
        boolean foundElite = false;
        boolean foundSeaView = false;
        for (PG pg : maxResults) {
            if (pg.getId() == 4) foundElite = true;
            if (pg.getId() == 7) foundSeaView = true;
        }
        assertTrue(foundElite, "Elite PG should be in results");
        assertTrue(foundSeaView, "Sea View PG should be in results");
    }

    @Test
    public void testCombinedFiltersIntersection() {
        // City = Pune, Gender = female, max budget = 8000.00
        // Expected match:
        // ID 1: Oxford PG (any gender preference, room rent 8000)
        // ID 2: Skyline PG (female gender preference, room rent 7500)
        // ID 5: Deccan Heritage PG (any gender preference, room rent 6500)
        // Count should be exactly 3.
        SearchFilters combined = new SearchFilters("Pune", null, 8000.00, "female", null, null);
        List<PG> results = pgDao.search(combined);
        assertEquals(3, results.size(), "Should find exactly 3 PGs matching Pune, female, max budget 8000");

        boolean id1 = false, id2 = false, id5 = false;
        for (PG pg : results) {
            if (pg.getId() == 1) id1 = true;
            if (pg.getId() == 2) id2 = true;
            if (pg.getId() == 5) id5 = true;
        }
        assertTrue(id1 && id2 && id5, "Results must contain PG 1, 2, and 5");
    }

    @Test
    public void testGenderFilterAsymmetry() {
        // A gender filter of "female" should return "female" and "any" PGs in Pune
        SearchFilters femaleFilter = new SearchFilters("Pune", null, null, "female", null, null);
        List<PG> femaleResults = pgDao.search(femaleFilter);
        assertEquals(4, femaleResults.size(), "Should return 4 PGs for female (IDs: 1, 2, 4, 5)");
        
        for (PG pg : femaleResults) {
            assertNotEquals("male", pg.getGenderPreference().toLowerCase(), "Female query should NEVER return male-only PG");
        }

        // A gender filter of "male" should return "male" and "any" PGs in Pune
        SearchFilters maleFilter = new SearchFilters("Pune", null, null, "male", null, null);
        List<PG> maleResults = pgDao.search(maleFilter);
        assertEquals(4, maleResults.size(), "Should return 4 PGs for male (IDs: 1, 3, 4, 5)");

        for (PG pg : maleResults) {
            assertNotEquals("female", pg.getGenderPreference().toLowerCase(), "Male query should NEVER return female-only PG");
        }
    }

    @Test
    public void testOwnerCrudOperations() {
        // Insert a new PG
        PG newPg = new PG(0, 1, "Test PG", "123 Main St", "Pune", "Kothrud", "Nice place", "any", true, true, true, false, true, false);
        PG inserted = pgDao.insert(newPg);
        assertNotNull(inserted);
        assertTrue(inserted.getId() > 0);

        // Retrieve by owner id
        List<PG> ownerPgs = pgDao.findByOwnerId(1);
        boolean found = false;
        for (PG pg : ownerPgs) {
            if (pg.getId() == inserted.getId()) {
                found = true;
                assertEquals("Test PG", pg.getName());
                assertTrue(pg.isAcAvailable());
                assertFalse(pg.isLaundryAvailable());
                break;
            }
        }
        assertTrue(found, "Inserted PG should be found in owner's PG list");

        // Update PG
        inserted.setName("Updated PG Name");
        inserted.setLaundryAvailable(true);
        pgDao.update(inserted);

        // Verify update
        PG updated = pgDao.findById(inserted.getId());
        assertNotNull(updated);
        assertEquals("Updated PG Name", updated.getName());
        assertTrue(updated.isLaundryAvailable());

        // Delete PG
        pgDao.delete(inserted.getId());

        // Verify deletion
        assertNull(pgDao.findById(inserted.getId()));
    }
}
