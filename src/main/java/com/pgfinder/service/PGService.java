package com.pgfinder.service;

import com.pgfinder.dao.PGDAO;
import com.pgfinder.dao.SearchFilters;
import com.pgfinder.model.PG;
import java.util.List;

public class PGService {
    private final PGDAO pgDAO = new PGDAO();

    public List<PG> getOwnerPGs(int ownerId) {
        return pgDAO.findByOwnerId(ownerId);
    }

    public PG addPG(PG pg) {
        // Basic validation
        if (pg.getName() == null || pg.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("PG name cannot be empty");
        }
        if (pg.getCity() == null || pg.getCity().trim().isEmpty()) {
            throw new IllegalArgumentException("City cannot be empty");
        }
        if (pg.getArea() == null || pg.getArea().trim().isEmpty()) {
            throw new IllegalArgumentException("Area cannot be empty");
        }
        return pgDAO.insert(pg);
    }

    public void updatePG(PG pg) {
        if (pg.getName() == null || pg.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("PG name cannot be empty");
        }
        pgDAO.update(pg);
    }

    public void deletePG(int pgId) {
        pgDAO.delete(pgId);
    }

    public List<PG> getAllPGs() {
        return pgDAO.findAll();
    }

    public List<PG> searchPGs(SearchFilters filters) {
        return pgDAO.search(filters);
    }
}
