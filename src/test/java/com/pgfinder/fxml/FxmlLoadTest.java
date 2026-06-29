package com.pgfinder.fxml;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Loads every FXML file with FXMLLoader to catch missing fx:controller handlers and @FXML mismatches.
 */
class FxmlLoadTest {

    @BeforeAll
    static void initJavaFx() {
        AtomicBoolean started = new AtomicBoolean(false);
        try {
            Platform.startup(() -> started.set(true));
        } catch (IllegalStateException ex) {
            // Toolkit already initialized
        }
        if (!started.get() && !Platform.isFxApplicationThread()) {
            Platform.startup(() -> {});
        }
    }

    @Test
    void allFxmlFilesLoadWithoutException() throws Exception {
        Path fxmlDir = Path.of("src/main/resources/fxml");
        List<String> failures = new ArrayList<>();

        try (Stream<Path> paths = Files.list(fxmlDir)) {
            for (Path fxml : paths.filter(p -> p.toString().endsWith(".fxml")).toList()) {
                String name = fxml.getFileName().toString();
                try {
                    FXMLLoader loader = new FXMLLoader(fxml.toUri().toURL());
                    loader.load();
                } catch (IOException ex) {
                    failures.add(name + ": " + ex.getMessage());
                } catch (RuntimeException ex) {
                    failures.add(name + ": " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
                }
            }
        }

        if (!failures.isEmpty()) {
            fail("FXML load failures:\n" + String.join("\n", failures));
        }
    }
}
