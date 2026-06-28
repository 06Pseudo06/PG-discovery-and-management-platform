package com.pgfinder.util;

import com.pgfinder.model.PG;

public final class SelectedPGManager {

    private static PG selectedPG;

    private SelectedPGManager() {
    }

    public static void setSelectedPG(PG pg) {
        selectedPG = pg;
    }

    public static PG getSelectedPG() {
        return selectedPG;
    }

    public static void clear() {
        selectedPG = null;
    }

    public static boolean hasSelectedPG() {
        return selectedPG != null;
    }
}