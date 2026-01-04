package org.example.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic smoke tests to ensure FXML files still load (no broken fx:id, missing controller, etc.).
 * These tests don't start a Stage.
 */
class FxmlSmokeTest {

    @Test
    void loginFxmlLoads() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/ui/views/login.fxml"));
        assertNotNull(root);
    }

    @Test
    void guestFxmlLoads() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/ui/views/guest.fxml"));
        assertNotNull(root);
    }

    @Test
    void staffFxmlLoads() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/ui/views/staff.fxml"));
        assertNotNull(root);
    }

    @Test
    void managerFxmlLoads() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/org/example/ui/views/manager.fxml"));
        assertNotNull(root);
    }
}

