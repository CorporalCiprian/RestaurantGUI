package org.example.ui.dialogs;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public final class StaffDialog {

    private StaffDialog() {
    }

    public record StaffInput(String username, String password) {
    }

    public static Optional<StaffInput> showAddStaffDialog() {
        Dialog<StaffInput> dialog = new Dialog<>();
        dialog.setTitle("Add Staff");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        grid.add(new Label("Username"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password"), 0, 1);
        grid.add(passwordField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            String u = usernameField.getText() == null ? "" : usernameField.getText().trim();
            String p = passwordField.getText() == null ? "" : passwordField.getText();
            if (u.isBlank() || p.isBlank()) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Invalid input");
                a.setHeaderText(null);
                a.setContentText("Username and password are required.");
                a.showAndWait();
                return null;
            }
            return new StaffInput(u, p);
        });

        return dialog.showAndWait();
    }
}
