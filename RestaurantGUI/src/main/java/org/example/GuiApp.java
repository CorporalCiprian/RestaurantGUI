package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.auth.Role;
import org.example.persistence.DatabaseSeeder;
import org.example.persistence.JpaUtil;
import org.example.persistence.ProdusRepository;
import org.example.service.AuthService;
import org.example.ui.controllers.LoginController;

public class GuiApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Seed the database with initial data
        new DatabaseSeeder(new ProdusRepository()).seedIfEmpty();

        // Seed with a manager and a staff user
        AuthService authService = new AuthService();
        authService.register("manager", "manager", Role.MANAGER);
        authService.register("staff", "staff", Role.STAFF);


        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/ui/views/login.fxml"));
        Parent root = loader.load();

        // Optional: inject a shared AuthService instance
        try {
            LoginController controller = loader.getController();
            if (controller != null) {
                controller.setAuthService(authService);
            }
        } catch (Exception ignored) {
        }

        primaryStage.setTitle("Restaurant");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    @Override
    public void stop() {
        // ensure JPA resources are released
        try {
            JpaUtil.shutdown();
        } catch (Exception ignored) {
        }
    }
}
