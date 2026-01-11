package org.example.ui.controllers;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.auth.Role;
import org.example.model.User;
import org.example.service.AuthService;
import org.example.ui.util.FxAsync;

import java.io.IOException;
import java.util.Optional;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    @FXML
    private Button guestButton;

    private AuthService authService = new AuthService();

    public void setAuthService(AuthService authService) {
        if (authService != null) {
            this.authService = authService;
        }
    }

    @FXML
    private void login() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Username and password are required.");
            return;
        }

        setUiBusy(true);
        errorLabel.setText("Logging in...");

        Task<Optional<User>> task = new Task<>() {
            @Override
            protected Optional<User> call() {
                return authService.login(username, password);
            }
        };

        task.setOnSucceeded(e -> {
            setUiBusy(false);
            Optional<User> user = task.getValue();
            if (user != null && user.isPresent()) {
                errorLabel.setText("");
                openRoleView(user.get().getRole(), user);
            } else {
                errorLabel.setText("Invalid username or password.");
            }
        });

        task.setOnFailed(e -> {
            setUiBusy(false);
            Throwable ex = task.getException();
            errorLabel.setText(ex == null ? "Login failed." : ("Login failed: " + ex.getMessage()));
        });

        FxAsync.submit(task);
    }

    private void setUiBusy(boolean busy) {
        if (usernameField != null) usernameField.setDisable(busy);
        if (passwordField != null) passwordField.setDisable(busy);
        if (loginButton != null) loginButton.setDisable(busy);
        if (guestButton != null) guestButton.setDisable(busy);
    }

    @FXML
    private void loginAsGuest() {
        openRoleView(Role.GUEST, Optional.empty());
    }

    private void openRoleView(Role role, Optional<User> user) {
        try {
            String fxmlFile;
            String title;
            switch (role) {
                case GUEST:
                    fxmlFile = "/org/example/ui/views/guest.fxml";
                    title = "Guest View";
                    break;
                case STAFF:
                    fxmlFile = "/org/example/ui/views/staff.fxml";
                    title = "Staff View";
                    break;
                case MANAGER:
                    fxmlFile = "/org/example/ui/views/manager.fxml";
                    title = "Manager View";
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + role);
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            if (role == Role.STAFF && user.isPresent()) {
                StaffController controller = loader.getController();
                controller.setCurrentUser(user.get());
            }

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Error loading view.");
        }
    }
}
