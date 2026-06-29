package com.pgfinder.navigation;

import com.pgfinder.config.SchemaMigrator;
import com.pgfinder.dao.UserDAO;
import com.pgfinder.model.PG;
import com.pgfinder.model.User;
import com.pgfinder.service.AuthService;
import com.pgfinder.service.AuthenticationException;
import com.pgfinder.service.PGService;
import com.pgfinder.util.SceneManager;
import com.pgfinder.util.SelectedPGManager;
import com.pgfinder.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Runtime navigation smoke test: loads every screen with session context and database.
 */
class NavigationSmokeTest {

    private static final String OWNER_EMAIL = "owner1@pgfinder.com";
    private static final String STUDENT_EMAIL = "student1@pgfinder.com";
    private static final String TEST_PASSWORD = "password123";

    private static final AuthService AUTH = new AuthService();
    private static final UserDAO USER_DAO = new UserDAO();
    private static final PGService PG_SERVICE = new PGService();

    @BeforeAll
    static void initJavaFxAndDb() throws Exception {
        AtomicBoolean started = new AtomicBoolean(false);
        try {
            Platform.startup(() -> started.set(true));
        } catch (IllegalStateException ignored) {
        }

        AtomicReference<Exception> initError = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                Stage stage = new Stage();
                SceneManager.setPrimaryStage(stage);
                SchemaMigrator.runMigration();
            } catch (Exception ex) {
                initError.set(ex);
            }
        });
        Platform.runLater(() -> {});
        Thread.sleep(500);

        if (initError.get() != null) {
            throw initError.get();
        }
    }

    @Test
    void allStudentScreensLoad() throws Exception {
        User student = resolveStudentUser();
        SessionManager.setCurrentUser(student);
        SelectedPGManager.clear();

        List<String> screens = List.of(
                "StudentDashboard.fxml",
                "BrowsePG.fxml",
                "MyStay.fxml",
                "StudentChat.fxml",
                "StudentAnnouncements.fxml",
                "PGHistory.fxml",
                "Reviews.fxml",
                "Settings.fxml"
        );

        List<String> failures = loadScreensOnFxThread(screens);

        List<PG> listings = PG_SERVICE.getAllPGs();
        if (!listings.isEmpty()) {
            SelectedPGManager.setSelectedPG(listings.get(0));
            failures.addAll(loadScreensOnFxThread(List.of("PGDetails.fxml", "Booking.fxml")));
        } else {
            failures.add("PGDetails.fxml / Booking.fxml: skipped — no PG listings in database");
        }

        if (!failures.isEmpty()) {
            fail("Student screen load failures:\n" + String.join("\n", failures));
        }
    }

    @Test
    void allOwnerScreensLoad() throws Exception {
        User owner = resolveOwnerUser();
        SessionManager.setCurrentUser(owner);

        List<PG> ownerPgs = PG_SERVICE.getOwnerPGs(owner.getId());
        if (!ownerPgs.isEmpty()) {
            SelectedPGManager.setSelectedPG(ownerPgs.get(0));
        } else {
            SelectedPGManager.clear();
        }

        List<String> screens = List.of(
                "OwnerDashboard.fxml",
                "MyPGs.fxml",
                "RoomsBeds.fxml",
                "BookingRequests.fxml",
                "Tenants.fxml",
                "Announcements.fxml",
                "Chat.fxml",
                "OwnerReviews.fxml",
                "OwnerSettings.fxml"
        );

        List<String> failures = loadScreensOnFxThread(screens);
        failures.addAll(loadScreensOnFxThread(List.of("Login.fxml", "Register.fxml")));

        if (!failures.isEmpty()) {
            fail("Owner screen load failures:\n" + String.join("\n", failures));
        }
    }

    private User resolveOwnerUser() throws Exception {
        return resolveUser(OWNER_EMAIL, "Rajesh Mehta", "OWNER", "9876543210");
    }

    private User resolveStudentUser() throws Exception {
        return resolveUser(STUDENT_EMAIL, "Amit Patel", "STUDENT", "9123456780");
    }

    private User resolveUser(String email, String name, String role, String phone) throws Exception {
        try {
            return AUTH.login(email, TEST_PASSWORD);
        } catch (AuthenticationException ex) {
            User existing = USER_DAO.findByEmail(email);
            if (existing != null) {
                return existing;
            }
            return AUTH.register(name, email, TEST_PASSWORD, role, phone);
        }
    }

    private List<String> loadScreensOnFxThread(List<String> fxmlFiles) throws Exception {
        AtomicReference<List<String>> result = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                result.set(loadScreens(fxmlFiles));
            } catch (Exception ex) {
                error.set(ex);
            }
        });
        Platform.runLater(() -> {});
        Thread.sleep(1500);

        if (error.get() != null) {
            throw error.get();
        }
        return result.get() != null ? result.get() : List.of("FX thread did not return results");
    }

    private List<String> loadScreens(List<String> fxmlFiles) {
        List<String> failures = new ArrayList<>();
        for (String fxml : fxmlFiles) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxml));
                loader.load();
            } catch (IOException ex) {
                failures.add(fxml + ": " + ex.getMessage());
            } catch (RuntimeException ex) {
                failures.add(fxml + ": " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
            }
        }
        return failures;
    }
}
