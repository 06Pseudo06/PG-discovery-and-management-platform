package com.pgfinder.util;

import com.pgfinder.model.PG;

public final class SelectedPGManager {

    private static PG selectedPG;
    private static int selectedPGId;

    private SelectedPGManager() {
    }

    public static void setSelectedPG(PG pg) {
        selectedPG = pg;
        selectedPGId = (pg != null) ? pg.getId() : 0;
    }

    public static PG getSelectedPG() {
        return selectedPG;
    }

    public static void setSelectedPGId(int id) {
        selectedPGId = id;
        selectedPG = null;
    }

    public static int getSelectedPGId() {
        return selectedPGId;
    }

    public static boolean hasSelectedPG() {
        return selectedPG != null || selectedPGId != 0;
    }

    public static void clear() {
        selectedPG = null;
        selectedPGId = 0;
    }
}